package engine.agent.families.cf6.interfaces;

public interface Conveyer {

	public abstract void msgStartThisConveyer();
	public abstract void msgStopThisConveyer();
	public abstract void msgRestartAllConveyers();
	public abstract void msgStopAllConveyers();

}
