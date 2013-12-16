package engine.agent.families.cf5.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Robot;

public class MockRobot extends MockAgent implements Robot {
	public MockRobot(String name) {
		super(name);
	}

	public EventLog log = new EventLog();

	@Override
	public void msgRobotPickup(Glass glass) {
		log.add(new LoggedEvent(
				"Received message msgRobotPickup from popup to indicate glass needs pickup, and says glass state is "+
				glass.getState().toString()+
				" with recipe: needBreakout "+glass.getRecipe().getNeedBreakout()+", needCutting "+glass.getRecipe().getNeedCutting()+
				", needDrilling "+glass.getRecipe().getNeedDrilling()+", needGrinding "+glass.getRecipe().getNeedGrinding()+
				", needBaking "+glass.getRecipe().getNeedBaking()+", needPainting "+glass.getRecipe().getNeedPainting()+
				", needUV "+glass.getRecipe().getNeedUV()+", needWashing "+glass.getRecipe().getNeedWashing()+
				" is now on popup"));
	}

	@Override
	public void msgReceiveGlass() {
		log.add(new LoggedEvent(
				"Received message msgReceiveGlass from popup to indicate popup is ready to receive glass"));	
	}
}
