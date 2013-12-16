package engine.agent.families.cf5.test;

import org.junit.Test;

import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf5.ConveyorAgent;
import engine.agent.families.cf5.test.mock.EventLog;
import engine.agent.families.cf5.test.mock.LoggedEvent;
import engine.agent.families.cf5.test.mock.MockConveyorFamily;
import engine.agent.families.cf5.test.mock.MockPopup;
import engine.agent.families.cf5.test.mock.MockSensor;

import junit.framework.TestCase;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class ConveyorAgentGlassTest extends TestCase implements TReceiver {
	
	public EventLog transducerLog = new EventLog();
	
	public ConveyorAgent conveyor;
	public MockConveyorFamily previousConveyorFamily;
	public Glass testGlass1;
	public Glass testGlass2;
	public Glass testGlass3;
	public MockPopup popup;
	public MockSensor popupSensor;
	public Transducer myTransducer;

	@Override
	protected void setUp() throws Exception {
		// Create and initialize a transducer
		myTransducer = new Transducer();
		myTransducer.startTransducer();
		myTransducer.register(this, TChannel.CONVEYOR);
		
		// Create a ConveyorAgent
		conveyor = new ConveyorAgent("Conveyor", 0, 0, myTransducer);
		
		// Create a MockConveyorFamily
		previousConveyorFamily = new MockConveyorFamily("conveyor 0");
		
		// Create a Mock Popup
		popup = new MockPopup("popup");
		
		// Create a Mock EntrySensor
		popupSensor = new MockSensor("Popup Sensor");

		conveyor.setPopup(popup);
		conveyor.setPopupSensor(popupSensor);
		conveyor.setPreviousConveyor(previousConveyorFamily);
	}
	
	/**
	 * This test should test the normal scenario for one glass pane that does not need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorOneGlassDoesNotNeedDrilling() throws InterruptedException {
		testGlass1 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for one glass pane that needs processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorOneGlassNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes that don't need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorTwoGlassNoneNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe());
		Glass testGlass2 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes with only the first need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorTwoGlassFirstNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass2 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes with only the second need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorTwoGlassSecondNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe());
		Glass testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
	}
	
	/**
	 * This test should test the normal scenario for two glass panes with both need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorTwoGlassBothNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes which doesn't need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassNoneNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe());
		Glass testGlass2 = new Glass(new Recipe());
		Glass testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with only the first need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassFirstNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass2 = new Glass(new Recipe());
		Glass testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with only the second need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassSecondNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe());
		Glass testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with only the third need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassThirdNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe());
		Glass testGlass2 = new Glass(new Recipe());
		Glass testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		}
	
	/**
	 * This test should test the normal scenario for three glass panes with the first and second need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassFirstAndSecondNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass3 = new Glass(new Recipe());
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with the first and third need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassFirstAndThirdNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass2 = new Glass(new Recipe());
		Glass testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
	}
	
	/**
	 * This test should test the normal scenario for three glass panes with the second and third need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassSecondAndThirdNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe());
		Glass testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should not need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling false"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		}
	
	/**
	 * This test should test the normal scenario for three glass panes with all need processing.
	 * @throws InterruptedException 
	 */
	@Test
	public void testNormConveyorThreeGlassAllNeedDrilling() throws InterruptedException {
		
		Glass testGlass1 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass2 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		Glass testGlass3 = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		// Set initial state
		conveyor.setPopUpCleared(false);
		
		conveyor.msgGlassOnConveyor(testGlass1);
		conveyor.msgGlassOnConveyor(testGlass2);
		conveyor.msgGlassOnConveyor(testGlass3);
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);

		conveyor.msgGlassApproachingPopup(testGlass1);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 1 message."
				+ getLogs(), popupSensor.log.size() == 1);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the first glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);

		conveyor.msgGlassApproachingPopup(testGlass2);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 2 messages."
				+ getLogs(), popupSensor.log.size() == 2);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the second glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		popup.log.clear();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock Sensor should have the glass passed onto it. Event log: "
						+ getLogs(), popupSensor.log
						.containsString("Received message msgSenseGlass"));
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);

		conveyor.msgGlassApproachingPopup(testGlass3);
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 1 message."
						+ getLogs(), popup.log.size() == 1);
		
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		
		//run scheduler again to see if the conveyor reacts correctly to not receiving permission to place glass on popup
		
		conveyor.releasePopupSemaphore();
		
		conveyor.pickAndExecuteAnAction();
		
		assertTrue("Sensor should have only received 3 messages."
				+ getLogs(), popupSensor.log.size() == 3);
		
		assertTrue(
				"Mock popup should have been asked if its clear. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgIsPopupClear"));
		
		assertTrue("Popup should have only received 2 messages."
						+ getLogs(), popup.log.size() == 2);
		
		Thread.sleep(100);
		
		//Conveyor already stopped, shouldn't send the message to transducer again
		//or message previous conveyor family to stop
		assertFalse(
				"Transducer should have received a conveyor stop message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_STOP"));
		
		assertTrue("Transducer should have only received 0 messages."
						+ getLogs(), transducerLog.size() == 0);
		transducerLog.clear();
		
		assertFalse(
				"Previous Conveyor Family should have received a conveyor stop message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStopConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 0 messages."
				+ getLogs(), previousConveyorFamily.log.size() == 0);
		previousConveyorFamily.log.clear();

		conveyor.msgPopupClear();
		
		conveyor.pickAndExecuteAnAction();

		Thread.sleep(100);
		
		assertTrue(
				"Previous Conveyor Family should have received a conveyor start message. Event log: "
						+ getLogs(), previousConveyorFamily.log
						.containsString("Received message msgStartConveyor"));
		
		assertTrue("Previous Conveyor Family should have only received 1 message."
				+ getLogs(), previousConveyorFamily.log.size() == 1);
		previousConveyorFamily.log.clear();
		assertTrue(
				"Transducer should have received a conveyor start message. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: CONVEYOR_DO_START"));
		assertTrue("Transducer should have only received 1 message."
						+ getLogs(), transducerLog.size() == 1);
		transducerLog.clear();
		assertTrue(
				"Mock popup should have received the third glass. Event log: "
						+ getLogs(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertTrue("Popup should have only received 3 messages."
						+ getLogs(), popup.log.size() == 3);
		assertTrue(
				"The glass should need drilling according to recipe. Event log: "
						+ getLogs(), popup.log
						.containsString("needDrilling true"));
		}
	
	public String getLogs() {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		sb.append("-------Popup Log-------");
		sb.append(newLine);
		sb.append(popup.log.toString());
		sb.append(newLine);
		sb.append("-------End Popup Log-------");

		sb.append(newLine);

		sb.append("-------Popup Sensor Log-------");
		sb.append(newLine);
		sb.append(popupSensor.log.toString());
		sb.append("-------End Popup Sensor Log-------");
		
		sb.append(newLine);
		
		sb.append("-------Conveyor Family Log-------");
		sb.append(newLine);
		sb.append(previousConveyorFamily.log.toString());
		sb.append("-------Conveyor Family Log-------");
		
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
