package engine.agent.families;

public interface ConveyorFamilyInterface {
    /** 
    * Message from previous conveyor family handing glass over.
	* @param glass the glass
    */
	public abstract void msgHereIsGlass(Glass glass);

    /** 
    * Set next family for this conveyor family from factoryPanel
    */
	public abstract void setNextFamily(ConveyorFamilyInterface nextFamily);
	
    /** 
    * Set previous family for this conveyor family from factoryPanel
    */
	public abstract void setPreviousFamily(ConveyorFamilyInterface previousFamily);

    /** 
    * Message from next conveyor family saying it has stopped
    */
	public abstract void msgStartConveyor();

    /** 
    * Message from next conveyor family saying it has restarted
    */
	public abstract void msgStopConveyor();
}
