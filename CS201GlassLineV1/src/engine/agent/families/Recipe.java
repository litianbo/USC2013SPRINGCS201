package engine.agent.families;

public class Recipe {
	private Boolean needManualBreakout;
	private Boolean needBreakout;
	private Boolean needCrossseam;
	private Boolean needCutting;
	private Boolean needDrilling;
	private Boolean needGrinding;
	private Boolean needBaking;
	private Boolean needPainting;
	private Boolean needUV;
	private Boolean needWashing;

	public Recipe(Boolean needManualBreakout, Boolean needBreakout,  Boolean needCrossseam,  Boolean needCutting,  Boolean needDrilling,  
			Boolean needGrinding,  Boolean needBaking,  Boolean needPainting,  Boolean needUV, Boolean needWashing) {
		this.needManualBreakout = needManualBreakout;
		this.needBreakout = needBreakout;
		this.needCrossseam = needCrossseam;
		this.needCutting = needCutting;
		this.needDrilling = needDrilling;
		this.needGrinding = needGrinding;
		this.needBaking = needBaking;
		this.needPainting = needPainting;
		this.needUV = needUV;
		this.needWashing = needWashing;
	}
	
	public Recipe() {
		this.needManualBreakout = false;
		this.needBreakout = false;
		this.needCrossseam = false;
		this.needCutting = false;
		this.needDrilling = false;
		this.needGrinding = false;
		this.needBaking = false;
		this.needPainting = false;
		this.needUV = false;
		this.needWashing = false;
	}
	
	public Boolean getNeedManualBreakout(){
		return needManualBreakout;
	}
	
	public Boolean getNeedBreakout() {
		return needBreakout;
	}
	
	public Boolean getNeedCrossseam() {
		return needCrossseam;
	}
	
	public Boolean getNeedCutting() {
		return needCutting;
	}
	
	public Boolean getNeedDrilling() {
		return needDrilling;
	}
	
	public Boolean getNeedGrinding() {
		return needGrinding;
	}
	
	public Boolean getNeedBaking() {
		return needBaking;
	}
	
	public Boolean getNeedPainting() {
		return needPainting;
	}
	
	public Boolean getNeedUV() {
		return needUV;
	}
	
	public Boolean getNeedWashing() {
		return needWashing;
	}
	
}
