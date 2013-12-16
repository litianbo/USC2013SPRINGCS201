package engine.agent.families.cf7.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Sensor;

public class MockSensor extends MockAgent implements Sensor {

	public MockSensor(String name) {
		super(name);
	}

	public EventLog log = new EventLog();
	
	public void msgHereIsGlass(Glass g){
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass."));
	}
}
