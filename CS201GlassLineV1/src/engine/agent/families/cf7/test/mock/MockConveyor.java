package engine.agent.families.cf7.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Conveyor;

public class MockConveyor extends MockAgent implements Conveyor {

	public MockConveyor(String name) {
		super(name);
	}

	public EventLog log = new EventLog();
	
	public void msgHereIsGlass(Glass g){
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass from front sensor."));
	}
	
	public void msgSendGlass(){
		log.add(new LoggedEvent(
				"Received message msgSendGlass from back sensor."));
	}
	
	public void msgStartConveyor(){
		log.add(new LoggedEvent(
				"Received message msgStartConveyor."));
	}
	
	public void msgStopConveyor(){
		log.add(new LoggedEvent(
				"Received message msgStopConveyor."));
	}

	@Override
	public void msgPopupRequestingStart() {
		log.add(new LoggedEvent(
				"Received message msgPopupRequestingStart."));
	}

	@Override
	public void msgPopupRequestingStop() {
		log.add(new LoggedEvent(
				"Received message msgPopupRequestingStop."));
	}

	@Override
	public void msgGlassOnFrontSensor() {
		log.add(new LoggedEvent(
				"Received message msgGlassOnFrontSensor."));
	}
}
