
package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The logo panel is a place holder
 */
@SuppressWarnings("serial")
public class LogoPanel extends JPanel
{
	/** The control panel this is linked to */
	ControlPanel parent;

	public LogoPanel(ControlPanel cp)
	{
		parent = cp;

		JLabel logoLabel = new JLabel();
		ImageIcon logoIcon = new ImageIcon("imageicons/logoPanel.png");
		logoLabel.setIcon(logoIcon);

		this.setBackground(Color.black);
		this.setForeground(Color.black);
		this.setPreferredSize(new Dimension(logoIcon.getIconWidth(), logoIcon.getIconHeight()));

		this.add(logoLabel);
	}
}
