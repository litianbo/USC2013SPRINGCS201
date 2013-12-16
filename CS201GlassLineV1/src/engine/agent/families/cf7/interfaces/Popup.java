package engine.agent.families.cf7.interfaces;

import engine.agent.families.Glass;

public interface Popup {

	public abstract void msgHereIsGlass(Glass g);
	
	public abstract void msgGlassMachined(Glass g, Machine m);
	
	public abstract String getName();

	public abstract void msgGlassReadyForRelease(Machine m);

	public abstract void msgBrokenGlassRemoved(Machine m);
	
	public abstract void msgConveyorStopped();
	
	public abstract void msgConveyorStarted();
}
