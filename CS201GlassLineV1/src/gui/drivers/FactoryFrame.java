
package gui.drivers;

import gui.panels.FactoryPanel;

import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * The FactoryFrame is the highest level GUI class in the Factory Project. It is
 * responsible only for managing monitor display and initializing the panels.
 */
@SuppressWarnings("serial")
public class FactoryFrame extends JFrame
{
	public static final int WINDOW_WIDTH = 1610;

	public static final int WINDOW_HEIGHT = 900;

	/**
	 * The window title
	 */
	public static final String WINDOW_TITLE = "Glass Line - Team 18";

	/**
	 * The default frames per second. The speed slider will start at (the
	 * closest value to) this value.
	 */
	public static final int DEFAULT_FRAMES_PER_SECOND = 24;

	/**
	 * The universal timer that synchronizes GUI events
	 */
	Timer guiTimer = new Timer(1000 / DEFAULT_FRAMES_PER_SECOND, null);

	/**
	 * The panel that contains everything in the factory
	 */
	private JPanel topPanel;

	/**
	 * The panel that actually runs the factory
	 */
	private FactoryPanel fPanel;

	/**
	 * Creates a new FactoryFrame, initializes the parts, and
	 * displays both panels
	 */
	public FactoryFrame()
	{
		super(WINDOW_TITLE);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		// make top container panel
		topPanel = new JPanel();
		topPanel.setLayout(new CardLayout());

		// add top panel to frame
		this.add(topPanel);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBackground(Color.black);
		this.setForeground(Color.black);

		// start the timer to update GUI objects
		guiTimer.setDelay(1000 / DEFAULT_FRAMES_PER_SECOND);
		guiTimer.start();

		// welcome
		switchToFactoryPanel();
	}

	/**
	 * Displays the frame
	 */
	public void showFrame()
	{
		this.setVisible(true);
	}

	/**
	 * Returns the universal gui timer
	 */
	public Timer getTimer()
	{
		return guiTimer;
	}

	/**
	 * Sets the time timestep
	 */
	public void setTimerDelay(int timestep)
	{
		guiTimer.setDelay(timestep);
	}

	/**
	 * Switches to the factory running panel, keeps same display panel
	 */
	public void switchToFactoryPanel()
	{
		fPanel = new FactoryPanel(this);
		topPanel.add(fPanel, "Factory");
		((CardLayout)topPanel.getLayout()).show(topPanel, "Factory");
	}
}
