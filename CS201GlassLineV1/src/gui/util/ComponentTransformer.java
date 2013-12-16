
package gui.util;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * Container which can transform its children, for example:
 * 
 * JButton button = new JButton(&quot;Button&quot;);
 * RotationTransformer t = new RotationTransformer(button);
 * t.rotate(Math.PI / 2);
 * 
 * Note:
 * This component was designed to transform simple components
 * like JButton, JLabel etc.
 */
@SuppressWarnings("serial")
public class ComponentTransformer extends JPanel
{
	/**
	 * this is glass panel taking care of transforming click so that transformed label takes clicks on it not on the
	 * previous location
	 */
	private Component glassPane = new MagicGlassPane();

	/** Component that is rotating inside the panel */
	private Component view;

	/** Visible rectangle of the panel */
	private Rectangle visibleRect;

	/** setting rendering hints for the graphics */
	private Map<?, ?> renderingHints;

	/** class that represent the data which is linearly transformed */
	private AffineTransform at;

	/**
	 * this point is used to make sure when transformation happens object will do transformation according to its center
	 */
	private Point preTransformedCenter = null;

	/** Angle of the view */
	private double viewAngle = 0;

	/**
	 * Default constructor it creates the panel for transformation but there is no component inside the panel
	 */
	public ComponentTransformer()
	{
		this(null);
	}

	/**
	 * runs other constructor with adding view to it
	 * @param view
	 *        is the component which will get transformed inside the panel
	 */
	public ComponentTransformer(JComponent view)
	{
		this(view, new AffineTransform());
	}

	/**
	 * This constructor gets the component and transform if user wants to pass his own transform object
	 * otherwise it creates a new instance of affine transform
	 * @param view
	 * @param at
	 */
	public ComponentTransformer(JComponent view, AffineTransform at)
	{
		super(null);
		this.setOpaque(false);
		setTransform(at);
		super.addImpl(glassPane, null, 0);
		setView(view);
		Handler handler = new Handler();
		addHierarchyBoundsListener(handler);
		addComponentListener(handler);
	}

	/**
	 * 
	 * @return the component inside the panel
	 */
	public Component getView()
	{
		return view;
	}

	/**
	 * sets the view and removes the old view then if view is not
	 * @param view
	 */
	public void setView(Component view)
	{
		if (getView() != null)
		{
			super.remove(getView());
		}
		if (view != null)
		{
			super.addImpl(view, null, 1);
		}
		this.view = view;
		doLayout();
		revalidate();
		repaint();
	}

	/**
	 * overriding the function from the JPanel in case anybody uses it
	 */
	protected void addImpl(Component comp, Object constraints, int index)
	{
		setView(comp);
	}

	/**
	 * 
	 * @return map for rendering hints
	 */
	public Map<?, ?> getRenderingHints()
	{
		if (renderingHints == null)
		{
			return null;
		}
		return new HashMap<Object, Object>(renderingHints);
	}

	/**
	 * 
	 * @param renderingHints
	 */
	public void setRenderingHints(Map<?, ?> renderingHints)
	{
		if (renderingHints == null)
		{
			this.renderingHints = null;
		}
		else
		{
			this.renderingHints = new HashMap<Object, Object>(renderingHints);
		}
		repaint();
	}

	/**
	 * removes the view from the panel
	 */
	public void remove(int index)
	{
		if (view != null)
		{
			super.remove(index);
			view = null;
		}
		else
		{
			throw new AssertionError("No component is inside the panel");
		}
	}

	/**
	 * Overriding the function from the JPanel so it would never set optimized drawing
	 */
	public boolean isOptimizedDrawingEnabled()
	{
		return false;
	}

	/**
	 * makes sure that layout for the panel is always null layout
	 */
	public void setLayout(LayoutManager mgr)
	{
		if (mgr != null)
		{
			throw new IllegalArgumentException("Only null layout is supported");
		}
		super.setLayout(mgr);
	}

	/**
	 * sets the location of the glass panel that takes care of click transformation to 0,0 in the main panel
	 * it also sets the location of the view in the visible triangle of the panel so view is always visible
	 * it also sets the size of the panel to be the same as the view so that it would take least amount of space
	 */
	public void doLayout()
	{
		// double newSize =
		// Math.sqrt(Math.pow(this.getTransformedSize().getSize().getHeight(),2)+Math.pow(this.getTransformedSize().getSize().getHeight(),2));
		if (view != null)
		{
			view.setSize(view.getPreferredSize());
			this.setSize(getTransformedSize().getSize());
			visibleRect = getVisibleRect();
			view.setLocation(visibleRect.x, visibleRect.y);
		}
		glassPane.setLocation(0, 0);
		glassPane.setSize(getWidth(), getHeight());
	}

	/**
	 * returns the new size of the transformed component it is the rectangle that holds the transformed component in it
	 * it make sure that the boundaries are taken into account
	 */
	public Dimension getPreferredSize()
	{
		if (isPreferredSizeSet())
		{
			return super.getPreferredSize();
		}
		Dimension size = getTransformedSize().getSize();
		Insets insets = getInsets();
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		return size;
	}

	/**
	 * 
	 * @return the size of the triangle that holds the transformed graphics of the view
	 */
	private Rectangle getTransformedSize()
	{
		if (view != null)
		{
			Dimension viewSize = view.getSize();
			Rectangle viewRect = new Rectangle(viewSize);
			return at.createTransformedShape(viewRect).getBounds();
		}
		return new Rectangle(super.getPreferredSize());
	}

	/**
	 * overriding the paint method so it would draw the transformed graphics instead of original graphics of the
	 * component;
	 */
	public void paint(Graphics g)
	{
		// repaint the whole transformer in case the view component was repainted
		Rectangle clipBounds = g.getClipBounds();
		if (clipBounds != null && !clipBounds.equals(visibleRect))
		{
			repaint();
		}

		adjustLocation();

		// makes sure the view exists and ints transforms is valid then paints them in the panel
		if (view != null && at.getDeterminant() != 0)
		{
			// gets the current graphics of the panel which view is included in it
			Graphics2D g2 = (Graphics2D)g.create();
			Insets insets = getInsets();
			Rectangle bounds = getBounds();

			// don't forget about insets
			bounds.x += insets.left;
			bounds.y += insets.top;
			bounds.width -= insets.left + insets.right;
			bounds.height -= insets.top + insets.bottom;
			double centerX1 = bounds.getCenterX();
			double centerY1 = bounds.getCenterY();

			Rectangle tb = getTransformedSize();
			double centerX2 = tb.getCenterX();
			double centerY2 = tb.getCenterY();

			// set antialiasing by default, but it seems like it doesn't do it!!!
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			if (renderingHints != null)
			{
				g2.addRenderingHints(renderingHints);
			}
			// translate the origin to the center of the view component again so transformation happens according to the
			// center
			double tx = centerX1 - centerX2 - getX();
			double ty = centerY1 - centerY2 - getY();
			g2.translate((int)tx, (int)ty);
			// Creates the actual graphics of the transform to paint it (it maps the transformed matrix into actual
			// graphics)
			g2.transform(at);
			view.paint(g2);
			g2.dispose();
		}
		// paint the border
		paintBorder(g);

	}

	/**
	 * this class takes care of transforming mouse events so they happen on the new transformed component
	 * 
	 * @author Sina Rezaimehr
	 * 
	 */
	private class MagicGlassPane extends JPanel
	{

		private Component mouseEnteredComponent;

		private Component mouseDraggedComponent;

		private Component mouseCurrentComponent;

		/**
		 * default constructor
		 * it makes the panel transparent and enables all mouse events on it so that it will receive all mouse events
		 * even if it does not
		 * have listener
		 */
		public MagicGlassPane()
		{
			super(null);
			setOpaque(false);
			enableEvents(AWTEvent.MOUSE_EVENT_MASK);
			enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
			enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
			// make sure the component is not focus traversable
			ToolTipManager.sharedInstance().registerComponent(this);
		}

		/**
		 * gets a mouse event transforms according to current transformation of the return the new event
		 * @param event
		 * @return
		 */
		private MouseEvent transformMouseEvent(MouseEvent event)
		{
			if (event == null)
			{
				throw new IllegalArgumentException("MouseEvent is null");
			}
			// setting the variables of the event that don't change
			MouseEvent newEvent;
			if (event instanceof MouseWheelEvent)
			{
				MouseWheelEvent mouseWheelEvent = (MouseWheelEvent)event;
				newEvent = new MouseWheelEvent(mouseWheelEvent.getComponent(), mouseWheelEvent.getID(),
					mouseWheelEvent.getWhen(), mouseWheelEvent.getModifiers(),
					mouseWheelEvent.getX(), mouseWheelEvent.getY(),
					mouseWheelEvent.getClickCount(), mouseWheelEvent.isPopupTrigger(),
					mouseWheelEvent.getScrollType(), mouseWheelEvent.getScrollAmount(),
					mouseWheelEvent.getWheelRotation());
			}
			else
			{
				newEvent = new MouseEvent(event.getComponent(), event.getID(),
					event.getWhen(), event.getModifiers(),
					event.getX(), event.getY(),
					event.getClickCount(), event.isPopupTrigger(), event.getButton());
			}

			// do the transform only if the view is not null and we have valid transform
			if (view != null && at.getDeterminant() != 0)
			{
				Rectangle viewBounds = getTransformedSize();
				Insets insets = ComponentTransformer.this.getInsets();
				int xgap = (getWidth() - (viewBounds.width + insets.left + insets.right)) / 2;
				int ygap = (getHeight() - (viewBounds.height + insets.top + insets.bottom)) / 2;

				double x = newEvent.getX() + viewBounds.getX() - insets.left;
				double y = newEvent.getY() + viewBounds.getY() - insets.top;
				Point2D p = new Point2D.Double(x - xgap, y - ygap);

				Point2D tp;
				try
				{
					// getting reverse transformation of the mouse click so it work according to orrientation of the
					// view
					tp = at.inverseTransform(p, null);
				}
				catch (NoninvertibleTransformException ex)
				{
					// can't happen, we check it before
					throw new AssertionError("NoninvertibleTransformException");
				}
				// Use transformed coordinates to get the current component
				mouseCurrentComponent =
					SwingUtilities.getDeepestComponentAt(view, (int)tp.getX(), (int)tp.getY());
				if (mouseCurrentComponent == null)
				{
					mouseCurrentComponent = ComponentTransformer.this;
				}
				Component tempComponent = mouseCurrentComponent;
				if (mouseDraggedComponent != null)
				{
					tempComponent = mouseDraggedComponent;
				}

				Point point = SwingUtilities.convertPoint(view, (int)tp.getX(), (int)tp.getY(), tempComponent);
				newEvent.setSource(tempComponent);
				newEvent.translatePoint(point.x - event.getX(), point.y - event.getY());
			}
			return newEvent;
		}

		/**
		 * processing Mouse events transforming them and dispatch new event
		 */
		protected void processMouseEvent(MouseEvent e)
		{
			MouseEvent transformedEvent = transformMouseEvent(e);
			switch (e.getID()) {
				case MouseEvent.MOUSE_ENTERED:
					if (mouseDraggedComponent == null || mouseCurrentComponent == mouseDraggedComponent)
					{
						dispatchMouseEvent(transformedEvent);
					}
					break;
				case MouseEvent.MOUSE_EXITED:
					if (mouseEnteredComponent != null)
					{
						dispatchMouseEvent(createEnterExitEvent(mouseEnteredComponent, MouseEvent.MOUSE_EXITED, e));
						mouseEnteredComponent = null;
					}
					break;
				case MouseEvent.MOUSE_RELEASED:
					if (mouseDraggedComponent != null && e.getButton() == MouseEvent.BUTTON1)
					{
						transformedEvent.setSource(mouseDraggedComponent);
						mouseDraggedComponent = null;
					}
					dispatchMouseEvent(transformedEvent);
					break;
				default:
					dispatchMouseEvent(transformedEvent);
			}
			super.processMouseEvent(e);
		}

		/**
		 * function that dispatches the new created event with transformed elements to all the listeners that listens to
		 * the first created event
		 * @param event
		 */
		private void dispatchMouseEvent(MouseEvent event)
		{
			MouseListener[] mouseListeners =
				event.getComponent().getMouseListeners();
			for (MouseListener listener : mouseListeners)
			{
				// skip all ToolTipManager's related listeners
				if (!listener.getClass().getName().startsWith("javax.swing.ToolTipManager"))
				{
					switch (event.getID()) {
						case MouseEvent.MOUSE_PRESSED:
							listener.mousePressed(event);
							break;
						case MouseEvent.MOUSE_RELEASED:
							listener.mouseReleased(event);
							break;
						case MouseEvent.MOUSE_CLICKED:
							listener.mouseClicked(event);
							break;
						case MouseEvent.MOUSE_EXITED:
							listener.mouseExited(event);
							break;
						case MouseEvent.MOUSE_ENTERED:
							listener.mouseEntered(event);
							break;
						default:
							throw new AssertionError();
					}
				}
			}
		}

		/**
		 * processing Mouse motion events transforming them and dispatch new event
		 */
		protected void processMouseMotionEvent(MouseEvent e)
		{
			MouseEvent transformedEvent = transformMouseEvent(e);
			if (mouseEnteredComponent == null)
			{
				mouseEnteredComponent = mouseCurrentComponent;
			}
			switch (e.getID()) {
				case MouseEvent.MOUSE_MOVED:
					if (mouseCurrentComponent != mouseEnteredComponent)
					{
						dispatchMouseEvent(createEnterExitEvent(mouseEnteredComponent, MouseEvent.MOUSE_EXITED, e));
						dispatchMouseEvent(createEnterExitEvent(mouseCurrentComponent, MouseEvent.MOUSE_ENTERED, e));
					}
					break;
				case MouseEvent.MOUSE_DRAGGED:
					if (mouseDraggedComponent == null)
					{
						mouseDraggedComponent = mouseEnteredComponent;
					}
					if (mouseEnteredComponent == mouseDraggedComponent
						&& mouseCurrentComponent != mouseDraggedComponent)
					{
						dispatchMouseEvent(createEnterExitEvent(mouseDraggedComponent, MouseEvent.MOUSE_EXITED, e));
					}
					else if (mouseEnteredComponent != mouseDraggedComponent
						&& mouseCurrentComponent == mouseDraggedComponent)
					{
						dispatchMouseEvent(createEnterExitEvent(mouseDraggedComponent, MouseEvent.MOUSE_ENTERED, e));
					}
					if (mouseDraggedComponent != null)
					{
						transformedEvent.setSource(mouseDraggedComponent);
					}
					break;
			}
			mouseEnteredComponent = mouseCurrentComponent;
			// dispatch MouseMotionEvent
			MouseMotionListener[] mouseMotionListeners =
				transformedEvent.getComponent().getMouseMotionListeners();
			for (MouseMotionListener listener : mouseMotionListeners)
			{
				// skip all ToolTipManager's related listeners
				if (!listener.getClass().getName().startsWith("javax.swing.ToolTipManager"))
				{
					switch (transformedEvent.getID()) {
						case MouseEvent.MOUSE_MOVED:
							listener.mouseMoved(transformedEvent);
							break;
						case MouseEvent.MOUSE_DRAGGED:
							listener.mouseDragged(transformedEvent);
							break;
						default:
							throw new AssertionError();
					}
				}
			}
			super.processMouseMotionEvent(e);
		}

		/**
		 * processing Mouse wheel events transforming them and dispatch new event
		 */
		protected void processMouseWheelEvent(MouseWheelEvent e)
		{
			MouseWheelEvent transformedEvent = (MouseWheelEvent)transformMouseEvent(e);
			MouseWheelListener[] mouseWheelListeners =
				transformedEvent.getComponent().getMouseWheelListeners();
			for (MouseWheelListener listener : mouseWheelListeners)
			{
				listener.mouseWheelMoved(transformedEvent);
			}
			super.processMouseWheelEvent(e);
		}

		/**
		 * makes sure the tool tip shown is for the view not for the panel
		 */
		public String getToolTipText(MouseEvent event)
		{
			if (mouseEnteredComponent instanceof JComponent)
			{
				return ((JComponent)mouseEnteredComponent).getToolTipText();
			}
			return null;
		}

		/**
		 * 
		 * @param c
		 * @param eventId
		 * @param mouseEvent
		 * @return
		 */
		private MouseEvent createEnterExitEvent(Component c, int eventId, MouseEvent mouseEvent)
		{
			return new MouseEvent(c, eventId, mouseEvent.getWhen(), 0,
				mouseEvent.getX(), mouseEvent.getY(), 0,
				false, MouseEvent.NOBUTTON);
		}

		/**
		 * overriding to string method from object
		 */
		public String toString()
		{
			return "GlassPane";
		}
	}

	/**
	 * This class helps view component to be in the visible area;
	 * this is important when transformer is inside JScrollPane
	 */
	private class Handler extends ComponentAdapter implements HierarchyBoundsListener
	{
		public void componentMoved(ComponentEvent e)
		{
			update();
		}

		public void ancestorMoved(HierarchyEvent e)
		{
			update();
		}

		public void ancestorResized(HierarchyEvent e)
		{
			update();
		}

		private void update()
		{
			if (!getVisibleRect().equals(visibleRect))
			{
				revalidate();
			}
		}
	}

	/**
	 * Never returns null
	 * it just creates a new affinetransform and returns
	 */
	public AffineTransform getTransform()
	{
		return new AffineTransform(at);
	}

	/**
	 * sets the new received transform to the respective field variable so the paint method can use the new transform
	 * and paint
	 * @param at
	 *        new transform for teh view
	 */
	public void setTransform(AffineTransform at)
	{
		if (at == null)
		{
			throw new IllegalArgumentException("AffineTransform is null");
		}
		this.at = new AffineTransform(at);
		// so it wont re-size when I change the graphics
		myRevalidate();
		repaint();

	}

	/**
	 * a validate I made so I can overrride it when ever i need to
	 */
	protected void myRevalidate()
	{
		revalidate();
	}

	/**
	 * creates a new AffineTransform for rotating Theta degrees in Radian
	 * @param theta
	 *        degrees for turning in radians
	 */
	public void rotate(double theta)
	{
		// keep track of the center location so it the transformation wont change the center of the object
		setPreTransformCenter();

		AffineTransform transform = getTransform();
		transform.rotate(theta);
		viewAngle += theta;
		setTransform(transform);

	}

	/**
	 * Creates a new AffineTransform that scales to new dimensions
	 * @param sx
	 *        new x dimension
	 * @param sy
	 *        new y dimension
	 */
	public void scale(double sx, double sy)
	{

		setPreTransformCenter();

		AffineTransform transform = getTransform();
		transform.scale(sx, sy);
		setTransform(transform);
	}

	/**
	 * creates new AffineTransform for shearing to new dimension
	 * @param sx
	 *        x shearing
	 * @param sy
	 *        y shearing
	 */
	public void shear(double sx, double sy)
	{
		// keep track of the center location so it the transformation wont change the center of the object
		setPreTransformCenter();

		AffineTransform transform = getTransform();
		transform.shear(sx, sy);
		setTransform(transform);
	}

	/**
	 * sets the panel in new location so that its center conforms in x and y
	 * @param x
	 *        the new x location for the center
	 * @param y
	 *        the new y location for the center
	 */
	public void setCenterLocation(int x, int y)
	{
		super.setLocation(x - (this.getWidth() / 2), y - (this.getHeight() / 2));
	}

	/**
	 * 
	 * @return the center location of the panel with respect to the parent panel
	 */
	public Point getCenterLocation()
	{
		return new Point((int)(this.getX() + (this.getWidth() / 2)), (int)(this.getY() + (this.getHeight() / 2)));
	}

	/**
	 * gets Transformed bounds in local origin
	 */
	public Polygon getTransformedBounds()
	{
		// makes sure the view exists and ints transforms is valid then paints them in the panel
		if (view != null && at.getDeterminant() != 0)
		{

			Insets insets = getInsets();
			Rectangle bounds = getBounds();

			// don't forget about insets
			bounds.x += insets.left;
			bounds.y += insets.top;
			bounds.width -= insets.left + insets.right;
			bounds.height -= insets.top + insets.bottom;
			double centerX1 = bounds.getCenterX();
			double centerY1 = bounds.getCenterY();

			Rectangle tb = getTransformedSize();
			double centerX2 = tb.getCenterX();
			double centerY2 = tb.getCenterY();

			// translate the origin to the center of the panel so the component will turn out to be in the center of the
			// panel
			double tx = centerX1 - centerX2 - getX();
			double ty = centerY1 - centerY2 - getY();

			Point2D first = new Point((int)(view.getX()), (int)(view.getY()));
			Point2D second = new Point((int)(view.getX() + view.getWidth()), (int)(view.getY()));
			Point2D third = new Point((int)(view.getX() + view.getWidth()), (int)(view.getY() + view.getHeight()));
			Point2D forth = new Point((int)(view.getX()), (int)(view.getY() + view.getHeight()));

			first = at.transform(first, null);
			second = at.transform(second, null);
			third = at.transform(third, null);
			forth = at.transform(forth, null);

			return new Polygon(new int[] { (int)(first.getX() + tx + getX()),
					(int)(second.getX() + tx + getX()),
					(int)(third.getX() + tx + getX()),
					(int)(forth.getX() + tx + getX()) },
				new int[] { (int)(first.getY() + ty + getY()),
						(int)(second.getY() + ty + getY()),
						(int)(third.getY() + ty + getY()),
						(int)(forth.getY() + ty + getY()) },
				4);
		}
		else
			return null;
	}

	/**
	 * 
	 * @return transformed area of the component inside the panels
	 */
	public Area getTransformedArea()
	{

		Polygon polygon = getTransformedBounds();

		Area transformedArea = new Area(polygon);
		return transformedArea;
	}

	/**
	 * @return the viewAngle
	 */
	public double getViewAngle()
	{
		return viewAngle;
	}

	/**
	 * transforms the point according inside the panel
	 * @param point
	 * @return transformed point with respect to the panel origin
	 */
	public Point2D transformPoint(Point2D point)
	{
		// makes sure the view exists and ints transforms is valid then paints them in the panel
		if (view != null && at.getDeterminant() != 0)
		{

			Insets insets = getInsets();
			Rectangle bounds = getBounds();

			// don't forget about insets
			bounds.x += insets.left;
			bounds.y += insets.top;
			bounds.width -= insets.left + insets.right;
			bounds.height -= insets.top + insets.bottom;
			double centerX1 = bounds.getCenterX();
			double centerY1 = bounds.getCenterY();

			Rectangle tb = getTransformedSize();
			double centerX2 = tb.getCenterX();
			double centerY2 = tb.getCenterY();

			// translate the origin to the center of the panel so the component will turn out to be in the center of the
			// panel
			double tx = centerX1 - centerX2 - getX();
			double ty = centerY1 - centerY2 - getY();

			Point2D returnPoint = at.transform(point, null);

			returnPoint = new Point((int)(returnPoint.getX() + tx), (int)(returnPoint.getY() + ty));

			return returnPoint;

		}
		else
			return null;
	}

	/**
	 * transforms a point and gives it location in parent space of the panel
	 * @param point
	 * @return
	 */
	public Point2D transformPointInParentSpace(Point2D point)
	{
		Point temp = (Point)transformPoint(point);
		return new Point((int)(temp.getX() + getX()), (int)(temp.getY() + getY()));
	}

	/**
	 * sets the center location of the panel
	 * @param newLoc
	 */
	public void setCenterLocation(Point newLoc)
	{
		setCenterLocation((int)newLoc.getX(), (int)newLoc.getY());
	}

	/**
	 * keep track of the center location so it the transformation wont change the center of the object
	 */
	protected void setPreTransformCenter()
	{
		preTransformedCenter = getCenterLocation();
	}

	/**
	 * makes sure the component gets transformed with keeping the center of the panel fixed so transformation wont
	 * happen around the top left corner of the panel
	 */
	protected void adjustLocation()
	{
		if (preTransformedCenter != null)
		{
			setCenterLocation((int)(preTransformedCenter.getX()), (int)(preTransformedCenter.getY()));
			preTransformedCenter = null;
		}
	}

}
