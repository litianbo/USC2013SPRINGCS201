
package gui.components;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.ImageIcon;

import transducer.TChannel;
import transducer.TEvent;

/**
 * Class to represent the robot that moves the part around once its on the conveyor
 */
@SuppressWarnings("serial")
public class GUIPartMover extends GuiComponent implements Serializable
{
	/**
	 * The time it takes to transition the alpha
	 */
	float transitionSpeed;

	/**
	 * Boolean to tell whether or not the robot is transitioning in
	 */
	boolean transitioningIn;

	/**
	 * Boolean to tell whether or not the robot is transitioning out
	 */
	boolean transitioningOut;

	/**
	 * Public constructor for GUIPartMover
	 */
	public GUIPartMover()
	{
		setIcon(new ImageIcon("imageicons/partMoverRobotImage.png"));
		transitionSpeed = 0.1f;
		transitioningIn = false;
		transitioningOut = false;
	}

	/**
	 * Message to make robot to fade in
	 */
	public void msgFadeIn()
	{
		transitioningIn = true;
	}

	/**
	 * Message to make robot to fade out
	 */
	public void msgFadeOut()
	{
		transitioningOut = true;
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
