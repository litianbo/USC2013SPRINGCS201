package gui.panels.subcontrolpanels;

import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import gui.panels.ControlPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import transducer.TChannel;
import transducer.TEvent;

/**
 * The GlassSelectPanel class contains buttons allowing the user to select what
 * type of glass to produce.
 */
@SuppressWarnings("serial")
public class GlassSelectPanel extends JPanel implements ActionListener {
	/** The ControlPanel this is linked to */
	private ControlPanel parent;
	
	private JPanel optionSelectPanel;
	private JPanel buttonPanel;

	/** Checkboxes to select recipe **/
	private JCheckBox cutOption;
	private JCheckBox breakoutOption;
	private JCheckBox manualBreakoutOption;
	private JCheckBox crossseamOption;
	private JCheckBox drillOption;
	private JCheckBox grindOption;
	private JCheckBox washOption;
	private JCheckBox uvOption;
	private JCheckBox bakeOption;
	private JCheckBox paintOption;

	/** Button to place the order **/
	private JButton placeOrder;
	
	Timer buttonTimer = new Timer();

	/**
	 * Creates a new GlassSelect and links it to the control panel
	 * 
	 * @param cp
	 *            the ControlPanel linked to it
	 */
	public GlassSelectPanel(ControlPanel cp) {
		parent = cp;

		initPanel();
	}

	public void initPanel() {
		this.setBackground(Color.black);
		this.setForeground(Color.black);
		
		this.setLayout(new BorderLayout());
		
		optionSelectPanel = new JPanel();
		optionSelectPanel.setLayout(new GridLayout(5,2));
		
		optionSelectPanel.setBackground(Color.black);
		optionSelectPanel.setForeground(Color.black);
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		buttonPanel.setBackground(Color.black);
		buttonPanel.setForeground(Color.black);

		cutOption = new JCheckBox("Cut");
		breakoutOption = new JCheckBox("Breakout");
		manualBreakoutOption = new JCheckBox("Manual Breakout");
		crossseamOption = new JCheckBox("Crossseam");
		drillOption = new JCheckBox("Drill");
		grindOption = new JCheckBox("Grind");
		washOption = new JCheckBox("Wash");
		uvOption = new JCheckBox("UV");
		bakeOption = new JCheckBox("Bake");
		paintOption = new JCheckBox("Paint");

		cutOption.setForeground(Color.white);
		cutOption.setBackground(Color.black);
		breakoutOption.setForeground(Color.white);
		breakoutOption.setBackground(Color.black);
		manualBreakoutOption.setForeground(Color.white);
		manualBreakoutOption.setBackground(Color.black);
		crossseamOption.setForeground(Color.white);
		crossseamOption.setBackground(Color.black);
		drillOption.setForeground(Color.white);
		drillOption.setBackground(Color.black);
		grindOption.setForeground(Color.white);
		grindOption.setBackground(Color.black);
		washOption.setForeground(Color.white);
		washOption.setBackground(Color.black);
		uvOption.setForeground(Color.white);
		uvOption.setBackground(Color.black);
		bakeOption.setForeground(Color.white);
		bakeOption.setBackground(Color.black);
		paintOption.setForeground(Color.white);
		paintOption.setBackground(Color.black);

		optionSelectPanel.add(cutOption);
		optionSelectPanel.add(breakoutOption);
		optionSelectPanel.add(manualBreakoutOption);
		optionSelectPanel.add(crossseamOption);
		optionSelectPanel.add(drillOption);
		optionSelectPanel.add(grindOption);
		optionSelectPanel.add(washOption);
		optionSelectPanel.add(uvOption);
		optionSelectPanel.add(bakeOption);
		optionSelectPanel.add(paintOption);
		
		this.add(optionSelectPanel, BorderLayout.CENTER);

		placeOrder = new JButton("Place Order");
//		placeOrder.setMinimumSize(new Dimension(5, 25));
//		placeOrder.setPreferredSize(new Dimension(5, 25));
//		placeOrder.setMaximumSize(new Dimension(5, 25));
		placeOrder.addActionListener(this);

		buttonPanel.add(placeOrder);
		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Returns the parent panel
	 * 
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent() {
		return parent;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(placeOrder)) {
			createNewGlass();

			// Unselect all checkboxes
			cutOption.setSelected(false);
			breakoutOption.setSelected(false);
			manualBreakoutOption.setSelected(false);
			crossseamOption.setSelected(false);
			drillOption.setSelected(false);
			grindOption.setSelected(false);
			washOption.setSelected(false);
			uvOption.setSelected(false);
			bakeOption.setSelected(false);
			paintOption.setSelected(false);

			placeOrder.setEnabled(false);
		}
	}
	
	public void createNewGlass() {
		Object[] args = new Object[1];
		
		// Create a recipe for a new piece of glass
		Recipe recipe = new Recipe(manualBreakoutOption.isSelected(),
				breakoutOption.isSelected(), crossseamOption.isSelected(),
				cutOption.isSelected(), drillOption.isSelected(),
				grindOption.isSelected(), bakeOption.isSelected(),
				paintOption.isSelected(), uvOption.isSelected(),
				washOption.isSelected());

		// Create a new piece of glass with the above recipe
		Glass glass = new Glass(recipe);

		args[0] = glass;
		
		// Tell the first conveyor family that a new piece of glass has been
		// created
		parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,
				TEvent.SET_RECIPE, args);
		parent.getTransducer().fireEvent(TChannel.BIN,
				TEvent.BIN_CREATE_PART, null);
	}
	
	public void reenableOrderButton() {
		placeOrder.setEnabled(true);
	}
}
