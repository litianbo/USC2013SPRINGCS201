package engine.agent.families.cf7.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Machine;

public class MockMachine extends MockAgent implements Machine {

	public MockMachine(String name) {
		super(name);
	}

	public EventLog log = new EventLog();
	
	public void msgHereIsGlass(Glass g){
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass from popup."));
	}

	@Override
	public void msgPopupAvailable() {
		log.add(new LoggedEvent(
				"Received message msgPopupAvailable from popup."));	
	}

	@Override
	public void msgLoadGlass() {
		log.add(new LoggedEvent(
				"Received message msgLoadGlass from popup."));	
	}
}
