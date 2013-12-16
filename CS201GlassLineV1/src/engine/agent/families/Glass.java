package engine.agent.families;

public class Glass {
	public enum GlassState { start, doneBreakout, doneCrossseam, doneCutting, doneDrilling, doneGrinding, doneBaking, donePainting, doneUK, doneWashing }
	private GlassState state;
	private Recipe recipe;

	public Glass (Recipe r) {
		this.recipe = r;
		this.state = GlassState.start;
	}
	
	public String getState() {
		return state.toString();
	}
	
	public Recipe getRecipe() {
		return recipe;
	}
	
	public void setState(GlassState state) {
		this.state = state;
	}

}
