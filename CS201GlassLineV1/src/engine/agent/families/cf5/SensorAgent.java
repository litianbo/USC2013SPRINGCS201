package engine.agent.families.cf5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Conveyor;
import engine.agent.families.cf5.interfaces.Sensor;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;

public class SensorAgent extends Agent implements Sensor {
	
	List<MyGlass> glasses = Collections.synchronizedList(new ArrayList<MyGlass>());
	enum Status {idle, glassInbound, glassPassingOverSensor};

	enum SensorType{entry, popup}
	SensorType sensorType;

	String name;
	
	ConveyorFamilyInterface previousConveyor;

	Conveyor myConveyor;
	int conveyorFamilyID;
	
	boolean restartPreviousConveyorFamily = false;
	
    Timer checkTimer;
	
    /** Private class storing all the information for each glass,
     * including state on sensor. */
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

    /** Constructor for SensorAgent class 
     * @param name name of the sensor 
     * @param myTransducer transducer for sensor*/
	public SensorAgent(String name, String sensorType, int conveyorFamilyID, Transducer t){
		super(name, t);
	
		this.name = name;
		
		if (sensorType.equals("Entry")){
			this.sensorType = SensorType.entry;
		}
		else if (sensorType.equals("Popup")){
			this.sensorType = SensorType.popup;
		}
		
		this.conveyorFamilyID = conveyorFamilyID;
		
		if (t != null){
			transducer.register(this, TChannel.SENSOR);
		}
	}
	
    // *** MESSAGES ***
	
    /** Message from the previous conveyor that glass is approaching.
	* @param glass the glass
    */
	public void msgHereIsGlass(Glass glass) {
		MyGlass g = new MyGlass(glass);
		g.state = Status.glassInbound;
		glasses.add(g);
		stateChanged();
	}
	
    /** Message from the conveyor that glass is approaching.
    * @param glass the glass
    */
	public void msgSenseGlass(Glass glass){
		MyGlass g = new MyGlass(glass);
		g.state = Status.glassInbound;
		glasses.add(g);
		stateChanged();
	}
	
    /** Message from the conveyor to restart the previous conveyor family.
    */
	public void msgStartConveyor(){
		restartPreviousConveyorFamily = true;
		stateChanged();
	}
	
    /** Scheduler.  Determine what action is called for, and do it. */
    public boolean pickAndExecuteAnAction() {

	    //if there exists a myGlass g such that g.state != glassPassingOverSensor or number of glasses = 0 and restartPreviousConveyorFamily = true then sendRestart()
    	if (restartPreviousConveyorFamily){
		    MyGlass tempMyGlass = null;
		    synchronized(glasses){
		    	for(MyGlass g:glasses){
		    		if (g.state != Status.glassPassingOverSensor){
		    			tempMyGlass = g;
		    			break;
		    		}
		    	}
		    }
		    if (tempMyGlass != null || glasses.size() == 0){
	    		sendRestart();
		    	return true;
		    }
    	}
    	
    	return false;
    }

    // *** ACTIONS ***
    
    /** Restart previous conveyor*/
    private void sendRestart(){
		checkTimer = new Timer();
		checkTimer.schedule(new TimerTask(){
    	    public void run(){//this routine is like a message reception    
    	    	previousConveyor.msgStartConveyor();
    	    }
    	}, 500);		
    	restartPreviousConveyorFamily = false;
    }
    
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		//if the event detected occured on this conveyor family
		//print(event.toString()+" "+((Integer)args[0]).toString());
		if ((Integer)args[0] == conveyorFamilyID){
			if (event == TEvent.SENSOR_GUI_PRESSED){
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.glassInbound){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
					if (sensorType == SensorType.popup){
						myConveyor.msgGlassApproachingPopup(tempMyGlass.glass);
						glasses.remove(tempMyGlass);
						stateChanged();
					}
					else{
						tempMyGlass.state = Status.glassPassingOverSensor;
						stateChanged();
					}
		    	}
			}
			else if (event == TEvent.SENSOR_GUI_RELEASED){
		    	MyGlass tempMyGlass = null;
		    	synchronized(glasses){
		    		for(MyGlass g:glasses){
		    			if (g.state == Status.glassPassingOverSensor){
		    				tempMyGlass = g;
		    				break;
		    			}
		    		}
		    	}
		    	if (tempMyGlass != null){
					if (sensorType == SensorType.entry){
						myConveyor.msgGlassOnConveyor(tempMyGlass.glass);
						glasses.remove(tempMyGlass);
						stateChanged();
					}
		    	}
			}
		}
	}
	
	/** Hack to set the conveyor for the sensor */
	public void setConveyor(Conveyor conveyor){
		myConveyor = conveyor;
	}
	
    /** Hack to set the previous conveyor family */
    public void setPreviousConveyor(ConveyorFamilyInterface previousConveyor){
    	this.previousConveyor = previousConveyor;
    }
}
