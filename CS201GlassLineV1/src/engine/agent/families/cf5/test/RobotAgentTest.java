package engine.agent.families.cf5.test;

import org.junit.Test;

import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf5.RobotAgent;
import engine.agent.families.cf5.test.mock.EventLog;
import engine.agent.families.cf5.test.mock.LoggedEvent;
import engine.agent.families.cf5.test.mock.MockPopup;

import junit.framework.TestCase;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class RobotAgentTest extends TestCase implements TReceiver {

	/**
	 * This is the PopupAgent to be tested.
	 */
	public RobotAgent robot;
	
	
	public EventLog transducerLog = new EventLog();

	/**
	 * Test method for
	 * {@link factory.RobotAgent#msgRobotPickup(Factory.Glass)}
	 * .
	 * 
	 * This method creates a RobotAgent, a Transducer and a mockPopup. The robot is
	 * messaged that the popup has a glass pane ready to be picked up. The robot's scheduler is
	 * then called. The transducer should receive WORKSTATION_DO_LOAD_GLASS after the
	 * scheduler is called.  The robot receives the fired event WORKSTATION_GUI_ACTION_FINISHED
	 * from transducer saying that the machine has processed the glass pane.  The robot's scheduler is
	 * then called. The popup should receive msgWantToDropOffGlass after the
	 * scheduler is called.  
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgRobotPickup() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe());
		
		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to drill channel
		myTransducer.register(this, TChannel.DRILL);
		
		// Create a PopupAgent
		RobotAgent robot = new RobotAgent("Robot", 0, myTransducer);
		
		// Create a MockPopup
		MockPopup popup = new MockPopup("popup");
		
		robot.setPopup(popup);
		
		// Message the robot that the popup has glass ready for pickup by robot
		robot.msgRobotPickup(testGlass);

		// This will check that you're not messaging the transducer in the
		// robot's message reception.
		assertEquals(
				"Transducer should have an empty event log before the robot's scheduler is called. Instead, the Transducer event log reads: "
						+ transducerLog.toString(), 0, transducerLog.size());
		
		// Call the robot's scheduler
		robot.pickAndExecuteAnAction();

		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ transducerLog.toString(), transducerLog
						.containsString("Received fired event: WORKSTATION_DO_LOAD_GLASS"));
		assertEquals(
				"Only 1 message should have been sent to the transducer. Event log: "
						+ transducerLog.toString(), 1, transducerLog.size());
		transducerLog.clear();
		
		// Send WORKSTATION_GUI_ACTION_FINISHED fired event
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_GUI_ACTION_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// clear transducer log to remove test fired event
		transducerLog.clear();
		
		// This will check that you're not messaging the popup in the
		// robot's fired event method.
		assertEquals(
				"Mock Popup should have an empty event log before the robot's scheduler is called. Instead, the popup event log reads: "
						+ popup.log.toString(), 0, popup.log.size());
		
		// Needed to release the semaphores that was acquired in the last method
		robot.releasePopupSemaphore();
		
		// Call the robot's scheduler
		robot.pickAndExecuteAnAction();
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ popup.log.toString(), popup.log
						.containsString("Received message msgWantToDropOffGlass"));
		assertEquals(
				"Only 1 message should have been sent to the transducer. Event log: "
						+ popup.log.toString(), 1, popup.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.RobotAgent#msgReceiveGlass()}
	 * .
	 * 
	 * This method creates a RobotAgent, a Transducer and a mockPopup. (reuses testMsgRobotPickup to get to this point)  
	 * The robot is messaged that the popup has a glass pane ready to be picked up. The robot's scheduler is
	 * then called. The transducer should receive WORKSTATION_RELEASE_GLASS and the popup should 
	 * receive msgDropOffGlass after the scheduler is called.  
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgReceiveGlass() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe());
		
		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to drill channel
		myTransducer.register(this, TChannel.DRILL);
		
		// Create a PopupAgent
		RobotAgent robot = new RobotAgent("Robot", 0, myTransducer);
		
		// Create a MockPopup
		MockPopup popup = new MockPopup("popup");
		
		robot.setPopup(popup);
		
		// Message the robot that the popup has glass ready for pickup by robot
		robot.msgRobotPickup(testGlass);
		
		// Call the robot's scheduler
		robot.pickAndExecuteAnAction();

		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Send WORKSTATION_GUI_ACTION_FINISHED fired event
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_GUI_ACTION_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Needed to release the semaphores that was acquired in the last method
		robot.releasePopupSemaphore();
		
		// Call the robot's scheduler
		robot.pickAndExecuteAnAction();
		
		// clear logs to remove test fired event and any previous non-testing messages
		transducerLog.clear();
		popup.log.clear();
		
		// Message the robot that the popup is ready to receive glass
		robot.msgReceiveGlass();
		
		// This will check that you're not messaging the transducer in the
		// robot's message reception.
		assertEquals(
				"Transducer should have an empty event log before the robot's scheduler is called. Instead, the Transducer event log reads: "
						+ transducerLog.toString(), 0, transducerLog.size());
		
		// This will check that you're not messaging the popup in the
		// robot's message reception.
		assertEquals(
				"Mock Popup should have an empty event log before the robot's scheduler is called. Instead, the popup event log reads: "
						+ popup.log.toString(), 0, popup.log.size());
		
		// Call the robot's scheduler
		robot.pickAndExecuteAnAction();

		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ transducerLog.toString(), transducerLog
						.containsString("Received fired event: WORKSTATION_RELEASE_GLASS"));
		assertEquals(
				"Only 1 message should have been sent to the transducer. Event log: "
						+ transducerLog.toString(), 1, transducerLog.size());
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received message. Event log: "
						+ popup.log.toString(), popup.log
						.containsString("Received message msgDropOffGlass"));
		assertEquals(
				"Only 1 message should have been sent to the transducer. Event log: "
						+ popup.log.toString(), 1, popup.log.size());
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		transducerLog.add(new LoggedEvent(
				"Received fired event: "+event.toString()+" on channel "+channel.toString()+" "+((Integer)args[0]).toString()));	
	}

}
