
package gui.components;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import shared.ImageIcons;
import shared.enums.ConveyorDirections;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * GUIBreakout is a graphical representation of the breakout component
 * that breaks a partially cut piece of glass out of the excess piece
 */
@SuppressWarnings("serial")
public class GUIBreakout extends GuiAnimationComponent
{
	ArrayList<ImageIcon> imageIcons = new ArrayList<ImageIcon>(); // ArrayList of ImageIcons

	boolean doneAnimation = false;

	GUIGlass guiPart;

	ConveyorDirections direction = ConveyorDirections.UP;

	/**
	 * Constructor for GUIBreakout
	 */
	public GUIBreakout(Transducer t)
	{
		initializeImages();
		setIcon(imageIcons.get(0));
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
		transducer = t;
		transducer.register(this, TChannel.BREAKOUT);
	}

	/**
	 * Loads all the images (frames of the animation) used by GUIBreakout
	 */
	public void initializeImages()
	{
		imageIcons = (ArrayList<ImageIcon>)ImageIcons.getIconList("breakOut");
	}

	/**
	 * Calls the animation function
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (animationState == AnimationState.MOVING)
		{
			movePartsIn();
		}
		else if (animationState == AnimationState.ANIMATING)
		{
			doAnimate();
		}
		else if (animationState == AnimationState.DONE)
		{
			movePartsOut();
		}
	}

	/**
	 * Goes through the image icons to animate the Breakout
	 */
	public void doAnimate()
	{
		if (counter < imageIcons.size())
		{
			setIcon(imageIcons.get(counter));

			counter++;
		}
		else
		{
			animationState = GuiAnimationComponent.AnimationState.IDLE;
			setIcon(imageIcons.get(0));
			counter = 0;

			transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_GUI_ACTION_FINISHED, null);
		}
	}

	@Override
	public void addPart(GUIGlass part)
	{
		this.guiPart = part;
		animationState = AnimationState.MOVING;
	}

	/**
	 * Called from the actionPerformed method.
	 * 
	 * For the GUIPart in the movingPosition of the GUIConveyor, increments its position toward partFinalPos.
	 * Also increments the position of the GUIConveyorArrow(s) in the same direction and magnitude as the GUIParts.
	 * Upon completion of a "one block" movement of the parts, informs the agent.
	 */
	private void movePartsIn()
	{
		if (direction.equals(ConveyorDirections.DOWN))
		{
			if (guiPart.getCenterY() < getCenterY())
				guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterX() + 1);
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}
		else if (direction.equals(ConveyorDirections.UP))
		{
			if (guiPart.getCenterY() > getCenterY())
				guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterX() - 1);
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}
		else if (direction.equals(ConveyorDirections.LEFT))
		{
			if (guiPart.getCenterX() > getCenterX())
				guiPart.setCenterLocation(guiPart.getCenterX() - 1, guiPart.getCenterY());
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}
		else
		{
			if (guiPart.getCenterX() < getCenterX())
				guiPart.setCenterLocation(guiPart.getCenterX() + 1, guiPart.getCenterY());
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}

	}

	private void movePartsOut()
	{

		if (direction.equals(ConveyorDirections.DOWN))
		{
			guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterY() + 1);
		}
		else if (direction.equals(ConveyorDirections.UP))
		{
			guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterY() - 1);
		}
		else if (direction.equals(ConveyorDirections.LEFT))
		{
			guiPart.setCenterLocation(guiPart.getCenterX() - 1, guiPart.getCenterY());
		}
		else
		{
			guiPart.setCenterLocation(guiPart.getCenterX() + 1, guiPart.getCenterY());
		}
		if (!guiPart.getBounds().intersects(getBounds()))
		{
			nextComponent.addPart(guiPart);
			guiPart = null;
			animationState = AnimationState.IDLE;
			transducer.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_FINISHED, null);//monroe added
		}

	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{

		if (channel.toString().equals(this.channel))
			;
		{
			if (event == TEvent.WORKSTATION_DO_ACTION)
			{
				animationState = AnimationState./*MOVING*/ANIMATING;//monroe changed
			}
			if (event == TEvent.WORKSTATION_RELEASE_GLASS)
			{
				animationState = AnimationState.DONE;
			}
		}

	}
}
