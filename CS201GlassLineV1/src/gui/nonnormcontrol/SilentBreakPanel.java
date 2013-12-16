package gui.nonnormcontrol;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class SilentBreakPanel extends JPanel implements ActionListener, TReceiver{
	private Transducer myTransducer;
	private JLabel drillSilentBreakLabel = new JLabel("Drill Silent Break:");
	private JLabel crossseamerSilentBreakLabel = new JLabel("Cross-Seamer Silent Break:");
	private JLabel grinderSilentBreakLabel = new JLabel("Grinder Silent Break:");
	private JLabel drillClearGlassLabel = new JLabel("Break glass on Drill:");
	private JLabel crossSeamerClearGlassLabel = new JLabel("Break glass on Cross-Seamer:");
	private JLabel grinderClearGlassLabel = new JLabel("Break glass on Grinder:");
    private JCheckBox drillTopSilentBreak = new JCheckBox("Top", false);
    private JCheckBox drillBottomSilentBreak = new JCheckBox("Bottom", false); 
    private JCheckBox crossSeamerTopSilentBreak = new JCheckBox("Top", false); 
    private JCheckBox crossSeamerBottomSilentBreak = new JCheckBox("Bottom", false); 
    private JCheckBox grinderTopSilentBreak = new JCheckBox("Top", false); 
    private JCheckBox grinderBottomSilentBreak = new JCheckBox("Bottom", false);
    private JButton drillTopClearGlass = new JButton("Top");
    private JButton drillBottomClearGlass = new JButton("Bottom ");
    private JButton crossSeamerTopClearGlass = new JButton("Top");
    private JButton crossSeamerBottomClearGlass = new JButton("Bottom");
    private JButton grinderTopClearGlass = new JButton("Top");
    private JButton grinderBottomClearGlass = new JButton("Bottom");

	
	public SilentBreakPanel(){
		this.setBackground(Color.black);
		this.setForeground(Color.black);
		
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gblWorkStationSpeedPanel = new GridBagLayout();
		setLayout(gblWorkStationSpeedPanel);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		drillSilentBreakLabel.setForeground(Color.white);
		add(drillSilentBreakLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		drillTopSilentBreak.setForeground(Color.white);
		drillTopSilentBreak.setBackground(Color.black);
		add(drillTopSilentBreak, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 0;
		drillBottomSilentBreak.setForeground(Color.white);
		drillBottomSilentBreak.setBackground(Color.black);
		add(drillBottomSilentBreak, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		grinderSilentBreakLabel.setForeground(Color.white);
		grinderSilentBreakLabel.setBackground(Color.white);
		add(grinderSilentBreakLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		grinderTopSilentBreak.setForeground(Color.white);
		grinderTopSilentBreak.setBackground(Color.black);
		add(grinderTopSilentBreak, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		grinderBottomSilentBreak.setForeground(Color.white);
		grinderBottomSilentBreak.setBackground(Color.black);
		add(grinderBottomSilentBreak, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.LINE_START;
		crossseamerSilentBreakLabel.setForeground(Color.white);
		add(crossseamerSilentBreakLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 4;
		crossSeamerTopSilentBreak.setForeground(Color.white);
		crossSeamerTopSilentBreak.setBackground(Color.black);
		add(crossSeamerTopSilentBreak, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 4;
		crossSeamerBottomSilentBreak.setForeground(Color.white);
		crossSeamerBottomSilentBreak.setBackground(Color.black);
		add(crossSeamerBottomSilentBreak, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 8;
		drillClearGlassLabel.setForeground(Color.white);
		drillClearGlassLabel.setBackground(Color.black);
		add(drillClearGlassLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 8;
		drillTopClearGlass.setForeground(Color.white);
		drillTopClearGlass.setBackground(Color.black);
		add(drillTopClearGlass, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 8;
		drillBottomClearGlass.setForeground(Color.white);
		drillBottomClearGlass.setBackground(Color.black);
		add(drillBottomClearGlass, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 10;
		crossSeamerClearGlassLabel.setForeground(Color.white);
		crossSeamerClearGlassLabel.setBackground(Color.black);
		add(crossSeamerClearGlassLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 10;
		crossSeamerTopClearGlass.setForeground(Color.white);
		crossSeamerTopClearGlass.setBackground(Color.black);
		add(crossSeamerTopClearGlass, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 10;
		crossSeamerBottomClearGlass.setForeground(Color.white);
		crossSeamerBottomClearGlass.setBackground(Color.black);
		add(crossSeamerBottomClearGlass, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 12;
		grinderClearGlassLabel.setForeground(Color.white);
		grinderClearGlassLabel.setBackground(Color.black);
		add(grinderClearGlassLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 12;
		grinderTopClearGlass.setForeground(Color.white);
		grinderTopClearGlass.setBackground(Color.black);
		add(grinderTopClearGlass, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 12;
		grinderBottomClearGlass.setForeground(Color.white);
		grinderBottomClearGlass.setBackground(Color.black);
		add(grinderBottomClearGlass, gbc);
		
		
		drillTopSilentBreak.addActionListener(this);
		drillBottomSilentBreak.addActionListener(this);
		crossSeamerTopSilentBreak.addActionListener(this);
		crossSeamerBottomSilentBreak.addActionListener(this);
		grinderTopSilentBreak.addActionListener(this);
		grinderBottomSilentBreak.addActionListener(this);
	    drillTopClearGlass.addActionListener(this);
	    drillBottomClearGlass.addActionListener(this);
	    crossSeamerTopClearGlass.addActionListener(this);
	    crossSeamerBottomClearGlass.addActionListener(this);
	    grinderTopClearGlass.addActionListener(this);
	    grinderBottomClearGlass.addActionListener(this);

	}
	
	public void setMyTransducer(Transducer transducer) {
		myTransducer = transducer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Integer[] args = new Integer[1];
		if (e.getSource() == drillTopSilentBreak){
			args[0] = 0;
			if (drillTopSilentBreak.isSelected()){
				myTransducer.fireEvent(TChannel.DRILL, TEvent.SILENT_BREAK, args);
				System.out.println("drill top silent break");
			}
			else{
				myTransducer.fireEvent(TChannel.DRILL, TEvent.SILENT_FIX, args);
				System.out.println("drill top silent fix");
			}
		}
		else if (e.getSource() == drillBottomSilentBreak){
			args[0] = 1;
			if (drillBottomSilentBreak.isSelected()){
				myTransducer.fireEvent(TChannel.DRILL, TEvent.SILENT_BREAK, args);
				System.out.println("drill bottom silent break");
			}
			else{
				myTransducer.fireEvent(TChannel.DRILL, TEvent.SILENT_FIX, args);
				System.out.println("drill bottom silent fix");
			}
		}
		else if (e.getSource() == crossSeamerTopSilentBreak){
			args[0] = 0;
			if (crossSeamerTopSilentBreak.isSelected()){
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.SILENT_BREAK, args);
				System.out.println("crossseamer top silent break");
			}
			else{
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.SILENT_FIX, args);
				System.out.println("crossseamer top silent fix");
			}
		}
		else if (e.getSource() == crossSeamerBottomSilentBreak){
			args[0] = 1;
			if (crossSeamerBottomSilentBreak.isSelected()){
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.SILENT_BREAK, args);
				System.out.println("crossseamer bottom silent break");
			}
			else{
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.SILENT_FIX, args);
				System.out.println("crossseamer bottom silent fix");
			}
		}
		else if (e.getSource() == grinderTopSilentBreak){
			args[0] = 0;
			if (grinderTopSilentBreak.isSelected()){
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.SILENT_BREAK, args);
				System.out.println("grinder top silent break");
			}
			else{
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.SILENT_FIX, args);
				System.out.println("grinder top silent fix");
			}
		}
		else if (e.getSource() == grinderBottomSilentBreak){
			args[0] = 1;
			if (grinderBottomSilentBreak.isSelected()){
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.SILENT_BREAK, args);
				System.out.println("grinder bottom silent break");
			}
			else{
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.SILENT_FIX, args);
				System.out.println("grinder bottom silent fix");
			}
		}
		else if (e.getSource() == drillTopClearGlass){
			args[0] = 0;
			myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_BREAK_GLASS, args);
			System.out.println("drill top remove glass");
		}
		else if (e.getSource() == drillBottomClearGlass){
			args[0] = 1;
			myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_BREAK_GLASS, args);
			System.out.println("drill bottom remove glass");
		}
		else if (e.getSource() == crossSeamerTopClearGlass){
			args[0] = 0;
			myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_BREAK_GLASS, args);
			System.out.println("cross-seamer top remove glass");
		}
		else if (e.getSource() == crossSeamerBottomClearGlass){
			args[0] = 1;
			myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_BREAK_GLASS, args);
			System.out.println("cross-seamer bottom remove glass");
		}
		else if (e.getSource() == grinderTopClearGlass){
			args[0] = 0;
			myTransducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_BREAK_GLASS, args);
			System.out.println("grinder top remove glass");
		}
		else if (e.getSource() == grinderBottomClearGlass){
			args[0] = 1;
			myTransducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_BREAK_GLASS, args);
			System.out.println("grinder bottom remove glass");
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
	}

}