
package gui.components;

import gui.panels.DisplayPanel;
import gui.util.ComponentTransformer;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

/**
 * The GuiComponent class is the superclass for all components in the factory
 * GUI. It provides methods to change the location of the component, and to
 * update the component. Subcomponents must override the update method with
 * their own function.
 */
@SuppressWarnings("serial")
public abstract class GuiComponent extends JLabel implements ActionListener, TReceiver
{
	/** Reference to parent panel for communication */
	protected DisplayPanel parent;

	/** Reference to the transformer panel of the component */
	protected ComponentTransformer transformer = null;

	/** Transducer for the GuiComponents */
	Transducer transducer;

	/** Name to identify the GuiComponent */
	String name;

	GUIGlass part;

	GuiComponent nextComponent;

	/**
	 * default constructor
	 */
	public GuiComponent()
	{
		super();
	}

	/**
	 * Constructor links the component to the parent panel
	 * 
	 * @param dPanel
	 *        the DisplayPanel linked to this panel
	 */
	public GuiComponent(DisplayPanel dPanel)
	{
		super();
		parent = dPanel;
	}

	/** a constructor for setting image Icon */
	public GuiComponent(ImageIcon icon)
	{
		super(icon);
		setSize(icon.getIconWidth(), icon.getIconHeight());
	}

	/** gets the transformer panel of the object */
	public ComponentTransformer getTransformer()
	{
		return transformer;
	}

	/** sets the object to draw ints boundries */
	public void setBoundaryDrawing(boolean boundaryDrawing)
	{
		if (boundaryDrawing)
			this.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
		else
			this.setBorder(null);

	}

	/**
	 * Moves this component to a new location. The middle of the new location is
	 * specified by the x and y parameters in the coordinate space of this
	 * component's parent.
	 * 
	 * @param x
	 *        the x-coordinate of the new location's middle point in the
	 *        parent's coordinate space
	 * @param y
	 *        the y-coordinate of the new location's middle point in the
	 *        parent's coordinate space
	 */
	public final void setCenterLocation(int x, int y)
	{
		if (transformer == null)
			super.setLocation(x - getWidth() / 2, y - getHeight() / 2);
		else
			transformer.setCenterLocation(x, y);
	}

	/**
	 * Moves this component to a new location. The middle point of the new
	 * location is specified by the x and y parameters in the coordinate space
	 * of this component's parent.
	 * 
	 * @param p
	 *        the point defining the middle point of the new location, given
	 *        in the coordinate space of this component's parent
	 */
	public final void setCenterLocation(Point p)
	{
		if (transformer == null)
			setCenterLocation((int)p.getX(), (int)p.getY());
		else
			transformer.setCenterLocation((int)p.getX(), (int)p.getY());

	}

	/**
	 * Gets the location of this component in the form of a point specifying the
	 * component's middle point. The location will be relative to the parent's
	 * coordinate space.
	 * 
	 * @return an instance of Point representing the middle point of the
	 *         component's bounds in the coordinate space of the component's
	 *         parent
	 */
	public final Point getCenterLocation()
	{
		if (transformer == null)
			return (new Point(getX() + getWidth() / 2, getY() + getHeight() / 2));
		else
			return (new Point(transformer.getX() + transformer.getWidth() / 2, transformer.getY()
				+ transformer.getHeight() / 2));

	}

	/**
	 * Moves this component relative to its current location a specified
	 * distance. Keep in mind that this is relative movement!
	 * 
	 * @param dx
	 *        the horizontal distance to move the component. To move left, dx
	 *        should be negative, to move right, positive
	 * @param dy
	 *        the vertical distance to move the component. To move up, dy
	 *        should be negative, to move down, positive
	 */
	public final void moveComponent(int dx, int dy)
	{
		if (transformer == null)
			setLocation(getX() + dx, getY() + dy);
		else
			transformer.setLocation(transformer.getX() + dx, transformer.getY() + dy);
	}

	/**
	 * setting up transformer
	 */
	public final void setupTransformer()
	{
		transformer = new ComponentTransformer();
		transformer.setView(this);
	}

	/**
	 * sets up transformer for the kitting but which is more accurate
	 */
	public final void setupTransformerAroundCenter()
	{
		transformer = new ComponentTransformer()
		{
			protected void setPreTransformCenter()
			{
			}

			protected void adjustLocation()
			{
			}

			protected void myRevalidate()
			{
			}
		};
		transformer.setView(this);
	}

	/**
	 * Returns the parent DisplayPanel
	 * 
	 * @return the parent panel
	 */
	public DisplayPanel getGuiParent()
	{
		return parent;
	}

	/**
	 * @return the Angle
	 */
	public double getAngle()
	{
		if (transformer == null)
			return 0;
		else
			return transformer.getViewAngle();
	}

	/**
	 * rotating Theta degrees in Radian
	 * @param theta
	 *        degrees for turning in radians
	 */
	public void rotate(double theta)
	{
		if (transformer != null)
			transformer.rotate(theta);
	}

	/**
	 * returns the X center of the component
	 * @return
	 */
	public int getCenterX()
	{
		if (transformer == null)
			return getX() + getWidth() / 2;
		else
			return transformer.getX() + transformer.getWidth() / 2;
	}

	/**
	 * returns the Y center of the component
	 * @return
	 */
	public int getCenterY()
	{
		if (transformer == null)
			return getY() + getHeight() / 2;
		else
			return transformer.getY() + transformer.getHeight() / 2;
	}

	/**
	 * sets the parent panel for this component
	 * @param parent
	 */
	public void setGuiParent(DisplayPanel parent)
	{
		this.parent = parent;
	}

	/**
	 * 
	 * @return the polygon which is around the image
	 */
	public Polygon getPolyBounds()
	{
		if (transformer == null)
		{
			return new Polygon(new int[] { getX(), getX() + getWidth(), getX(), getX() + getHeight() },
				new int[] { getY(), getY(), getY() + getHeight(), getY() + getHeight() },
				4);
		}
		else
			return transformer.getTransformedBounds();
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setTransducer(Transducer t)
	{
		this.transducer = t;
	}

	public void addPart(GUIGlass part)
	{
		this.part = part;
	}

	public void addNextComponent(GuiComponent nextComponent)
	{
		this.nextComponent = nextComponent;
	}
	
	public void setParent(DisplayPanel panel)
	{
		parent = panel;
	}

	/**
	 * Called every time the gui Timer is fired. Updates the Component graphically
	 * according to its function. Subclasses must implement this method with their
	 * own animations.
	 */
	@Override
	public abstract void actionPerformed(ActionEvent ae);

	/**
	 * Called every time the transducer is fired. Corresponds to an Agent command,
	 * to which the component should respond.
	 */
	@Override
	public abstract void eventFired(TChannel channel, TEvent event, Object[] args);
}
