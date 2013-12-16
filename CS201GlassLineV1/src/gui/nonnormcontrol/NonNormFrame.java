package gui.nonnormcontrol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import transducer.Transducer;

@SuppressWarnings("serial")
public class NonNormFrame extends JFrame{
	// TODO: Feel free to adjust WINDOW_WIDTH and WINDOW_HEIGHT
	private static final int WINDOW_WIDTH = 350;
	private static final int WINDOW_HEIGHT = 400;
	
	private static final String WINDOW_TITLE = "Non-Normative Control";
	
	private JTabbedPane nonNormSelectPane;
	
	// TODO: Declare non-norm panels here
	private WorkstationSpeedPanel workstationSpeedPanel;
	private WorkstationBreakPanel workstationBreakPanel;
	private OtherBreakPanel otherBreakPanel;
	private SilentBreakPanel silentBreakPanel;
	
	public NonNormFrame() {
		super(WINDOW_TITLE);
		this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
				
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBackground(Color.black);
		this.setForeground(Color.black);
		this.setLayout(new FlowLayout());
		
		UIManager.put("TabbedPane.selected", Color.gray);
				
		nonNormSelectPane = new JTabbedPane(JTabbedPane.TOP);
		
		workstationSpeedPanel = new WorkstationSpeedPanel();
		workstationBreakPanel = new WorkstationBreakPanel();
		otherBreakPanel = new OtherBreakPanel();
		silentBreakPanel = new SilentBreakPanel();
		initTabs();
		
		this.add(nonNormSelectPane);
	}
	
	public void initTabs() {
		// TODO: Add panels to the tabbed pane like this:
		nonNormSelectPane.add("Online", workstationBreakPanel);
		nonNormSelectPane.add("Conveyor/Popup", otherBreakPanel);
		nonNormSelectPane.add("Offline", workstationSpeedPanel);
		nonNormSelectPane.add("Other", silentBreakPanel);
		//workstationSpeedPanel.setPreferredSize(new Dimension(350, 400));
	}

	public void showFrame() {
		this.setVisible(true);
	}
	
	public void setAllTransducer(Transducer transducer){
		workstationSpeedPanel.setMyTransducer(transducer);
		workstationBreakPanel.setMyTransducer(transducer);
		otherBreakPanel.setMyTransducer(transducer);
		silentBreakPanel.setMyTransducer(transducer);
	}

}
