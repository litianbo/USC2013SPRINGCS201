package engine.agent.families.cf7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Machine;
import engine.agent.families.cf7.interfaces.Popup;

public class MachineAgent extends Agent implements Machine {
	
	public enum MachiningState { NOT_MACHINED, IN_PROGRESS, MACHINED, WAITING_TO_LEAVE, WAITING_TO_BREAK, BROKEN };
	public enum GUIWorkstationState { EMPTY, LOADED, REQUESTED_LOAD, REQUESTED_RELEASE, REQUESTED_REMOVAL };
	public enum PopupState { UP, DOWN, REQUESTED_UP };
	
	// inner class for machining
	private class MachineGlass {
		Glass glass;
		MachiningState state;
		Boolean actionInProcess;
		
		private MachineGlass(Glass g){
			glass = g;
			state = MachiningState.NOT_MACHINED;
			actionInProcess = false;
		}
	}
	
	// Data
	Transducer transducer;
	Popup popup;
	int index;
	Object[] args;
	GUIWorkstationState GUIstate;
	PopupState popupState;
	
	private List<MachineGlass> glass;
	boolean processingEnabled;
	
	// Turn on debug messages
	boolean debugMessages;
	
	// Constructor
	public MachineAgent(Transducer t, int i){
		super();
		transducer = t;
		index = i;
		args = new Object[1];
		args[0] = index;
		this.name = ("Grinder" + index);
		
		glass = Collections.synchronizedList(new ArrayList<MachineGlass>());
		GUIstate = GUIWorkstationState.EMPTY;
		popupState = PopupState.DOWN;
		processingEnabled = true;
		
		debugMessages = false;
	}
	
	// Messages
	public void msgHereIsGlass(Glass g) {
		MachineGlass mg = new MachineGlass(g);
		glass.add(mg);
		stateChanged();
	}
	
	public void msgPopupAvailable(){
		if(debugMessages){
			System.out.println("Machine " + this.getName() + " sees that the popup is available.");
		}
		popupState = PopupState.UP;
		stateChanged();
	}
	
	public void msgLoadGlass(){
		if(debugMessages){
			System.out.println("Machine " + this.getName() + " should load glass from popup.");
		}
		popupState = PopupState.UP;
		stateChanged();
	}
	
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		if(channel == TChannel.GRINDER){
			if(((Integer)args[0])==index){
				if(event == TEvent.WORKSTATION_LOAD_FINISHED){
					if(debugMessages){
						System.out.println(this.name + " has registered event WORKSTATION_LOAD_FINISHED.");
					}
					GUIstate = GUIWorkstationState.LOADED;
					stateChanged();
				}
				else if(event == TEvent.WORKSTATION_RELEASE_FINISHED){
					if(debugMessages){
						System.out.println(this.name + " has registered event WORKSTATION_RELEASE_FINISHED.");
					}
					GUIstate = GUIWorkstationState.EMPTY;
					System.out.println("GUIstate from eventFired of " + this.getName() + ": " + GUIstate);
					stateChanged();
				}
				else if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){
					if(debugMessages){
						System.out.println(this.name + " has registered event WORKSTATION_GUI_ACTION_FINISHED.");
					}
					glass.get(0).actionInProcess = false;
					stateChanged();
				}
				else if(event == TEvent.SILENT_BREAK){
					if(debugMessages){
						System.out.println(this.name + " has registered event SILENT_BREAK.");
					}
					processingEnabled = false;
					stateChanged();
				}
				else if(event == TEvent.SILENT_FIX){
					if(debugMessages){
						System.out.println(this.name + " has registered event SILENT_FIX.");
					}
					processingEnabled = true;
					stateChanged();
				}
				else if(event == TEvent.WORKSTATION_REMOVE_GLASS){
					if(debugMessages){
						System.out.println(this.name + " has registered event WORKSTATION_REMOVE_GLASS.");
					}
					GUIstate = GUIWorkstationState.REQUESTED_REMOVAL;
					stateChanged();
				}
				else if(event == TEvent.WORKSTATION_BREAK_GLASS){
					if(glass.size() > 0){
						if(debugMessages){
							System.out.println(this.name + " has registered event WORKSTATION_BREAK_GLASS.");
						}

						glass.get(0).state = MachiningState.WAITING_TO_BREAK;
						stateChanged();
					}
				}
			}
		}
	}
	
	// Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		if(glass.size() > 1){
			System.out.println("ERROR: More than 1 piece of glass in machine.");
		}
		if(glass.size() > 0){
			if(GUIstate == GUIWorkstationState.REQUESTED_REMOVAL){
				removeGlass();
				return true;
			}
			else if(glass.get(0).state == MachiningState.WAITING_TO_BREAK){
				breakGlass();
				return true;
			}
			else if(glass.get(0).state == MachiningState.WAITING_TO_LEAVE){
				if(popupState == PopupState.DOWN){
					requestPopup();
					return true;
				}
				if(popupState == PopupState.UP){
					if(debugMessages){
						System.out.println("Popup state via " + this.getName() + ": " + popupState);
						System.out.println("GUIstate of " + this.getName() + ": " + GUIstate);
					}
					if(GUIstate == GUIWorkstationState.LOADED){
						workstationReleaseGlassBackEnd();
						workstationReleaseGlassGUI();
						return true;
					}
					/*else if(GUIstate == GUIWorkstationState.EMPTY){
						workstationReleaseGlassBackEnd();
						return true;
					}*/
				}
			}
			if(glass.get(0).state == MachiningState.MACHINED){
				glass.get(0).state = MachiningState.WAITING_TO_LEAVE;
				return true;
			}	
			else if(glass.get(0).state == MachiningState.NOT_MACHINED){
				if(GUIstate == GUIWorkstationState.LOADED){
					startMachining();
					return true;
				}
				else if(GUIstate == GUIWorkstationState.EMPTY){
					if(popupState == PopupState.UP){
						requestLoad();
						return true;
					}
				}
			}
			else if(glass.get(0).state == MachiningState.IN_PROGRESS){
				if(glass.get(0).actionInProcess == false){
					finishMachining();
					return true;
				}
			}			
		}
		return false;
	}
	
	public void breakGlass(){
		if(debugMessages){
			System.out.println(this.getName() + " breaking glass.");
		}
		
		glass.get(0).state = MachiningState.BROKEN;
	}
	
	public void removeGlass(){
		if(glass.get(0).state == MachiningState.BROKEN){
			if(debugMessages){
				System.out.println(this.getName() + " removing broken glass.");
			}
			GUIstate = GUIWorkstationState.EMPTY;
			glass.remove(0);
			popup.msgBrokenGlassRemoved(this);
		}
	}
	
	public void workstationReleaseGlassGUI(){
		if(debugMessages){
			System.out.println(this.getName() + " releasing GUIGlass to popup.");
		}
		transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_RELEASE_GLASS, args);
		GUIstate = GUIWorkstationState.REQUESTED_RELEASE;	
	}
	
	public void workstationReleaseGlassBackEnd(){
		if(debugMessages){
			System.out.println(this.getName() + " releasing glass to popup.");
		}
		popup.msgGlassMachined(glass.get(0).glass, this);
		glass.remove(0);
		popupState = PopupState.DOWN;
	}
	
	public void startMachining(){
		if(debugMessages){
			System.out.println("Glass in " + this.getName() + " starting machining.");
		}
		glass.get(0).state = MachiningState.IN_PROGRESS;
		transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_ACTION, args);
		glass.get(0).actionInProcess = true;
	}
	
	public void finishMachining(){
		if(processingEnabled){
			if(debugMessages){
				System.out.println("Glass in " + this.getName() + " finished machining.");
			}
			glass.get(0).state = MachiningState.MACHINED;
		}
	}
	
	public void requestLoad(){
		transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
		GUIstate = GUIWorkstationState.REQUESTED_LOAD;
		popupState = PopupState.DOWN;
	}
	
	public void requestPopup(){
		popup.msgGlassReadyForRelease(this);
		popupState = PopupState.REQUESTED_UP;
	}
	
	public void setPopup(Popup p){
		popup = p;
	}
	
	public void setDebugMessages(boolean set){
		debugMessages = set;
	}
}

