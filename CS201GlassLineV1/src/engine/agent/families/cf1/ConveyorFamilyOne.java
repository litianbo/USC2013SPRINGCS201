package engine.agent.families.cf1;

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

public class ConveyorFamilyOne extends Agent implements ConveyorFamilyInterface
{
	//DATA
	ConveyorFamilyInterface nextNeighbor, previousNeighbor;
	
	//sensor booleans
	boolean popupLoaded = false;
	boolean sensor1 = false;
	boolean sensor2 = false;
	boolean released = false;
	
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
	public ConveyorFamilyOne(String nombre, Transducer tdeuce)
	{
		super(nombre, tdeuce);
		this.transducer.register(this, TChannel.SENSOR);
		this.transducer.register(this, TChannel.CONVEYOR);
		//TODO name this dependent on channel used this.transducer.register(this, TChannel.);
		this.name = nombre;
		transducer = tdeuce;
		//TODO CHANGE THESE DEPENDING ON WHICH FAMILY USED
		channelSensor1[0] = 2;
		channelSensor2[0] = 3;
		channelConveyor[0] = 1;
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
		
		if(beltDisabled && conveyorOn)
		{
			StopConveyor();
			return true;
		}
		
		if(conveyorFlagged && conveyorOn)
		{
			//Turn the conveyor off
			if(sensor2)
			{
				StopConveyor();
				return true;
			}
		}
		
		if(!beltDisabled && conveyorFlagged && !conveyorOn && !sensor2)
		{
			StartConveyor();
			return true;
		}
		
		if(!conveyorFlagged && !beltDisabled)
		{
			
			if(!conveyorOn){
				StartConveyor();
				return true;
			}
			
			if(sensor2)
			{
				ReleaseGlass();
				return true;
			}
		}
			
		/*	if(popupLoaded)
			{
				if(sensor2)
				{
					StopConveyor();
					return true;
				}
				
				if(glassList.get(0).machined && !machining)
				{
					ReleaseGlass();
					return true;
				}
				
				if(!glassList.get(0).machined && !machining)
				{
					MachineGlass();
					return true;
				}
			}*/		
		
		//print("No actions fired returning false");
		return false;
	}
	
	
	//ACTIONS
	
	//Load the workstation
	/*private void LoadPopup()
	{
		//fire the transducer event
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, channelConveyor);
		print(name + ": Moving glass onto popup.");
	}*/
	
	//Stop the conveyor
	private void StopConveyor()
	{
		//fire the event to turn off the conveyor
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, channelConveyor);
		previousNeighbor.msgStopConveyor();
		conveyorOn = false;
		print(name + ": Turning off conveyor.");
	}
	
	//Start the conveyor
	private void StartConveyor()
	{
		//fire the event to turn on the conveyor
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, channelConveyor);
		previousNeighbor.msgStartConveyor();
		conveyorOn = true;
		print(name + ": Turning on conveyor.");
	}
	
	//Release Glass to next family
	private void ReleaseGlass()
	{
		//fire the transducer event to release the glass
		//transducer.fireEvent(TChannel., TEvent.WORKSTATION_RELEASE_PART, channelWorkstation);
		
		System.out.println(glassList.get(0));
		//released = true;
		nextNeighbor.msgHereIsGlass(glassList.get(0).glass);
		glassList.remove(0);
		sensor2 = false;
		print(name + ": Releasing glass to next family");
		
	}
	
	//Machine glass
	/*private void MachineGlass()
	{
		machining = true;
		glassList.get(0).machined = true;
		//fire the transducer event
		//TODO CHANGE TCHANNEL AND ARGS DEPENDING ON WHICH FAMILY THIS CODE IS USED FOR
		//transducer.fireEvent(TChannel., TEvent.WORKSTATION_DO_ACTION, channelWorkstation);
	}*/
	
	//Transducer Firing
	public synchronized void eventFired(TChannel channel, TEvent event, Object[] args) 
	{
		Integer channelNumber;
		channelNumber = (Integer)args[0];
		
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
		
		
		/*if(channel == TChannel.)
		{
		
		//Workstation
		if(channelNumber == this.channelWorkStation[0])
		{			
			//work station		
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				popupLoaded = true;
			}
			
			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				popupLoaded = false;
				glassList.remove(0);
			}
			
			if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				machining = false;
			}
		}//END NUMBER CHECK
		}//END CHANNEL CHECK */
		
		if(channel == TChannel.SENSOR)
		{
		//sensors	
		if(channelNumber == this.channelSensor1[0])
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				sensor1 = true;
				stateChanged();
			}
			
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				sensor1 = false;
				stateChanged();
			}
		}
		
		if(channelNumber == this.channelSensor2[0])
		{
			if(event == TEvent.SENSOR_GUI_PRESSED)
			{
				sensor2 = true;
				released = true;
				stateChanged();
			}
			
			if(event == TEvent.SENSOR_GUI_RELEASED)
			{
				sensor2 = false;
				released = false;
				stateChanged();
			}
		}//END NUMBER CHECK
		}//END TCHANNEL CHECK
			
	}



	
	
}
