package engine.agent.families.cf10;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;

public class ConveyorAgentTen extends Agent implements ConveyorFamilyInterface {
	// DATA
	ConveyorFamilyInterface ncf, pcf;

	// sensor booleans
	boolean loaded = false;
	boolean sensor2Pressed = false;
	Object[] numberOfSensor2 = new Object[1];
	Object[] numberOfConveyor = new Object[1];
	boolean conveyorStopped = false;
	boolean nextSensorPressed = false;
	boolean nextConveyorStopped = false;
	boolean breakConveyor = false;
	boolean stationDisable = false;
	Semaphore machineDone = new Semaphore(0);
	Semaphore releaseFinished = new Semaphore(0);
	List<MyGlass> glassList = Collections
			.synchronizedList(new ArrayList<MyGlass>());
	State state = State.NEXT_START;

	enum State {
		NULL, NEXT_STOP, NEXT_START, STOPPED, NONNORM_STOPPED
	};

	private class MyGlass {
		boolean machined;
		Glass glass;

		private MyGlass(Glass glass) {
			this.glass = glass;
			machined = false;
		}
	}

	String name;

	public ConveyorAgentTen(Transducer transducer) {
		super("Painter", transducer);
		this.transducer.register(this, TChannel.SENSOR);
		this.transducer.register(this, TChannel.PAINTER);
		this.transducer.register(this, TChannel.CONVEYOR);
		this.transducer = transducer;
		numberOfSensor2[0] = 21;
		numberOfConveyor[0] = 10;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				numberOfConveyor);
	}

	// SETTERS
	public void setNextFamily(ConveyorFamilyInterface next) {
		ncf = next;
	}

	public void setPreviousFamily(ConveyorFamilyInterface previous) {
		pcf = previous;
	}

	public void msgHereIsGlass(Glass glass) {
		System.out.println("Conveyor family 10 receiving glass");
		MyGlass myGlass = new MyGlass(glass);
		myGlass.machined = false;
		glassList.add(myGlass);
		stateChanged();
	}

	public void msgStartConveyor() {
		nextConveyorStopped = false;
		state = State.NEXT_START;
		stateChanged();
	}

	public void msgStopConveyor() {
		nextConveyorStopped = true;
		state = State.NEXT_STOP;
		stateChanged();
	}

	// scheduler
	public boolean pickAndExecuteAnAction() {
		if (!breakConveyor && !stationDisable) {
			if (conveyorStopped && !nextConveyorStopped
					&& state == State.NONNORM_STOPPED) {
				startConveyor();
				print("test1");
				print("loaded: " + loaded);
				print("glass size:" + glassList.size());
				state = State.NEXT_START;
				return true;
			}
			if (conveyorStopped && nextConveyorStopped && !loaded) {
				startConveyor();
				return true;
			}

			if (nextConveyorStopped && !conveyorStopped && loaded) {
				stopConveyor();
				return true;
			}
			if (conveyorStopped && !nextConveyorStopped && !loaded) {
				startConveyor();
				print("test2");
				state = State.NEXT_START;
				return true;
			}
			if (state == State.STOPPED) {
				if (!nextConveyorStopped && loaded) {
					state = State.NEXT_START;
					return true;
				}
			}
			if (state == State.NEXT_STOP) {
				if (sensor2Pressed && !conveyorStopped) {
					stopConveyor();
					return true;
				}

			} else if (state == State.NEXT_START) {

				if (loaded) {
					// if another glass coming in, stop the conveyor
					if (sensor2Pressed && !conveyorStopped) {
						stopConveyor();
						return true;
					}
					if (glassList.size() > 0) {

						// release glass if not need oven
						if (!glassList.get(0).glass.getRecipe()
								.getNeedPainting() && !nextSensorPressed) {
							releaseGlass();
							return true;
						}
						// release if done
						if (glassList.get(0).glass.getRecipe()
								.getNeedPainting()
								&& glassList.get(0).machined
								&& !nextSensorPressed) {
							releaseGlass();
							return true;
						}
						// do animation if needed
						if (glassList.get(0).glass.getRecipe()
								.getNeedPainting()
								&& !glassList.get(0).machined) {
							machineGlass();
							return true;
						}

					}

				}

			}
		} else {
			if (breakConveyor && !conveyorStopped) {
				nonnormStop();
				return true;
			}
			if (stationDisable && !conveyorStopped && loaded) {
				nonnormStop();
				return true;
			}

		}
		return false;
	}

	// methods
	private void nonnormStop() {
		print("non norm stop the conveyor");
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,
				numberOfConveyor);
		conveyorStopped = true;
		state = State.NONNORM_STOPPED;
		// sensor2Pressed = false;
		pcf.msgStopConveyor();
	}

	private void stopConveyor() {
		print("conveyor stops");
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,
				numberOfConveyor);
		conveyorStopped = true;
		state = State.STOPPED;
		// sensor2Pressed = false;
		pcf.msgStopConveyor();

	}

	// Start the conveyor
	private void startConveyor() {
		print("conveyor starts");
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				numberOfConveyor);
		conveyorStopped = false;

		pcf.msgStartConveyor();

	}

	private void releaseGlass() {
		if (glassList.get(0).machined) {
			try {
				machineDone.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		transducer.fireEvent(TChannel.PAINTER,
				TEvent.WORKSTATION_RELEASE_GLASS, null);
		ncf.msgHereIsGlass(glassList.get(0).glass);
		loaded = false;
		glassList.remove(0);
		print("Releasing glass to next family");
		try {
			releaseFinished.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void machineGlass() {

		glassList.get(0).machined = true;

		transducer.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_DO_ACTION,
				null);
	}

	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		if (channel == TChannel.CONVEYOR && event == TEvent.BREAK
				&& args[0].equals(numberOfConveyor[0])) {
			breakConveyor = true;
			stateChanged();
		}
		if (channel == TChannel.CONVEYOR && event == TEvent.FIX
				&& args[0].equals(numberOfConveyor[0])) {
			breakConveyor = false;
			stateChanged();
		}
		if (channel == TChannel.PAINTER && event == TEvent.BREAK) {
			stationDisable = true;
			stateChanged();
		}
		if (channel == TChannel.PAINTER && event == TEvent.FIX) {
			stationDisable = false;
			stateChanged();
		}
		if (channel == TChannel.PAINTER) {

			if (event == TEvent.WORKSTATION_LOAD_FINISHED) {
				loaded = true;
				stateChanged();
			}

			if (event == TEvent.WORKSTATION_RELEASE_FINISHED) {
				loaded = false;
				releaseFinished.release();
				stateChanged();
			}

			if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
				machineDone.release();
				stateChanged();
			}
		}
		if (channel == TChannel.SENSOR) {
			if (args[0].equals((Integer) numberOfSensor2[0] + 1)) {
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					nextSensorPressed = true;
					stateChanged();
				}

				if (event == TEvent.SENSOR_GUI_RELEASED) {
					nextSensorPressed = false;
					stateChanged();
				}
			}
		}
		if (channel == TChannel.SENSOR) {

			if (args[0].equals(numberOfSensor2[0])) {
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					sensor2Pressed = true;
					stateChanged();
				}

				if (event == TEvent.SENSOR_GUI_RELEASED) {
					sensor2Pressed = false;
					stateChanged();
				}
			}
		}

	}

}
