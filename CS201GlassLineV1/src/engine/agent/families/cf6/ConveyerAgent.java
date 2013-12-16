package engine.agent.families.cf6;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.cf6.interfaces.Conveyer;

/**
 * This agent responds to events fired by the GUI conveyor.
 * 
 * @author Harry Trieu
 * 
 */
public class ConveyerAgent extends Agent implements Conveyer {
	/** DATA **/
	private Integer CONVEYER_NUMBER;

	// Agent references
	private ConveyorFamilyInterface previousConveyor;
	private EntranceSensorAgent entranceSensor;

	private Object[] args;

	// Conveyer state information
	public enum ConveyerState {
		STARTED, INITIAL_STOP, THIS_CONVEYER_STOPPED, ALL_CONVEYERS_STOPPED, BROKEN, WAITING_TO_RESTART_ALL_CONVEYERS
	};

	public enum ConveyerEvent {
		START, STOP_THIS_CONVEYER, STOP_ALL_CONVEYERS, RESTART_ALL_CONVEYORS, BREAK, FIX
	};

	// Conveyer starts off stopped
	private ConveyerState state = ConveyerState.INITIAL_STOP;

	// List of conveyer events
	private List<ConveyerEvent> events = Collections
			.synchronizedList(new ArrayList<ConveyerEvent>());

	private Timer restartConveyersTimer = new Timer();

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            of the agent
	 */
	public ConveyerAgent(String name, Integer number, Transducer ft) {
		super(name, ft);

		transducer.register(this, TChannel.CONVEYOR);

		this.CONVEYER_NUMBER = number;

		args = new Object[1];
		args[0] = CONVEYER_NUMBER;
	}

	/** MESSAGES **/
	/**
	 * Called by the GUI/transducer to break/stop the conveyer.
	 */
	public void msgBreakConveyer() {
		events.add(ConveyerEvent.BREAK);
		stateChanged();
	}

	/**
	 * Called by the GUI/transducer to fix/restart the conveyer.
	 */
	public void msgFixConveyer() {
		events.add(ConveyerEvent.FIX);
		stateChanged();
	}

	/**
	 * Called by the popup when both workstations are busy.
	 */
	public void msgStopAllConveyers() {
		events.add(ConveyerEvent.STOP_ALL_CONVEYERS);
		stateChanged();
	}

	/**
	 * Called by the EntranceSensorAgent when a piece of glass has released the
	 * sensor.
	 */
	public void msgStartThisConveyer() {
		events.add(ConveyerEvent.START);
		stateChanged();
	}

	/**
	 * Called when the popup is raised or the next conveyer family tells our
	 * family to stop.
	 */
	public void msgStopThisConveyer() {
		events.add(ConveyerEvent.STOP_THIS_CONVEYER);
		stateChanged();
	}

	/**
	 * Called when the popup has been lowered or the next conveyer family tells
	 * us it's ok to restart.
	 */
	public void msgRestartAllConveyers() {
		events.add(ConveyerEvent.RESTART_ALL_CONVEYORS);
		stateChanged();
	}

	/** SCHEDULER **/
	@Override
	public boolean pickAndExecuteAnAction() {
		if (events.isEmpty()) {
			return false;
		}

		ConveyerEvent event = events.remove(0);

		// Regardless of what state the conveyer is in, stop this and all
		// preceding conveyers if this conveyer breaks.
		if (event == ConveyerEvent.BREAK) {
			stopAllConveyers();
			state = ConveyerState.BROKEN;
			return true;
		}

		// Restart all conveyers if this conveyer is broken and gets fixed.
		if (state == ConveyerState.BROKEN) {
			if (event == ConveyerEvent.FIX) {
				startAllConveyers();
				state = ConveyerState.STARTED;
				return true;
			}
		}

		// Stop this conveyer if popup is raised or we get a stop message from
		// next conveyer family
		if (state == ConveyerState.STARTED) {
			if (event == ConveyerEvent.STOP_THIS_CONVEYER) {
				// print("Performing an emergency stop.");
				stopThisConveyer();
				state = ConveyerState.THIS_CONVEYER_STOPPED;
				return true;
			}
		}

		// If conveyers are started and we get a message to stop all conveyers,
		// stop this conveyer and all preceding conveyers.
		if (state == ConveyerState.STARTED) {
			if (event == ConveyerEvent.STOP_ALL_CONVEYERS) {
				// print("Performing an emergency stop.");
				stopAllConveyers();
				state = ConveyerState.ALL_CONVEYERS_STOPPED;
				return true;
			}
		}

		// Start this conveyer again after being stopped for raised popup or
		// the next family
		if (state == ConveyerState.THIS_CONVEYER_STOPPED) {
			if (event == ConveyerEvent.RESTART_ALL_CONVEYORS) {
				startThisConveyer();
				state = ConveyerState.STARTED;
				return true;
			}
		}

		// Start all conveyers again after being stopped because both
		// workstations were occupied
		if (state == ConveyerState.ALL_CONVEYERS_STOPPED) {
			if (event == ConveyerEvent.RESTART_ALL_CONVEYORS) {
				startAllConveyers();
				state = ConveyerState.STARTED;
				return true;
			}
		}

		// Start the conveyer for the first time when the first piece of glass
		// hits the entrance sensor
		if (state == ConveyerState.INITIAL_STOP) {
			if (event == ConveyerEvent.START) {
				// print("Performing a restart after emergency stop.");
				startThisConveyer();
				state = ConveyerState.STARTED;
				return true;
			}
		}

		// TODO: Debug statement for Francesca.
		print("No scheduler rules match. Current state = " + state
				+ ". Current event: " + event);

		// No rules match
		return false;
	}

	/** ACTIONS **/
	/**
	 * Checks whether or not the entrance sensor is pressed, restarts previous
	 * conveyors if it isn't.
	 */
	private void checkSensorAndRestartConveyers() {
		if (entranceSensor.msgCheckForTriggeredSensor()) {
			print("Congestion.  Waiting 5000 ms for it to clear before telling other families to restart.");

			restartConveyersTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					checkSensorAndRestartConveyers();
				}
			}, 5000);

		} else {
			previousConveyor.msgStartConveyor();
			
			print("All conveyers restarted.");
		}
	}

	/**
	 * Restarts this and all preceding conveyers.
	 */
	private void startAllConveyers() {
		print("Restarting all conveyers.");

		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);

		checkSensorAndRestartConveyers();

		stateChanged();
	}

	/**
	 * Stops this and all preceding conveyers.
	 */
	private void stopAllConveyers() {
		print("Stopping all conveyors.");

		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);

		previousConveyor.msgStopConveyor();

		stateChanged();
	}

	/**
	 * Start the conveyer (animation).
	 */
	private void startThisConveyer() {
		print("Starting this conveyor.");

		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);

		stateChanged();
	}

	/**
	 * Stop the conveyer (animation).
	 */
	private void stopThisConveyer() {
		print("Stopping this conveyor.");

		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);

		stateChanged();
	}

	/** EXTRA **/
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event,
			Object[] args) {
		Integer conveyerNumber = (Integer) args[0];

		if (event == TEvent.BREAK && conveyerNumber == CONVEYER_NUMBER) {
			msgBreakConveyer();
		} else if (event == TEvent.FIX && conveyerNumber == CONVEYER_NUMBER) {
			msgFixConveyer();
		}

	}

	/**
	 * Sets a reference to the entrance sensor agent.
	 * 
	 * @param entranceSensor
	 */
	public void setEntranceSensorAgent(EntranceSensorAgent entranceSensor) {
		this.entranceSensor = entranceSensor;
	}

	/**
	 * Sets the previous conveyor family so we can tell it to stop as needed.
	 * 
	 * @param cfi
	 */
	public void setPreviousFamily(ConveyorFamilyInterface cfi) {
		this.previousConveyor = cfi;
	}

	/**
	 * @return the state
	 */
	public ConveyerState getState() {
		return state;
	}

	/**
	 * @return the events
	 */
	public List<ConveyerEvent> getEvents() {
		return events;
	}

}
