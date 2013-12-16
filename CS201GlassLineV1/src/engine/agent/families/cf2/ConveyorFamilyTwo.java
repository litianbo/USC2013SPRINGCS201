package engine.agent.families.cf2;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;
import engine.agent.Agent;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.GlassClass;

public class ConveyorFamilyTwo extends Agent implements ConveyorFamilyInterface
{
	//DATA
	ConveyorFamilyInterface nextNeighbor, previousNeighbor;
	
	//sensor booleans
	boolean popupLoaded = false;
	boolean sensor1 = false;
	boolean sensor2 = false;
	
	
	//NON NORMATIVE CASES
	boolean beltDisabled = false;
	boolean stationDisabled = false;
	
	//Transducer
	//Transducer transducer;
	
	Object[] channelSensor1 = new Object[1];
	Object[] channelSensor2 = new Object[1];
	Object[] channelWorkStation = new Object[1];
	Object[] channelConveyor = new Object[1];
	
	//WorkstationAgent workstation;
	
	boolean conveyorOn = true;
	boolean conveyorFlagged = false;
	boolean machining = false;
	
	List<GlassClass> glassList = Collections.synchronizedList(new ArrayList<GlassClass>());

	String name;
	
	//CONSTRUCTOR
	public ConveyorFamilyTwo(String nombre, Transducer tdeuce)
	{
		super(nombre, tdeuce);
		this.transducer.register(this, TChannel.SENSOR);
		this.transducer.register(this, TChannel.BREAKOUT);
		this.transducer.register(this, TChannel.CONTROL_PANEL);
		this.transducer.register(this, TChannel.CONVEYOR);
		this.name = nombre;
		transducer = tdeuce;
		//TODO CHANGE THESE DEPENDING ON WHICH FAMILY USED
		channelSensor1[0] = 4;
		channelSensor2[0] = 5;
		channelConveyor[0] = 2;
		channelWorkStation[0] = 0;	
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, channelConveyor);
	}
	
	//SETTERS
	public void setNextFamily(ConveyorFamilyInterface next)
	{
		nextNeighbor = next;
	}
	
	public void setPreviousFamily(ConveyorFamilyInterface previous)
	{
		previousNeighbor = previous;
	}
	
	
	//MESSAGES
	
	//sent by previous family with a piece of incoming glass
	public void msgHereIsGlass(Glass glass) 
	{
		System.out.println("Conveyor family two receiving glass");
		//Create the glass
		GlassClass temp = new GlassClass(glass);
		temp.machined = false;
		
		//add to queue
		glassList.add(temp);
		stateChanged();
	}

	
	//sent by another family to start
	public void msgStartConveyor() 
	{
		conveyorFlagged = false;
		stateChanged();
	}

	//sent by another family to stop
	public void msgStopConveyor() 
	{
		conveyorFlagged = true;
		stateChanged();
	}
	
	//SCHEDULER
	public boolean pickAndExecuteAnAction() 
	{
		//System.out.println("I'm in the scheduler yo");
		if(beltDisabled && conveyorOn)
		{
			StopConveyor();
			return true;
		}
		
		//stop the conveyor if the workstation is broken
		if(stationDisabled && popupLoaded && conveyorOn)
		{
			StopConveyor();
			return true;
		}
		
		if(conveyorFlagged && conveyorOn)
		{
			//Turn the conveyor off
			if(popupLoaded)
			{
				StopConveyor();
				return true;
			}
		}
		
		if(!beltDisabled && conveyorFlagged && !conveyorOn && !popupLoaded)
		{
			StartConveyor();
			return true;
		}
		
		if(!conveyorFlagged && !beltDisabled && !stationDisabled)
		{
			
			if(sensor2 && !popupLoaded && !conveyorOn)
			{
				LoadPopup();
				return true;
			}
			if(!conveyorOn){
				StartConveyor();
				return true;
			}
			if(popupLoaded)
			{
				//System.out.println("[scheduler] Popup was loaded");
				if(sensor2)
				{
					StopConveyor();
					return true;
				}
				
				if(!glassList.get(0).glass.getRecipe().getNeedBreakout())
				{
					ReleaseGlass();
					return true;
				}
				
				if(glassList.get(0).glass.getRecipe().getNeedBreakout() && !machining && glassList.get(0).machined)
				{
					ReleaseGlass();
					return true;
				}
				
				if(glassList.get(0).glass.getRecipe().getNeedBreakout() && !machining)
				{
					//System.out.println("[scheudler] Going to machine glass");
					MachineGlass();
					return true;
				}
			}
		}
		
		
		//print("No actions fired returning false");
		return false;
	}
	
	
	//ACTIONS
	
	//Load the workstation
	private void LoadPopup()
	{
		//fire the transducer event
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, channelConveyor);
		print(name + ": Moving glass onto popup.");
	}
	
	//Stop the conveyor
	private void StopConveyor()
	{
		//fire the event to turn off the conveyor
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, channelConveyor);
		conveyorOn = false;
		previousNeighbor.msgStopConveyor();
		print(name + ": Turning off conveyor.");
	}
	
	//Start the conveyor
	private void StartConveyor()
	{
		//fire the event to turn on the conveyor
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, channelConveyor);
		conveyorOn = true;
		previousNeighbor.msgStartConveyor();
		print(name + ": Turning on conveyor.");
	}
	
	//Release Glass to next family
	private void ReleaseGlass()
	{
		//fire the transducer event to release the glass
		transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, channelWorkStation);
		nextNeighbor.msgHereIsGlass(glassList.get(0).glass);
		popupLoaded = false;
		glassList.remove(0);
		print(name + ": Releasing glass to next family");
		
	}
	
	//Machine glass
	private void MachineGlass()
	{
		machining = true;
		glassList.get(0).machined = true;
		//fire the transducer event
		transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_DO_ACTION, channelWorkStation);
	}
	
	//Transducer Firing
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) 
	{
		Integer channelNumber;
		
		if(channel == TChannel.CONVEYOR)
		{
			channelNumber = (Integer)args[0];
			if(channelNumber == this.channelConveyor[0])
			{
				if(event == TEvent.BREAK)
				{
					this.beltDisabled = true;
					stateChanged();
				}
				
				if(event == TEvent.FIX)
				{
					this.beltDisabled = false;
					//this.conveyorFlagged = false;
					stateChanged();
				}
			}
		}
	
		
		if(channel == TChannel.BREAKOUT)
		{
		
		//Workstation
		//if(channelNumber == this.channelWorkStation[0])
			
			//work station		
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				popupLoaded = true;
				//transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, channelConveyor);
				stateChanged();
				//System.out.println(event);
			}
			
			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				popupLoaded = false;
				
				stateChanged();
			}
			
			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				machining = false;
				stateChanged();
			}
			
			if(event == TEvent.BREAK)
			{
				this.stationDisabled = true;
				stateChanged();
			}
			
			if(event == TEvent.FIX)
			{
				this.stationDisabled = false;
				stateChanged();
			}
		}//END NUMBER CHECK
		
		if(channel == TChannel.SENSOR)
		{
		//sensors	
			channelNumber = (Integer)args[0];
		if(channelNumber == this.channelSensor1[0])
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				sensor1 = true;
			}
			
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				sensor1 = false;
			}
		}
		
		if(channelNumber == this.channelSensor2[0])
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				sensor2 = true;
			}
			
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				sensor2 = false;
			}
		}//END NUMBER CHECK
		}//END TCHANNEL CHECK
			
	}




	
	
}
