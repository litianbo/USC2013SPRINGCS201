
package gui.components;

import gui.panels.DisplayPanel;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;

import shared.enums.ConveyorDirections;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * GUIConveyor is a graphical representation of the conveyer component
 * that transfers glass around the factory
 */
@SuppressWarnings("serial")
public class GUIConveyor extends GuiComponent
{
	/** the direction of the conveyor */
	private ConveyorDirections direction;

	/** a number representing the number of blocks the conveyor can hold */
	private int capacity;

	/** the index of the conveyor */
	private int myIndex;

	/** an array of the GUIParts on the conveyor */
	private ArrayList<GUIGlass> guiParts;

	/** length of the GUIConveyor, gotten from the imageicon */
	private double myLength;

	/** an array of center-positions the GUIPart can be at */
	private Point[] positions;

	/** a boolean used to turn conveyor movement on or off */
	private boolean moving = false;

	/** a number derived from the imageicon for the GUIConveyorConnector */
	private int guiConveyorConnectorLength = 17;

	/** this boolean is checked to see if the user has overrided the automatic conveyor movement */
	private boolean movingOverrided = false;

	/**
	 * this boolean is checked to see, if the user has overrided the automatic conveyor movement, whether a moving state
	 * change was "lost" to the override
	 */
	private boolean movingChangeLostToOverride = false;

	/** a list of the GUIConveyor's dynamically-moving arrows */
	public ArrayList<GUIConveyorArrow> arrows = new ArrayList<GUIConveyorArrow>();

	/** a dynamically moving arrow */
	public GUIConveyorArrow arrow1;

	static public ImageIcon conveyorUp = new ImageIcon("imageicons/conveyor/Lane_up.png");

	static public ImageIcon conveyorDown = new ImageIcon("imageicons/conveyor/Lane_down.png");

	static public ImageIcon conveyorLeft = new ImageIcon("imageicons/conveyor/Lane_left.png");

	static public ImageIcon conveyorRight = new ImageIcon("imageicons/conveyor/Lane_right.png");

	/**
	 * constructor to give GUIConveyor its direction and other properties.
	 * 
	 * ImageIcons and direction-velocities are set according to the ConveyorDirections direction parameter.
	 * 
	 * @param factoryPanel
	 *        the FactoryPanel containing this GUIConveyor
	 * @param capacity
	 *        the max blocks the GUIConveyor can contain
	 * @param direction
	 *        the direction of the conveyor. Used to choose correct image icon and directions.
	 * @param speed
	 *        the speed of the conveyor when moving
	 */
	public GUIConveyor(DisplayPanel p, ConveyorDirections direction, int speed, int capacity, int index, Transducer t)
	{
		super(p);
		// Set current angle
		this.direction = direction;
		transducer = t;

		this.capacity = capacity;
		myIndex = index;

		guiParts = new ArrayList<GUIGlass>();
		positions = new Point[capacity + 1];

		moving = false;

		arrow1 = new GUIConveyorArrow(direction, 0);
		arrows.add(arrow1);
		setIcon(new ImageIcon("imageicons/conveyor/Lane_" + direction.toString().toLowerCase() + ".png"));
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
		for (GUIConveyorArrow arrow : arrows)
		{
			parent.add(arrow, DisplayPanel.DND_LAYER + 1);
			parent.setLayer(arrow, DisplayPanel.DND_LAYER + 1);
		}
		transducer.register(this, TChannel.CONVEYOR);
	}

	/**
	 * Checks to see if GUIConveyor movement is turned on (boolean moving); if so, calls moveParts()
	 */
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		if (moving)
		{
			moveParts();
		}
		for (int i = guiParts.size() - 1; i >= 0; i--)
		{
			if (!guiParts.get(i).getBounds().intersects(this.getBounds()))
			{
				nextComponent.addPart(guiParts.get(i));
				guiParts.remove(i);
			}
		}
		repaint();
	}

	/**
	 * Called from the actionPerformed method.
	 * 
	 * For the GUIPart in the movingPosition of the GUIConveyor, increments its position toward partFinalPos.
	 * Also increments the position of the GUIConveyorArrow(s) in the same direction and magnitude as the GUIParts.
	 * Upon completion of a "one block" movement of the parts, informs the agent.
	 */
	private void moveParts()
	{
		for (GUIGlass p : guiParts)
		{
			if (direction.equals(ConveyorDirections.DOWN))
			{
				p.setCenterLocation(p.getCenterX(), p.getCenterY() + 1);
			}
			else if (direction.equals(ConveyorDirections.UP))
			{
				p.setCenterLocation(p.getCenterX(), p.getCenterY() - 1);
			}
			else if (direction.equals(ConveyorDirections.LEFT))
			{
				p.setCenterLocation(p.getCenterX() - 1, p.getCenterY());
			}
			else
			{
				p.setCenterLocation(p.getCenterX() + 1, p.getCenterY());
			}
		}
	}

	/**
	 * Adds a GUIPart to the GUIConveyor
	 * @param guipart
	 *        the GUIPart to add to this GUIConveyor's first position
	 */
	public void msgDoAddFirstPart(GUIGlass guipart)
	{
		// print("GUIConveyorNew Received  msgDoAddFirstPart(GUIPart guipart)");
		guiParts.add(0, guipart);
		guiParts.get(0).setCenterLocation((int)positions[0].getX(), (int)positions[0].getY());
	}

	/**
	 * Removes the first GUIPart from the GUIConveyor
	 * @param guipart
	 *        removes the GUIPart residing in the GUIConveyor's first block
	 */
	public void msgDoRemoveFirstPart(GUIGlass guipart)
	{
		guiParts.remove(0);
	}

	/**
	 * Calls the parent setBounds(x,y,width,height), but also calls this class's initialize() method.
	 */
	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		initialize();
	}

	/**
	 * Invoked when the panel containing this GUIConveyor sets the bounds.
	 * Now that the GUIConveyor has a location, the points list is populated, and length and width set accordingly.
	 */
	private void initialize()
	{
		if (direction == ConveyorDirections.RIGHT)
		{
			this.myLength = getIcon().getIconWidth();
			for (int i = 0; i < capacity; i++)
			{
				int cellX = (int)(myLength / capacity * (i + 0.5));
				positions[i] = new Point(this.getX() + cellX, this.getCenterY());
			}
			positions[capacity] = new Point(this.getX() + (int)myLength + guiConveyorConnectorLength, this.getCenterY());
		}
		else if (direction == ConveyorDirections.LEFT)
		{
			this.myLength = getIcon().getIconWidth();
			for (int i = 0; i < capacity; i++)
			{
				int cellX = (int)(myLength / capacity * (i + 0.5));
				positions[i] = new Point(this.getX() + (int)myLength - cellX, this.getCenterY());
			}
			positions[capacity] = new Point(this.getX() - guiConveyorConnectorLength, this.getCenterY());
		}
		else if (direction == ConveyorDirections.UP)
		{
			this.myLength = getIcon().getIconHeight();
			for (int i = 0; i < capacity; i++)
			{
				int cellY = (int)(myLength / capacity * (i + 0.5));
				positions[i] = new Point(this.getCenterX(), this.getY() + (int)myLength - cellY);
			}
			positions[capacity] = new Point(this.getCenterX(), this.getY() - guiConveyorConnectorLength);
		}
		else if (direction == ConveyorDirections.DOWN)
		{
			this.myLength = getIcon().getIconHeight();
			for (int i = 0; i < capacity; i++)
			{
				int cellY = (int)(myLength / capacity * (i + 0.5));
				positions[i] = new Point(this.getCenterX(), this.getY() + cellY);
			}
			positions[capacity] = new Point(this.getCenterX(), this.getY() + (int)myLength + guiConveyorConnectorLength);
		}
	}

	/**
	 * ONLY for Conveyor Status&Control Panel! Manual override for the moving state of the conveyor.
	 * @param b
	 *        set moving: true or false
	 */
	public void overrideMovingTo(boolean b)
	{

		boolean tempMoving = moving;

		if (movingOverrided == true)
		{
			// System.out.println("Conveyor Control: I am cancelling an override before doing a new override!");
			cancelMovingOverride();
		}

		if (tempMoving != b)
		{
			movingChangeLostToOverride = true;
		}
		else
		{
			movingChangeLostToOverride = false;
		}

		moving = b;
		movingOverrided = true;
	}

	/**
	 * Called from the Conveyor Status&Control Panel when the user changes a conveyor's control back to "AUTO".
	 * If a moving state change was lost to the override, this method also restores the "lost" moving state
	 */
	public void cancelMovingOverride()
	{
		if (movingChangeLostToOverride == true)
		{ // reverse the moving state if a change was lost during the override
			// System.out.println("Conveyor Control: I am reversing states!");
			if (moving == true)
			{
				moving = false;
			}
			else if (moving == false)
			{
				moving = true;
			}

			movingChangeLostToOverride = false;
		}

		movingOverrided = false;
	}

	/** @return is conveyor movement on */
	public boolean isMoving()
	{
		return moving;
	}

	public void addPart(GUIGlass part)
	{
		guiParts.add(part);
	}

	public ConveyorDirections getDirection()
	{
		return direction;
	}

	public void paint(Graphics g)
	{
		super.paint(g);
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if (channel.equals(TChannel.CONVEYOR) && ((Integer)args[0]).equals(myIndex))
		{
			if (event.equals(TEvent.CONVEYOR_DO_START))
			{
				moving = true;
			}
			if (event.equals(TEvent.CONVEYOR_DO_STOP))
			{
				moving = false;
			}
		}

	}

}
