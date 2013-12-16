
package gui.components;

import java.awt.Graphics;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import shared.enums.ConveyorDirections;
import transducer.TChannel;
import transducer.TEvent;

/**
 * GUIConveyorConnector is a graphical representation of the conveyer connector
 * component connecting conveyors
 */
@SuppressWarnings("serial")
public class GUIShuttle extends GuiComponent
{

	/** the current GUI glass part */
	GUIGlass currentPart;

	/** a boolean that controls whether the line is moving */
	boolean moving = false;

	/** a number that tracks how far a GUIPart has moved */
	int moveCounter = 0;

	int movementQueue = 0;

	/** a boolean to track whether the GUIPart the GUIConveyorConnector has is snapped to the center */
	boolean doneAligning = false;

	/** the direction of the conveyor connector */
	public ConveyorDirections direction;

	/** An int used to represent X-velocity */
	private int vX;

	/** An int used to represent Y-velocity */
	private int vY;

	/** a boolean to determine if this GUIConveyorConnector rotates the GUIPart */
	boolean rotates = false;

	/** a boolean to check if the GUIPart has finished being rotated */
	boolean doneRotating = false;

	int rotationDegrees = 0;

	public static ImageIcon upConnector = new ImageIcon("imageicons/conveyorConnectorImage_up.png");

	public static ImageIcon downConnector = new ImageIcon("imageicons/conveyorConnectorImage_down.png");

	public static ImageIcon leftConnector = new ImageIcon("imageicons/conveyorConnectorImage_left.png");

	public static ImageIcon rightConnector = new ImageIcon("imageicons/conveyorConnectorImage_right.png");

	/** checks whether the GUIPart on this connector needs to be trashed */
	boolean trashPart = false;

	/**
	 * A constructor for GUIConveyorConnector that takes in a cardinal direction
	 * and sets the correct direction-image and velocity direction
	 * @param dir
	 *        the conveyor direction
	 */
	public GUIShuttle(ConveyorDirections dir)
	{
		direction = dir;
		this.rotates = true;

		if (direction == ConveyorDirections.UP)
		{
			setIcon(upConnector);
			setvX(0);
			setvY(-6);
		}
		else if (direction == ConveyorDirections.DOWN)
		{
			setIcon(downConnector);
			setvX(0);
			setvY(6);
		}
		else if (direction == ConveyorDirections.LEFT)
		{
			setIcon(leftConnector);
			setvX(-6);
			setvY(0);
		}
		else if (direction == ConveyorDirections.RIGHT)
		{
			setIcon(rightConnector);
			setvX(6);
			setvY(0);
		}
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
	}

	/**
	 * A constructor for corner GUIConveyorConnectors that takes in a cardinal direction
	 * and sets the correct direction-image and velocity direction, and says it rotates
	 * 
	 * @param dir
	 *        the conveyor direction
	 * @param rotates
	 *        this GUIConveyorConnector rotates the part
	 */
	public GUIShuttle(ConveyorDirections dir, boolean rotates)
	{
		direction = dir;
		this.rotates = true;

		if (direction == ConveyorDirections.UP)
		{
			setIcon(upConnector);
			setvX(0);
			setvY(-6);
		}
		else if (direction == ConveyorDirections.DOWN)
		{
			setIcon(downConnector);
			setvX(0);
			setvY(6);
		}
		else if (direction == ConveyorDirections.LEFT)
		{
			setIcon(leftConnector);
			setvX(-6);
			setvY(0);
		}
		else if (direction == ConveyorDirections.RIGHT)
		{
			setIcon(rightConnector);
			setvX(6);
			setvY(0);
		}
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
		// guiPartMover = new GUIPartMover();
	}

	/**
	 * Moves a GUIPart one block in a direction.
	 * Before moving a block, calls alignPart to make sure the part is squarely on the conveyor.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		if (part != null)
		{
			movePart();
		}

	}

	private void movePart()
	{
		if (direction == ConveyorDirections.UP)
		{
			if (part.getCenterX() > getCenterX())
			{
				part.setCenterLocation(part.getCenterX() - 1, part.getCenterY());
			}
			else
			{
				part.setCenterLocation(part.getCenterX(), part.getCenterY() - 1);
			}
		}
		else if (direction == ConveyorDirections.DOWN)
		{
			if (part.getCenterX() < getCenterX())
			{
				part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
			}
			else
			{
				part.setCenterLocation(part.getCenterX(), part.getCenterY() + 1);
			}
		}
		else if (direction == ConveyorDirections.LEFT)
		{
			if (part.getCenterY() < getCenterY())
			{
				part.setCenterLocation(part.getCenterX(), part.getCenterY() + 1);
			}
			else
			{
				part.setCenterLocation(part.getCenterX() - 1, part.getCenterY());
			}
		}
		else if (direction == ConveyorDirections.RIGHT)
		{
			if (part.getCenterY() > getCenterY())
			{
				part.setCenterLocation(part.getCenterX(), part.getCenterY() - 1);
			}
			else
			{
				part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
			}
		}
		if (!part.getBounds().intersects(getBounds()))
		{
			nextComponent.addPart(part);
			part = null;
		}
	}

	/**
	 * adds a GUIPart to this connector
	 * @param part
	 *        the GUIPart to add to the GUIConveyorConnector
	 */
	public void msgHereIsGUIPart(GUIGlass part)
	{
		this.currentPart = part;

		doneAligning = false;
		doneRotating = false;
	}

	/**
	 * This message tells GUIConveyorConnector to initiate a one-block move
	 */
	public void msgDoMoveOneBlock()
	{
		if (!moving)
		{

			moving = true;
			moveCounter = 0;
		}
		else
		{
			movementQueue++;
		}
	}

	/**
	 * lets the backend agent give the frontend GUI a reference to the agent
	 * @param agent
	 *        the agent for this GUIConveyorConnector
	 */
	public void paint(Graphics g)
	{
		super.paint(g);
	}

	public int getvX()
	{
		return vX;
	}

	public void setvX(int vX)
	{
		this.vX = vX;
	}

	public int getvY()
	{
		return vY;
	}

	public void setvY(int vY)
	{
		this.vY = vY;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{

	}
}
