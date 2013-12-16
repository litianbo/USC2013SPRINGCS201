
package gui.panels.subcontrolpanels;

import gui.nonnormcontrol.OtherBreakPanel;
import gui.nonnormcontrol.SilentBreakPanel;
import gui.nonnormcontrol.WorkstationBreakPanel;
import gui.nonnormcontrol.WorkstationSpeedPanel;
import gui.panels.ControlPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import transducer.Transducer;

/**
 * The NonNormPanel is responsible for initiating and managing non-normative
 * situations. It contains buttons for each possible non-norm.
 */
@SuppressWarnings("serial")
public class NonNormPanel extends JPanel
{
	/** The control panel this is linked to */
	ControlPanel parent;

	/** Title label **/
	JLabel titleLabel;
	
	Transducer transducer;
	
	public final static Dimension nonNormPanelSize = new Dimension(400, 350);
	public final static Dimension nonNormSelectSize = new Dimension(390, 330);
	
	private JTabbedPane nonNormSelectPane;
	
	// TODO: Declare non-norm panels here
	private WorkstationSpeedPanel workstationSpeedPanel;
	private WorkstationBreakPanel workstationBreakPanel;
	private OtherBreakPanel otherBreakPanel;
	private SilentBreakPanel silentBreakPanel;

	/**
	 * Creates a new HavocPanel and links the control panel to it
	 * 
	 * @param cp
	 *        the ControlPanel linked to it
	 */
	public NonNormPanel(ControlPanel cp)
	{
		this.parent = cp;
		
		this.setMaximumSize(nonNormPanelSize);
		this.setPreferredSize(nonNormPanelSize);
		
		this.setBackground(Color.black);
		this.setForeground(Color.black);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		titleLabel = new JLabel("NON NORMATIVES");
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
		JPanel titleLabelPanel = new JPanel();
		titleLabelPanel.add(titleLabel);
		//titleLabelPanel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		titleLabelPanel.setBackground(Color.black);
		
		UIManager.put("TabbedPane.selected", Color.gray);
				
		nonNormSelectPane = new JTabbedPane(JTabbedPane.TOP);
		nonNormSelectPane.setMaximumSize(nonNormSelectSize);
		nonNormSelectPane.setPreferredSize(nonNormSelectSize);
		
		workstationSpeedPanel = new WorkstationSpeedPanel();
		workstationBreakPanel = new WorkstationBreakPanel();
		otherBreakPanel = new OtherBreakPanel();
		silentBreakPanel = new SilentBreakPanel();
		
		initTabs();
		
		this.add(titleLabelPanel);
		this.add(nonNormSelectPane);
	}
	
	public void initTabs()
	{
		// TODO: Add panels to the tabbed pane like this:
		nonNormSelectPane.add("Online", workstationBreakPanel);
		nonNormSelectPane.add("Offline", workstationSpeedPanel);
		nonNormSelectPane.add("Conveyor/Popup", otherBreakPanel);
		nonNormSelectPane.add("Other", silentBreakPanel);
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
	
	public void setAllTransducer(Transducer transducer)
	{
		workstationSpeedPanel.setMyTransducer(transducer);
		workstationBreakPanel.setMyTransducer(transducer);
		otherBreakPanel.setMyTransducer(transducer);
		silentBreakPanel.setMyTransducer(transducer);
	}

}
