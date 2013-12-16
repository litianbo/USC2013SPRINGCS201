package engine.agent.families.cf5.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import engine.agent.families.Glass;
import engine.agent.families.Glass.GlassState;
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

public class PopupAgentGlassTest extends TestCase implements TReceiver {
	
	public EventLog transducerLog = new EventLog();
	
	public PopupAgent popup;
	public MockConveyorFamily nextConveyorFamily;
	public Glass testGlass1;
	public Glass testGlass2;
	public Glass testGlass3;
	public MockConveyor conveyor;
	List<MockRobot> robots = new ArrayList<MockRobot>();
	public Transducer myTransducer;

	@Override
	protected void setUp() throws Exception {
		// Create and initialize a transducer
		myTransducer = new Transducer();
		myTransducer.startTransducer();
		myTransducer.register(this, TChannel.POPUP);
		
		// Create a PopupAgent
		popup = new PopupAgent("Popup", 0, myTransducer);
		
		// Create a Mock Conveyor
		conveyor = new MockConveyor("popup");
		
		// Create a MockConveyorFamily
		nextConveyorFamily = new MockConveyorFamily("conveyor 2");
		
		//populate robots list with mock robots
		robots.add(new MockRobot("Robot 1"));
		robots.add(new MockRobot("Robot 2"));

		//Hack to set up robots for popup
		List<Robot> robotSet = new ArrayList<Robot>();
		
		for (Robot r: robots){
			robotSet.add(r);
		}
		
		popup.setConveyor(conveyor);
		popup.setRobots(robotSet);
		popup.setNextConveyor(nextConveyorFamily);
	}
	
	/**
	 * This test should test the normal scenario for one glass pane that does not need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupOneGlassDoesNotNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass and see that it doesn't need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for one glass pane that need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupOneGlassNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should check the glass and see that it needs processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Since there are only one glass being processed there should be no fired event or messages
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes that don't need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupTwoGlassNoneNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		testGlass2 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		//TODO Changed
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes that only the first need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupTwoGlassFirstNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass2 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Checks robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Since there is second piece of glass inbound the popup should ready itself to receive the next glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should sense it's in the position to receive the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		//Popup should ignore robot message for now and see that second glass doesn't need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes that only the second need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupTwoGlassSecondNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// TODO Changed
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass and see that it needs processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Since there are only one glass being processed there should be no fired event or messages
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass2.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 message."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes that both need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupTwoGlassBothNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Checks robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Since there is second piece of glass inbound the popup should ready itself to receive the next glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should sense it's in the position to receive the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup should check the glass and see that it needs processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 2 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(1).log.size() == 1);
		
		// Since there are only one glass being processed there should be no fired event or messages
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		popup.msgWantToDropOffGlass(robots.get(1));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 2 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(1).log.size() == 2);
		
		testGlass2.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(1), testGlass2);
	
		// Popup is waiting for the second glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes that don't need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassNoneNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		testGlass2 = new Glass(new Recipe());
		testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// TODO Changed
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should tell conveyor that its ready for the third glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes that only the first need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassFirstNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass2 = new Glass(new Recipe());
		testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Robot wanting to drop off finished glass
		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup should ready itself to receive the finished glass from one of the robots
		popup.pickAndExecuteAnAction();*/
		
		// TODO Changed
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 2 message."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
	
		// Popup should tell conveyor that its ready for the secthirdond glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the third glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes that only the second need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassSecondNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// TODO
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Checks robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Since there is third piece of glass inbound the popup should ready itself to receive the next glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		/*// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// TODO changed
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should tell conveyor that its ready for the third glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the third glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should ready itself to receive the finished glass from one of the robots
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass2.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes that only the third need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassThirdNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		testGlass2 = new Glass(new Recipe());
		testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// TODO Changed
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
	
		// Popup should tell conveyor that its ready for the third glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass and see that it needs processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Since this is the last glass being processed there should be no fired event or messages
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass3.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass3);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 message."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with the first and second need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassFirstAndSecondNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);

		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Robot wanting to drop off finished glass
		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup should ignore this message for now and should check the glass to see that it need processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(1).log.size() == 1);

		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		//Popup should check robots
		popup.pickAndExecuteAnAction();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Robot wanting to drop off finished glass
		popup.msgWantToDropOffGlass(robots.get(1));
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 2 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(1).log.size() == 2);
		
		testGlass2.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(1), testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
	
		// Popup should tell conveyor that its ready for the third glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the third glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with the first and third need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassFirstAndThirdNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass2 = new Glass(new Recipe());
		testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);

		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Robot wanting to drop off finished glass
		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup should ready itself to receive the finished glass from one of the robots
		popup.pickAndExecuteAnAction();*/
		
		// TODO Changed
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 2 message."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
	
		// Popup should tell conveyor that its ready for the secthirdond glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the third glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass and see that it needs processing
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 3 messages."
						+ getLogs(), robots.get(0).log.size() == 3);
		
		// Since this is the last glass being processed there should be no fired event or messages
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 3 messages."
				+ getLogs(), robots.get(0).log.size() == 3);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		popup.releaseRobotSemaphore();
		
		// Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 4 messages."
						+ getLogs(), robots.get(0).log.size() == 4);
		
		testGlass3.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass3);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 4 messages."
				+ getLogs(), robots.get(0).log.size() == 4);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 message."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes that only the second and third need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassSecondAndThirdNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does not need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling false"));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 0 messages."
				+ getLogs(), robots.get(0).log.size() == 0);
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		/*popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// TODO changed
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Checks robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Since there is third piece of glass inbound the popup should ready itself to receive the next glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		/*popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot should have only received 1 message."
				+ getLogs(), robots.get(0).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should tell conveyor that its ready for the third glass
		popup.pickAndExecuteAnAction();
		
		// TODO Changed
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the third glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to raise glass for robot pickup
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(1).log.size() == 1);

		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		/*popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();*/
		
		//TODO Changed
		
		//Popup should check robots
		popup.pickAndExecuteAnAction();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass2.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		popup.msgWantToDropOffGlass(robots.get(1));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		//Popup should check robots
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		//popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 2 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(1).log.size() == 2);
		
		testGlass3.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(1), testGlass3);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes that all need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormPopupThreeGlassAllNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		popup.setGlassLoaded(false);
		popup.setPopupRaised(false);
		
		// Conveyor messaging to see if popup is clear or not
		popup.msgIsPopupClear();
		
		// Popup should check itself and robots to see if it's loaded or otherwise got its hands full
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the first glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		popup.msgGlassOnPopup(testGlass1);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for second glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();

		assertTrue("Mock Conveyor should have received 1 message."
				+ getLogs(), conveyor.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(0).log.size() == 1);
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup prepares itself for the second glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
	
		// Popup should tell conveyor that its ready for the second glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the second glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 2 message."
				+ getLogs(), conveyor.log.size() == 2);
		
		popup.msgGlassOnPopup(testGlass2);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Conveyor messaging to see if popup is clear or not for third glass
		popup.msgIsPopupClear();
		
		popup.releasePopupSemaphore();
		
		// Popup should ignore this message for now and should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		popup.releaseRobotSemaphore();
		
		// Popup should ask for pickup of glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 2 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 1 message."
						+ getLogs(), robots.get(1).log.size() == 1);
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		//Popup should check robots
		popup.pickAndExecuteAnAction();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(0).log.size() == 2);
		
		testGlass1.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass1);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 0 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 0);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releaseGlassSemaphore();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the first finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 1 message."
						+ getLogs(), nextConveyorFamily.log.size() == 1);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		popup.msgWantToDropOffGlass(robots.get(1));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 2 messages."
				+ getLogs(), robots.get(0).log.size() == 2);
		assertTrue("Robot 2 should have only received 1 message."
				+ getLogs(), robots.get(1).log.size() == 1);
		assertTrue("Mock Conveyor should have received 2 messages."
				+ getLogs(), conveyor.log.size() == 2);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check its robots
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should lower itself to receive the third glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should tell conveyor that its ready for the third glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Conveyor should have received that the popup is ready for the third glass. Event log: "
						+ getLogs(), conveyor.log
						.containsString("Received message msgPopupClear"));
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		popup.msgGlassOnPopup(testGlass3);
		
		// Popup should not do anything since it's waiting for transducer to send a popup load finished event
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		// Popup should check the glass to see that it does need processing
		popup.pickAndExecuteAnAction();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to raise glass for robot pickup
		popup.pickAndExecuteAnAction();
		
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move up event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgRobotPickup"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("glass state is start"));
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("needDrilling true"));
		assertTrue("Robot should have only received 3 messages."
						+ getLogs(), robots.get(0).log.size() == 3);
		
		//Popup should check robots
		popup.pickAndExecuteAnAction();
		
		popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 2 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(1).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 2 messages."
						+ getLogs(), robots.get(1).log.size() == 2);
		
		testGlass2.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(1), testGlass2);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 3 messages."
				+ getLogs(), robots.get(0).log.size() == 3);
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the second finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 2 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 2);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
		
		popup.msgWantToDropOffGlass(robots.get(0));
		
		// Popup is waiting for the glass to be moved off the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 3 messages."
				+ getLogs(), robots.get(0).log.size() == 3);
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_RELEASE_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		//Popup should check robots
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		//popup.releaseRobotSemaphore();
		
		//Popup should send message after its clear to retrieve glass
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Robot 1 should have received a pickup request message. Event log: "
						+ getLogs(), robots.get(0).log
						.containsString("Received message msgReceiveGlass"));
		assertTrue("Robot should have only received 4 messages."
						+ getLogs(), robots.get(0).log.size() == 4);
		
		testGlass3.setState(GlassState.doneDrilling);
		
		popup.msgDropOffGlass(robots.get(0), testGlass3);
		
		// Popup is waiting for the glass to be loaded on the popup so there should be no new messages/fired events
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue("Transducer should have received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		assertTrue("Robot 1 should have only received 4 messages."
				+ getLogs(), robots.get(0).log.size() == 4);
		assertTrue("Robot 2 should have only received 2 messages."
				+ getLogs(), robots.get(1).log.size() == 2);
		assertTrue("Mock Conveyor should have received 3 messages."
				+ getLogs(), conveyor.log.size() == 3);
		assertTrue("Mock Conveyor Family should have received 2 messages."
				+ getLogs(), nextConveyorFamily.log.size() == 2);
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.releasePopupSemaphore();
		
		// Popup should prepare to release glass
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a popup move down event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		//Fire necessary event and clear it from log
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		Thread.sleep(100);
		transducerLog.clear();
		
		popup.pickAndExecuteAnAction();
		
		Thread.sleep(100);

		assertTrue(
				"Transducer should have received a popup release glass event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock Conveyor Family should have received the third finished glass. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue("Mock Conveyor Family should have only received 3 messages."
						+ getLogs(), nextConveyorFamily.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("needDrilling true"));
		assertTrue(
				"The glass should have the same state as before. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("glass state is doneDrilling"));
	}
	
	public String getLogs() {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		sb.append("-------Conveyor Log-------");
		sb.append(newLine);
		sb.append(conveyor.log.toString());
		sb.append(newLine);
		sb.append("-------End Conveyor Log-------");

		sb.append(newLine);

		sb.append("-------Robot 1 Log-------");
		sb.append(newLine);
		sb.append(robots.get(0).log.toString());
		sb.append("-------End Robot 1 Log-------");
		
		sb.append(newLine);

		sb.append("-------Robot 2 Log-------");
		sb.append(newLine);
		sb.append(robots.get(1).log.toString());
		sb.append("-------End Robot 2 Log-------");
		
		sb.append(newLine);

		sb.append("-------Conveyor Family Log-------");
		sb.append(newLine);
		sb.append(nextConveyorFamily.log.toString());
		sb.append("-------End Conveyor Family Log-------");
		
		sb.append(newLine);
		
		sb.append("------Transducer Log------");
		sb.append(newLine);
		sb.append(transducerLog.toString());
		sb.append("-------End Transducer Log-------");

		return sb.toString();

	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		transducerLog.add(new LoggedEvent(
				"Received fired event: "+event.toString()+" on channel "+channel.toString()+" "+((Integer)args[0]).toString()));	
	}
}
