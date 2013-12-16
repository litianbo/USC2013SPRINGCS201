package engine.agent.families.cf5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Conveyor;
import engine.agent.families.cf5.interfaces.Popup;
import engine.agent.families.cf5.interfaces.Sensor;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

import engine.agent.Agent;

/** Conveyor agent for conveyor family 1.
 *  Keeps track of incoming glass and 
 *  interacts with glass, sensors and popup
 */
public class ConveyorAgent extends Agent implements Conveyor {
	Semaphore popUpHold = new Semaphore(0);
	
	List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
	enum Status {onConveyor, nearPopup};
	
	enum WorkingState {working, breakingDown, broken, startingUp};
	WorkingState workingState = WorkingState.working;
	
	String name;
	
	Boolean popUpCleared = false;
	Boolean myConveyorStopped = false;
	Boolean glassEnroute = false;
	
	Sensor entrySensor;
	Sensor popupSensor;
	
	Popup myPopup;
	
	ConveyorFamilyInterface previousConveyor;
	
	int conveyorFamilyID;
	int popupID;

    /** Private class storing all the information for each glass,
     * including state on conveyor. */
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

    /** Constructor for ConveyorAgent class 
     * @param name name of the conveyor
     * @param int number of robots serving conveyor */
	public ConveyorAgent(String name, int conveyorFamilyID, int popupID, Transducer t){
		super(name, t);
		
		this.name = name;
		this.conveyorFamilyID = conveyorFamilyID;
		this.popupID = popupID;
		if (transducer != null){
			transducer.register(this, TChannel.POPUP);
			//debug
			transducer.register(this, TChannel.CONVEYOR);
		}
		Integer[] args = new Integer[1];
		args[0] = conveyorFamilyID;
		myConveyorStopped = false;
		//Hack start up conveyor at init
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
	}

    // *** MESSAGES ***
	
    /** Message from entry sensor that glass is on conveyor.
	* @param glass the glass
    */
	public void msgGlassOnConveyor(Glass glass){
		MyGlass g = new MyGlass(glass);
		g.state = Status.onConveyor;
		glasses.add(g);
		stateChanged();
	}

    /** Message from entry sensor saying glass is approaching popup.
    * @param glass the glass
    */
	public void msgGlassApproachingPopup (Glass glass){
		MyGlass g = new MyGlass(glass);
		g.state = Status.nearPopup;
		glasses.add(g);
    	stateChanged();
	}
	
    /** Message from popup saying it is currently ready to receive new glass.
    */
	public void msgPopupClear(){
		popUpHold.release();
		popUpCleared = true;
    	stateChanged();
	}

	
    /** Scheduler.  Determine what action is called for, and do it. */
    public boolean pickAndExecuteAnAction() {
    		//if workingState == breakingDown then breakConveyor();
    		if (workingState == WorkingState.breakingDown){
    			breakConveyor();
    			return true;
    		}

        	//if workingState == startingUp then fixConveyor();
    		if (workingState == WorkingState.startingUp){
    			fixConveyor();
    			return true;
    		}
    		
    		if (workingState == WorkingState.working){
			    //System.out.println(glasses.size());
			    //if there exists g in glasses such that g.state  = nearPopup and popUpCleared = true then passOntoPopup();
			    if (popUpCleared){
				    MyGlass tempMyGlass = null;
				    synchronized(glasses){
				    	for(MyGlass g:glasses){
				    		if (g.state == Status.nearPopup){
				    			tempMyGlass = g;
				    			break;
				    		}
				    	}
				    }
				    if (tempMyGlass != null){
				    	passOntoPopup(tempMyGlass);
				    	return true;
				    }
			    }
		    
			    if (!glassEnroute){
			    //if there exists a MyGlass g such that g.state = nearPopup and glassEnroute = false then check popup
				    MyGlass tempMyGlass = null;
				    synchronized(glasses){
				    	for(MyGlass g:glasses){
				    		if (g.state == Status.nearPopup){
				    			tempMyGlass = g;
				    			break;
				    		}
				    	}
				    }
				    if (tempMyGlass != null){
				    	checkPopup(tempMyGlass);
				    	return true;
				    }
			    }
			    
			    //if there exists a myGlass g such that g.state = onConveyor then passByPopupSensor(g)
			    MyGlass tempMyGlass = null;
			    synchronized(glasses){
			    	for(MyGlass g:glasses){
			    		if (g.state == Status.onConveyor){
			    			tempMyGlass = g;
			    			break;
			    		}
			    	}
			    }
			    if (tempMyGlass != null){
			    	passByPopupSensor(tempMyGlass);
			    	return true;
			    }
    		}

	//we have tried all our rules (in this case only one) and found
	//nothing to do. So return false to main loop of abstract agent
	//and wait.
	return false;
    }

    // *** ACTIONS ***
    
    /** Restart conveyor and move glass onto popup
     * @param myGlass queued glass that will be moving onto popup */
    private void passOntoPopup(MyGlass myGlass){
    	popUpCleared = false;
    	glassEnroute = true;
    	if (myConveyorStopped){
    		entrySensor.msgStartConveyor();
    		Integer[] args = new Integer[1];
    		args[0] = conveyorFamilyID;
    		myConveyorStopped = false;
    		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
    	}
    	myPopup.msgGlassOnPopup(myGlass.glass); 
    	glasses.remove(myGlass);
    	stateChanged();
    }
    
    /** Check to see if popup is lowered and empty to move next piece of glass onto it, stop conveyor
     * @param myGlass queued glass that will be moving onto popup */
    private void checkPopup(MyGlass myGlass){
    	//print("Asking Popup Clear");
    	myPopup.msgIsPopupClear();
	    if (!myConveyorStopped){
	    	previousConveyor.msgStopConveyor();
	    	Integer[] args = new Integer[1];
	    	args[0] = conveyorFamilyID;
	    	myConveyorStopped = true;
	    	transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
	    }
	    try {
			popUpHold.acquire();
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
    	stateChanged();
    }
    
    /** Wait for new piece of glass to move close to popup by waiting for sensor verification
     * @param myGlass queued glass that will be moving toward popup */
    private void passByPopupSensor(MyGlass myGlass){
    	popupSensor.msgSenseGlass(myGlass.glass);
    	glasses.remove(myGlass);
    	stateChanged();
    }
    
    /** Non-Norm: Simulate conveyor breaking
     *  */
    private void breakConveyor(){
	    if (!myConveyorStopped){
	    	previousConveyor.msgStopConveyor();
	    	Integer[] args = new Integer[1];
	    	args[0] = conveyorFamilyID;
	    	myConveyorStopped = true;
	    	transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);
	    }
    	workingState = WorkingState.broken;
    }
    
    /** Non-Norm: Simulate conveyor starting back up
     *  */
    private void fixConveyor(){
    	if (myConveyorStopped && glasses.size() == 0){
    		entrySensor.msgStartConveyor();
    		Integer[] args = new Integer[1];
    		args[0] = conveyorFamilyID;
    		myConveyorStopped = false;
    		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
    	}
    	workingState = WorkingState.working;
    	stateChanged();
    }
    
    /** Hack to set the entry sensor for the conveyor */
    public void setEntrySensor(Sensor sensor){
    	entrySensor = sensor;
    }
    
    /** Hack to set the popup sensor for the conveyor */
    public void setPopupSensor(Sensor sensor){
    	popupSensor = sensor;
    }
    
    /** Hack to set the popup for the conveyor */
    public void setPopup(Popup popup){
    	myPopup = popup;
    }
    
    /** Hack to set the previous conveyor family */
    public void setPreviousConveyor(ConveyorFamilyInterface previousConveyor){
    	this.previousConveyor = previousConveyor;
    }
    
    /** For JUnit Testing **/
    
    /** Hack to set the conveyor's start/stop */
    public void setMyConveyorStopped(Boolean state){
    	myConveyorStopped = state;
    }
    
    /** Hack to set if popup is cleared */
    public void setPopUpCleared(Boolean state){
    	popUpCleared = state;
    }
    
    /** Hack to release popup semaphore */
    public void releasePopupSemaphore(){
    	popUpHold.release();
    }
    
    /** End For JUnit Testing **/
    
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args){
		if ((Integer)args[0] == conveyorFamilyID){
			//System.out.println(event);
			if (channel == TChannel.CONVEYOR){
				if (event == TEvent.BREAK){
					workingState = WorkingState.breakingDown;
					stateChanged();
				}
				else if (event == TEvent.FIX){
					workingState = WorkingState.startingUp;
					stateChanged();
				}
			}
		}
		else if ((Integer)args[0] == 0){
			if (channel == TChannel.POPUP){
				if (event == TEvent.POPUP_GUI_LOAD_FINISHED){
					glassEnroute = false;
				}
			}
		}
	}

}
