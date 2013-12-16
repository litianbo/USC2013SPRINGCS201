package engine.agent.families.cf5;

import java.util.ArrayList;
import java.util.List;

import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Robot;
import engine.agent.families.ConveyorFamilyInterface;
import transducer.Transducer;

public class ConveyorFamilyInterfaceClass implements ConveyorFamilyInterface{

	//Mock conveyor families
	//MockConveyorFamily conveyor0;
	//MockConveyorFamily conveyor2;*/
	
	ConveyorFamilyInterface conveyor0;
	ConveyorFamilyInterface conveyor2;
	
	//conveyor 1
	ConveyorAgent conveyor1;
	
	PopupAgent popup;
	
	SensorAgent sensorPopup;
	SensorAgent sensorEntry;
	
	List<RobotAgent> robots = new ArrayList<RobotAgent>();
	
	public ConveyorFamilyInterfaceClass(Transducer transducer){
		//Initialize all agents for conveyor 1
		//v0 only, mock conveyor families
		//this.conveyor0 = previousConveyor;
		//this.conveyor2 = nextConveyor;
		
		conveyor1 = new ConveyorAgent("Conveyor 1", 5, 0, transducer);
		
		popup = new PopupAgent("Popup", 0, transducer);
		sensorPopup = new SensorAgent("Popup Sensor", "Popup", 11, transducer);
		sensorEntry = new SensorAgent("Entry Sensor", "Entry", 10, transducer);
		
		sensorPopup.setConveyor(conveyor1);
		sensorEntry.setConveyor(conveyor1);
		
		popup.setConveyor(conveyor1);
		
		//populate robots list with robot agents
		robots.add(new RobotAgent("Robot 1", 0, transducer));
		robots.add(new RobotAgent("Robot 2", 1, transducer));
		
		for (RobotAgent r: robots){
			r.setPopup(popup);
		}
		
		//Hack to set up robots for popup
		List<Robot> robotSet = new ArrayList<Robot>();
		
		for (Robot r: robots){
			robotSet.add(r);
		}
		
		popup.setRobots(robotSet);
		
		//setup conveyor

		conveyor1.setEntrySensor(sensorEntry);
		conveyor1.setPopupSensor(sensorPopup);
		conveyor1.setPopup(popup);
		
		//start agents
		for (RobotAgent r: robots){
			r.startThread();
		}
		
		sensorPopup.startThread();
		sensorEntry.startThread();
		popup.startThread();
		conveyor1.startThread();
		
		//v0 only, pass a glass object straight into conveyor
		/*sensorEntry.msgHereIsGlass(new Glass(new Recipe(false, false, false, true, false, false, false, false, false)));
		sensorEntry.msgHereIsGlass(new Glass(new Recipe(false, false, false, true, false, false, false, false, false)));
		sensorEntry.msgHereIsGlass(new Glass(new Recipe(false, false, false, false, false, false, false, false, false)));*/
	}
	
	public void msgHereIsGlass(Glass glass) {
		sensorEntry.msgHereIsGlass(glass);
	}

	public void msgStartConveyor() {
		popup.msgStartConveyor();
	}

	public void msgStopConveyor() {
		popup.msgStopConveyor();
	}

	public void setNextFamily(ConveyorFamilyInterface nextFamily) {
		popup.setNextConveyor(nextFamily);
	}

	public void setPreviousFamily(ConveyorFamilyInterface previousFamily) {
		conveyor1.setPreviousConveyor(previousFamily);
		sensorEntry.setPreviousConveyor(previousFamily);
	}

}
