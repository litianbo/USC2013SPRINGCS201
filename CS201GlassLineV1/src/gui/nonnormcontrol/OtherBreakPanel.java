package gui.nonnormcontrol;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class OtherBreakPanel extends JPanel implements ActionListener, TReceiver {
	private Transducer myTransducer;
	private JLabel conveyorBreakLabel = new JLabel("Break a Conveyor:");
	private JLabel popupBreakLabel = new JLabel("Break a Popup:");
	private JLabel truckBreakLabel = new JLabel("Break the Truck:");
    private JCheckBox conveyor0Break = new JCheckBox("0", false);
    private JCheckBox conveyor1Break = new JCheckBox("1", false); 
    private JCheckBox conveyor2Break = new JCheckBox("2", false); 
    private JCheckBox conveyor3Break = new JCheckBox("3", false); 
    private JCheckBox conveyor4Break = new JCheckBox("4", false); 
    private JCheckBox conveyor5Break = new JCheckBox("5", false); 
    private JCheckBox conveyor6Break = new JCheckBox("6", false); 
    private JCheckBox conveyor7Break = new JCheckBox("7", false); 
    private JCheckBox conveyor8Break = new JCheckBox("8", false); 
    private JCheckBox conveyor9Break = new JCheckBox("9", false); 
    private JCheckBox conveyor10Break = new JCheckBox("10", false); 
    private JCheckBox conveyor11Break = new JCheckBox("11", false);
    private JCheckBox conveyor12Break = new JCheckBox("12", false); 
    private JCheckBox conveyor13Break = new JCheckBox("13", false); 
    private JCheckBox conveyor14Break = new JCheckBox("14", false); 
    private JCheckBox popup0Break = new JCheckBox("Drill", false);
    private JCheckBox popup1Break = new JCheckBox("Cross-Seamer", false); 
    private JCheckBox popup2Break = new JCheckBox("Grinder", false); 
    private JCheckBox truckBreak = new JCheckBox("Truck Break", false);
	private JPanel conveyorBreakPanel = new JPanel();

	public OtherBreakPanel() {
		this.setBackground(Color.black);
		this.setForeground(Color.black);

		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gblWorkStationSpeedPanel = new GridBagLayout();
		setLayout(gblWorkStationSpeedPanel);
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		conveyorBreakLabel.setForeground(Color.white);
		add(conveyorBreakLabel, gbc);
		
		createConveyorBreakPanel();
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(conveyorBreakPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		add(new JLabel(" "), gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		popupBreakLabel.setForeground(Color.white);
		add(popupBreakLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		popup0Break.setForeground(Color.white);
		popup0Break.setBackground(Color.black);
		add(popup0Break, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		popup1Break.setForeground(Color.white);
		popup1Break.setBackground(Color.black);
		add(popup1Break, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		popup2Break.setForeground(Color.white);
		popup2Break.setBackground(Color.black);
		add(popup2Break, gbc);

		gbc.gridx = 0;
		gbc.gridy = 7;
		add(new JLabel(" "), gbc);

		gbc.gridx = 0;
		gbc.gridy = 8;
		truckBreakLabel.setForeground(Color.white);
		truckBreakLabel.setBackground(Color.white);
		add(truckBreakLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 9;
		truckBreak.setForeground(Color.white);
		truckBreak.setBackground(Color.black);
		add(truckBreak, gbc);
		
		setActionListeners();
	}
	
	public void createConveyorBreakPanel() {
		GridLayout grid = new GridLayout(3, 5);
		conveyorBreakPanel.setLayout(grid);
		conveyorBreakPanel.setBackground(Color.black);
		conveyorBreakPanel.setForeground(Color.black);
		
		conveyor0Break.setForeground(Color.white);
		conveyor0Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor0Break);

		conveyor1Break.setForeground(Color.white);
		conveyor1Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor1Break);

		conveyor2Break.setForeground(Color.white);
		conveyor2Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor2Break);

		conveyor3Break.setForeground(Color.white);
		conveyor3Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor3Break);

		conveyor4Break.setForeground(Color.white);
		conveyor4Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor4Break);

		conveyor5Break.setForeground(Color.white);
		conveyor5Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor5Break);

		conveyor6Break.setForeground(Color.white);
		conveyor6Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor6Break);

		conveyor7Break.setForeground(Color.white);
		conveyor7Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor7Break);

		conveyor8Break.setForeground(Color.white);
		conveyor8Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor8Break);

		conveyor9Break.setForeground(Color.white);
		conveyor9Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor9Break);

		conveyor10Break.setForeground(Color.white);
		conveyor10Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor10Break);

		conveyor11Break.setForeground(Color.white);
		conveyor11Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor11Break);

		conveyor12Break.setForeground(Color.white);
		conveyor12Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor12Break);

		conveyor13Break.setForeground(Color.white);
		conveyor13Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor13Break);

		conveyor14Break.setForeground(Color.white);
		conveyor14Break.setBackground(Color.black);
		conveyorBreakPanel.add(conveyor14Break);
	}
	
	public void setActionListeners() {
		conveyor0Break.addActionListener(this);
		conveyor1Break.addActionListener(this);
		conveyor2Break.addActionListener(this);
		conveyor3Break.addActionListener(this);
		conveyor4Break.addActionListener(this);
		conveyor5Break.addActionListener(this);
		conveyor6Break.addActionListener(this);
		conveyor7Break.addActionListener(this);
		conveyor8Break.addActionListener(this);
		conveyor9Break.addActionListener(this);
		conveyor10Break.addActionListener(this);
		conveyor11Break.addActionListener(this);
		conveyor12Break.addActionListener(this);
		conveyor13Break.addActionListener(this);
		conveyor14Break.addActionListener(this);
		popup0Break.addActionListener(this);
		popup1Break.addActionListener(this);
		popup2Break.addActionListener(this);
		truckBreak.addActionListener(this);
	}
	
	public void setMyTransducer(Transducer transducer) {
		myTransducer = transducer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Integer[] args = new Integer[1];
		if (e.getSource() == conveyor0Break){
			args[0] = 0;
			if (conveyor0Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("0 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("0 fix");
			}
		}
		else if (e.getSource() == conveyor1Break){
			args[0] = 1;
			if (conveyor1Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("1 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("1 fix");
			}
		}
		else if (e.getSource() == conveyor2Break){
			args[0] = 2;
			if (conveyor2Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("2 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("2 fix");
			}
		}
		else if (e.getSource() == conveyor3Break){
			args[0] = 3;
			if (conveyor3Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("3 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("3 fix");
			}
		}
		else if (e.getSource() == conveyor4Break){
			args[0] = 4;
			if (conveyor4Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("4 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("4 fix");
			}
		}
		else if (e.getSource() == conveyor5Break){
			args[0] = 5;
			if (conveyor5Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("5 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("5 fix");
			}
		}
		else if (e.getSource() == conveyor6Break){
			args[0] = 6;
			if (conveyor6Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("6 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("6 fix");
			}
		}
		else if (e.getSource() == conveyor7Break){
			args[0] = 7;
			if (conveyor7Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("7 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("7 fix");
			}
		}
		else if (e.getSource() == conveyor8Break){
			args[0] = 8;
			if (conveyor8Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("8 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("8 fix");
			}
		}
		else if (e.getSource() == conveyor9Break){
			args[0] = 9;
			if (conveyor9Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("9 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("9 fix");
			}
		}
		else if (e.getSource() == conveyor10Break){
			args[0] = 10;
			if (conveyor10Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("10 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("10 fix");
			}
		}
		else if (e.getSource() == conveyor11Break){
			args[0] = 11;
			if (conveyor11Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("11 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("11 fix");
			}
		}
		else if (e.getSource() == conveyor12Break){
			args[0] = 12;
			if (conveyor12Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("12 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("12 fix");
			}
		}
		else if (e.getSource() == conveyor13Break){
			args[0] = 13;
			if (conveyor13Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("13 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("13 fix");
			}
		}
		else if (e.getSource() == conveyor14Break){
			args[0] = 14;
			if (conveyor14Break.isSelected()){
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.BREAK, args);
				System.out.println("14 break");
			}
			else{
				myTransducer.fireEvent(TChannel.CONVEYOR, TEvent.FIX, args);
				System.out.println("14 fix");
			}
		}
		else if (e.getSource() == popup0Break){
			args[0] = 0;
			if (popup0Break.isSelected()){
				myTransducer.fireEvent(TChannel.POPUP, TEvent.BREAK, args);
				System.out.println("popup 0 break");
			}
			else{
				myTransducer.fireEvent(TChannel.POPUP, TEvent.FIX, args);
				System.out.println("popup 0 fix");
			}
		}
		else if (e.getSource() == popup1Break){
			args[0] = 1;
			if (popup1Break.isSelected()){
				myTransducer.fireEvent(TChannel.POPUP, TEvent.BREAK, args);
				System.out.println("popup 1 break");
			}
			else{
				myTransducer.fireEvent(TChannel.POPUP, TEvent.FIX, args);
				System.out.println("popup 1 fix");
			}
		}
		else if (e.getSource() == popup2Break){
			args[0] = 2;
			if (popup2Break.isSelected()){
				myTransducer.fireEvent(TChannel.POPUP, TEvent.BREAK, args);
				System.out.println("popup 2 break");
			}
			else{
				myTransducer.fireEvent(TChannel.POPUP, TEvent.FIX, args);
				System.out.println("popup 2 fix");
			}
		}
		else if (e.getSource() == truckBreak){
			args[0] = 0;
			if (truckBreak.isSelected()){
				myTransducer.fireEvent(TChannel.TRUCK, TEvent.BREAK, args);
				System.out.println("truck break");
			}
			else{
				myTransducer.fireEvent(TChannel.TRUCK, TEvent.FIX, args);
				System.out.println("truck fix");
			}
		}
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
	}

}
