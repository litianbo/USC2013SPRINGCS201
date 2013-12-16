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
import engine.agent.families.cf7.ConveyorAgent.ConveyorState;
import engine.agent.families.cf7.interfaces.Conveyor;
import engine.agent.families.cf7.interfaces.Machine;
import engine.agent.families.cf7.interfaces.Popup;
import engine.agent.families.cf7.interfaces.Sensor;

public class PopupAgent extends Agent implements Popup {
 
	public enum PopupMoveState { UP, DOWN };
	public enum GUIHeightState { UP, DOWN, MOVING_UP, MOVING_DOWN };
	public enum GUILoadState { LOADED, RELEASED, LOADING, RELEASING, PENDING };
	public enum MachiningState { NOT_MACHINED, MACHINED };
	public enum WorkstationLocation { UPPER, LOWER };
	public enum WorkstationState { FULL, EMPTY, REQUESTED_RELEASE, RELEASE_PENDING };
	public enum WorkingState { WORKING, BEGIN_WORKING, BROKEN, BEGIN_BREAKING };
	
	// inner class for popup
	private class PopupGlass {
		Glass glass;
		MachiningState state;
		
		private PopupGlass(Glass g){
			glass = g;
			state = MachiningState.NOT_MACHINED;
		}
	}
	
	// inner class for machines
	private class Workstation {
		Machine machine;
		WorkstationLocation location;
		WorkstationState state;
		WorkingState workingState;
		
		private Workstation(Machine m, int i){
			machine = m;
			state = WorkstationState.EMPTY;
			workingState = WorkingState.WORKING;
			if(i == 0){
				location = WorkstationLocation.UPPER;
			}
			else {
				location = WorkstationLocation.LOWER;
			}
		}
		
	}
	
	// Data
	Transducer transducer;
	Conveyor conveyor;
	private List<Workstation> workstations;
	Sensor backSensor;
	public PopupMoveState moveState;
	public GUIHeightState heightState;
	public ConveyorState conveyorState;
	public GUILoadState loadState;
	public WorkingState workingState;
	int index;
	Object args[];
	
	private List<PopupGlass> glass;
	ConveyorFamilyInterface nextFamily;
	boolean nextFamilyAbleToReceiveGlass;
	
	// Turn on debug messages
	boolean debugMessages;
	
	// Constructor
	public PopupAgent(Transducer t, int i){
		super();
		transducer = t;
		moveState = PopupMoveState.DOWN;
		heightState = GUIHeightState.DOWN;
		loadState = GUILoadState.RELEASED;
		conveyorState = ConveyorState.STARTED;
		workingState = WorkingState.WORKING;
		index = i;
		args = new Object[1];
		args[0] = (Object)index;
		this.name = ("Popup" + index);
		workstations = Collections.synchronizedList(new ArrayList<Workstation>());	
		glass = Collections.synchronizedList(new ArrayList<PopupGlass>());
		nextFamilyAbleToReceiveGlass = true;
		
		debugMessages = false;
	}
	
	// Messages
	public void msgConveyorStopped(){
		if(debugMessages){
			System.out.println(this.getName() + " will not release glass until next conveyor is ready.");
		}
		nextFamilyAbleToReceiveGlass = false;
		stateChanged();
	}
	
	public void msgConveyorStarted(){
		if(debugMessages){
			System.out.println(this.getName() + " is ready to release glass onto next conveyor.");
		}
		nextFamilyAbleToReceiveGlass = true;
		stateChanged();
	}
	
	public void msgHereIsGlass(Glass g){
		PopupGlass pg = new PopupGlass(g);
		if(g.getRecipe().getNeedGrinding()){
			pg.state = MachiningState.NOT_MACHINED;
			if(debugMessages){
				System.out.println(this.getName() + " has received glass from conveyor to be processed.");	
			}
		}
		else {
			pg.state = MachiningState.MACHINED;
			loadState = GUILoadState.PENDING;
			if(debugMessages){
				System.out.println(this.getName() + " has received glass from conveyor to pass on.");
			}
		}
		glass.add(pg);
		stateChanged();
	}
	
	public void msgGlassMachined(Glass g, Machine m){
		PopupGlass pg = new PopupGlass(g);
		pg.state = MachiningState.MACHINED;
		glass.add(pg);
		
		Workstation tempWorkstation = null;
		synchronized(workstations){
			for(Workstation w:workstations){
				if(w.machine == m){
					tempWorkstation = w;
					break;
				}
			}
		}
		if(tempWorkstation != null){
			tempWorkstation.state = WorkstationState.EMPTY;
			if(debugMessages){
				System.out.println(this.getName() + " has received glass from workstation " + tempWorkstation.machine.getName() + ".");
			}
		}
		stateChanged();
	}
	
	public void msgBrokenGlassRemoved(Machine m){
		Workstation tempWorkstation = null;
		synchronized(workstations){
			for(Workstation w:workstations){
				if(w.machine == m){
					tempWorkstation = w;
					break;
				}
			}
		}
		if(tempWorkstation != null){
			tempWorkstation.state = WorkstationState.EMPTY;
			loadState = GUILoadState.PENDING;
			if(debugMessages){
				System.out.println(this.getName() + " has been notified that glass in " + tempWorkstation.machine.getName() + " has been removed.");
			}
		}
		stateChanged();
	}
	
	public void msgGlassReadyForRelease(Machine m){
		Workstation tempWorkstation = null;
		synchronized(workstations){
			for(Workstation w: workstations){
				if(w.machine == m){
					tempWorkstation = w;
					break;
				}
			}
		}
		if(tempWorkstation != null){
			tempWorkstation.state = WorkstationState.REQUESTED_RELEASE;
			if(debugMessages){
				System.out.println("Workstation " + tempWorkstation.machine.getName() + 
					" has requested to release glass to popup " + this.getName() + ".");
			}
		}
		stateChanged();
	}
	
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		if(channel == TChannel.POPUP){
			if(((Integer)args[0])==index){
				if(event == TEvent.POPUP_GUI_LOAD_FINISHED){
					loadState = GUILoadState.LOADED;
					if(debugMessages){
						System.out.println(this.getName() + " registered POPUP_GUI_LOAD_FINISHED");
					}
					stateChanged();
				}
				else if(event == TEvent.POPUP_GUI_RELEASE_FINISHED){
					loadState = GUILoadState.RELEASED;
					if(debugMessages){
						System.out.println(this.getName() + " registered POPUP_GUI_RELEASE_FINISHED");
					}
					stateChanged();
				}
				else if(event == TEvent.POPUP_GUI_MOVED_DOWN){
					heightState = GUIHeightState.DOWN;
					if(debugMessages){
						System.out.println(this.getName() + " registered POPUP_GUI_MOVED_DOWN");
					}
					stateChanged();
				}
				else if(event == TEvent.POPUP_GUI_MOVED_UP){
					heightState = GUIHeightState.UP;
					if(debugMessages){
						System.out.println(this.getName() + " registered POPUP_GUI_MOVED_UP");
					}
					stateChanged();
				}
				else if(event == TEvent.BREAK){
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
		else if(channel == TChannel.GRINDER){
			if(event == TEvent.WORKSTATION_LOAD_FINISHED){
				loadState = GUILoadState.RELEASED;
				if(debugMessages){
					System.out.println(this.getName() + " registered WORKSTATION_LOAD_FINISHED event.");
				}
				stateChanged();
			}
			else if(event == TEvent.BREAK){
				workstations.get((Integer)args[0]).workingState = WorkingState.BROKEN;
				if(debugMessages){
					System.out.println(this.getName() + " registered workstation BREAK event.");
				}
				stateChanged();
			}
			else if(event == TEvent.FIX){
				workstations.get((Integer)args[0]).workingState = WorkingState.WORKING;
				if(debugMessages){
					System.out.println(this.getName() + " registered workstation FIX event.");
				}
				stateChanged();
			}
		}
	}
	
	// Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		if(glass.size() > 1){
			System.out.println("ERROR: More than one piece of glass on popup!");
		}
		
		if(workingState == WorkingState.BEGIN_BREAKING){
			breakPopup();
			return true;
		}
		else if(workingState == WorkingState.BEGIN_WORKING){
			fixPopup();
			return true;
		}
		else if(workingState == WorkingState.WORKING){
			if(conveyorState == ConveyorState.STARTED){
				if(!areWorkstationsAvailable()){
					stopConveyor(" stopping conveyor because workstations are full.");
					return true;
				}
				if(glass.size() > 0){
					if(glass.get(0).state == MachiningState.NOT_MACHINED){
						if(loadState == GUILoadState.LOADED){
							stopConveyor(" stopping conveyor until glass released.");
							return true;
						}
					}
					else if(glass.get(0).state == MachiningState.MACHINED){
						if(loadState == GUILoadState.LOADED){
							releaseGlassToNextFamilyGUI();
							return true;
						}
						else if(loadState == GUILoadState.RELEASED){
							releaseGlassToNextFamilyBackEnd();
							return true;
						}
					}
				}
				else if(glass.size() == 0){
					synchronized(workstations){
						for(Workstation w:workstations){
							if(w.state == WorkstationState.REQUESTED_RELEASE){
								if(moveState == PopupMoveState.DOWN){
									stopConveyor(" stopping conveyor because machine is ready to release glass.");
									return true;
								}
							}
						}
					}
				}
			}
			else if(conveyorState == ConveyorState.STOPPED){
				if(glass.size() > 0){
					if(glass.get(0).state == MachiningState.NOT_MACHINED){
						if(moveState == PopupMoveState.DOWN){
							if(heightState == GUIHeightState.DOWN){
								if(loadState == GUILoadState.LOADED){
									movePopupUp(" moving up to transfer glass to machine.");
									return true;
								}
							}
							else if(heightState == GUIHeightState.UP){
								moveState = PopupMoveState.UP;
								return true;
							}
						}
						else if(moveState == PopupMoveState.UP){
							releaseGlassToWorkstation();
							return true;
						}
					}
					else if(glass.get(0).state == MachiningState.MACHINED){
						if(moveState == PopupMoveState.DOWN){
							if(loadState == GUILoadState.LOADED){
								releaseGlassToNextFamilyGUI();
								return true;
							}
							if(loadState == GUILoadState.RELEASED){
								releaseGlassToNextFamilyBackEnd();
								return true;
							}
						}
						else if(moveState == PopupMoveState.UP){
							if(heightState == GUIHeightState.UP){
								if(debugMessages){
									System.out.println("Load state of popup: " + loadState.toString());
								}
								if(loadState == GUILoadState.LOADED){
									movePopupDown(" moving down after loading machined glass.");
									return true;	
								}
							}
							else if(heightState == GUIHeightState.DOWN){
								moveState = PopupMoveState.DOWN;
								return true;
							}
						}
					}
				}
				else if(glass.size() == 0){
					// No glass on popup, conveyor stopped
					Workstation tempWorkstation = null;
					synchronized(workstations){
						for(Workstation w:workstations){
							if(w.state == WorkstationState.REQUESTED_RELEASE){
								tempWorkstation = w;
								break;
							}
						}
					}
					if(tempWorkstation != null){
						if(moveState == PopupMoveState.DOWN){
							if(heightState == GUIHeightState.DOWN){
								movePopupUp(" moving up to get machined glass.");
								return true;
							}
							else if(heightState == GUIHeightState.UP){
								moveState = PopupMoveState.UP;
								return true;
							}
						}
						else if(moveState == PopupMoveState.UP){
							tempWorkstation.machine.msgPopupAvailable();
							tempWorkstation.state = WorkstationState.RELEASE_PENDING;
							loadState = GUILoadState.PENDING;
							return true;
						}
					}
					if(loadState == GUILoadState.RELEASED){
						if(moveState == PopupMoveState.UP){
							if(heightState == GUIHeightState.UP){
								movePopupDown(" moving down after releasing glass to machine.");
								return true;
							}
							else if(heightState == GUIHeightState.DOWN){
								moveState = PopupMoveState.DOWN;
								return true;
							}
						}
						else if(moveState == PopupMoveState.DOWN){
							if(heightState == GUIHeightState.DOWN){
								if(areWorkstationsAvailable()){
									if(debugMessages){
										System.out.println(this.getName() + " restarting conveyor.");
									}
									conveyor.msgPopupRequestingStart();
									conveyorState = ConveyorState.STARTED;
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;		
	}
	
	// Actions
	public boolean areWorkstationsAvailable(){
		Workstation tempWorkstation = null;
		synchronized(workstations){
			for(Workstation w : workstations){
				if(w.state == WorkstationState.EMPTY){
					if(w.workingState == WorkingState.WORKING){
						tempWorkstation = w;
						break;
					}
				}
			}
		}
		if(tempWorkstation != null){
			if(debugMessages){
				System.out.println("Grinder workstation(s) are available.");
			}
			return true;
		}
		return false;
	}
	
	public void releaseGlassToWorkstation(){
		Workstation tempWorkstation = null;
		synchronized(workstations){
			for(Workstation w : workstations){
				if(w.state == WorkstationState.EMPTY){
					if(w.workingState == WorkingState.WORKING){
						tempWorkstation = w;
						break;
					}
				}
			}	
		}
		if(tempWorkstation != null){
			if(debugMessages){
				System.out.println(this.getName() + " releasing glass to workstation.");
			}
			tempWorkstation.machine.msgHereIsGlass(glass.get(0).glass);
			tempWorkstation.machine.msgLoadGlass();
			glass.remove(0);
			loadState = GUILoadState.RELEASING;
			tempWorkstation.state = WorkstationState.FULL;
		}
	}
	
	public void releaseGlassToNextFamilyGUI(){
		if(nextFamilyAbleToReceiveGlass){
			if(debugMessages){
				System.out.println("GUI" + this.getName() + " releasing glass to next family.");
			}
			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
			loadState = GUILoadState.RELEASING;
		}
	}
	
	public void releaseGlassToNextFamilyBackEnd(){
		if(debugMessages){	
			System.out.println(this.getName() + " released glass to next family.");
		}
		nextFamily.msgHereIsGlass(glass.get(0).glass);
		glass.remove(0);
	}
	
	public void stopConveyor(String printString){
		if(debugMessages){
			System.out.println(this.getName() + printString);
		}
		conveyor.msgPopupRequestingStop();
		conveyorState = ConveyorState.STOPPED;
	}
	
	public void movePopupDown(String printString){
		if(debugMessages){
			System.out.println(this.getName() + printString);
		}
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
		heightState = GUIHeightState.MOVING_DOWN;	
	}
	
	public void movePopupUp(String printString){
		if(debugMessages){
			System.out.println(this.getName() + printString);
		}
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
		heightState = GUIHeightState.MOVING_UP;
	}
	
	public void breakPopup(){
		workingState = WorkingState.BROKEN;
		conveyor.msgPopupRequestingStop();
		if(debugMessages){
			System.out.println(this.getName() + " is broken.");	
		}
	}
	
	public void fixPopup(){
		workingState = WorkingState.WORKING;
		if(moveState == PopupMoveState.DOWN){
			if(heightState == GUIHeightState.DOWN){
				if(areWorkstationsAvailable()){
					if(debugMessages){
						System.out.println(this.getName() + " restarting conveyor after fixing.");
					}
					conveyor.msgPopupRequestingStart();
					conveyorState = ConveyorState.STARTED;
				}
			}
		}
		if(debugMessages){	
			System.out.println(this.getName() + " is fixed.");
		}
	}
	
	// Other
	public void setConveyor(Conveyor c){
		conveyor = c;
	}
	public void setMachine(Machine m, int i){
		Workstation w = new Workstation(m, i);
		workstations.add(w);
	}
	public void setBackSensor(Sensor s){
		backSensor = s;
	}
	
	public void setNextFamily(ConveyorFamilyInterface c){
		nextFamily = c;
	}
	
	public void setDebugMessages(boolean set){
		debugMessages = set;
	}
}
