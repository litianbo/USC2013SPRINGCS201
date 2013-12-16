
package gui.components;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import shared.ImageIcons;
import shared.enums.ConveyorDirections;
import shared.enums.MachineType;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * Manual version of breakout to be used in non-normative scenarios
 * Will be given a part by GUIBreakout, it moves it to itself, animates and tells agent it's done
 */
@SuppressWarnings("serial")
public class GUIManualBreakout extends GuiAnimationComponent
{
	boolean putPartToPopUp;

	GUIPartMover guiPartMover;

	GUIGlass guiPart;

	ConveyorDirections direction = ConveyorDirections.UP;

	MachineType type;

	/**
	 * Constructor for GUIManualBreakout
	 */
	public GUIManualBreakout(Transducer t)
	{
		this.guiPartMover = new GUIPartMover();
		imageIcons = (ArrayList<ImageIcon>)ImageIcons.getIconList("manualBreakout");
		setIcon(imageIcons.get(0));
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
		type = MachineType.MANUAL_BREAKOUT;
		transducer = t;
		transducer.register(this, TChannel.MANUAL_BREAKOUT);

	}




	/**
	 * Method that does the machine animation
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
			// currentPart.setLastOperation(ComponentOperations.MANUALBREAKOUT);

			setIcon(imageIcons.get(0));
			counter = 0;
			
			animationState = AnimationState.IDLE;
			transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_GUI_ACTION_FINISHED, null);
		}
	}

	/**
	 * Method that starts the animations for the specific machines
	 */
	public void msgDoProcess()
	{
		animationState = AnimationState.ANIMATING;
	}

	/**
	 * Animates it and moves the part
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
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
	 * Paints the ManualBreakout
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
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
				guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterY() + 1);
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}
		else if (direction.equals(ConveyorDirections.UP))
		{
			if (guiPart.getCenterY() > getCenterY())
				guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterY() - 1);
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}
		else if (direction.equals(ConveyorDirections.LEFT))
		{
			if (guiPart.getCenterX() > getCenterX())
				guiPart.setCenterLocation(guiPart.getCenterX() - 1, guiPart.getCenterY());
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
			}
		}
		else
		{
			if (guiPart.getCenterX() < getCenterX())
				guiPart.setCenterLocation(guiPart.getCenterX() + 1, guiPart.getCenterY());
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//monroe changed
				this.transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_LOAD_FINISHED, null);//monroe added
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
			transducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_FINISHED, null);//monroe added
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
