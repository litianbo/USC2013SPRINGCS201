package engine.agent.families.cf6;

import transducer.Transducer;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;

/**
 * This class handles constructing of CF6 agents and relaying incoming messages
 * to them.
 * 
 * This family performs cross-seaming on glass.
 * 
 * @author Harry Trieu
 * 
 */
public class ConveyorFamilySix implements ConveyorFamilyInterface {
	/** DATA **/
	private final Integer FAMILY_NUMBER = 6;
	private final Integer POPUP_NUMBER = 1;

	private Transducer transducer;

	/** Agents in My Conveyer Family **/
	private PopupAgent popupAgent;
	private EntranceSensorAgent entranceSensorAgent;
	private ExitSensorAgent exitSensorAgent;
	private ConveyerAgent conveyerAgent;
	private WorkstationAgent topWorkstationAgent;
	private WorkstationAgent bottomWorkstationAgent;

	/** Neighboring Conveyer Families **/
	private ConveyorFamilyInterface previousConveyor;
	private ConveyorFamilyInterface nextConveyor;

	/**
	 * Default constructor.
	 * 
	 * @param t
	 *            Transducer
	 */
	public ConveyorFamilySix(Transducer t) {
		// Set the transducer
		this.transducer = t;

		// Construct agents
		this.topWorkstationAgent = new WorkstationAgent("Top Crosseamer",
				transducer, 0);
		this.bottomWorkstationAgent = new WorkstationAgent("Bottom Crosseamer",
				transducer, 1);
		this.conveyerAgent = new ConveyerAgent("Conveyer " + FAMILY_NUMBER,
				FAMILY_NUMBER, transducer);
		this.popupAgent = new PopupAgent("Popup " + POPUP_NUMBER, POPUP_NUMBER,
				transducer, conveyerAgent, topWorkstationAgent,
				bottomWorkstationAgent);
		this.entranceSensorAgent = new EntranceSensorAgent("Entrance Sensor "
				+ (2 * FAMILY_NUMBER), (2 * FAMILY_NUMBER), transducer,
				conveyerAgent);
		this.exitSensorAgent = new ExitSensorAgent("Exit Sensor "
				+ ((2 * FAMILY_NUMBER) + 1), ((2 * FAMILY_NUMBER) + 1),
				transducer);

		// Set agent references
		topWorkstationAgent.setPopupAgent(popupAgent);
		bottomWorkstationAgent.setPopupAgent(popupAgent);
		conveyerAgent.setEntranceSensorAgent(entranceSensorAgent);
	}

	/**
	 * Starts all agent threads.
	 */
	public void startAgentThreads() {
		topWorkstationAgent.startThread();
		bottomWorkstationAgent.startThread();
		conveyerAgent.startThread();
		popupAgent.startThread();
		entranceSensorAgent.startThread();
		exitSensorAgent.startThread();
	}

	/**
	 * Called by the previous family when it is ready to pass on glass.
	 */
	@Override
	public void msgHereIsGlass(Glass glass) {
		popupAgent.msgHereIsGlass(glass);
	}

	/**
	 * Called when the family is constructed to set a reference to the next
	 * family.
	 */
	@Override
	public void setNextFamily(ConveyorFamilyInterface nextFamily) {
		this.nextConveyor = nextFamily;

		popupAgent.setNextFamily(nextConveyor);
	}

	/**
	 * Called when the family is constructed to set a reference to the previous
	 * family.
	 */
	@Override
	public void setPreviousFamily(ConveyorFamilyInterface previousFamily) {
		this.previousConveyor = previousFamily;

		conveyerAgent.setPreviousFamily(previousConveyor);
	}

	/**
	 * Called by the next family when it is ready to receive glass again.
	 */
	@Override
	public void msgStartConveyor() {
		System.out.println("CF6: CF7 is telling me to restart!");
		
		// "Unfreeze" the popup and let it continue passing glass on.
		popupAgent.msgStartForNextConveyer();
		
		// Tell this and preceding conveyers to restart.
		conveyerAgent.msgRestartAllConveyers();
	}

	/**
	 * Called by the next family when it can no longer receive glass.
	 */
	@Override
	public void msgStopConveyor() {
		System.out.println("CF6: CF7 is telling me to stop!");

		// "Freeze" the popup and do not allow it to receive glass from
		// workstations/pass the glass on to the next family.
		popupAgent.msgStopForNextConveyer();
		
		// Stop this and preceding conveyers.
		conveyerAgent.msgStopAllConveyers();
	}

}
