package engine.agent.families.cf5.interfaces;

import engine.agent.families.Glass;

public interface Conveyor {
	
    /** Message from entry sensor that there is new glass.
	* @param glass the glass
    */
	public abstract void msgGlassOnConveyor(Glass glass);

    /** Message from entry sensor saying glass is approaching popup.
    */
	public abstract void msgGlassApproachingPopup (Glass glass);

    /** Message from popup saying it is currently ready to receive new glass.
    */
	public abstract void msgPopupClear();
}
