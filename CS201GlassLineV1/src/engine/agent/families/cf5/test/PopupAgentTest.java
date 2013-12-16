package engine.agent.families.cf5.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf5.PopupAgent;
import engine.agent.families.cf5.interfaces.Robot;
import engine.agent.families.cf5.test.mock.EventLog;
import engine.agent.families.cf5.test.mock.LoggedEvent;
import engine.agent.families.cf5.test.mock.MockConveyor;
import engine.agent.families.cf5.test.mock.MockConveyorFamily;
import engine.agent.families.cf5.test.mock.MockRobot;

import junit.framework.TestCase;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class PopupAgentTest extends TestCase implements TReceiver {

	/**
	 * This is the PopupAgent to be tested.
	 */
	public PopupAgent popup;
	
	
	public EventLog transducerLog = new EventLog();

	/**
	 * Test method for
	 * {@link factory.PopupAgent#msgIsPopupClear()}
	 * .
	 * 
	 * This method creates a PopupAgent and a mockConveyor. The popup is
	 * messaged that the conveyor wishes to place a new glass pane on the popup. The popup's scheduler is
	 * then called. The conveyor should receive msgPopupClear after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgIsPopupClear() {
		
		// Create a PopupAgent
		PopupAgent popup = new PopupAgent("Popup", 0, null);
		
		// Create a MockConveyor
		MockConveyor conveyor = new MockConveyor("Conveyor");
		
		// Create a MockRobot
		MockRobot robot = new MockRobot("Robot");
		
		// Setup a list of mock robots to add to popup
		List<Robot> robotList = new ArrayList<Robot>();
		robotList.add(robot);
		
		popup.setRobots(robotList);
		
		popup.setConveyor(conveyor);

		// Message the popup that the conveyor wants to deliver glass
		popup.msgIsPopupClear();

		// This will check that you're not messaging the conveyor in the
		// popup's message reception.
		assertEquals(
				"Mock Conveyor should have an empty event log before the popup's scheduler is called. Instead, the mock conveyor's event log reads: "
						+ conveyor.log.toString(), 0, conveyor.log.size());
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock conveyor should have received message. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertEquals(
				"Only 1 message should have been sent to the conveyor. Event log: "
						+ conveyor.log.toString(), 1, conveyor.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.PopupAgent#msgGlassOnPopup(factory.Glass)}
	 * .
	 * 
	 * This method creates a PopupAgent and a mockRobot. The popup is
	 * messaged that a glass pane is being passed onto it from the conveyor. The popup's scheduler is
	 * then called. The popup is raised and the robot should receive msgRobotPickup after the
	 * scheduler is called.
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgGlassOnPopup() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));

		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to popup channel
		myTransducer.register(this, TChannel.POPUP);
		
		// Create a PopupAgent
		PopupAgent popup = new PopupAgent("Popup", 0, myTransducer);
		
		// Create a MockRobot
		MockRobot robot = new MockRobot("Robot");
		
		// Setup a list of mock robots to add to popup
		List<Robot> robotList = new ArrayList<Robot>();
		robotList.add(robot);
		
		popup.setRobots(robotList);

		// Message the conveyor that it will be receiving glass
		popup.msgGlassOnPopup(testGlass);
		
		// Send POPUP_GUI_LOAD_FINISHED fired event
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// clear transducer log to remove test fired event
		transducerLog.clear();

		// This will check that you're not messaging the robot in the
		// popup's message reception.
		assertEquals(
				"Mock Robot should have an empty event log before the popup's scheduler is called. Instead, the mock robot's event log reads: "
						+ robot.log.toString(), 0, robot.log.size());

		// Needed to release the semaphores that was acquired in the last method
		popup.releasePopupSemaphore();
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();
		
		//sleep needed for agent to check fired events
		Thread.sleep(300);

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ transducerLog.toString(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertEquals(
				"Only 1 message should have been sent to the transducer. Event log: "
						+ transducerLog.toString(), 1, transducerLog.size());
		transducerLog.clear();
		
		// Send POPUP_GUI_MOVED_UP fired event
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
			
		// clear transducer log to remove test fired event
		transducerLog.clear();
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();

		assertTrue(
				"Mock robot should have received message containing the glass. Event log: "
						+ robot.log.toString(), robot.log
						.containsString("Received message msgRobotPickup"));
		assertEquals(
				"Only 1 message should have been sent to the robot. Event log: "
						+ robot.log.toString(), 1, robot.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.PopupAgent#msgWantToDropOffGlass(factory.RobotAgent)}
	 * .
	 * 
	 * This method creates a PopupAgent and a mockRobot. The popup is
	 * messaged that a processed glass pane is ready to be put back on the popup by a robot. The popup's scheduler is
	 * then called. The robot should receive msgReceiveGlass after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgWantToDropOffGlass() {
		
		// Create a PopupAgent
		PopupAgent popup = new PopupAgent("Popup", 0, null);
		
		// Create a MockRobot
		MockRobot robot = new MockRobot("Robot");
		
		// Setup a list of mock robots to add to popup
		List<Robot> robotList = new ArrayList<Robot>();
		robotList.add(robot);
		
		popup.setRobots(robotList);
		
		// Set initial state
		popup.setPopupRaised(true);

		// Message the conveyor that it will be receiving glass
		popup.msgWantToDropOffGlass(robot);

		// This will check that you're not messaging the robot in the
		// popup's message reception.
		assertEquals(
				"Mock Robot should have an empty event log before the popup's scheduler is called. Instead, the mock robot's event log reads: "
						+ robot.log.toString(), 0, robot.log.size());

		// Needed to release the semaphores that was acquired in the last method
		popup.releaseRobotSemaphore();
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock robot should have received message. Event log: "
						+ robot.log.toString(), robot.log
						.containsString("Received message msgReceiveGlass"));
		assertEquals(
				"Only 1 message should have been sent to the robot. Event log: "
						+ robot.log.toString(), 1, robot.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.PopupAgent#msgDropOffGlass(factory.RobotAgent, factory.Glass)}
	 * .
	 * 
	 * This method creates a PopupAgent, a mockRobot and a mockConveyorFamily. The popup is
	 * messaged that a glass pane is being passed onto it from a robot. The popup's scheduler is
	 * then called. The popup is lowered and the next conveyorfamily should receive msgGlassOffPopup after the
	 * scheduler is called.
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgDropOffGlass() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe());

		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to popup channel
		myTransducer.register(this, TChannel.POPUP);
		
		// Create a PopupAgent
		PopupAgent popup = new PopupAgent("Popup", 0, myTransducer);
		
		// Create a MockRobot
		MockRobot robot = new MockRobot("Robot");
		
		// Create a MockConveyorFamily
		MockConveyorFamily nextConveyor = new MockConveyorFamily("Conveyor 2");
		
		popup.setNextConveyor(nextConveyor);
		
		// Set initial state
		//popup.setGlassLoaded(true);
		popup.setPopupRaised(true);

		// Message the popup that it will be receiving glass
		popup.msgDropOffGlass(robot, testGlass);
		
		// Needed to release the semaphores that was acquired in the last method
		popup.releasePopupSemaphore();
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();
		
		// Send POPUP_GUI_LOAD_FINISHED fired event
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);

		// clear transducer log to remove test fired event
		transducerLog.clear();
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ transducerLog.toString(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertEquals(
				"Only 1 message should have been sent to the transducer. Event log: "
						+ transducerLog.toString(), 1, transducerLog.size());
		transducerLog.clear();
		
		// Send POPUP_GUI_MOVED_DOWN fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);

		// clear transducer log to remove test fired event
		transducerLog.clear();

		// This will check that you're not messaging the conveyor in the
		// popup's message reception.
		assertEquals(
				"Mock Conveyor Family should have an empty event log before the popup's scheduler is called. Instead, the mock conveyor family's event log reads: "
						+ nextConveyor.log.toString(), 0, nextConveyor.log.size());
		
		popup.releaseGlassSemaphore();
		
		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();
		
		// Send POPUP_GUI_MOVED_UP fired event
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);

		// Call the popup's scheduler
		popup.pickAndExecuteAnAction();

		assertTrue(
				"Mock conveyor family should have received message containing the glass. Event log: "
						+ nextConveyor.log.toString(), nextConveyor.log
						.containsString("Received message msgHereIsGlass"));
		assertEquals(
				"Only 1 message should have been sent to the conveyor family. Event log: "
						+ nextConveyor.log.toString(), 1, nextConveyor.log.size());
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		transducerLog.add(new LoggedEvent(
				"Received fired event: "+event.toString()+" on channel "+channel.toString()+" "+((Integer)args[0]).toString()));	
	}
	
}
