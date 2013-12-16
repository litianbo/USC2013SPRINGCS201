package engine.agent.families.cf5.interfaces;

import engine.agent.families.Glass;

public interface Popup{
    /** Message from next conveyor family saying it has stopped
    */
	public abstract void msgStartConveyor();
	
    /** Message from next conveyor family saying it has restarted
    */
	public abstract void msgStopConveyor();
	
    /** Message from conveyor for passing glass onto popup.
	* @param glass the glass
    */
	public abstract void msgGlassOnPopup(Glass glass);

    /** Message from a robot saying they are done processing their glass.
	* @param robot the robot
    */
	public abstract void msgWantToDropOffGlass(Robot robot);

    /** Message from robot to drop glass off back onto popup.
    * @param robot the robot
	* @param glass the glass
    */
	public abstract void msgDropOffGlass(Robot robot, Glass glass);

    /** Message from conveyor asking if the popup is clear to pass on more glass.
    */
	public abstract void msgIsPopupClear();
	
	/** Message from robot saying that it is either broken or fixed
	 * @param robot the robot
	 * @param state either broken or not
	 */
	public abstract void msgRobotBroken(Robot robot, Boolean state);
	
	/** Message from robot saying that it is returning to idle
	 * @param robot the robot
	 */
	public abstract void msgRobotReset(Robot robot);
}
