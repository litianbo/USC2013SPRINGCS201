package engine.agent.families.cf7.interfaces;

import engine.agent.families.Glass;

public interface Machine {

	public abstract void msgHereIsGlass(Glass g);
	
	public abstract void msgPopupAvailable();
	
	public abstract void msgLoadGlass();
	
	public abstract String getName();

}
