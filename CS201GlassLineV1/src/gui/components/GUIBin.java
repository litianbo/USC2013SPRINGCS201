
package gui.components;

import gui.panels.DisplayPanel;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * GUIBin is a graphical representation of the bin component
 * that holds the parts delivered by the trucks.
 */
@SuppressWarnings("serial")
public class GUIBin extends GuiComponent
{
	/**
	 * stateEmpty = whether or not the bin is empty
	 */
	public boolean stateEmpty;

	private enum BinState
	{
		CREATING, IDLE
	};

	BinState state = BinState.IDLE;

	Point target;

	/**
	 * Public constructor for GUIBin
	 * @param bin
	 *        reference to the bin that GUIBin is created in
	 */
	public GUIBin(Transducer t)
	{
		super();
		transducer = t;
		setIcon(new ImageIcon("imageicons/binImage.png"));
		transducer.register(this, TChannel.BIN);
		setSize(getIcon().getIconWidth(), getIcon().getIconHeight());
	}

	@Override
	public void addNextComponent(GuiComponent component)
	{
		nextComponent = component;
		target = new Point(nextComponent.getCenterX() + (2 * nextComponent.getIcon().getIconWidth()) / 5,
			nextComponent.getCenterY());
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (part != null)
		{
			movePartOut();
		}
	}

	public void movePartOut()
	{
		if (part.getCenterX() < target.getX())
			part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
		else if (part.getCenterX() > target.getX())
			part.setCenterLocation(part.getCenterX() - 1, part.getCenterY());

		if (part.getCenterY() < target.getY())
			part.setCenterLocation(part.getCenterX(), part.getCenterY() + 1);
		else if (part.getCenterY() > target.getY())
			part.setCenterLocation(part.getCenterX(), part.getCenterY() - 1);

		if (part.getCenterX() == target.getX() && part.getCenterY() == target.getY())
		{
			nextComponent.addPart(part);
			part = null;
			transducer.fireEvent(TChannel.BIN, TEvent.BIN_PART_CREATED, null);
		}
	}

	public void paint(Graphics g)
	{
		super.paint(g);
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if (event == TEvent.BIN_CREATE_PART)
		{
			GUIGlass part = new GUIGlass();
			this.part = part;
			part.setCenterLocation(getCenterX(), getCenterY());
			parent.getActivePieces().add(part);
			parent.getParent().getGuiParent().getTimer().addActionListener(part);
			parent.add(part, DisplayPanel.ROOF_LAYER);
		}
	}
}
