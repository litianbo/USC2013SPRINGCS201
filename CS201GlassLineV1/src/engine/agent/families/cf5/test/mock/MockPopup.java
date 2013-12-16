package engine.agent.families.cf5.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Popup;
import engine.agent.families.cf5.interfaces.Robot;


public class MockPopup extends MockAgent implements Popup {
	public MockPopup(String name) {
		super(name);
	}

	public EventLog log = new EventLog();

	@Override
	public void msgStartConveyor() {
		log.add(new LoggedEvent(
				"Received message msgStartConveyor from next conveyor family saying that it has restarted"));	
	}

	@Override
	public void msgStopConveyor() {
		log.add(new LoggedEvent(
				"Received message msgStopConveyor from next conveyor family saying that it has stopped"));	
	}
	
	@Override
	public void msgGlassOnPopup(Glass glass) {
		log.add(new LoggedEvent(
				"Received message msgGlassOnPopup from conveyor to indicate glass with state "+ glass.getState() +
				" with recipe: needBreakout "+glass.getRecipe().getNeedBreakout()+", needCutting "+glass.getRecipe().getNeedCutting()+
				", needDrilling "+glass.getRecipe().getNeedDrilling()+", needGrinding "+glass.getRecipe().getNeedGrinding()+
				", needBaking "+glass.getRecipe().getNeedBaking()+", needPainting "+glass.getRecipe().getNeedPainting()+
				", needUV "+glass.getRecipe().getNeedUV()+", needWashing "+glass.getRecipe().getNeedWashing()+
				" is now on popup"));
	}

	@Override
	public void msgWantToDropOffGlass(Robot robot) {
		log.add(new LoggedEvent(
				"Received message msgWantToDropOffGlass from robot that it's ready to drop off glass"));
	}

	@Override
	public void msgDropOffGlass(Robot robot, Glass glass) {
		log.add(new LoggedEvent(
				"Received message msgDropOffGlass from robot to drop off glass with state "+ glass.getState() +
				" with recipe: needBreakout "+glass.getRecipe().getNeedBreakout()+", needCutting "+glass.getRecipe().getNeedCutting()+
				", needDrilling "+glass.getRecipe().getNeedDrilling()+", needGrinding "+glass.getRecipe().getNeedGrinding()+
				", needBaking "+glass.getRecipe().getNeedBaking()+", needPainting "+glass.getRecipe().getNeedPainting()+
				", needUV "+glass.getRecipe().getNeedUV()+", needWashing "+glass.getRecipe().getNeedWashing()));
	}

	@Override
	public void msgIsPopupClear() {
		log.add(new LoggedEvent(
				"Received message msgIsPopupClear from conveyor asking if the popup is clear so it can receive new glass"));
	}

	@Override
	public void msgRobotBroken(Robot robot, Boolean state) {
		log.add(new LoggedEvent(
				"Received message msgRobotBroken from robot saying that its broken state is "+state));
	}
	
	@Override
	public void msgRobotReset(Robot robot) {
		log.add(new LoggedEvent(
				"Received message msgRobotReset from robot saying that its ready for more glass"));
	}
}
