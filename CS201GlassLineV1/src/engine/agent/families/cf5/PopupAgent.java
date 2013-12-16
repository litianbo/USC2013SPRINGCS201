package engine.agent.families.cf5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Conveyor;
import engine.agent.families.cf5.interfaces.Popup;
import engine.agent.families.cf5.interfaces.Robot;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

import engine.agent.Agent;

public class PopupAgent extends Agent implements Popup{
	Semaphore robotHold = new Semaphore(0);//binary semaphore, fair
	Semaphore popUpMovingHold = new Semaphore(0);
	Semaphore releaseGlassHold = new Semaphore(0);
	Semaphore restartCFHold = new Semaphore(0);
	
	List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
	List<MyRobot> robots = Collections.synchronizedList(new ArrayList<MyRobot>());
	enum Status {notProcessed, needsProccessing, readyToProcess, processed, needsDropOff, readyToDropOff};

	enum RobotState{idle, processing, done, droppingOff, offline}

	enum PopUpStatus{notRaised, moving, raised};
	PopUpStatus popUpStatus = PopUpStatus.notRaised;
	
	enum GlassLoadStatus{notLoaded, loadingOn, loaded, OffLoading};
	GlassLoadStatus glassLoadStatus = GlassLoadStatus.notLoaded;
	
	enum WorkingState {working, breakingDown, broken, startingUp};
	WorkingState workingState = WorkingState.working;
	
	Boolean checkIfRobotsWorking = false;
	Boolean processedGlassLeaving = false;
	Boolean checkPopup = false;
	Boolean nextConveyorStopped = false;
	
    Timer topCheckTimer;
    Timer bottomCheckTimer;
    
    int expirationTime = 10000;
    
	int glassProcessing = 0;
	int maxNumProcessing = 2;

	String name;

	Conveyor myConveyor;
	
	ConveyorFamilyInterface nextConveyor;
	
	int conveyorFamilyID;
	
    /** Private class storing all the information for each robot,
     * including working state. */
	private class MyRobot{
		Robot robot;
		RobotState robotState;
		Boolean broken;

		/** Constructor for MyRobot class.
		 * @param Robot robot
		 */
		public MyRobot (Robot robot){
			this.robot = robot;
			robotState = RobotState.idle;
			broken = false;
		}
	}

    /** Private class storing all the information for each glass,
     * including state on popup. */
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

    /** Constructor for PopupAgent class 
     * @param name name of the popup*/
	public PopupAgent(String name, int conveyorFamilyID, Transducer t){
		super(name, t);
	
		this.name = name;
		this.conveyorFamilyID = conveyorFamilyID;
		if (transducer != null){
			transducer.register(this, TChannel.POPUP);
		}
	}

    // *** MESSAGES ***
	
    /** Message from next conveyor family saying it has stopped
    */
	public void msgStartConveyor(){
		nextConveyorStopped = false;
		if (restartCFHold.availablePermits() == 0){
			restartCFHold.release();
		}
		stateChanged();
	}

    /** Message from next conveyor family saying it has restarted
    */
	public void msgStopConveyor(){
		nextConveyorStopped = true;
		stateChanged();
	}
	
    /** Message from conveyor asking if the popup is clear to pass on more glass.
    */
	public void msgIsPopupClear(){
		checkPopup = true;
		stateChanged();
	}

	
    /** Message from conveyor for passing glass onto popup.
	* @param glass the glass
    */
	public void msgGlassOnPopup(Glass glass){
		++glassProcessing;
		glassLoadStatus = GlassLoadStatus.loadingOn;
		MyGlass g = new MyGlass(glass);
		g.state = Status.notProcessed;
		glasses.add(g);
		stateChanged();
	}

    /** Message from a robot saying they are done processing their glass.
	* @param robot the robot
    */
	public void msgWantToDropOffGlass(Robot robot){
		int robotIndex = -1;
		MyRobot tempMyRobot = null;
    	synchronized(robots){
			for(MyRobot r:robots){
				robotIndex++;
				if (r.robot == robot){
					tempMyRobot = r;
					break;
				}
			}
    	}
    	if (tempMyRobot != null){
    		tempMyRobot.robotState = RobotState.done;
    		if (robotIndex == 0){
    			if (topCheckTimer != null){
	    			topCheckTimer.cancel();
	    			topCheckTimer.purge();
    			}
    		}
    		else{
    			if (bottomCheckTimer != null){
    			bottomCheckTimer.cancel();
    			bottomCheckTimer.purge();
    			}
    		}
    	}
    	stateChanged();
	}

    /** Message from robot to drop glass off back onto popup.
    * @param robot the robot
	* @param glass the glass
    */
	public void msgDropOffGlass(Robot robot, Glass glass){
		robotHold.release();
		glassLoadStatus = GlassLoadStatus.loadingOn;
		MyRobot tempMyRobot = null;
    	synchronized(robots){
			for(MyRobot r:robots){
				if (r.robot == robot){
					tempMyRobot = r;
					break;
				}
			}
    	}
    	if (tempMyRobot != null){
    		tempMyRobot.robotState = RobotState.idle;
    	}
		MyGlass g = new MyGlass(glass);
		g.state = Status.processed;
		glasses.add(g);
		processedGlassLeaving = true;
		stateChanged();
	}
	
	/** Message from robot saying that it is either broken or fixed
	 * @param robot the robot
	 * @param state either broken or not
	 */
	public void msgRobotBroken(Robot robot, Boolean state){
		MyRobot tempMyRobot = null;
    	synchronized(robots){
			for(MyRobot r:robots){
				if (r.robot == robot){
					tempMyRobot = r;
					break;
				}
			}
    	}
    	if (tempMyRobot != null){
    		if (state){
        		maxNumProcessing--;
    		}
    		else{
        		maxNumProcessing++;
    		}
    		tempMyRobot.broken = state;
    	}
    	stateChanged();
	}
	
	/** Message from robot saying that it is returning to idle
	 * @param robot the robot
	 */
	public void msgRobotReset(Robot robot){
		MyRobot tempMyRobot = null;
    	synchronized(robots){
			for(MyRobot r:robots){
				if (r.robot == robot){
					tempMyRobot = r;
					break;
				}
			}
    	}
    	if (tempMyRobot != null){
    		tempMyRobot.robotState = RobotState.idle;
    		--glassProcessing;
    	}
    	stateChanged();
	}
	
    /** Scheduler.  Determine what action is called for, and do it. */
    public boolean pickAndExecuteAnAction() {
    	//if workingState == breakingDown then breakRobot();
    	if (workingState == WorkingState.breakingDown){
    		breakPopup();
    		return true;
    	}
    	
    	//if workingState == startingUp then fixRobot();
    	if (workingState == WorkingState.startingUp){
    		fixPopup();
    		return true;
    	}
    	
    	if (workingState == WorkingState.working){
	    	//if checkIfRobotsWorking = true then checkRobots;
	    	if (checkIfRobotsWorking){
	    		checkRobots();
	    		return true;
	    	}
	
			if (glassLoadStatus == GlassLoadStatus.loaded){
		    	//if there exists g in glasses such that g.status = notProcessed then checkGlass(g)
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
		    		checkGlass(tempMyGlass);
		    		return true;
		    	}
		    	
		    	//if there exists g in glasses such that g.state = readyToProcess then requestPickup(g)
		    	tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.readyToProcess){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
		    		requestPickup(tempMyGlass);
		    		return true;
		    	}
		    	
		    	//if there exists g in glasses such that g.state = readyToDropOff then moveGlassToConveyor(g)
			    tempMyGlass = null;
			    synchronized(glasses){
			    	for(MyGlass g:glasses){
			    		if (g.state == Status.readyToDropOff){
			    			tempMyGlass = g;
			    			break;
			    		}
			    	}
			    }
			    if (tempMyGlass != null){
			    	moveGlassToConveyor(tempMyGlass);
			    	return true;
			    }
		    	
		    	//if there exists g in glasses such that g.status = processed then moveGlassDown(g)
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
		    		moveGlassDown(tempMyGlass);
		    		return true;
		    	}
		    	
	    	}
	    	else if (glassLoadStatus == GlassLoadStatus.notLoaded && popUpStatus == PopUpStatus.raised){
		    	
		    	//if there exists r in robots such that r.robotState = done then retrieveGlass(r);
		    	MyRobot tempMyRobot = null;
		    	synchronized(robots){
		    		for(MyRobot r:robots){
		    			if (r.robotState == RobotState.done && !r.broken){
		    				tempMyRobot = r;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyRobot != null){
		    		retrieveGlass(tempMyRobot);
		    		return true;
		    	}
	    	}
			
			//if there exists a non-broken robot and checkPopup = true and glassProcessing < maxNumProcessing and glassLoadStatus = notLoaded then checkPopup();
			MyRobot tempMyRobot = null;
	    	synchronized(robots){
	    		for(MyRobot r:robots){
	    			if (!r.broken){
	    				tempMyRobot = r;
	    				break;
	    			}
	    		}
	    	}			
			if (tempMyRobot != null && checkPopup && glassProcessing < maxNumProcessing && glassLoadStatus == GlassLoadStatus.notLoaded){
				checkPopup();
				return true;
			}
    	}

	//we have tried all our rules (in this case only one) and found
	//nothing to do. So return false to main loop of abstract agent
	//and wait.
	return false;
    }
    
    // *** ACTIONS ***
    
    /** check to see if any robots are busy processing glass, if both are occupied raise popup to receive finished glass
     */
    private void checkRobots(){
    	MyRobot tempMyRobot = null;
    	synchronized(robots){
    		for(MyRobot r:robots){
    			if (r.robotState == RobotState.idle && !r.broken){
    				tempMyRobot = r;
    				break;
    			}
    		}
    	}
    	if (tempMyRobot == null || (glassProcessing != 0)){
    		if (popUpStatus == PopUpStatus.notRaised){
	    		Integer[] args = new Integer[1];
	    		args[0] = conveyorFamilyID;
	        	popUpStatus = PopUpStatus.moving;
	        	transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
	        	try {
	        		popUpMovingHold.acquire();
	        	}catch(Exception e){
	        		//print("Unexpected exception caught in "+this.toString());
	        	}
    		}
    	}
    	else{
    		if (popUpStatus == PopUpStatus.raised){
	    		Integer[] args = new Integer[1];
	    		args[0] = conveyorFamilyID;
	        	popUpStatus = PopUpStatus.moving;
	        	transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
	        	try {
	        		popUpMovingHold.acquire();
	        	}catch(Exception e){
	        		//print("Unexpected exception caught in "+this.toString());
	        	}
    		}
    	}
    	checkIfRobotsWorking = false;
    	stateChanged();
    }

    /** message robot that is done with glass to be ready to drop off glass onto popup
     * @param myRobot robot that is ready to drop off glass */
    private void retrieveGlass(MyRobot myRobot){
    	myRobot.robot.msgReceiveGlass();
    	myRobot.robotState = RobotState.droppingOff;
    	try {
    		robotHold.acquire();
    	}catch(Exception e){
    		//print("Unexpected exception caught in "+this.toString());
    	}
    	stateChanged();
    }

    /** checks to see if glass requires processing with conveyor's machines and moves popup into place
     * @param myGlass queued glass that will be checked to either be processed or passed along(recipe says false to machine process) */
    private void checkGlass(MyGlass myGlass){
    	if (myGlass.glass.getRecipe().getNeedDrilling()){
    		Integer[] args = new Integer[1];
    		args[0] = conveyorFamilyID;
        	myGlass.state = Status.needsProccessing;
    		popUpStatus = PopUpStatus.moving;
    		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
        	try {
        		popUpMovingHold.acquire();
        	}catch(Exception e){
        		//print("Unexpected exception caught in "+this.toString());
        	}
    	}
    	else{
    		--glassProcessing;
    		myGlass.state = Status.processed;
    	}
    	stateChanged();
    }
    
    /** Checks to see if any robots are available then messages one for glass pickup
     * @param myGlass queued glass that will be passed to robot*/
    private void requestPickup(MyGlass myGlass){
    	int robotIndex = -1;
    	if (myGlass.glass.getRecipe().getNeedDrilling()){
        	MyRobot tempMyRobot = null;
        	synchronized(robots){
        		for(MyRobot r:robots){
        			robotIndex++;
        			if (r.robotState == RobotState.idle && !r.broken){
        				tempMyRobot = r;
        				break;
        			}
        		}
        	}
        	if (tempMyRobot != null){
        		tempMyRobot.robotState = RobotState.processing;
        		tempMyRobot.robot.msgRobotPickup(myGlass.glass);
        		glasses.remove(myGlass);
				checkIfRobotsWorking = true;
				glassLoadStatus = GlassLoadStatus.notLoaded;
				if (robotIndex == 0){
					topCheckTimer = new Timer();
		        	topCheckTimer.schedule(new TimerTask(){
		        	    public void run(){//this routine is like a message reception    
		        	    	print("Top workstation broken!");
		            		Integer[] args = new Integer[1];
		            		args[0] = 0;
		            		transducer.fireEvent(TChannel.DRILL, TEvent.OPERATOR_NEEDED, args);
		        	    }
		        	}, expirationTime);
				}
				else{
					bottomCheckTimer = new Timer();
		        	bottomCheckTimer.schedule(new TimerTask(){
		        	    public void run(){//this routine is like a message reception    
		        	    	print("Bottom workstation broken!");
		            		Integer[] args = new Integer[1];
		            		args[0] = 1;
		            		transducer.fireEvent(TChannel.DRILL, TEvent.OPERATOR_NEEDED, args);
		        	    }
		        	}, expirationTime);				
				}
        	}
    	}
    	else{
    		myGlass.state = Status.processed;
    	}
    	stateChanged();
    }

    /** Lower popup to move glass back onto conveyor
     * @param myGlass queued glass that will be passed back to conveyor */
    private void moveGlassDown (MyGlass myGlass){
		Integer[] args = new Integer[1];
		args[0] = conveyorFamilyID;
		myGlass.state = Status.needsDropOff;
		popUpStatus = PopUpStatus.moving;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
    	try {
    		popUpMovingHold.acquire();
    	}catch(Exception e){
    		//print("Unexpected exception caught in "+this.toString());
    	}
    	stateChanged();
    }
    
    /** Move finished glass back onto conveyor
     * @param myGlass queued glass that will be passed back to conveyor */
    private void moveGlassToConveyor(MyGlass myGlass){
    	if (nextConveyorStopped){
    		try {
				restartCFHold.acquire();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
    	}
	    Integer[] args = new Integer[1];
	    args[0] = conveyorFamilyID;
	    transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
	    nextConveyor.msgHereIsGlass(myGlass.glass);
	    glasses.remove(myGlass);
	    glassLoadStatus = GlassLoadStatus.OffLoading;
	    try {
			releaseGlassHold.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	//V0: sleep needed so gui can catch up
        /*try {
    		Thread.sleep(200);
    	} catch (InterruptedException e) {
    		//e.printStackTrace();
    	}*/
    	stateChanged();
    }
    
    private void checkPopup(){
    	//print("Checking Popup: "+glassProcessing+" max: "+maxNumProcessing+" is load status "+glassLoadStatus);
    	if (popUpStatus == PopUpStatus.notRaised){ 
	    	myConveyor.msgPopupClear();
	    	checkPopup = false;
	    }
    	else {
    		Integer[] args = new Integer[1];
    		args[0] = conveyorFamilyID;
    		popUpStatus = PopUpStatus.moving;
    		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
    	    try {
    	    	popUpMovingHold.acquire();
    	    }catch(Exception e){
    	    	//print("Unexpected exception caught in "+this.toString());
    	    }
    	}
    	stateChanged();
    }
    
    /** Non-Norm: Break Popup
     */
    private void breakPopup(){
    	workingState = WorkingState.broken;
    }
    
    /** Non-Norm: Fix Popup
     */
    private void fixPopup(){
    	workingState = WorkingState.working;
    }
    
	/** Hack to set the conveyor for the popup */
	public void setConveyor(Conveyor conveyor){
		myConveyor = conveyor;
	}
	
	/** Hack to set the robots for the popup */
	public void setRobots(List<Robot> robots){
        for (Robot r: robots){
    		MyRobot tempRobot = new MyRobot(r);
        	this.robots.add(tempRobot);
        }
	}
	
    /** Hack to set the next conveyor family */
    public void setNextConveyor(ConveyorFamilyInterface nextConveyor){
    	this.nextConveyor = nextConveyor;
    }

    /** For JUnit Testing **/
    
    /** Hack to set glass loaded */
    public void setGlassLoaded(Boolean state){
    	if (state){
    		glassLoadStatus = GlassLoadStatus.loaded;
    	}
    	else{
    		glassLoadStatus = GlassLoadStatus.notLoaded;
    	}
    }
    
    /** Hack to set popup raised 
     * @param state state is raised or not*/
    public void setPopupRaised(Boolean state){
    	if (state){
    		popUpStatus = PopUpStatus.raised;
    	}
    	else{
    		popUpStatus = PopUpStatus.notRaised;
    	}
    }
    
    /** Hack to release robot drop semaphore */
    public void releaseRobotSemaphore(){
    	robotHold.release();
    }
    
    /** Hack to release popup movement semaphore */
    public void releasePopupSemaphore(){
    	popUpMovingHold.release();
    }
    
    /** Hack to release glass release semaphore */
    public void releaseGlassSemaphore(){
    	releaseGlassHold.release();
    }
    
    /** End For JUnit Testing **/
	
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		//if the event detected occured on this conveyor family
		//print(event.toString()+" "+((Integer)args[0]).toString());
		if ((Integer)args[0] == conveyorFamilyID){
			if (event == TEvent.POPUP_GUI_RELEASE_FINISHED){
				releaseGlassHold.release();
				checkIfRobotsWorking = true;
				glassLoadStatus = GlassLoadStatus.notLoaded;
				if (processedGlassLeaving){
					--glassProcessing;
					processedGlassLeaving = false;
				}
				stateChanged();
			}
			else if (event == TEvent.POPUP_GUI_LOAD_FINISHED){
				glassLoadStatus = GlassLoadStatus.loaded;
				stateChanged();
			}
			else if (event == TEvent.POPUP_GUI_MOVED_UP){
				popUpMovingHold.release();
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.needsProccessing){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
		    		tempMyGlass.state = Status.readyToProcess;
		    	}
		    	popUpStatus = PopUpStatus.raised;
				stateChanged();
			}
			else if (event == TEvent.POPUP_GUI_MOVED_DOWN){
				popUpMovingHold.release();
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.needsDropOff){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
		    		tempMyGlass.state = Status.readyToDropOff;
		    	}
		    	popUpStatus = PopUpStatus.notRaised;
				stateChanged();
			}
			else if (event == TEvent.BREAK){
				workingState = WorkingState.breakingDown;
				stateChanged();
			}
			else if (event == TEvent.FIX){
				workingState = WorkingState.startingUp;
				stateChanged();
			}
		}
	}
}
