package engine.agent.families.cf13;

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

public class ConveyorAgentThirteen extends Agent implements
		ConveyorFamilyInterface {
	ConveyorFamilyInterface ncf, pcf;
	boolean loaded = false;
	boolean sensor2Pressed = false;
	boolean sensor2Release = false;
	boolean conveyorStopped = false;
	boolean nextSensorPressed = false;
	boolean nextConveyorStopped = false;
	boolean breakConveyor = false;
	boolean stationDisable = false;
	Semaphore machineDone = new Semaphore(0);
	Semaphore releaseFinished = new Semaphore(0);
	Semaphore sensor2Released = new Semaphore(0);
	Object[] numberOfSensor2 = new Object[1];
	Object[] numberOfConveyor = new Object[1];
	State state = State.NEXT_START;

	enum State {
		NULL, NEXT_STOP, NEXT_START, STOPPED, NONNORM_STOPPED
	};

	List<MyGlass> glassList = Collections
			.synchronizedList(new ArrayList<MyGlass>());

	private class MyGlass {
		boolean machined;
		Glass glass;

		private MyGlass(Glass glass) {
			this.glass = glass;
			machined = false;
		}
	}

	public ConveyorAgentThirteen(Transducer transducer) {
		super("OVEN", transducer);
		this.transducer = transducer;
		this.transducer.register(this, TChannel.SENSOR);
		this.transducer.register(this, TChannel.OVEN);
		this.transducer.register(this, TChannel.CONVEYOR);
		numberOfSensor2[0] = 27;
		numberOfConveyor[0] = 13;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				numberOfConveyor);
	}

	public void setNextFamily(ConveyorFamilyInterface next) {
		this.ncf = next;
	}

	public void setPreviousFamily(ConveyorFamilyInterface previous) {
		this.pcf = previous;
	}

	// messages
	public void msgHereIsGlass(Glass glass) {
		System.out.println("Conveyor family 13 receiving glass");
		MyGlass temp = new MyGlass(glass);
		temp.machined = false;
		glassList.add(temp);
		stateChanged();
	}

	// next family tell me to start
	public void msgStartConveyor() {
		nextConveyorStopped = false;
		state = State.NEXT_START;
		stateChanged();
	}

	// next family tell me to stop
	public void msgStopConveyor() {
		nextConveyorStopped = true;
		state = State.NEXT_STOP;
		stateChanged();
	}

	// Scheduler
	public boolean pickAndExecuteAnAction() {
		if (!breakConveyor && !stationDisable) {
			if (conveyorStopped && !nextConveyorStopped
					&& state == State.NONNORM_STOPPED) {
				startConveyor();
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
				if (sensor2Pressed && !conveyorStopped)
					stopConveyor();
				return true;
			} else if (state == State.NEXT_START) {

				if (loaded) {
					// if another glass coming in, stop the conveyor
					if (sensor2Pressed && !conveyorStopped) {
						stopConveyor();
						return true;
					}
					if (glassList.size() > 0) {

						// release glass if not need oven
						if (!glassList.get(0).glass.getRecipe().getNeedBaking()
								&& !nextSensorPressed) {
							releaseGlass();
							return true;
						}
						// release if done
						if (glassList.get(0).glass.getRecipe().getNeedBaking()
								&& glassList.get(0).machined
								&& !nextSensorPressed) {
							releaseGlass();
							return true;
						}
						// do animation if needed
						if (glassList.get(0).glass.getRecipe().getNeedBaking()
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

	// stop the conveyor
	private void stopConveyor() {

		print("stop the conveyor");
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,
				numberOfConveyor);
		conveyorStopped = true;
		state = State.STOPPED;
		// sensor2Pressed = false;
		// sensor2Release = false;
		pcf.msgStopConveyor();
		// stateChanged();
	}

	// start conveyor
	private void startConveyor() {
		// fire the event to turn on the conveyor
		print("start the conveyor");
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				numberOfConveyor);
		conveyorStopped = false;

		pcf.msgStartConveyor();
		// stateChanged();
	}

	// release glass
	private void releaseGlass() {
		print("release glass to next family");
		if (glassList.get(0).machined) {
			try {
				machineDone.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_RELEASE_GLASS,
				null);
		ncf.msgHereIsGlass(glassList.get(0).glass);
		loaded = false;
		glassList.remove(0);
		try {
			releaseFinished.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// stateChanged();
	}

	// do animation
	private void machineGlass() {
		print("do animation");
		glassList.get(0).machined = true;
		transducer.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_DO_ACTION, null);
	}

	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		if (channel == TChannel.OVEN && event == TEvent.BREAK) {
			stationDisable = true;
			stateChanged();
		}
		if (channel == TChannel.OVEN && event == TEvent.FIX) {
			stationDisable = false;
			stateChanged();
		}
		if (channel == TChannel.OVEN
				&& event == TEvent.WORKSTATION_LOAD_FINISHED) {
			loaded = true;
			print("oven load finished");
			stateChanged();
		}
		if (channel == TChannel.OVEN
				&& event == TEvent.WORKSTATION_RELEASE_FINISHED) {
			loaded = false;
			releaseFinished.release();
			stateChanged();
		}
		if (channel == TChannel.OVEN
				&& event == TEvent.WORKSTATION_GUI_ACTION_FINISHED) {
			machineDone.release();

		}
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED
				&& args[0].equals(numberOfSensor2[0])) {
			sensor2Pressed = true;
			stateChanged();
		}
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED
				&& args[0].equals(numberOfSensor2[0])) {
			sensor2Pressed = false;
			sensor2Release = true;
			stateChanged();
		}
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED
				&& args[0].equals(28)) {
			nextSensorPressed = true;
			stateChanged();
		}
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED
				&& args[0].equals(28)) {
			nextSensorPressed = false;
			stateChanged();
		}
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
	}
}
