package engine.agent.families.cf6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.Glass;
import engine.agent.families.Glass.GlassState;
import engine.agent.families.cf6.interfaces.Workstation;

/**
 * This agent controls the off-conveyor workstations.
 * 
 * @author Harry Trieu
 * 
 */
public class WorkstationAgent extends Agent implements Workstation {
	/** DATA **/
	private Integer WORKSTATION_NUMBER;

	private Object[] args;

	// Internal class to keep track of glass
	private class MyGlass {
		private Glass glass;
		private GlassStatus status;
		private boolean isBroken;

		public MyGlass(Glass g, GlassStatus s) {
			this.glass = g;
			this.status = s;
			this.isBroken = false;
		}
	}

	private enum GlassStatus {
		RECEIVED, PROCESSED, WAITING_FOR_RELEASE
	};

	private List<MyGlass> glassToProcess = Collections
			.synchronizedList(new ArrayList<MyGlass>());

	// Semaphores for animation
	private Semaphore waitForWorkstationToLoad = new Semaphore(0, true);
	private Semaphore waitForWorkstationToProcess = new Semaphore(0, true);
	private Semaphore waitForWorkstationToRelease = new Semaphore(0, true);
	private Semaphore waitToResumeProcessingGlass = new Semaphore(0, true);
	private Semaphore waitForGlassRemoval = new Semaphore(0, true);

	// Agent references
	private PopupAgent popupAgent;

	// Boolean to control when the workstation can process glass
	// Also used to prevent uncontrolled releasing of waitForWorkstationToLoad
	// semaphore
	private boolean workstationCanProcessGlass = true;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            of the workstation
	 * @param trans
	 *            Transducer for communication
	 * @param number
	 *            of the workstation (0 for top, 1 for bottom)
	 */
	public WorkstationAgent(String name, Transducer trans, Integer number) {
		super(name, trans);

		this.WORKSTATION_NUMBER = number;

		args = new Object[1];
		args[0] = WORKSTATION_NUMBER;

		transducer.register(this, TChannel.CROSS_SEAMER);
	}

	/** MESSAGES **/
	/**
	 * Called by the GUI/transducer when broken glass is removed.
	 */
	public void msgGlassHasBeenRemoved() {
		waitForGlassRemoval.release();
	}

	/**
	 * Called by the GUI/transducer to let the workstation know glass is broken.
	 */
	public void msgGlassIsBroken() {
		MyGlass tempGlass = null;

		synchronized (glassToProcess) {
			for (MyGlass g : glassToProcess) {
				if (g.status == GlassStatus.RECEIVED) {
					tempGlass = g;
					break;
				}
			}
		}

		if (tempGlass != null) {
			tempGlass.isBroken = true;
			stateChanged();
		}
	}

	/**
	 * Called by the transducer/GUI when we don't want the workstation to
	 * release glass.
	 */
	public void msgStopReleasingGlass() {
		workstationCanProcessGlass = false;
	}

	/**
	 * Called by the transducer/GUI when we want the workstation to resume
	 * releasing glass.
	 */
	public void msgResumeReleasingGlass() {
		waitToResumeProcessingGlass.release();
		workstationCanProcessGlass = true;
	}

	/**
	 * A message sent by the popup when there is a piece of glass that needs
	 * processing.
	 */
	@Override
	public void msgProcessGlass(Glass glass) {
		glassToProcess.add(new MyGlass(glass, GlassStatus.RECEIVED));
		stateChanged();
	}

	/**
	 * A message sent by the popup when it is ready to receive processed glass.
	 */
	@Override
	public void msgReleaseGlass(Glass glass) {
		MyGlass tempGlass = null;

		synchronized (glassToProcess) {
			for (MyGlass g : glassToProcess) {
				if (g.glass.equals(glass)) {
					tempGlass = g;
					break;
				}
			}
		}

		if (tempGlass != null) {
			tempGlass.status = GlassStatus.WAITING_FOR_RELEASE;
		}

		stateChanged();
	}

	/**
	 * Called when the animation has finished loading the workstation.
	 */
	public void msgWorkstationLoadFinished() {
		waitForWorkstationToLoad.release();
	}

	/**
	 * Called when the workstation has finished processing glass.
	 */
	public void msgProcessingDone() {
		waitForWorkstationToProcess.release();
	}

	/**
	 * Called when the workstation has finished releasing glass to the popup.
	 */
	public void msgReleasingDone() {
		waitForWorkstationToRelease.release();
	}

	/** SCHEDULER **/
	@Override
	public boolean pickAndExecuteAnAction() {
		// If a new piece of glass has been received, load the workstation and
		// process the glass
		if (!glassToProcess.isEmpty()) {
			MyGlass tempGlass = null;

			synchronized (glassToProcess) {
				for (MyGlass g : glassToProcess) {
					if (g.status == GlassStatus.RECEIVED) {
						tempGlass = g;
						break;
					}
				}
			}

			if (tempGlass != null) {
				loadGlass(tempGlass);

				if (tempGlass.isBroken) {
					removeBrokenGlass(tempGlass);
				} else {
					processGlass(tempGlass);
				}

				return true;
			}
		}

		// If a piece of glass has been processed and the popup is ready for
		// release, release the glass back to the popup
		if (!glassToProcess.isEmpty()) {
			MyGlass tempGlass = null;

			synchronized (glassToProcess) {
				for (MyGlass g : glassToProcess) {
					if (g.status == GlassStatus.WAITING_FOR_RELEASE) {
						tempGlass = g;
						break;
					}
				}
			}

			if (tempGlass != null) {
				releaseGlassToPopup(tempGlass);
				return true;
			}
		}

		// No scheduler rules match
		return false;
	}

	/** ACTIONS **/
	/**
	 * This action calls the animations to load the workstation and process
	 * glass. The popup is notified when it has finished.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void loadGlass(MyGlass g) {
		print("Loading glass!");

		doLoadWorkstation();

		if (!workstationCanProcessGlass) {
			print("Workstation is broken and cannot process glass.");

			try {
				waitToResumeProcessingGlass.acquire();
			} catch (InterruptedException e) {
				print("Error acquiring waitToResumeProcessingGlass semaphore!");
				e.printStackTrace();
			}

			print("Workstation has been fixed and can now begin processing glass.");
		}

	}

	/**
	 * This action processes glass sitting on the workstation.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void processGlass(MyGlass g) {
		print("Processing glass!");

		// Fire animation events
		doProcessGlass();

		g.status = GlassStatus.PROCESSED;
		g.glass.setState(GlassState.doneCrossseam);

		print("Done processing glass!");

		// Let the popup agent know we are done processing glass
		popupAgent.msgWorkstationDoneProcessing(g.glass);

		stateChanged();
	}

	/**
	 * This action removes broken glass from the workstation.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void removeBrokenGlass(MyGlass g) {
		print("Glass is broken. Waiting for operator to remove it.");

		doRemoveBrokenGlass();

		print("Broken glass has been removed.");

		// inform the popup and remove from list
		popupAgent.msgBrokenGlassRemoved(g.glass);
		glassToProcess.remove(g);

		stateChanged();
	}

	/**
	 * This action calls the animation to release glass to the popup and
	 * notifies the popup when it has finished.
	 * 
	 * @param g
	 *            MyGlass
	 */
	private void releaseGlassToPopup(MyGlass g) {
		print("Releasing glass back to the popup!");

		// Fire animation events
		doReleaseGlass();

		// Let the popup agent know we are done releasing
		popupAgent.msgWorkstationDoneReleasing();
		glassToProcess.remove(g);

		stateChanged();
	}

	/** ANIMATION **/
	/**
	 * Tells the GUI workstation to animate loading of glass and waits for it to
	 * finish.
	 */
	private void doLoadWorkstation() {
		transducer.fireEvent(TChannel.CROSS_SEAMER,
				TEvent.WORKSTATION_DO_LOAD_GLASS, args);

		try {
			waitForWorkstationToLoad.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForWorkstationToLoad semaphore.");
			e.printStackTrace();
		}
	}

	/**
	 * Tells the GUI workstation to animate processing of glass and waits for it
	 * to finish.
	 */
	private void doProcessGlass() {
		transducer.fireEvent(TChannel.CROSS_SEAMER,
				TEvent.WORKSTATION_DO_ACTION, args);

		try {
			waitForWorkstationToProcess.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForWorkstationToLoad semaphore.");
			e.printStackTrace();
		}
	}

	/**
	 * Tells the GUI workstation to animate releasing of glass and waits for it
	 * to finish.
	 */
	private void doReleaseGlass() {
		transducer.fireEvent(TChannel.CROSS_SEAMER,
				TEvent.WORKSTATION_RELEASE_GLASS, args);

		try {
			waitForWorkstationToRelease.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForWorkstationToRelease semaphore.");
			e.printStackTrace();
		}
	}

	/**
	 * Tells the GUI workstation to remove broken glass.
	 */
	private void doRemoveBrokenGlass() {
		transducer.fireEvent(TChannel.CROSS_SEAMER,
				TEvent.WORKSTATION_DO_ACTION, args);

		try {
			waitForGlassRemoval.acquire();
		} catch (InterruptedException e) {
			print("Error acquiring waitForGlassRemoval semaphore.");
			e.printStackTrace();
		}
	}

	/** EXTRA **/
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		Integer workstationNumber = (Integer) args[0];

		if (event == TEvent.WORKSTATION_LOAD_FINISHED
				&& workstationNumber == WORKSTATION_NUMBER
				&& workstationCanProcessGlass) {
			msgWorkstationLoadFinished();
		} else if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED
				&& workstationNumber == WORKSTATION_NUMBER) {
			msgProcessingDone();
		} else if (event == TEvent.WORKSTATION_RELEASE_FINISHED
				&& workstationNumber == WORKSTATION_NUMBER) {
			msgReleasingDone();
		} else if (event == TEvent.SILENT_BREAK
				&& workstationNumber == WORKSTATION_NUMBER) {
			msgStopReleasingGlass();
		} else if (event == TEvent.SILENT_FIX
				&& workstationNumber == WORKSTATION_NUMBER) {
			msgResumeReleasingGlass();
		} else if (event == TEvent.WORKSTATION_BREAK_GLASS
				&& workstationNumber == WORKSTATION_NUMBER) {
			msgGlassIsBroken();
		} else if (event == TEvent.WORKSTATION_REMOVE_GLASS
				&& workstationNumber == WORKSTATION_NUMBER) {
			msgGlassHasBeenRemoved();
		}

	}

	/**
	 * Sets the reference to this family's popup agent.
	 * 
	 * @param popup
	 *            Popup Agent
	 */
	public void setPopupAgent(PopupAgent popup) {
		this.popupAgent = popup;
	}

}
