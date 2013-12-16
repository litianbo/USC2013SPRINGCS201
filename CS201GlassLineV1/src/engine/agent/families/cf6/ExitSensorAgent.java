package engine.agent.families.cf6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.cf6.interfaces.ExitSensor;

/**
 * This agent responds to events fired by the GUI exit sensor.
 * 
 * @author Harry Trieu
 *
 */
public class ExitSensorAgent extends Agent implements ExitSensor {
	/** DATA **/
	private Integer SENSOR_NUMBER;
	
	public enum SensorState {
		INACTIVE, TRIGGERED
	};

	public enum SensorEvent {
		SENSOR_PRESS, SENSOR_RELEASE
	};

	// Sensor starts in an inactive state
	private SensorState state = SensorState.INACTIVE;

	private List<SensorEvent> events = Collections
			.synchronizedList(new ArrayList<SensorEvent>());

	// private ConveyerAgent conveyerAgent;
	// private Popup popup;

	/**
	 * Default constructor.
	 * 
	 * @param name of the agent
	 * @param number of the sensor
	 * @param ft Transducer
	 */
	public ExitSensorAgent(String name, Integer number, Transducer ft) {
		super(name, ft);
		// this.conveyerAgent = conveyerAgent;
		transducer.register(this, TChannel.SENSOR);
		this.SENSOR_NUMBER = number;
		// this.popup = popup;
	}

	/** MESSAGES **/
	/**
	 * Called by the GUI/transducer when a piece of glass presses the sensor.
	 */
	public void msgGUISensorPressed() {
		events.add(SensorEvent.SENSOR_PRESS);
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
				print("Sensor Pressed!");
				state = SensorState.TRIGGERED;
				return true;
			}
		}

		// If the sensor has been triggered and gets released, chagne sensor
		// state to inactive.
		if (state == SensorState.TRIGGERED) {
			if (event == SensorEvent.SENSOR_RELEASE) {
				print("Sensor Released!");
				state = SensorState.INACTIVE;
				return true;
			}
		}

		// no rules match
		return false;
	}

	/** ACTIONS **/
//	private void giveGlassToPopup() {
//		popup.msgIncomingGlassFromConveyer();
//		stateChanged();
//	}

	/** EXTRA **/
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		Integer sensorNumber = (Integer)args[0];
		
		if (event == TEvent.SENSOR_GUI_PRESSED && sensorNumber == SENSOR_NUMBER) {
			msgGUISensorPressed();
		} else if (event == TEvent.SENSOR_GUI_RELEASED && sensorNumber == SENSOR_NUMBER) {
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

}
