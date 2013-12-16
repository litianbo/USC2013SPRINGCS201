package engine.agent.families.cf7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Conveyor;
import engine.agent.families.cf7.interfaces.Popup;
import engine.agent.families.cf7.interfaces.Sensor;

public class SensorAgent extends Agent implements Sensor {

	// Data
	public enum SensorType { FRONT, BACK };
	public enum SensorState { PRESSED, RELEASED };
	public enum SensorUpdate { PRESSED, RELEASED };
	
	public Transducer transducer;
	Conveyor conveyor;
	Popup popup;
	public SensorType type;
	SensorState state;
	SensorUpdate update;
	int index;
	
	private List<Glass> glass;
	Boolean glassRequested, factoryStarted;
	
	// Turn on debug messages
	boolean debugMessages;
	
	// Constructor for sensor on popup
	public SensorAgent(Transducer t, int i){
		super();
		transducer = t;
		index = i;
		this.name = ("Sensor" + index);
		state = SensorState.RELEASED;
		update = SensorUpdate.RELEASED;
		
		glass = Collections.synchronizedList(new ArrayList<Glass>());
		glassRequested = false;
		factoryStarted = false;
		
		debugMessages = false;
	}
	
	// Messages
	public void msgHereIsGlass(Glass g){
		if(debugMessages){
			if(g.getRecipe().getNeedGrinding()){
				System.out.println("ConveyorFamily7 sensor has received glass to be processed.");
			}
			else {
				System.out.println("ConveyorFamily7 sensor has received glass to pass on.");
			}
		}
		glass.add(g);
		glassRequested = false;
		stateChanged();
	}
	
	@Override
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) {
		if(channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED && ((Integer)args[0])==index){
			update = SensorUpdate.PRESSED;
			if(debugMessages){
				System.out.println(this.getName() + " registered SENSOR_GUI_PRESSED");
			}
			stateChanged();
		}
		else if(channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED && ((Integer)args[0])==index){
			update = SensorUpdate.RELEASED;
			if(debugMessages){
				System.out.println(this.getName() + " registered SENSOR_GUI_RELEASED");
			}
			stateChanged();
		}
	}	
	
	
	// Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		if(glass.size() > 1){
			System.out.println("ERROR: Sensor has more than 1 piece of glass.");
		}
		if(type == SensorType.FRONT){
			if(state == SensorState.PRESSED){
				if(update == SensorUpdate.RELEASED){
					if(!glass.isEmpty()){
						sendGlassToConveyor();
						return true;
					}
				}
			}
			else if(state == SensorState.RELEASED){
				if(update == SensorUpdate.PRESSED){
					registerGlassFront();
					return true;
				}
			}
		}
		if(type == SensorType.BACK){
			if(state == SensorState.PRESSED){
				if(update == SensorUpdate.RELEASED){
					if(!glass.isEmpty()){
						sendGlassToPopup();
						return true;
					}
				}
			}
			else if(state == SensorState.RELEASED){
				if(update == SensorUpdate.PRESSED){
					if(!glass.isEmpty()){
						registerGlassBack();
						return true;
					}
					else {
						if(!glassRequested){
							requestGlass();
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	// Actions
	void sendGlassToConveyor(){
		if(debugMessages){
			System.out.println("In scheduler, front sensor was released.");
		}
		conveyor.msgHereIsGlass(glass.get(0));
		glass.remove(0);
		state = SensorState.RELEASED;
	}
	
	void registerGlassFront(){
		if(debugMessages){
			System.out.println("In scheduler, front sensor has registered glass.");
		}
		conveyor.msgGlassOnFrontSensor();
		if(!factoryStarted){
			conveyor.msgStartConveyor();
			factoryStarted = true;
		}
		state = SensorState.PRESSED;
	}
	
	void sendGlassToPopup(){
		if(debugMessages){
			System.out.println("In scheduler, back sensor was released.");
		}
		popup.msgHereIsGlass(glass.get(0));
		glass.remove(0);
		state = SensorState.RELEASED;
	}
	
	void registerGlassBack(){
		if(debugMessages){
			System.out.println("In scheduler, back sensor has registered glass.");
		}
		state = SensorState.PRESSED;
	}
	
	void requestGlass(){
		if(debugMessages){
			System.out.println("In scheduler, back sensor has requested glass.");
		}
		conveyor.msgSendGlass();
		glassRequested = true;
	}
	
	
	// Other
	public void setConveyor(Conveyor c){
		conveyor = c;
	}
	public void setPopup(Popup p){
		popup = p;
	}
	
	public void setDebugMessages(boolean set){
		debugMessages = set;
	}
}
