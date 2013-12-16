
package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The TitlePanel holds a title "Factory Control"
 */
@SuppressWarnings("serial")
public class TitlePanel extends JPanel
{
	/** The control panel this is linked to */
	ControlPanel parent;

	/** JLabel title */
	JLabel titleLabel = new JLabel("FACTORY CONTROL");

	/**
	 * Creates a new TitlePanel and links the control panel to it
	 * @param cPanel
	 *        the ControlPanel linked to it
	 */
	public TitlePanel(ControlPanel cPanel)
	{
		parent = cPanel;

		this.setBackground(Color.black);
		this.setForeground(Color.black);

		setPreferredSize(new Dimension(320, 46));
		setMinimumSize(new Dimension(320, 46));
		setMaximumSize(new Dimension(320, 46));

		titleLabel.setForeground(Color.white);
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 32));
		titleLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(titleLabel);
	}

	/**
	 * Creates a new TitlePanel with a chosen text
	 */
	public TitlePanel(ControlPanel cPanel, String title)
	{
		this(cPanel);
		titleLabel = new JLabel(title);
	}

	/**
	 * Sets the title text
	 */
	public void setText(String s)
	{
		titleLabel.setText(s);
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
