package engine.agent.families.cf5.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Conveyor;


public class MockConveyor extends MockAgent implements Conveyor {
	public MockConveyor(String name) {
		super(name);
	}
	
	public EventLog log = new EventLog();
	
	@Override
	public void msgGlassOnConveyor(Glass glass) {
		log.add(new LoggedEvent(
				"Received message msgGlassOnConveyor from entry sensor to indicate glass is approaching, says glass state is "+
				glass.getState().toString()));
	}

	@Override
	public void msgGlassApproachingPopup(Glass glass) {
		log.add(new LoggedEvent(
				"Received message msgGlassApproachingPopup from entry sensor to indicate glass is near popup, says glass state is "+
				glass.getState().toString()));	
	}

	@Override
	public void msgPopupClear() {
		log.add(new LoggedEvent(
				"Received message msgPopupClear from popup saying that it is ready for new glass"));	
	}
}
