
package gui.components;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.ImageIcon;

import shared.enums.ConveyorDirections;
import transducer.TChannel;
import transducer.TEvent;

/**
 * A graphical representation of a dynamic arrow on a GUIConveyor, indicating direction of movement
 */
@SuppressWarnings("serial")
public class GUIConveyorArrow extends GuiComponent implements Serializable
{
	/**
	 * A number representing the arrow's block on the conveyor
	 */
	int posInLine;

	/** the direction of the conveyor, determines image icons */
	public ConveyorDirections direction;

	/**
	 * because GUIConveyors can be created before they are given a location, this is checked to see if the
	 * GUIConveyorArrow needs to be given a location
	 */
	public boolean hasBeenPlaced = false;

	/**
	 * Constructor which chooses the appropriate ImageIcon based on the direction parameter
	 * 
	 * @param dir
	 *        conveyor direction
	 * @param pos
	 *        position in line on the conveyor
	 */
	public GUIConveyorArrow(ConveyorDirections dir, int pos)
	{
		this.direction = dir;
		posInLine = pos;

		if (direction == ConveyorDirections.UP)
		{
			setIcon(new ImageIcon("imageicons/arrowLaneArrow_up.png"));
		}
		else if (direction == ConveyorDirections.DOWN)
		{
			setIcon(new ImageIcon("imageicons/arrowLaneArrow_down.png"));
		}
		else if (direction == ConveyorDirections.LEFT)
		{
			setIcon(new ImageIcon("imageicons/arrowLaneArrow_left.png"));
		}
		else if (direction == ConveyorDirections.RIGHT)
		{
			setIcon(new ImageIcon("imageicons/arrowLaneArrow_right.png"));
		}

	}

	/**
	 * Function to get the arrow's position on the conveyor
	 * @return The part's position in line
	 */
	public int getPosInLine()
	{
		return posInLine;
	}

	/**
	 * Sets the arrow's position in line
	 * @param newpos
	 *        The part's new position in line
	 */
	public void setPosInLine(int newpos)
	{
		posInLine = newpos;
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		repaint();
	}

	public void paint(Graphics g)
	{
		super.paint(g);
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
	
	}
}
