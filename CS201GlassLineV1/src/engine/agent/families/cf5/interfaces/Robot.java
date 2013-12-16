package engine.agent.families.cf5.interfaces;

import engine.agent.families.Glass;

public interface Robot{
    /** Message from popup to pick up available glass.
	* @param glass glass
    */
	public abstract void msgRobotPickup(Glass glass);

    /** Message from popup saying its ready to receive glass.
    */
	public abstract void msgReceiveGlass();
}
