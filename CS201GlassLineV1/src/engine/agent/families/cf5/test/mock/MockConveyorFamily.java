package engine.agent.families.cf5.test.mock;

import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;

public class MockConveyorFamily extends MockAgent implements ConveyorFamilyInterface {
	public MockConveyorFamily(String name) {
		super(name);
	}

	public EventLog log = new EventLog();

	@Override
	public void msgHereIsGlass(Glass glass) {
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass from previous conveyor family to indicate glass is approaching says glass state is "+
				glass.getState().toString() +
				" with recipe: needBreakout "+glass.getRecipe().getNeedBreakout()+", needCutting "+glass.getRecipe().getNeedCutting()+
				", needDrilling "+glass.getRecipe().getNeedDrilling()+", needGrinding "+glass.getRecipe().getNeedGrinding()+
				", needBaking "+glass.getRecipe().getNeedBaking()+", needPainting "+glass.getRecipe().getNeedPainting()+
				", needUV "+glass.getRecipe().getNeedUV()+", needWashing "+glass.getRecipe().getNeedWashing()+
				" is now on popup"));
	}

	@Override
	public void msgStartConveyor() {
		log.add(new LoggedEvent(
				"Received message msgStartConveyor from next conveyor to indicate that it has restarted"));
	}

	@Override
	public void msgStopConveyor() {
		log.add(new LoggedEvent(
				"Received message msgStopConveyor from next conveyor to indicate that it has stopped"));
	}

	@Override
	public void setNextFamily(ConveyorFamilyInterface nextFamily) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPreviousFamily(ConveyorFamilyInterface previousFamily) {
		// TODO Auto-generated method stub
		
	}
}
