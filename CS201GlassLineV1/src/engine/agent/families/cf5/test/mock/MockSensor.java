package engine.agent.families.cf5.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf5.interfaces.Sensor;


public class MockSensor extends MockAgent implements Sensor {
		public MockSensor(String name) {
			super(name);
		}

		public EventLog log = new EventLog();
		

		@Override
		public void msgHereIsGlass(Glass glass) {
			log.add(new LoggedEvent(
					"Received message msgHereIsGlass from previous conveyor to indicate glass is approaching, says glass state is "+
					glass.getState().toString()));
		}
		
		@Override
		public void msgSenseGlass(Glass glass) {
			log.add(new LoggedEvent(
					"Received message msgSenseGlass from conveyor to indicate glass is approaching and  says glass state is "+
					glass.getState().toString()));
		}

		@Override
		public void msgStartConveyor() {
			log.add(new LoggedEvent(
					"Received message msgStartConveyor from conveyor to restart previous conveyor family"));
		}
}
