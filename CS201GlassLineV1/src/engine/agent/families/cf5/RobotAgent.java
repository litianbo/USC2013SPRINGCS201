package engine.agent.families.cf5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import engine.agent.families.Glass;
import engine.agent.families.Glass.GlassState;
import engine.agent.families.cf5.interfaces.Popup;
import engine.agent.families.cf5.interfaces.Robot;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

import engine.agent.Agent;

public class RobotAgent extends Agent implements Robot{
	Semaphore popupHold = new Semaphore(0);//binary semaphore, fair
	
	List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
	enum Status {notProcessed, processing, processed, needDropOff , canDropOff, broken, cleaningUp};
	
	enum WorkingState {working, breakingDown, broken, startingUp};
	WorkingState workingState = WorkingState.working;
	
	boolean machineActionBroken = false;

	String name;

	Popup myPopup;
	
	int conveyorFamilyID;
    
    /** Private class storing all the information for each glass,
     * including state on robot. */
	private class MyGlass{
		Status state;
		Glass glass;

		/** Constructor for MyGlass class.
		 * @param Glass glass
		 */
		public MyGlass(Glass glass){
			this.glass = glass;
		}
	}

    /** Constructor for RobotAgent class 
     * @param name name of the robot*/
	public  RobotAgent(String name, int conveyorFamilyID, Transducer t){
		super(name, t);
	
		this.name = name;
		this.conveyorFamilyID = conveyorFamilyID;
		if (transducer != null){
			transducer.register(this, TChannel.DRILL);
		}
	}
	
    // *** MESSAGES ***
	
    /** Message from popup to pick up available glass.
	* @param glass glass
    */
	public void msgRobotPickup(Glass glass){
		MyGlass g = new MyGlass(glass);
		g.state = Status.notProcessed;
		glasses.add(g);
		stateChanged();
	}

    /** Message from popup saying its ready to receive glass.
    */
	public void msgReceiveGlass(){
		popupHold.release();
		MyGlass tempMyGlass = null;
    	synchronized(glasses){
			for(MyGlass g:glasses){
				if (g.state == Status.needDropOff){
					tempMyGlass = g;
					break;
				}
			}
    	}
    	if (tempMyGlass != null){
    		tempMyGlass.state = Status.canDropOff;
    	}
		stateChanged();
	}

    /** Scheduler.  Determine what action is called for, and do it. */
    public boolean pickAndExecuteAnAction() {
    	//if workingState == breakingDown then breakRobot();
    	if (workingState == WorkingState.breakingDown){
    		breakRobot();
    		return true;
    	}
    	
    	//if workingState == startingUp then fixRobot();
    	if (workingState == WorkingState.startingUp){
    		fixRobot();
    		return true;
    	}
    	
    	if (workingState == WorkingState.working){
	    	//if there exists g in glasses such that g.state = notProcessed then processGlass(g)
	    	MyGlass tempMyGlass = null;
	    	synchronized(glasses){
	    		for(MyGlass g:glasses){
	    			if (g.state == Status.notProcessed){
	    				tempMyGlass = g;
	    				break;
	    			}
	    		}
	    	}
	    	if (tempMyGlass != null){
	    		processGlass(tempMyGlass);
	    		return true;
	    	}
	    	
	    	//if there exists g in glasses such that g.state = processed then returnGlass(g)
	    	tempMyGlass = null;
	    	synchronized(glasses){
	    		for(MyGlass g:glasses){
	    			if (g.state == Status.processed){
	    				tempMyGlass = g;
	    				break;
	    			}
	    		}
	    	}
	    	if (tempMyGlass != null){
	    		returnGlass(tempMyGlass);
	    		return true;
	    	}
	
	    	//if there exists g in glasses such that g.state = canDropOff then dropOffGlass(g)
	    	tempMyGlass = null;
	    	synchronized(glasses){
	    		for(MyGlass g:glasses){
	    			if (g.state == Status.canDropOff){
	    				tempMyGlass = g;
	    				break;
	    			}
	    		}
	    	}
	    	if (tempMyGlass != null){
	    		dropOffGlass(tempMyGlass);
	    		return true;
	    	}
	    	
	    	//if there exists g in glasses such that g.state = cleaningUp then removeGlass(g)
	    	tempMyGlass = null;
	    	synchronized(glasses){
	    		for(MyGlass g:glasses){
	    			if (g.state == Status.cleaningUp){
	    				tempMyGlass = g;
	    				break;
	    			}
	    		}
	    	}
	    	if (tempMyGlass != null){
	    		removeGlass(tempMyGlass);
	    		return true;
	    	}
	    	
    	}
    	
    	//we have tried all our rules (in this case only one) and found
    	//nothing to do. So return false to main loop of abstract agent
    	//and wait.
    	return false;
    }
    
    // *** ACTIONS ***
    
    /** process the glass in machine
     * @param myGlass glass to be processed */
	private void processGlass(MyGlass myGlass){
		Integer[] args = new Integer[1];
		args[0] = conveyorFamilyID;
		myGlass.state = Status.processing;
		transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
	}

	
    /** messaging popup that glass is done
     * @param myGlass glass that's done */
	private void returnGlass(MyGlass myGlass){
		myPopup.msgWantToDropOffGlass(this);
		myGlass.state = Status.needDropOff;
		try{
			popupHold.acquire();
		}catch(Exception e){
			//print("Unexpected exception in "+this.toString());
		}
    	stateChanged();
	}
	
    /** drop off glass onto popup
     * @param myGlass glass to be dropped off */
	private void dropOffGlass(MyGlass myGlass){
		Integer[] args = new Integer[1];
		args[0] = conveyorFamilyID;
		transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_RELEASE_GLASS, args);
		myPopup.msgDropOffGlass(this, myGlass.glass);
		glasses.remove(myGlass);
    	stateChanged();
	}
	
	/**
	 *  remove glass from robot
	 * @param myGlass glass being cleared off
	 */
	private void removeGlass(MyGlass myGlass){
		glasses.remove(myGlass);
		myPopup.msgRobotReset(this);
		stateChanged();
	}
	
	/** message popup that robot is broken
	 */
	private void breakRobot(){
		myPopup.msgRobotBroken(this, true);
		workingState = WorkingState.broken;
	}
	
	/** message popup that robot is fixed
	 */
	private void fixRobot(){
		myPopup.msgRobotBroken(this, false);
		workingState = WorkingState.working;
	}
	
    /** Hack to set the popup for the robot */
    public void setPopup(Popup popup){
    	myPopup = popup;
    }
    
    /** For JUnit Testing **/
    
    /** Hack to release popup semaphore */
    public void releasePopupSemaphore(){
    	popupHold.release();
    }
    
    /** End For JUnit Testing **/

	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		//if the event detected occured on this conveyor family
		//print(event.toString()+" "+((Integer)args[0]).toString());
		if ((Integer)args[0] == conveyorFamilyID){
			//TODO Get event to properly remove broken glass from machine, glass state set to cleaningUp
			if (event == TEvent.WORKSTATION_LOAD_FINISHED && !machineActionBroken){
				transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_ACTION, args);
			}
			else if (event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.processing){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
		    		tempMyGlass.state = Status.processed;
		    		tempMyGlass.glass.setState(GlassState.doneDrilling);
					stateChanged();
		    	}
			}
			else if (event == TEvent.BREAK){
				workingState = WorkingState.breakingDown;
				stateChanged();
			}
			else if (event == TEvent.FIX){
				workingState = WorkingState.startingUp;
				stateChanged();
			}
			else if (event == TEvent.SILENT_BREAK){
				machineActionBroken = true;
				stateChanged();
			}
			else if (event == TEvent.SILENT_FIX){
				machineActionBroken = false;
				stateChanged();
			}
			else if (event == TEvent.WORKSTATION_BREAK_GLASS){
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state != Status.needDropOff && g.state != Status.canDropOff){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
		    		tempMyGlass.state = Status.broken;
					stateChanged();
		    	}
			}
			else if (event == TEvent.WORKSTATION_REMOVE_GLASS){
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.broken){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
		    		tempMyGlass.state = Status.cleaningUp;
					stateChanged();
		    	}
			}
		}
	}
}
