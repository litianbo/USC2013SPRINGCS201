package gui.nonnormcontrol;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class WorkstationSpeedPanel extends JPanel implements ChangeListener, ActionListener, TReceiver{
	private Transducer myTransducer;
    private JSlider drillSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 1);
    private JSlider crossSeamerSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 1);
    private JSlider grinderSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 1);
    private JLabel drillSpeedLabel = new JLabel("Drill Machine Delay:");
    private JLabel crossseamerSpeedLabel = new JLabel("Cross Seamer Machine Delay:");
    private JLabel grinderSpeedLabel = new JLabel("Grinder Machine Delay:");
    private JLabel drillMachineBreakLabel = new JLabel("Break Drill Machine:");
    private JLabel crossseamerMachineBreakLabel = new JLabel("Break Cross Seamer Machine:");
    private JLabel grinderMachineBreakLabel = new JLabel("Break Grinder Machine:");
    private JCheckBox topDrillMachine = new JCheckBox("Top", false);
    private JCheckBox bottomDrillMachine = new JCheckBox("Bottom", false); 
    private JCheckBox topCrossseamerMachine = new JCheckBox("Top", false);
    private JCheckBox bottomCrossseamerMachine = new JCheckBox("Bottom", false); 
    private JCheckBox topGrinderMachine = new JCheckBox("Top", false);
    private JCheckBox bottomGrinderMachine = new JCheckBox("Bottom", false); 
    
	public WorkstationSpeedPanel(){
		this.setBackground(Color.black);
		this.setForeground(Color.black);
		
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gblWorkStationSpeedPanel = new GridBagLayout();
		setLayout(gblWorkStationSpeedPanel);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		drillSpeedLabel.setForeground(Color.white);
		add(drillSpeedLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		drillSpeedSlider.setBackground(Color.black);
		add(drillSpeedSlider, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		crossseamerSpeedLabel.setForeground(Color.white);
		add(crossseamerSpeedLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 4;
		crossSeamerSpeedSlider.setBackground(Color.black);
		add(crossSeamerSpeedSlider, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 5;
		grinderSpeedLabel.setForeground(Color.white);
		add(grinderSpeedLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 6;
		grinderSpeedSlider.setBackground(Color.black);
		add(grinderSpeedSlider, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.LINE_START;
		drillMachineBreakLabel.setForeground(Color.white);
		add(drillMachineBreakLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 8;
		topDrillMachine.setForeground(Color.white);
		topDrillMachine.setBackground(Color.black);
		add(topDrillMachine, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 8;
		bottomDrillMachine.setForeground(Color.white);
		bottomDrillMachine.setBackground(Color.black);
		add(bottomDrillMachine, gbc);

		gbc.gridx = 0;
		gbc.gridy = 9;
		crossseamerMachineBreakLabel.setForeground(Color.white);
		add(crossseamerMachineBreakLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 10;
		topCrossseamerMachine.setForeground(Color.white);
		topCrossseamerMachine.setBackground(Color.black);
		add(topCrossseamerMachine, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 10;
		bottomCrossseamerMachine.setForeground(Color.white);
		bottomCrossseamerMachine.setBackground(Color.black);
		add(bottomCrossseamerMachine, gbc);

		
		gbc.gridx = 0;
		gbc.gridy = 11;
		grinderMachineBreakLabel.setForeground(Color.white);
		add(grinderMachineBreakLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 12;
		topGrinderMachine.setForeground(Color.white);
		topGrinderMachine.setBackground(Color.black);
		add(topGrinderMachine, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 12;
		bottomGrinderMachine.setForeground(Color.white);
		bottomGrinderMachine.setBackground(Color.black);
		add(bottomGrinderMachine, gbc);
		
		drillSpeedSlider.addChangeListener(this);
		crossSeamerSpeedSlider.addChangeListener(this);
		grinderSpeedSlider.addChangeListener(this);
		topDrillMachine.addActionListener(this);
		bottomDrillMachine.addActionListener(this);
		topCrossseamerMachine.addActionListener(this);
		bottomCrossseamerMachine.addActionListener(this);
		topGrinderMachine.addActionListener(this);
		bottomGrinderMachine.addActionListener(this);
	}
	
	public void setMyTransducer(Transducer transducer){
		myTransducer = transducer;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
        	if (source == drillSpeedSlider){
	    		Integer[] args = new Integer[1];
	    		args[0] = (int)source.getValue();
	            myTransducer.fireEvent(TChannel.DRILL, TEvent.CHANGE_WORKSTATION_SPEED, args);
        	}
        	else if (source == crossSeamerSpeedSlider){
	    		Integer[] args = new Integer[1];
	    		args[0] = (int)source.getValue();
	            myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.CHANGE_WORKSTATION_SPEED, args);
        	}
        	else if (source == grinderSpeedSlider){
	    		Integer[] args = new Integer[1];
	    		args[0] = (int)source.getValue();
	            myTransducer.fireEvent(TChannel.GRINDER, TEvent.CHANGE_WORKSTATION_SPEED, args);
        	}
        }
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == topDrillMachine){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (topDrillMachine.isSelected()){
				myTransducer.fireEvent(TChannel.DRILL, TEvent.BREAK, args);
				System.out.println("Top drill break");
			}
			else{
				myTransducer.fireEvent(TChannel.DRILL, TEvent.FIX, args);
				System.out.println("Top drill fix");
			}
		}
		else if (e.getSource() == bottomDrillMachine){
    		Integer[] args = new Integer[1];
    		args[0] = 1;
			if (bottomDrillMachine.isSelected()){
				myTransducer.fireEvent(TChannel.DRILL, TEvent.BREAK, args);
				System.out.println("Bottom drill break");
			}
			else{
				myTransducer.fireEvent(TChannel.DRILL, TEvent.FIX, args);
				System.out.println("Bottom drill fix");
			}
		}
		else if (e.getSource() == topCrossseamerMachine){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (topCrossseamerMachine.isSelected()){
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.BREAK, args);
				System.out.println("Top crossseam break");
			}
			else{
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.FIX, args);
				System.out.println("Top crossseam fix");
			}
		}
		else if (e.getSource() == bottomCrossseamerMachine){
    		Integer[] args = new Integer[1];
    		args[0] = 1;
			if (bottomCrossseamerMachine.isSelected()){
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.BREAK, args);
				System.out.println("Bottom crossseam break");
			}
			else{
				myTransducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.FIX, args);
				System.out.println("Bottom crossseam fix");
			}
		}
		else if (e.getSource() == topGrinderMachine){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (topGrinderMachine.isSelected()){
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.BREAK, args);
				System.out.println("Top grinder break");
			}
			else{
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.FIX, args);	
				System.out.println("Top grinder fix");
			}
		}
		else if (e.getSource() == bottomGrinderMachine){
    		Integer[] args = new Integer[1];
    		args[0] = 1;
			if (bottomGrinderMachine.isSelected()){
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.BREAK, args);
				System.out.println("Bottom grinder break");
			}
			else{
				myTransducer.fireEvent(TChannel.GRINDER, TEvent.FIX, args);	
				System.out.println("Bottom grinder fix");
			}
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
	}

}
