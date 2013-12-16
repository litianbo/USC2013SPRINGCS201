package engine.agent.families.cf5.interfaces;

import engine.agent.families.Glass;

public interface Sensor{
    /** Message from the previous conveyor that glass is approaching.
	* @param glass the glass
    */
	public abstract void  msgHereIsGlass(Glass glass);
	
    /** Message from the conveyor that glass is approaching.
    * @param glass the glass
    */
	public abstract void  msgSenseGlass(Glass glass);
	
    /** Message from the conveyor to restart the previous conveyor family.
    */
	public abstract void  msgStartConveyor();
}
