
package gui.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import transducer.TChannel;
import transducer.TEvent;

@SuppressWarnings("serial")
public class GuiAnimationComponent extends GuiComponent
{

	/** ArrayList of ImageIcons */
	protected ArrayList<ImageIcon> imageIcons = new ArrayList<ImageIcon>();

	/** Flag if an animation is running */
	boolean isAnimating;

	enum AnimationState
	{
		IDLE, ANIMATING, DONE, MOVING
	};

	AnimationState animationState = AnimationState.IDLE;

	TChannel channel;

	/** Counter for the frame of the animation */
	int counter;

	@Override
	public void actionPerformed(ActionEvent ae)
	{
		
	}

	GuiAnimationComponent()
	{
		super();
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		
	}

}
