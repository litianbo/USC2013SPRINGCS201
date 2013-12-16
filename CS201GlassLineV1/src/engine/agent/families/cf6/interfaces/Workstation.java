package engine.agent.families.cf6.interfaces;

import engine.agent.families.Glass;

public interface Workstation {

	public abstract void msgProcessGlass(Glass glass);
	public abstract void msgReleaseGlass(Glass glass);

}
