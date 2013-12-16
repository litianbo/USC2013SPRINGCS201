package engine.agent.families.cf7.interfaces;

import engine.agent.families.Glass;

public interface Sensor {

	public abstract void msgHereIsGlass(Glass g);
	
	public abstract String getName();

}
