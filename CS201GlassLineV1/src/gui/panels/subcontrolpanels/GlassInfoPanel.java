
package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import javax.swing.JPanel;

/**
 * The GlassInfoPanel class displays information on glass in production
 */
@SuppressWarnings("serial")
public class GlassInfoPanel extends JPanel
{
	/** The ControlPanel this is linked to */
	private ControlPanel parent;

	/**
	 * Creates a new GlassInfoPanel and links it to the control panel
	 * @param cp
	 *        the ControlPanel linked to it
	 */
	public GlassInfoPanel(ControlPanel cp)
	{
		parent = cp;
	}

	/**
	 * Returns the parent panel
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent()
	{
		return parent;
	}
}
