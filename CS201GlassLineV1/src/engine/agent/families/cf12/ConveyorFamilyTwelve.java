package engine.agent.families.cf12;

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

public class ConveyorFamilyTwelve extends Agent implements
		ConveyorFamilyInterface {
	// DATA
	ConveyorFamilyInterface ncf, pcf;
	boolean sensor2Pressed = false;
	boolean nextSensorPressed = false;
	Semaphore nextSensorRelease = new Semaphore(0);
	Object[] numberOfSensor2 = new Object[1];
	Object[] numberOfConveyor = new Object[1];
	boolean conveyorStopped = false;
	boolean breakConveyor = false;
	boolean nextConveyorStopped = false;
	List<MyGlass> glassList = Collections
			.synchronizedList(new ArrayList<MyGlass>());
	State state = State.NEXT_START;

	enum State {
		NEXT_START, NEXT_STOP, NULL, START, STOPPED, NONNORM_STOPPED;
	}

	private class MyGlass {
		boolean machined;
		Glass glass;

		private MyGlass(Glass glass) {
			this.glass = glass;
			machined = false;
		}
	}

	public ConveyorFamilyTwelve(Transducer transudcer) {
		super("conveyor12", transudcer);
		this.transducer.register(this, TChannel.SENSOR);
		this.transducer.register(this, TChannel.CONVEYOR);
		transducer = transudcer;
		numberOfSensor2[0] = 25;
		numberOfConveyor[0] = 12;
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
	// here is glass
	public void msgHereIsGlass(Glass glass) {
		MyGlass myGlass = new MyGlass(glass);
		myGlass.machined = false;
		glassList.add(myGlass);
		stateChanged();
	}

	// start conveyor
	public void msgStartConveyor() {
		state = State.NEXT_START;
		nextConveyorStopped = false;
		stateChanged();
	}

	// stop conveyor
	public void msgStopConveyor() {
		state = State.NEXT_STOP;
		nextConveyorStopped = true;
		stateChanged();
	}

	public boolean pickAndExecuteAnAction() {
		if (!breakConveyor) {
			if (conveyorStopped && !nextConveyorStopped
					&& state == State.NONNORM_STOPPED) {
				startConveyor();
				state = State.NEXT_START;
				return true;
			}

			/*************************/
			if (conveyorStopped && nextConveyorStopped && !sensor2Pressed
					&& state == State.NEXT_STOP) {
				startConveyor();
				return true;
			}

			if (sensor2Pressed && nextSensorPressed && !conveyorStopped) {
				stopConveyor();
				return true;
			}
			if (state == State.STOPPED && !nextConveyorStopped
					&& !nextSensorPressed) {
				state = State.NEXT_START;
				return true;
			}
			if (state == State.NEXT_STOP) {
				if (sensor2Pressed && !conveyorStopped)
					stopConveyor();
				return true;
			} else if (state == State.NEXT_START) {
				if (conveyorStopped) {
					startConveyor();
					return true;
				}

				if (sensor2Pressed && !nextConveyorStopped
						&& glassList.size() > 0) {
					releaseGlass();
					return true;
				}
			}
		} else {
			if (breakConveyor && !conveyorStopped) {
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
		if (state == State.NEXT_STOP) {
			state = State.NEXT_STOP;
		} else {
			state = State.NONNORM_STOPPED;
		}

		// sensor2Pressed = false;
		pcf.msgStopConveyor();
		// stateChanged();
	}

	private void stopConveyor() {
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,
				numberOfConveyor);
		pcf.msgStopConveyor();
		conveyorStopped = true;
		state = State.STOPPED;
		// sensor2Pressed = false;
		print("Turning off conveyor.");
		// stateChanged();
	}

	private void startConveyor() {
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				numberOfConveyor);
		pcf.msgStartConveyor();
		conveyorStopped = false;
		print("Turning on conveyor.");
		// stateChanged();
	}

	private void releaseGlass() {

		ncf.msgHereIsGlass(glassList.get(0).glass);
		glassList.remove(0);
		print("State: " + state);
		print("Releasing glass to next family");
		// stateChanged();
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
	}
}
