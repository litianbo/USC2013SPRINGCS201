package engine.agent.families.cf6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.cf6.interfaces.Conveyer;
import engine.agent.families.cf6.interfaces.Popup;
import engine.agent.families.cf6.interfaces.Workstation;

/**
 * This agent is the core of the conveyer family. It checks whether or not glass
 * needs processing and appropriately passes the glass on to the next family or
 * transfers it to a free workstation for processing.
 * 
 * @author Harry Trieu
 * 
 */
public class PopupAgent extends Agent implements Popup {
	/** DATA **/
	private Integer POPUP_NUMBER;

	// Object array to specify popup number when firing events
	private Object[] args;

	// Internal class to keep track of glass data
	private class MyGlass {
		private GlassStatus status;
		private Glass glass;
		private Workstation workstation;

		public MyGlass(Glass glass, GlassStatus status) {
			this.glass = glass;
			this.status = status;
			this.workstation = null;
		}
	}

	public enum GlassStatus {
		NEEDS_PROCESSING, NO_PROCESSING_NEEDED, DISPATCHED_TO_WORKSTATION, PROCESSED, REMOVED
	};

	// List of glass to process or forward.
	private List<MyGlass> glassReceived = Collections
			.synchronizedList(new ArrayList<MyGlass>());

	public enum PopupEvent {
		RECEIVED_GLASS_FROM_CONVEYER, WORKSTATION_DONE_PROCESSING, BREAK_POPUP, BREAK_TOP_WORKSTATION, BREAK_BOTTOM_WORKSTATION, FIX_TOP_WORKSTATION, FIX_BOTTOM_WORKSTATION, HALT_POPUP, BROKEN_GLASS_REMOVED
	};

	private List<PopupEvent> popupEvents = Collections
			.synchronizedList(new ArrayList<PopupEvent>());

	// Workstation State
	public enum WorkstationState {
		NONE_OCCUPIED, TOP_OCCUPIED, BOTTOM_OCCUPIED, BOTH_OCCUPIED
	};

	// Workstations start unoccupied
	private WorkstationState workstationState = WorkstationState.NONE_OCCUPIED;

	// Agent references
	private Conveyer myConveyer;
	private Workstation topWorkstation;
	private Workstation bottomWorkstation;
	private ConveyorFamilyInterface nextConveyerFamily;

	// Semaphores to control animations
	private Semaphore waitForPopupToMoveUp = new Semaphore(0, true);
	private Semaphore waitForPopupToMoveDown = new Semaphore(0, true);
	private Semaphore waitForPopupToRelease = new Semaphore(0, true);
	private Semaphore waitForWorkstationToRelease = new Semaphore(0, true);
	private Semaphore waitForPopupToLoadFromWorkstation = new Semaphore(0, true);
	private Semaphore waitForPopupToBeFixed = new Semaphore(0, true);

	// Experimental
	private Semaphore waitForNextFamilyToClear = new Semaphore(0, true);

	// Variable to differentiate between loading from workstation and loading
	// from conveyor
	private boolean loadingFromWorkstation = false;
	private boolean topWorkstationBroken = false;
	private boolean bottomWorkstationBroken = false;

	// Timers to gauge expectation error
	private Timer topExpectationTimer;
	private Timer bottomExpectationTimer;
	private final int EXPECTATION_TIME = 10000;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            of the agent
	 * @param ft
	 *            the transducer
	 * @param myConveyer
	 * @param nextConveyer
	 * @param topWorkstation
	 * @param bottomWorkstation
	 */
	public PopupAgent(String name, Integer number, Transducer ft,
			Conveyer myConveyer, Workstation topWorkstation,
			Workstation bottomWorkstation) {
		super(name, ft);

		transducer.register(this, TChannel.POPUP);
		transducer.register(this, TChannel.CROSS_SEAMER);

		this.POPUP_NUMBER = number;

		args = new Object[1];
		args[0] = POPUP_NUMBER;

		this.myConveyer = myConveyer;

		this.topWorkstation = topWorkstation;
		this.bottomWorkstation = bottomWorkstation;
	}

	/** MESSAGES **/
	/**
	 * Message sent by a workstation when it removes broken glass.
	 * 
	 * @param glass
	 */
	public void msgBrokenGlassRemoved(Glass glass) {
		popupEvents.add(PopupEvent.BROKEN_GLASS_REMOVED);

		MyGlass tempGlass = null;

		synchronized (glassReceived) {
			for (MyGlass g : glassReceived) {
				if (g.glass.equals(glass)) {
					tempGlass = g;
					break;
				}
			}
		}

		if (tempGlass != null) {
			tempGlass.status = GlassStatus.REMOVED;

			print("Informed that broken glass was removed from a workstation.");

			stateChanged();
		}
	}

	/**
	 * Message sent from the GUI informing us that the top workstation is
	 * broken.
	 */
	public void msgBreakTopWorkstation() {
		popupEvents.add(PopupEvent.BREAK_TOP_WORKSTATION);
		stateChanged();
	}

	/**
	 * Message sent from the GUI informing us that the top workstation is
	 * broken.
	 */
	public void msgBreakBottomWorkstation() {
		popupEvents.add(PopupEvent.BREAK_BOTTOM_WORKSTATION);
		stateChanged();
	}

	/**
	 * Message sent from the GUI informing us that the bottom workstation has
	 * been fixed.
	 */
	public void msgFixTopWorkstation() {
		popupEvents.add(PopupEvent.FIX_TOP_WORKSTATION);
		stateChanged();
	}

	/**
	 * Message sent from the GUI informing us that the bottom workstation has
	 * been fixed.
	 */
	public void msgFixBottomWorkstation() {
		popupEvents.add(PopupEvent.FIX_BOTTOM_WORKSTATION);
		stateChanged();
	}

	/**
	 * Called by the transducer when the GUI tells it to break.
	 */
	public void msgBreakThePopup() {
		popupEvents.add(PopupEvent.BREAK_POPUP);
		stateChanged();
	}

	/**
	 * Called by the transducer when the GUI tells it to fix.
	 */
	public void msgFixThePopup() {
		waitForPopupToBeFixed.release();
	}

	/**
	 * Called by previous conveyer family to let our popup agent know to expect
	 * glass.
	 * 
	 * @param glass
	 */
	public void msgHereIsGlass(Glass glass) {
		if (glass.getRecipe().getNeedCrossseam()) {
			glassReceived.add(new MyGlass(glass, GlassStatus.NEEDS_PROCESSING));
		} else {
			glassReceived.add(new MyGlass(glass,
					GlassStatus.NO_PROCESSING_NEEDED));
		}
	}

	/**
	 * Called when the GUI fires a POPUP_GUI_LOAD_FINISHED event.
	 */
	public void msgReceivedGlassFromConveyer() {
		popupEvents.add(PopupEvent.RECEIVED_GLASS_FROM_CONVEYER);
		stateChanged();
	}

	/**
	 * Called when the workstation has finished processing glass.
	 */
	public void msgWorkstationDoneProcessing(Glass glass) {
		popupEvents.add(PopupEvent.WORKSTATION_DONE_PROCESSING);

		MyGlass tempGlass = null;

		synchronized (glassReceived) {
			for (MyGlass g : glassReceived) {
				if (g.glass.equals(glass)) {
					tempGlass = g;
					break;
				}
			}
		}

		if (tempGlass != null) {
			tempGlass.status = GlassStatus.PROCESSED;
			stateChanged();
		}
	}

	/**
	 * Called by the next family when it cannot receive any glass.
	 */
	public void msgStopForNextConveyer() {
		popupEvents.add(PopupEvent.HALT_POPUP);
		stateChanged();
	}

	/**
	 * Called by the next family when it can resume receiving glass.
	 */
	public void msgStartForNextConveyer() {
		waitForNextFamilyToClear.release();
	}

	/**
	 * Message sent by the workstation when it's animation has finished
	 * "releasing" the glass back to the popup.
	 */
	public void msgWorkstationDoneReleasing() {
		waitForWorkstationToRelease.release();
	}

	/** SCHEDULER **/
	@Override
	public boolean pickAndExecuteAnAction() {
		if (popupEvents.isEmpty()) {
			return false;
		}

		PopupEvent popupEvent = popupEvents.remove(0);

		// Regardless of what state the popup is currently in, halt all activity
		// if it breaks.
		if (popupEvent == PopupEvent.BREAK_POPUP) {
			haltPopupUntilFixed();
			return true;
		}

		// Regardless of what state the popup is currently in, halt all activity
		// if the next family cannot take any more glass.
		if (popupEvent == PopupEvent.HALT_POPUP) {
			haltPopupUntilCongestionClears();
			return true;
		}

		// Do not allow the top workstation to receive glass.
		if (popupEvent == PopupEvent.BREAK_TOP_WORKSTATION) {
			topWorkstationBroken = true;
			return true;
		}

		// Do not allow the top workstation to receive glass.
		if (popupEvent == PopupEvent.BREAK_BOTTOM_WORKSTATION) {
			bottomWorkstationBroken = true;
			return true;
		}

		// Allow the top workstation to receive glass.
		if (popupEvent == PopupEvent.FIX_TOP_WORKSTATION) {
			topWorkstationBroken = false;
			return true;
		}

		// Allow the bottom workstation to receive glass.
		if (popupEvent == PopupEvent.FIX_BOTTOM_WORKSTATION) {
			bottomWorkstationBroken = false;
			return true;
		}

		// One or no workstation is occupied and we received glass that doesn't
		// need processing. If both workstations are occupied, we shouldn't
		// receive any glass, thus there is no case to handle glass if both
		// workstations are occupied.
		if (!(workstationState == WorkstationState.BOTH_OCCUPIED)) {
			if (popupEvent == PopupEvent.RECEIVED_GLASS_FROM_CONVEYER) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.NO_PROCESSING_NEEDED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					passGlassToNextConveyer(tempGlass);

					return true;
				}
			}
		}

		// Both workstations are occupied and the workstation notifies the popup
		// that a broken piece of glass has been removed.
		if (workstationState == WorkstationState.BOTH_OCCUPIED) {
			if (popupEvent == PopupEvent.BROKEN_GLASS_REMOVED) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.REMOVED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					if (tempGlass.workstation.equals(topWorkstation)) {
						glassReceived.remove(tempGlass);
						workstationState = WorkstationState.BOTTOM_OCCUPIED;
					} else if (tempGlass.workstation.equals(bottomWorkstation)) {
						glassReceived.remove(tempGlass);
						workstationState = WorkstationState.TOP_OCCUPIED;
					}

					restartConveyer();

					return true;
				}
			}
		}

		// The top workstation is occupied and the popup is notified that a
		// broken piece of glass has been removed.
		if (workstationState == WorkstationState.TOP_OCCUPIED) {
			if (popupEvent == PopupEvent.BROKEN_GLASS_REMOVED) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.REMOVED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					if (tempGlass.workstation.equals(topWorkstation)) {
						glassReceived.remove(tempGlass);
						workstationState = WorkstationState.NONE_OCCUPIED;

						// print("DEBUG+++++++");
					}

					return true;
				}
			}
		}

		// The bottom workstation is occupied and the popup is notified that a
		// broken piece of glass has been removed.
		if (workstationState == WorkstationState.BOTTOM_OCCUPIED) {
			if (popupEvent == PopupEvent.BROKEN_GLASS_REMOVED) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.REMOVED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					if (tempGlass.workstation.equals(bottomWorkstation)) {
						glassReceived.remove(tempGlass);
						workstationState = WorkstationState.NONE_OCCUPIED;

						// print("DEBUG+++++++");
					}

					return true;
				}
			}
		}

		// If both workstations are occupied and the popup is notified by a
		// workstation that it has finished processing a piece of glass, remove
		// glass from that workstation, free up the workstation for new glass,
		// and restart the conveyer.
		if (workstationState == WorkstationState.BOTH_OCCUPIED) {
			if (popupEvent == PopupEvent.WORKSTATION_DONE_PROCESSING) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.PROCESSED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {

					if (tempGlass.workstation.equals(topWorkstation)) {
						removeGlassFromTopWorkstation(tempGlass);
						workstationState = WorkstationState.BOTTOM_OCCUPIED;
					} else if (tempGlass.workstation.equals(bottomWorkstation)) {
						removeGlassFromBottomWorkstation(tempGlass);
						workstationState = WorkstationState.TOP_OCCUPIED;
					}

					restartConveyer();

					return true;
				}
			}
		}

		// If both workstations are free and the popup received a piece of glass
		// for processing, give the glass to the top workstation for processing.
		if (workstationState == WorkstationState.NONE_OCCUPIED) {
			if (popupEvent == PopupEvent.RECEIVED_GLASS_FROM_CONVEYER) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.NEEDS_PROCESSING) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					stopThisConveyer();

					if (!topWorkstationBroken) {
						transferGlassToTopWorkstation(tempGlass);
						workstationState = WorkstationState.TOP_OCCUPIED;
					} else if (!bottomWorkstationBroken) {
						transferGlassToBottomWorkstation(tempGlass);
						workstationState = WorkstationState.BOTTOM_OCCUPIED;
					} else {
						// Default behavior: pass glass on to the next family if
						// the workstations are both broken
						passGlassToNextConveyer(tempGlass);
					}

					restartConveyer();

					return true;
				}
			}
		}

		// If the top workstation is occupied and the popup received a piece of
		// glass for processing, give the glass to the bottom workstation for
		// processing.
		if (workstationState == WorkstationState.TOP_OCCUPIED) {
			if (popupEvent == PopupEvent.RECEIVED_GLASS_FROM_CONVEYER) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.NEEDS_PROCESSING) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {

					if (!bottomWorkstationBroken) {
						stopAllConveyers();
						transferGlassToBottomWorkstation(tempGlass);
						workstationState = WorkstationState.BOTH_OCCUPIED;
					} else {
						// Default behavior: pass glass on to the next family if
						// one workstation is occupied and the other is broken
						passGlassToNextConveyer(tempGlass);
					}

					// Conveyer remains stopped. We don't need to restart it
					// until a piece of glass has finished being processed.

					return true;
				}
			}
		}

		// If the bottom workstation is occupied and the popup received a piece
		// of glass for processing, give the glass to the top workstation for
		// processing.
		if (workstationState == WorkstationState.BOTTOM_OCCUPIED) {
			if (popupEvent == PopupEvent.RECEIVED_GLASS_FROM_CONVEYER) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.NEEDS_PROCESSING) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {

					if (!topWorkstationBroken) {
						stopAllConveyers();
						transferGlassToTopWorkstation(tempGlass);
						workstationState = WorkstationState.BOTH_OCCUPIED;
					} else {
						// Default behavior: pass glass on to the next family if
						// one workstation is occupied and the other is broken
						passGlassToNextConveyer(tempGlass);
					}

					// Conveyer remains stopped. We don't restart it until a
					// piece of glass has finished being processed.

					return true;
				}
			}
		}

		// If the top workstation is occupied and the popup is notified that it
		// has finished processing a piece of glass, remove the glass from the
		// workstation and free up the workstation for new glass.
		if (workstationState == WorkstationState.TOP_OCCUPIED) {
			if (popupEvent == PopupEvent.WORKSTATION_DONE_PROCESSING) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.PROCESSED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					// Conveyer should already be started.

					if (tempGlass.workstation.equals(topWorkstation)) {
						removeGlassFromTopWorkstation(tempGlass);
						workstationState = WorkstationState.NONE_OCCUPIED;
					}

					return true;
				}
			}
		}

		// If the bottom workstation is occupied and the popup is notified that
		// it
		// has finished processing a piece of glass, remove the glass from the
		// workstation and free up the workstation for new glass.
		if (workstationState == WorkstationState.BOTTOM_OCCUPIED) {
			if (popupEvent == PopupEvent.WORKSTATION_DONE_PROCESSING) {
				MyGlass tempGlass = null;

				synchronized (glassReceived) {
					for (MyGlass g : glassReceived) {
						if (g.status == GlassStatus.PROCESSED) {
							tempGlass = g;
							break;
						}
					}
				}

				if (tempGlass != null) {
					// Conveyer should already be started

					if (tempGlass.workstation.equals(bottomWorkstation)) {
						removeGlassFromBottomWorkstation(tempGlass);
						workstationState = WorkstationState.NONE_OCCUPIED;
					}

					return true;
				}
			}
		}

		return false;
	}

	/** ACTIONS **/
	/**
	 * This action halts all popup activity until the next family says it's OK
	 * to restart. The action handles the case where glass is being processed in
	 * the workstations when the message to stop comes in. The popup could still
	 * release glass to the next family, causing collision.
	 */
	private void haltPopupUntilCongestionClears() {
		print("Next family has told me to stop. Stopping popup activity.");

		try {
			waitForNextFamilyToClear.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForNextFamilyToClear semaphore.");
			e.printStackTrace();
		}

		print("Next family says it's OK to resume activity. Resuming.");

		stateChanged();
	}

	/**
	 * This action halts all popup activity and previous conveyors.
	 */
	private void haltPopupUntilFixed() {
		print("Popup has broken! Family halted until it is fixed.");

		stopAllConveyers();

		try {
			waitForPopupToBeFixed.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForPopupToBeFixed semaphore.");
			e.printStackTrace();
		}

		print("Popup has been fixed!");

		restartConveyer();

		stateChanged();
	}

	/**
	 * Message our conveyor telling it to restart.
	 */
	private void restartConveyer() {
		print("Telling all conveyors to restart.");
		myConveyer.msgRestartAllConveyers();
	}

	/**
	 * Message our conveyor telling it to stop.
	 */
	private void stopThisConveyer() {
		print("Telling this conveyor to stop.");
		myConveyer.msgStopThisConveyer();
	}

	/**
	 * Message telling all conveyors before us to stop.
	 */
	private void stopAllConveyers() {
		print("Telling all conveyors to stop.");
		myConveyer.msgStopAllConveyers();
	}

	/**
	 * This action passes glass to the next family.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void passGlassToNextConveyer(MyGlass g) {
		print("Passing glass to the next conveyer family.");

		// Message the next conveyer family with the current piece of glass.
		nextConveyerFamily.msgHereIsGlass(g.glass);

		// Release the glass to the next conveyer family and wait for animation
		// to finish
		doPopupReleaseGlass();

		glassReceived.remove(g);

		// TODO: REMOVE THIS HACK LATER
		// restartConveyer();

		stateChanged();
	}

	/**
	 * This action removes glass from the top workstation.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void removeGlassFromTopWorkstation(MyGlass g) {
		if (topExpectationTimer != null) {
			topExpectationTimer.cancel();
			topExpectationTimer.purge();
		}

		stopThisConveyer();

		// Wait for the popup move up animation to finish.
		doMovePopupUp();

		print("Removing glass from the top workstation!");

		// Tell the workstation to release the glass.
		topWorkstation.msgReleaseGlass(g.glass);

		// Wait for the workstation animation to finish.
		try {
			waitForWorkstationToRelease.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForWorkstationToRelease semaphore.");
			e.printStackTrace();
		}

		// Load glass from the workstation
		loadingFromWorkstation = true;
		doWaitForPopupToLoadFromWorkstation();
		print("Waiting for the popup to load from top workstation!");

		// Wait for the transducer move down animation to finish.
		doMovePopupDown();

		// Pass the glass on to the next family.
		// This function calls stateChanged() so we don't need to.
		passGlassToNextConveyer(g);

		restartConveyer();
	}

	/**
	 * This action removes glass from the bottom workstation.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void removeGlassFromBottomWorkstation(MyGlass g) {
		if (bottomExpectationTimer != null) {
			bottomExpectationTimer.cancel();
			bottomExpectationTimer.purge();
		}

		stopThisConveyer();

		// Wait for the popup move up animation to finish.
		doMovePopupUp();

		print("Removing glass from the bottom workstation!");

		// Tell the workstation to release the glass.
		bottomWorkstation.msgReleaseGlass(g.glass);

		// Wait for the workstation animation to finish.
		try {
			waitForWorkstationToRelease.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForWorkstationToRelease semaphore.");
			e.printStackTrace();
		}

		// Load glass from the bottom workstation
		loadingFromWorkstation = true;
		doWaitForPopupToLoadFromWorkstation();
		print("Waiting for the popup to load from bottom workstation!");

		// Wait for the transducer move down animation to finish.
		doMovePopupDown();

		// Pass the glass on to the next family.
		// This function calls stateChanged() so we don't need to.
		passGlassToNextConveyer(g);

		restartConveyer();
	}

	/**
	 * Transfers glass that needs processing to the top workstation.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void transferGlassToTopWorkstation(MyGlass g) {
		print("Transferring glass to the top workstation.");

		// Move the popup up and wait for animation to finish
		doMovePopupUp();

		// Transfer glass to workstation
		topWorkstation.msgProcessGlass(g.glass);

		// Move the popup down and wait for the animation to finish
		doMovePopupDown();

		// Set glass state and a reference to the workstation
		g.status = GlassStatus.DISPATCHED_TO_WORKSTATION;
		g.workstation = topWorkstation;

		topExpectationTimer = new Timer();
		topExpectationTimer.schedule(new TimerTask() {
			public void run() {
				print("Top workstation has failed to process glass!");
				Integer[] args = new Integer[1];
				args[0] = 0;
				transducer.fireEvent(TChannel.CROSS_SEAMER,
						TEvent.OPERATOR_NEEDED, args);
			}
		}, EXPECTATION_TIME);

		stateChanged();
	}

	/**
	 * Transfers glass that needs processing to the bottom workstation.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void transferGlassToBottomWorkstation(MyGlass g) {
		print("Transferring glass to the bottom workstation.");

		// Move the popup up and wait for animation to finish
		doMovePopupUp();

		// Transfer glass to workstation
		bottomWorkstation.msgProcessGlass(g.glass);

		// Move the popup down and wait for the animation to finish
		doMovePopupDown();

		// Set glass state and a reference to the workstation
		g.status = GlassStatus.DISPATCHED_TO_WORKSTATION;
		g.workstation = bottomWorkstation;

		bottomExpectationTimer = new Timer();
		bottomExpectationTimer.schedule(new TimerTask() {
			public void run() {
				print("Bottom workstation has failed to process glass!");
				Integer[] args = new Integer[1];
				args[0] = 1;
				transducer.fireEvent(TChannel.CROSS_SEAMER,
						TEvent.OPERATOR_NEEDED, args);
			}
		}, EXPECTATION_TIME);

		stateChanged();
	}

	/** ANIMATION **/
	/**
	 * Tells the animation to move the popup down and waits for it to finish.
	 */
	private void doMovePopupDown() {
		print("Moving down!");

		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);

		try {
			waitForPopupToMoveDown.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForPopupToMoveDown semaphore.");
			e.printStackTrace();
		}
	}

	/**
	 * Tells the animation to move the popup up and waits for it to finish.
	 */
	private void doMovePopupUp() {
		print("Moving up!");

		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);

		try {
			waitForPopupToMoveUp.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForPopupToMoveUp semaphore.");
			e.printStackTrace();
		}
	}

	/**
	 * Waits for the popup to load glass from the workstation.
	 */
	private void doWaitForPopupToLoadFromWorkstation() {
		try {
			waitForPopupToLoadFromWorkstation.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForPopupToLoadFromWorkstation semaphore.");
			e.printStackTrace();
		}
	}

	/**
	 * Tells the animation to release glass to the next family and waits for it
	 * to finish.
	 */
	private void doPopupReleaseGlass() {
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);

		try {
			waitForPopupToRelease.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForPopupToRelease semaphore.");
			e.printStackTrace();
		}
	}

	/** EXTRA **/
	/**
	 * This function listens to the transducer and processes relevant events.
	 */
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		Integer number = (Integer) args[0];

		// Check if the popup finished loading glass from the workstation or the
		// conveyor
		if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_GUI_LOAD_FINISHED
				&& number == POPUP_NUMBER && loadingFromWorkstation) {

			loadingFromWorkstation = false;
			waitForPopupToLoadFromWorkstation.release();

		} else if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_GUI_LOAD_FINISHED
				&& number == POPUP_NUMBER) {

			msgReceivedGlassFromConveyer();

		} else if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_GUI_MOVED_UP && number == POPUP_NUMBER) {

			waitForPopupToMoveUp.release();

		} else if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_GUI_MOVED_DOWN
				&& number == POPUP_NUMBER) {

			waitForPopupToMoveDown.release();

		} else if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_GUI_RELEASE_FINISHED
				&& number == POPUP_NUMBER) {

			waitForPopupToRelease.release();

		} else if (channel == TChannel.POPUP && event == TEvent.BREAK
				&& number == POPUP_NUMBER) {

			msgBreakThePopup();

		} else if (channel == TChannel.POPUP && event == TEvent.FIX
				&& number == POPUP_NUMBER) {

			msgFixThePopup();

		} else if (channel == TChannel.CROSS_SEAMER && event == TEvent.BREAK
				&& number == 0) {

			msgBreakTopWorkstation();

		} else if (channel == TChannel.CROSS_SEAMER && event == TEvent.BREAK
				&& number == 1) {

			msgBreakBottomWorkstation();

		} else if (channel == TChannel.CROSS_SEAMER && event == TEvent.FIX
				&& number == 0) {

			msgFixTopWorkstation();

		} else if (channel == TChannel.CROSS_SEAMER && event == TEvent.FIX
				&& number == 1) {

			msgFixBottomWorkstation();

		}
	}

	/**
	 * Sets a reference to the next conveyor family.
	 * 
	 * @param nextFamily
	 *            ConveyorFamily
	 */
	public void setNextFamily(ConveyorFamilyInterface nextFamily) {
		this.nextConveyerFamily = nextFamily;
	}

}
