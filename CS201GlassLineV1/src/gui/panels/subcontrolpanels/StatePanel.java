
package gui.panels.subcontrolpanels;

import gui.drivers.FactoryFrame;
import gui.panels.ControlPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import transducer.TChannel;
import transducer.TEvent;

/**
 * The StatePanel controls the factory's run state and the speed of the factory.
 * It can be extended to control music, etc.
 */
@SuppressWarnings("serial")
public class StatePanel extends JPanel
{
	/** The lowest value of the speed slider (corresponds to slow factory) */
	public static final int MIN_SLIDER_VALUE = 0;

	/** The highest value of the speed slider (corresponds to fast factory) */
	public static final int MAX_SLIDER_VALUE = 1000;

	/** The slowest fps of the factory */
	public static final int MIN_FRAMES_PER_SECOND = 1;

	/** The fastest fps of the factory */
	public static final int MAX_FRAMES_PER_SECOND = 1000;

	/** The ControlPanel this panel is linked to */
	ControlPanel parent;

	/** Buttons for each of the factory functions */
//	JButton startButton, stopButton;

	/** Slider controls how fast the factory should run */
	JSlider speedSlider;

	/** Debug button for checking the timestep synchronization */
	JButton debugButton;

	/**
	 * Creates a new StatePanel
	 * 
	 * @param cPanel
	 *        ControlPanel to link this panel to
	 * @param intialPause
	 *        whether the initial state of the factory is paused
	 */
	public StatePanel(ControlPanel cPanel, boolean initialPause)
	{
		parent = cPanel;

		this.setBackground(Color.black);
		this.setForeground(Color.black);

		// initialize buttons
		debugButton = new JButton();

//		startButton = new JButton("START");
//		startButton.setBackground(Color.white);
//		startButton.setForeground(Color.black);
//		startButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
//		startButton.setHorizontalAlignment(SwingConstants.CENTER);
//		startButton.setOpaque(true);
//		startButton.setBorderPainted(false);
//
//		stopButton = new JButton("STOP");
//		stopButton.setBackground(Color.white);
//		stopButton.setForeground(Color.black);
//		stopButton.setFont(new Font("SansSerif", Font.PLAIN, 18));
//		stopButton.setHorizontalAlignment(SwingConstants.CENTER);
//		stopButton.setOpaque(true);
//		stopButton.setBorderPainted(false);

		// initialize timer slider
		int initialSliderValue = (MAX_SLIDER_VALUE - MIN_SLIDER_VALUE)
			* (FactoryFrame.DEFAULT_FRAMES_PER_SECOND - MIN_FRAMES_PER_SECOND)
			/ (MAX_FRAMES_PER_SECOND - MIN_FRAMES_PER_SECOND) + MIN_SLIDER_VALUE + 1;

		System.out.println("Initial Speed Slider Value: " + initialSliderValue);
		speedSlider = new JSlider(MIN_SLIDER_VALUE, MAX_SLIDER_VALUE, initialSliderValue);

		try
		{
			parent.getGuiParent().getGuiParent().setTimerDelay(1000 / FactoryFrame.DEFAULT_FRAMES_PER_SECOND);
		}
		catch (NullPointerException npe)
		{
			System.out.println("No timer initialized for Control Panel!");
		}

		speedSlider.setToolTipText("" + FactoryFrame.DEFAULT_FRAMES_PER_SECOND);
		debugButton.setText("" + FactoryFrame.DEFAULT_FRAMES_PER_SECOND);

		// manage layout
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// set up buttons
//		startButton.addActionListener(new StartButtonListener());
//		stopButton.addActionListener(new StopButtonListener());
//		startButton.setEnabled(false);
//		stopButton.setEnabled(false);

		// setup sliders
		speedSlider.addChangeListener(new SpeedSliderListener());
		speedSlider.setSnapToTicks(false);
		speedSlider.setPreferredSize(new Dimension(200, 20));
		speedSlider.setBackground(Color.black);

		// add components
		JPanel stateButtonPanel = new JPanel();
		stateButtonPanel.setBackground(Color.black);
		stateButtonPanel.setPreferredSize(new Dimension(300, 50));
		GridLayout grid = new GridLayout(1, 3);
		grid.setHgap(2);
		stateButtonPanel.setLayout(grid);
//		stateButtonPanel.add(startButton);
//		stateButtonPanel.add(stopButton);

		JPanel stateSliderPanel = new JPanel();
		stateSliderPanel.setPreferredSize(new Dimension(300, 24));
		stateSliderPanel.setBackground(Color.black);
		// stateSliderPanel.setLayout(new GridLayout(1, 3));
		stateSliderPanel.setLayout(new FlowLayout());
		JLabel slow = new JLabel("SLOW");
		slow.setForeground(Color.white);
		slow.setFont(new Font("SansSerif", Font.PLAIN, 12));
		stateSliderPanel.add(slow);
		stateSliderPanel.add(speedSlider);
		JLabel fast = new JLabel("FAST");
		fast.setForeground(Color.white);
		fast.setFont(new Font("SansSerif", Font.PLAIN, 12));
		stateSliderPanel.add(fast);

		JPanel colorLinesPanel = new JPanel();
		colorLinesPanel.setPreferredSize(new Dimension(300, 10));
		colorLinesPanel.setBackground(Color.black);
		ImageIcon cl = new ImageIcon("imageicons/singleColoredLine.png");
		JLabel clLabel = new JLabel(cl);
		colorLinesPanel.add(clLabel);

		// uncomment the following line if you want to see a button flashing your timer speed
		// this.add(debugButton);

		try
		{
			parent.getGuiParent().getGuiParent().getTimer().addActionListener(new DebugButtonListener());
		}
		catch (NullPointerException npe)
		{
			System.out.println("No timer initialized for Control Panel!");
		}

//		this.add(stateButtonPanel);
		this.add(stateSliderPanel);
		this.add(colorLinesPanel);

	}

	/**
	 * Creates a new PausePanel, with pause selected
	 * 
	 * @param cPanel
	 *        the ControlPanel to link this panel to
	 */
	public StatePanel(ControlPanel cPanel)
	{
		this(cPanel, true);
	}

	/**
	 * Returns the parent panel
	 * 
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent()
	{
		return parent;
	}

	/**
	 * @return the startButton
	 */
//	public JButton getStartButton()
//	{
//		return startButton;
//	}

	/**
	 * @return the stopButton
	 */
//	public JButton getStopButton()
//	{
//		return stopButton;
//	}

	/**
	 * Listener class for the debug button NOTE: the button does not actually
	 * get this class. Rather, it is called every timestep to update the button
	 */
	public class DebugButtonListener implements ActionListener
	{
		/**
		 * Invoked every timestep, disables/enables the button like a heartbeat
		 */
		public void actionPerformed(ActionEvent ae)
		{
			if (debugButton.isEnabled())
			{
				debugButton.setEnabled(false);
			}
			else
			{
				debugButton.setEnabled(true);
			}
		}
	}

	/**
	 * Listener class for the start button
	 */
//	public class StartButtonListener implements ActionListener
//	{
//		/**
//		 * Invoked whenever the button is clicked, starts the control cell Note
//		 * that this button is disabled unless a kit config exists and the
//		 * factory is stopped
//		 */
//		public void actionPerformed(ActionEvent ae)
//		{
//			System.out.println("Control Panel START button clicked.");
//			if (parent.getTransducer() == null)
//			{
//				System.out.println("No transducer connected!");
//			}
//			else
//			{
//				parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.START, null);
//
//				startButton.setEnabled(false);
//				stopButton.setEnabled(true);
//			}
//		}
//	}

	/**
	 * Listener class for the stop button
	 */
//	public class StopButtonListener implements ActionListener
//	{
//		/**
//		 * Invoked whenever the button is clicked, starts the control cell Note
//		 * that this button is disabled unless the factory is running
//		 */
//		public void actionPerformed(ActionEvent ae)
//		{
//			System.out.println("Control Panel STOP button clicked.");
//			if (parent.getTransducer() == null)
//			{
//				System.out.println("No transducer connected!");
//			}
//			else
//			{
//				parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL, TEvent.STOP, null);
//
//				startButton.setEnabled(true);
//				stopButton.setEnabled(false);
//			}
//		}
//	}

	/**
	 * Listener class for the speed slider
	 */
	public class SpeedSliderListener implements ChangeListener
	{
		/**
		 * Invoked whenever the timer speed slider is changed, updates the
		 * factory speed. Note that lower on the slider is slower factory
		 */
		public void stateChanged(ChangeEvent ce)
		{
			int newSpeed = (speedSlider.getValue() * (MAX_FRAMES_PER_SECOND - MIN_FRAMES_PER_SECOND)
				/ (MAX_SLIDER_VALUE - MIN_SLIDER_VALUE) + MIN_FRAMES_PER_SECOND);

			// get to the timer, set new speed
			try
			{
				parent.getGuiParent().getGuiParent().setTimerDelay(1000 / newSpeed);
				// System.out.println("Timer delay set to " + (1000 / newSpeed));
			}
			catch (NullPointerException npe)
			{
				System.out.println("No Timer connected!");
			}

			debugButton.setText("" + newSpeed);
			speedSlider.setToolTipText("" + newSpeed);
		}
	}
}
