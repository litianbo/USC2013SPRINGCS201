
package gui.components;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import shared.ImageIcons;
import shared.enums.ConveyorDirections;
import shared.enums.MachineType;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * GUIComponentOnline is the class for oven, washer, cutter, paint and UVlamp
 */
@SuppressWarnings("serial")
public class GUIComponentOnline extends GuiAnimationComponent implements ActionListener, Serializable
{
	boolean moveTruck = false;

	boolean imageIconChange = false;

	boolean releasePart = false;

	int originalY;

	int imageCounter = 0;

	/**
	 * Image for Fire truck
	 */
	ImageIcon fireTruck = new ImageIcon("imageicons/truck/truck-water.png");

	GUIGlass currentPart;

	/**
	 * The ArrayList of icons for the animation
	 */
	ArrayList<ImageIcon> imageicons = new ArrayList<ImageIcon>();

	/**
	 * Frame counter
	 */
	int counter = 0;

	/**
	 * Boolean to represent whether or not the truck should drive back
	 */
	boolean driveBack = false;

	boolean doTruck = false;

	/**
	 * The point that this component will move the glass to
	 */
	Point targetPos;

	GUIGlass guiPart;

	MachineType type;

	ConveyorDirections direction;

	public void setDirection(ConveyorDirections direction)
	{
		this.direction = direction;
	}

	/**
	 * The protected constructor for GUIComponentOnline
	 * @param type
	 *        The type of machine
	 */
	public GUIComponentOnline(MachineType type, Transducer t)
	{
		this.type = type;
		transducer = t;
		initializeImages();
		targetPos = new Point(this.getCenterX(), this.getCenterY());

	}

	/**
	 * Moves from conveyor to machine
	 */
	public void msgPartPlaceOnMachine(GUIGlass guiPart)
	{
		isAnimating = true;
		initializeTargetPos();
		// partOriginalLoc = new Point(guiPart.getCenterX(), guiPart.getCenterY());
		// currentPart = guiPart;
		// hasPart = true;
		partIsOnMachine();
	}

	/**
	 * Method to activate animation once part is on the machine
	 */
	public void partIsOnMachine()
	{
		// ((OnlineMachineAgent) myAgent).msgDoneMovingFromConveyorToOnline();
	}

	/**
	 * Method that initializes the imageicons for the specific machines
	 * based on the MachineType enum
	 */
	public void initializeImages()
	{
		if (type == MachineType.CUTTER)
		{
			imageicons = (ArrayList<ImageIcon>)ImageIcons.getIconList("cutter");
			channel = TChannel.CUTTER;
			transducer.register(this, TChannel.CUTTER);
		}
		else if (type == MachineType.OVEN)
		{
			imageicons = (ArrayList<ImageIcon>)ImageIcons.getIconList("oven");
			channel = TChannel.OVEN;
			transducer.register(this, TChannel.OVEN);
		}
		else if (type == MachineType.UV_LAMP)
		{
			imageicons = (ArrayList<ImageIcon>)ImageIcons.getIconList("uvLamp");
			channel = TChannel.UV_LAMP;
			transducer.register(this, TChannel.UV_LAMP);
		}
		else if (type == MachineType.WASHER)
		{
			imageicons = (ArrayList<ImageIcon>)ImageIcons.getIconList("washer");
			channel = TChannel.WASHER;
			transducer.register(this, TChannel.WASHER);
		}
		else if (type == MachineType.PAINT)
		{
			imageicons = (ArrayList<ImageIcon>)ImageIcons.getIconList("paint");
			channel = TChannel.PAINTER;
			transducer.register(this, TChannel.PAINTER);
		}
		else
		{

			System.exit(0);
		}
		setIcon(imageicons.get(0));
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
	}

	/**
	 * Method to initialize the target position based on what machine it is.
	 */
	public void initializeTargetPos()
	{
		// if(type == MachineType.CUTTER)
		// partFinalPos = new Point(getCenterX(), getCenterY());
		// else if(type == MachineType.OVEN)
		// partFinalPos = new Point(getCenterX(), getCenterY());
		// else if (type == MachineType.UV_LAMP)
		// partFinalPos = new Point(getCenterX(), getCenterY());
		// else if (type == MachineType.WASHER)
		// partFinalPos = new Point(getCenterX(), getCenterY());
		// else if (type == MachineType.PAINT)
		// partFinalPos = new Point(getCenterX(), getCenterY());
	}

	/**
	 * Method that does the machine animation
	 */
	public void doAnimate()
	{
		if (counter < imageicons.size())
		{
			setIcon(imageicons.get(counter));
			counter++;
		}
		else
		{

			setIcon(imageicons.get(0));
			counter = 0;
			
			animationState = AnimationState.IDLE;

		
			transducer.fireEvent(channel, TEvent.WORKSTATION_GUI_ACTION_FINISHED, null);
		}
	}

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

		repaint();
	}

	/**
	 * Setter for the MachineType for GUIComponent
	 * @param t
	 *        The new type for the component
	 */
	public void setType(MachineType t, float degrees)
	{
		Dimension oldDim = new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight());
		type = t;
		initializeImages();
		Dimension deltaDim = new Dimension(getIcon().getIconWidth() - oldDim.width, getIcon().getIconHeight()
			- oldDim.height);
		if (degrees != 0.0f)
		{
			setBounds(getX() - deltaDim.width / 2, getY() - deltaDim.height / 2, getIcon().getIconWidth() * 5,
				getIcon().getIconHeight() * 5);
		}
		else
		{
			setBounds(getX() - deltaDim.width / 2, getY() - deltaDim.height / 2, getIcon().getIconWidth(), getIcon()
				.getIconHeight());
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
				guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterY() + 1);
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//changed by monroe
				this.transducer.fireEvent(this.channel, TEvent.WORKSTATION_LOAD_FINISHED, null);
			}
		}
		else if (direction.equals(ConveyorDirections.UP))
		{
			if (guiPart.getCenterY() > getCenterY())
				guiPart.setCenterLocation(guiPart.getCenterX(), guiPart.getCenterY() - 1);
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//changed by monroe
				this.transducer.fireEvent(this.channel, TEvent.WORKSTATION_LOAD_FINISHED, null);
			}
		}
		else if (direction.equals(ConveyorDirections.LEFT))
		{
			if (guiPart.getCenterX() > getCenterX())
				guiPart.setCenterLocation(guiPart.getCenterX() - 1, guiPart.getCenterY());
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//changed by monroe
				this.transducer.fireEvent(this.channel, TEvent.WORKSTATION_LOAD_FINISHED, null);
			}
		}
		else
		{
			if (guiPart.getCenterX() < getCenterX())
				guiPart.setCenterLocation(guiPart.getCenterX() + 1, guiPart.getCenterY());
			else{
				animationState = AnimationState./*ANIMATING*/IDLE;//changed by monroe
				this.transducer.fireEvent(this.channel, TEvent.WORKSTATION_LOAD_FINISHED, null);
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
			transducer.fireEvent(channel, TEvent.WORKSTATION_RELEASE_FINISHED, null);
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
				animationState = AnimationState./*MOVING*/ANIMATING;//changed by monroe
			}
			if (event == TEvent.WORKSTATION_RELEASE_GLASS)
			{
				animationState = AnimationState.DONE;
				releasePart = true;
			}
		}

	}
}
