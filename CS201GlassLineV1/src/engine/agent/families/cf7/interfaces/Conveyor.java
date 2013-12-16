package engine.agent.families.cf7.interfaces;

import engine.agent.families.Glass;

public interface Conveyor {

	public abstract void msgHereIsGlass(Glass g);
	
	public abstract void msgSendGlass();
	
	public abstract void msgStartConveyor();
	
	public abstract void msgStopConveyor();
	
	public abstract void msgPopupRequestingStart();
	
	public abstract void msgPopupRequestingStop();

	public abstract void msgGlassOnFrontSensor();
	
	public abstract String getName();

}
