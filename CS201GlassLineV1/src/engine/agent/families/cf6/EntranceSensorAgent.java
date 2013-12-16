package engine.agent.families.cf6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.cf6.interfaces.Conveyer;
import engine.agent.families.cf6.interfaces.EntranceSensor;

/**
 * This agent responds to events fired by the GUI entrance sensor.
 * 
 * @author Harry Trieu
 * 
 */
public class EntranceSensorAgent extends Agent implements EntranceSensor {
	/** DATA **/
	private Integer SENSOR_NUMBER;

	public enum SensorState {
		INACTIVE, TRIGGERED
	};

	public enum SensorEvent {
		SENSOR_PRESS, SENSOR_RELEASE
	};

	// Sensor starts off in the inactive state
	private SensorState state = SensorState.INACTIVE;

	private List<SensorEvent> events = Collections
			.synchronizedList(new ArrayList<SensorEvent>());

	// Agent references
	private Conveyer conveyer;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            of this agent
	 * @param conveyorAgent
	 *            reference to this family's conveyorAgent
	 */
	public EntranceSensorAgent(String name, Integer number, Transducer ft,
			Conveyer conveyer) {
		super(name, ft);

		transducer.register(this, TChannel.SENSOR);

		this.conveyer = conveyer;
		this.SENSOR_NUMBER = number;

		// print("Entrance sensor created.");
	}

	/** MESSAGES **/
	/**
	 * Called by the conveyor when it needs to check whether there is currently
	 * a piece of glass sitting on the sensor.
	 * 
	 * @return true if there is glass on the sensor
	 */
	public boolean msgCheckForTriggeredSensor() {
		if (state == SensorState.TRIGGERED) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Called by the GUI/transducer when a piece of glass presses the sensor.
	 */
	public void msgGUISensorPressed() {
		events.add(SensorEvent.SENSOR_PRESS);
		// print("Events size: " + events.size());
		stateChanged();
	}

	/**
	 * Called by the GUI/transducer when a piece of glass releases the sensor.
	 */
	public void msgGUISensorReleased() {
		events.add(SensorEvent.SENSOR_RELEASE);
		stateChanged();
	}

	/** SCHEDULER **/
	@Override
	public boolean pickAndExecuteAnAction() {
		if (events.isEmpty()) {
			return false;
		}

		SensorEvent event = events.remove(0);

		// If the sensor is inactive and gets pressed, tell the conveyor to
		// start and change sensor state to triggered.
		if (state == SensorState.INACTIVE) {
			if (event == SensorEvent.SENSOR_PRESS) {
				print("Sensor pressed!");
				tellConveyorToStart();
				state = SensorState.TRIGGERED;
				return true;
			}
		}

		// If the sensor has been triggered and gets released, chagne sensor
		// state to inactive.
		if (state == SensorState.TRIGGERED) {
			if (event == SensorEvent.SENSOR_RELEASE) {
				print("Sensor released!");
				state = SensorState.INACTIVE;
				return true;
			}
		}

		// no rules match
		return false;
	}

	/** ACTIONS **/
	/**
	 * Message the conveyer agent with a request to start the conveyer.
	 */
	private void tellConveyorToStart() {
		print("Telling conveyer to start.");
		conveyer.msgStartThisConveyer();
		stateChanged();
	}

	/** EXTRA **/
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		Integer sensorNumber = (Integer) args[0];

		// print("Received a transducer event" + sensorNumber);

		if (event == TEvent.SENSOR_GUI_PRESSED && sensorNumber == SENSOR_NUMBER) {
			msgGUISensorPressed();
		} else if (event == TEvent.SENSOR_GUI_RELEASED
				&& sensorNumber == SENSOR_NUMBER) {
			msgGUISensorReleased();
		}

	}

	/**
	 * @return the state
	 */
	public SensorState getState() {
		return state;
	}

	/**
	 * @return the events
	 */
	public List<SensorEvent> getEvents() {
		return events;
	}

	/**
	 * @return the conveyer
	 */
	public Conveyer getConveyer() {
		return conveyer;
	}

}
