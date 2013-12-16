package gui.nonnormcontrol;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class WorkstationBreakPanel extends JPanel implements ActionListener, TReceiver{
	private Transducer myTransducer;

    private JCheckBox breakout = new JCheckBox("Break Breakout Machine", false);
    private JCheckBox manualBreakout = new JCheckBox("Break Manual Breakout Machine", false);
    private JCheckBox cutter = new JCheckBox("Break Cutter Machine", false);
    private JCheckBox washer = new JCheckBox("Break Washer Machine", false);
    private JCheckBox uvLamp = new JCheckBox("Break UV Lamp Machine", false);
    private JCheckBox oven = new JCheckBox("Break Oven Machine", false);
    private JCheckBox painter = new JCheckBox("Break Painter Machine", false);
	
	public WorkstationBreakPanel(){
		this.setBackground(Color.black);
		this.setForeground(Color.black);
		
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gblWorkStationSpeedPanel = new GridBagLayout();
		setLayout(gblWorkStationSpeedPanel);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_START;

		
		gbc.gridx = 0;
		gbc.gridy = 7;
		add(new JLabel(" "), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 8;
		breakout.setForeground(Color.white);
		breakout.setBackground(Color.black);
		add(breakout, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 9;
		manualBreakout.setForeground(Color.white);
		manualBreakout.setBackground(Color.black);
		add(manualBreakout, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 10;
		cutter.setForeground(Color.white);
		cutter.setBackground(Color.black);
		add(cutter, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 11;
		washer.setForeground(Color.white);
		washer.setBackground(Color.black);
		add(washer, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 12;
		uvLamp.setForeground(Color.white);
		uvLamp.setBackground(Color.black);
		add(uvLamp, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 13;
		oven.setForeground(Color.white);
		oven.setBackground(Color.black);
		add(oven, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 14;
		painter.setForeground(Color.white);
		painter.setBackground(Color.black);
		add(painter, gbc);

		breakout.addActionListener(this);
		manualBreakout.addActionListener(this);
		cutter.addActionListener(this);
		washer.addActionListener(this);
		uvLamp.addActionListener(this);
		oven.addActionListener(this);
		painter.addActionListener(this);
	}
	
	public void setMyTransducer(Transducer transducer){
		myTransducer = transducer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == breakout){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (breakout.isSelected()){
				myTransducer.fireEvent(TChannel.BREAKOUT, TEvent.BREAK, args);
				System.out.println("Breakout break");
			}
			else{
				myTransducer.fireEvent(TChannel.BREAKOUT, TEvent.FIX, args);	
				System.out.println("Breakout fix");
			}
		}
		else if (e.getSource() == manualBreakout){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (manualBreakout.isSelected()){
				myTransducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.BREAK, args);
				System.out.println("Manual Breakout break");
			}
			else{
				myTransducer.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.FIX, args);	
				System.out.println("Manual Breakout fix");
			}
		}
		else if (e.getSource() == cutter){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (cutter.isSelected()){
				myTransducer.fireEvent(TChannel.CUTTER, TEvent.BREAK, args);
				System.out.println("Cutter break");
			}
			else{
				myTransducer.fireEvent(TChannel.CUTTER, TEvent.FIX, args);	
				System.out.println("Cutter fix");
			}
		}
		else if (e.getSource() == washer){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (washer.isSelected()){
				myTransducer.fireEvent(TChannel.WASHER, TEvent.BREAK, args);
				System.out.println("Washer break");
			}
			else{
				myTransducer.fireEvent(TChannel.WASHER, TEvent.FIX, args);	
				System.out.println("Washer fix");
			}
		}
		else if (e.getSource() == uvLamp){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (uvLamp.isSelected()){
				myTransducer.fireEvent(TChannel.UV_LAMP, TEvent.BREAK, args);
				System.out.println("uv break");
			}
			else{
				myTransducer.fireEvent(TChannel.UV_LAMP, TEvent.FIX, args);	
				System.out.println("uv fix");
			}
		}
		else if (e.getSource() == oven){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (oven.isSelected()){
				myTransducer.fireEvent(TChannel.OVEN, TEvent.BREAK, args);
				System.out.println("Oven break");
			}
			else{
				myTransducer.fireEvent(TChannel.OVEN, TEvent.FIX, args);	
				System.out.println("Oven fix");
			}
		}
		else if (e.getSource() == painter){
    		Integer[] args = new Integer[1];
    		args[0] = 0;
			if (painter.isSelected()){
				myTransducer.fireEvent(TChannel.PAINTER, TEvent.BREAK, args);
				System.out.println("Painter break");
			}
			else{
				myTransducer.fireEvent(TChannel.PAINTER, TEvent.FIX, args);	
				System.out.println("Painter fix");
			}
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
	}

}