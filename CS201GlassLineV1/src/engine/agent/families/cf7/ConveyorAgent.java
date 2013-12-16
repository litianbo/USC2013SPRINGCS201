package engine.agent.families.cf7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Conveyor;
import engine.agent.families.cf7.interfaces.Popup;
import engine.agent.families.cf7.interfaces.Sensor;

public class ConveyorAgent extends Agent implements Conveyor {

	public enum ConveyorState { STARTED, STOPPED };
	public enum GUIConveyorState { STARTED, STOPPED };
	public enum MessageState { START, STOP };
	public enum WorkingState { WORKING, BEGIN_WORKING, BROKEN, BEGIN_BREAKING };
	
	// Data
	Transducer transducer;
	Popup popup;
	Sensor frontSensor, backSensor;
	ConveyorFamilyInterface previousFamily;
	public Boolean glassOnBackSensor;
	public ConveyorState state;
	public GUIConveyorState GUIstate;
	public MessageState messageState;
	public WorkingState workingState;
	public int index, numGlassOnBackSensor;
	public boolean popupRequestedStart, glassOnFrontSensor, factoryStarted, stoppedPreviousFamily;
	
	private List<Glass> glass;
	Object[] args;
	
	// Turn on debugMessages
	boolean debugMessages;
	
	
	// Constructor
	public ConveyorAgent(Transducer t, Popup p, Sensor fs, Sensor bs, int i){
		super();
		transducer = t;
		popup = p;
		frontSensor = fs;
		backSensor = bs;
		glassOnBackSensor = false;
		numGlassOnBackSensor = 0;
		index = i;
		args = new Object[1];
		args[0] = index;
		this.name = ("Conveyor" + index);
		state = ConveyorState.STOPPED;
		GUIstate = GUIConveyorState.STOPPED;
		messageState = MessageState.STOP;
		workingState = WorkingState.WORKING;
		popupRequestedStart = true;
		glassOnFrontSensor = false;
		factoryStarted = false;
		stoppedPreviousFamily = false;
		
		glass = Collections.synchronizedList(new ArrayList<Glass>());
		debugMessages = false;
	}
	
	
	// Messages
	
	public void msgHereIsGlass(Glass g){
		if(debugMessages){
			if(g.getRecipe().getNeedGrinding()){
				System.out.println(this.getName() + " has received glass to be processed.");
			}
			else {
				System.out.println(this.getName() + " has received glass to pass on.");
			}
		}
		glassOnFrontSensor = false;
		glass.add(g);
		stateChanged();
	}
	
	public synchronized void msgSendGlass(){
		numGlassOnBackSensor++;
		glassOnBackSensor = true;
		stateChanged();
	}
	
	public void msgStartConveyor(){
		messageState = MessageState.START;
		if(debugMessages){
			System.out.println(this.getName() + " received message to start.");
		}
		stateChanged();
	}
	
	public void msgStopConveyor(){
		messageState = MessageState.STOP;
		if(debugMessages){
			System.out.println(this.getName() + " received message to stop.");
		}
		stateChanged();
	}
	
	public synchronized void msgPopupRequestingStart(){
		messageState = MessageState.START;
		popupRequestedStart = true;
		stateChanged();
	}
	
	
	public synchronized void msgPopupRequestingStop(){
		messageState = MessageState.STOP;
		popupRequestedStart = false;
		stateChanged();
	}
	
	public synchronized void msgGlassOnFrontSensor(){
		//TODO: make this an action
		if(factoryStarted){
			if(state == ConveyorState.STOPPED){
				previousFamily.msgStopConveyor();
				stoppedPreviousFamily = true;
				if(debugMessages){
					System.out.println(this.getName() + " asking conveyor6 to stop.");
				}
			}
		}
		else {
			factoryStarted = true;
		}
		glassOnFrontSensor = true;
		stateChanged();
	}
	
	// No conveyor callbacks
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		if(channel == TChannel.CONVEYOR){
			if(((Integer)args[0])==index){
				if(event == TEvent.BREAK){
					workingState = WorkingState.BEGIN_BREAKING;
					if(debugMessages){
						System.out.println(this.getName() + " registered BREAK event.");
					}
					stateChanged();
				}
				else if(event == TEvent.FIX){
					workingState = WorkingState.BEGIN_WORKING;
					if(debugMessages){
						System.out.println(this.getName() + " registered FIX event.");
					}
					stateChanged();
				}
			}
		}
	}
	
	// Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		if(!(workingState == WorkingState.BROKEN)){
			if(state == ConveyorState.STARTED){
				if(workingState == WorkingState.BEGIN_BREAKING){
					breakConveyor();
					return true;
				}
				else if(messageState == MessageState.STOP){
					stopConveyor();
					return true;
				}
				else if(GUIstate == GUIConveyorState.STOPPED){
					startConveyorGUI();
					return true;
				}
				else if(GUIstate == GUIConveyorState.STARTED){
					if(glassOnBackSensor){
						sendGlassToBackSensor();
						return true;
					}
				}
			}
			else if(state == ConveyorState.STOPPED){
				if(workingState == WorkingState.BEGIN_WORKING){
					fixConveyor();
					return true;
				}
				else if(messageState == MessageState.START){
					if(popupRequestedStart){
						startConveyor();
						return true;
					}
				}
				else if(GUIstate == GUIConveyorState.STARTED){
					stopConveyorGUI();
					return true;
				}
			}
		}
		return false;
	}

	
	// Actions
	public void startConveyor(){
		state = ConveyorState.STARTED;
		if(stoppedPreviousFamily){
			previousFamily.msgStartConveyor();
			stoppedPreviousFamily = false;
			if(debugMessages){
				System.out.println(this.getName() + " is starting previous family.");
			}
		}
		if(debugMessages){
			System.out.println(this.getName() + " starting.");
		}
	}
	
	public void startConveyorGUI(){
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		if(debugMessages){
			System.out.println("GUIConveyor " + this.getName() + " started.");
		}
		GUIstate = GUIConveyorState.STARTED;
	}
	
	public void stopConveyor(){
		state = ConveyorState.STOPPED;
		if(glassOnFrontSensor){
			previousFamily.msgStopConveyor();
			stoppedPreviousFamily = true;
			if(debugMessages){
				System.out.println(this.getName() + " is stopping previous family.");
			}
		}
		if(debugMessages){
			System.out.println(this.getName() + " stopping.");
		}
	}
	
	public void stopConveyorGUI(){
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
		if(debugMessages){
			System.out.println("GUIConveyor " + this.getName() + " stopped.");
		}
		GUIstate = GUIConveyorState.STOPPED;
	}
	
	public void sendGlassToBackSensor(){
		if(glass.size() > 0){
			backSensor.msgHereIsGlass(glass.get(0));
			numGlassOnBackSensor--;
			if(numGlassOnBackSensor == 0){
				glassOnBackSensor = false;
			}
			glass.remove(0);
		}
	}
	
	public void breakConveyor(){
		state = ConveyorState.STOPPED;
		workingState = WorkingState.BROKEN;
		previousFamily.msgStopConveyor();

		if(debugMessages){
			System.out.println(this.getName() + " is broken.");
		}
	}
	
	public void fixConveyor(){
		state = ConveyorState.STARTED;
		workingState = WorkingState.WORKING;
		previousFamily.msgStartConveyor();
		if(debugMessages){
			System.out.println(this.getName() + " is fixed.");
		}
	}
	
	public void setPreviousFamily(ConveyorFamilyInterface c){
		previousFamily = c;
	}
	
	public void setDebugMessages(boolean set){
		debugMessages = set;
	}
}
