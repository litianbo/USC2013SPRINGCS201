package engine.agent.families;

//did not want to modify the recipe of a glass object in my family, so this keeps track of if a piece of glass has been machined
public class GlassClass 
{
	public Glass glass;
	public boolean machined;
	
	public GlassClass(Glass g)
	{
		this.glass = g;
		machined = false;
	}
}
