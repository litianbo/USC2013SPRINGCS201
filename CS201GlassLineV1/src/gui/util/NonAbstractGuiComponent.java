
package gui.util;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import transducer.TChannel;
import transducer.TEvent;

import gui.components.GuiComponent;

@SuppressWarnings("serial")
public class NonAbstractGuiComponent extends GuiComponent
{

	/** a constructor for setting image Icon */
	public NonAbstractGuiComponent(ImageIcon icon)
	{
		super(icon);
	}

	@Override
	public void actionPerformed(ActionEvent ae)
	{

	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		
	}
}
