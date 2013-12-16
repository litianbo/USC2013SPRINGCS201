package engine.agent.families.cf14;

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

public class ConveyorAgentFourteen extends Agent implements
		ConveyorFamilyInterface {
	// DATA
	ConveyorFamilyInterface nextNeighbor, previousNeighbor;
	boolean loaded = false;
	Semaphore sensor2Pressed = new Semaphore(0);
	Semaphore sensor1Pressed = new Semaphore(0);
	boolean conveyorStopped = false;
	boolean nextConveyorStopped = false;
	boolean machining = false;
	boolean breakConveyor = false;
	boolean doLoadGlass = false;
	boolean fixConveyor = false;
	boolean truckDisable = false;
	Object[] numberOfSensor1 = new Object[1];
	Object[] numberOfSensor2 = new Object[1];
	Object[] channelConveyor = new Object[1];
	Semaphore loadFinished = new Semaphore(0);
	Semaphore emptyFinished = new Semaphore(0);
	Semaphore sensor2Released = new Semaphore(0);
	List<MyGlass> glassList = Collections
			.synchronizedList(new ArrayList<MyGlass>());
	State state = State.NULL;

	enum State {
		NULL, GLASS_COMING, LOADED, STOP
	}

	private class MyGlass {
		boolean machined;
		Glass glass;

		private MyGlass(Glass glass) {
			this.glass = glass;
			machined = false;
		}
	}

	public ConveyorAgentFourteen(Transducer transducer) {
		super("Truck", transducer);
		this.transducer.register(this, TChannel.SENSOR);
		this.transducer.register(this, TChannel.TRUCK);
		this.transducer.register(this, TChannel.CONVEYOR);
		this.transducer = transducer;
		numberOfSensor1[0] = 28;
		numberOfSensor2[0] = 29;
		channelConveyor[0] = 14;
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				channelConveyor);
	}

	public void setNextFamily(ConveyorFamilyInterface next) {
		nextNeighbor = next;
	}

	public void setPreviousFamily(ConveyorFamilyInterface previous) {
		previousNeighbor = previous;
	}

	// messages
	// sent by previous family with a piece of incoming glass
	public void msgHereIsGlass(Glass glass) {
		System.out.println("Conveyor family 14 receiving glass");
		// Create the glass
		MyGlass temp = new MyGlass(glass);
		temp.machined = false;

		// add to queue
		glassList.add(temp);
		stateChanged();
	}

	public void msgStartConveyor() {

	}

	public void msgStopConveyor() {

	}

	// scheduler
	public boolean pickAndExecuteAnAction() {
		if (!breakConveyor) {
			if (conveyorStopped && !loaded) {
				startConveyor();
				return true;
			}
			
			if (glassList.size() > 0 && loaded && !conveyorStopped) {
				stopConveyor();
			}
			if (glassList.size() > 0 && !doLoadGlass) {
				loadGlass();
				return true;
			}

		} else {
			if (!conveyorStopped) {
				stopConveyor();
				return true;
			}
			if (fixConveyor) {
				startConveyor();
				fixConveyor = false;
				breakConveyor = false;
				return true;
			}
		}
		return false;
	}

	// methods
	private void loadGlass() {
		try {
			sensor2Pressed.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		print("sensor released");
		transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_LOAD_GLASS, null);

		try {
			loadFinished.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopConveyor();
		print("empty glass");
		transducer.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
		try {
			emptyFinished.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		glassList.remove(0);
		print("empty finished");
		startConveyor();
		stateChanged();
	}

	private void stopConveyor() {

		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,
				channelConveyor);
		conveyorStopped = true;
		previousNeighbor.msgStopConveyor();
		print(" Turning off conveyor.");

	}

	private void startConveyor() {
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,
				channelConveyor);
		conveyorStopped = false;
		previousNeighbor.msgStartConveyor();
		print(" Turning on conveyor.");

	}

	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		if (channel == TChannel.CONVEYOR && args[0].equals(new Integer(14))) {
			if (event == TEvent.BREAK) {
				breakConveyor = true;

				stateChanged();
			}
			if (event == TEvent.FIX) {
				fixConveyor = true;
				stateChanged();
			}
		}
		if (channel == TChannel.TRUCK) {
			if (event == TEvent.BREAK) {
				truckDisable = true;
				stateChanged();
			}
			if (event == TEvent.FIX) {
				truckDisable = false;
				stateChanged();
			}
			if (event == TEvent.TRUCK_GUI_LOAD_FINISHED) {
				loadFinished.release();
				loaded = true;
			}
			if (event == TEvent.TRUCK_GUI_EMPTY_FINISHED) {
				emptyFinished.release();
				loaded = false;
				doLoadGlass = false;
			}
			if (event == TEvent.TRUCK_DO_LOAD_GLASS) {
				doLoadGlass = true;
			}

		}
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED
				&& args[0].equals(numberOfSensor2[0])) {
			sensor2Released.release();
		}
		if (channel == TChannel.SENSOR) {
			if (args[0].equals(numberOfSensor2[0])) {
				if (event == TEvent.SENSOR_GUI_PRESSED) {
					sensor2Pressed.release();
				}

			}
			if (args[0].equals(numberOfSensor1[0])) {
				sensor1Pressed.release();
			}
		}

	}

}
