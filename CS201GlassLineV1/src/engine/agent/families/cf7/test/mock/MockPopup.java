package engine.agent.families.cf7.test.mock;

import engine.agent.families.Glass;
import engine.agent.families.cf7.interfaces.Machine;
import engine.agent.families.cf7.interfaces.Popup;

public class MockPopup extends MockAgent implements Popup {

	public MockPopup(String name) {
		super(name);
	}

	public EventLog log = new EventLog();
	
	public void msgHereIsGlass(Glass g){
		log.add(new LoggedEvent(
				"Received message msgHereIsGlass from back sensor."));
	}

	public void msgGlassMachined(Glass g) {
		log.add(new LoggedEvent(
				"Received message msgGlassMachined from workstation."));
	}

	public void msgWorkstationsFull() {
		log.add(new LoggedEvent(
				"Received message msgWorkstationsFull from workstation."));
	}

	@Override
	public void msgGlassMachined(Glass g, Machine m) {
		log.add(new LoggedEvent(
				"Received message msgGlassMachined."));
	}

	@Override
	public void msgGlassReadyForRelease(Machine m) {
		log.add(new LoggedEvent(
				"Received message msgGlassReadyForRelease."));
	}

	@Override
	public void msgBrokenGlassRemoved(Machine m) {
		log.add(new LoggedEvent(
				"Received message msgGlassRemoved."));
	}

	@Override
	public void msgConveyorStopped() {
		log.add(new LoggedEvent(
				"Received message msgConveyorStopped."));
	}

	@Override
	public void msgConveyorStarted() {
		log.add(new LoggedEvent(
				"Received message msgConveyorStarted."));
	}
}
