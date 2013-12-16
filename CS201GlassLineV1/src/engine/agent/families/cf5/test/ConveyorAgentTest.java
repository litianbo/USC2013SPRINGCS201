package engine.agent.families.cf5.test;

import junit.framework.TestCase;

import org.junit.Test;

import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf5.ConveyorAgent;
import engine.agent.families.cf5.test.mock.EventLog;
import engine.agent.families.cf5.test.mock.LoggedEvent;
import engine.agent.families.cf5.test.mock.MockConveyorFamily;
import engine.agent.families.cf5.test.mock.MockPopup;
import engine.agent.families.cf5.test.mock.MockSensor;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class ConveyorAgentTest extends TestCase implements TReceiver {
	
	/**
	 * This is the ConveyorAgent to be tested.
	 */
	public ConveyorAgent conveyor;
	
	public EventLog transducerLog = new EventLog();
	
	/**
	 * Test method for
	 * {@link factory.ConveyorAgent#msgGlassOnConveyor(factory.Glass)}
	 * .
	 * 
	 * This method creates a ConveyorAgent and a mockSensor. The conveyor is
	 * messaged that a glass pane is being passed onto the conveyor. The conveyor's scheduler is
	 * then called. The sensor should receive msgSenseGlass after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgGlassOnConveyor() {

		Glass testGlass = new Glass(new Recipe());
		
		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();

		// Create a ConveyorAgent
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor 1", 0, 0, myTransducer);
		
		// Create a MockSensor
		MockSensor sensor = new MockSensor("Sensor");
		
		conveyor.setPopupSensor(sensor);

		// Message the conveyor that the glass is now on the conveyor proper
		conveyor.msgGlassOnConveyor(testGlass);

		// This will check that you're not messaging the sensor in the
		// conveyor's message reception.
		assertEquals(
				"Mock Sensor should have an empty event log before the Conveyor's scheduler is called. Instead, the mock sensor's event log reads: "
						+ sensor.log.toString(), 0, sensor.log.size());

		// Call the conveyor's scheduler
		conveyor.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock sensor should have received message containing the glass. Event log: "
						+ sensor.log.toString(), sensor.log
						.containsString("Received message msgSenseGlass"));
		assertEquals(
				"Only 1 message should have been sent to the sensor. Event log: "
						+ sensor.log.toString(), 1, sensor.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.ConveyorAgent#msgGlassApproachingPopup(factory.Glass)}
	 * .
	 * 
	 * This method creates a ConveyorAgent, a Transducer, a MockConveyorFamily and a MockPopup. The conveyor is
	 * messaged that a glass pane has passed over the entry sensor. The conveyor's scheduler is
	 * then called. The Transducer should receive CONVEYOR_DO_STOP and popup should receive msgIsPopupClear after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgGlassApproachingPopup() {

		Glass testGlass = new Glass(new Recipe());
		
		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();

		
		// Create a ConveyorAgent
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor 1", 0, 0, myTransducer);
		
		// Create a MockPopup
		MockPopup popup = new MockPopup("popup");
		
		// Create a MockConveyorFamily
		MockConveyorFamily previousConveyorFamily = new MockConveyorFamily("popup");
		
		conveyor.setPopup(popup);
		conveyor.setPreviousConveyor(previousConveyorFamily);

		// Message the conveyor that the entry sensor has detected glass
		conveyor.msgGlassApproachingPopup(testGlass);

		// This will check that you're not messaging the popup in the
		// conveyor's message reception.
		assertEquals(
				"Mock Popup should have an empty event log before the Conveyor's scheduler is called. Instead, the mock popup event log reads: "
						+ popup.log.toString(), 0, popup.log.size());

		// Needed to release the semaphores that was acquired in the last method
		conveyor.releasePopupSemaphore();
		
		// Call the conveyor's scheduler
		conveyor.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock popup should have received message containing the glass. Event log: "
						+ popup.log.toString(), popup.log
						.containsString("Received message msgIsPopupClear"));
		assertEquals(
				"Only 1 message should have been sent to the popup. Event log: "
						+ popup.log.toString(), 1, popup.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.ConveyorAgent#msgPopOffClear()}
	 * .
	 * 
	 * This method creates a ConveyorAgent, a Transducer, a MockConveyorFamily and a MockPopup. The conveyor is
	 * messaged that the popup is free to receive a new glass pane. The conveyor's scheduler is
	 * then called. The popup should receive msgGlassOnPopup after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgPopOffClear() {

		Glass testGlass = new Glass(new Recipe());
		
		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Create a ConveyorAgent
		ConveyorAgent conveyor = new ConveyorAgent("Conveyor 1", 0, 0, myTransducer);
		
		// Create a MockPopup
		MockPopup popup = new MockPopup("popup");
		
		// Create a MockConveyorFamily
		MockConveyorFamily previousConveyorFamily = new MockConveyorFamily("popup");
		
		conveyor.setPopup(popup);
		conveyor.setPreviousConveyor(previousConveyorFamily);

		// Message the conveyor that the entry sensor has detected glass
		conveyor.msgGlassApproachingPopup(testGlass);
		
		// Message the conveyor that the popup is ready for glass
		conveyor.msgPopupClear();

		// This will check that you're not messaging the popup in the
		// conveyor's message reception.
		assertEquals(
				"Mock Popup should have an empty event log before the Conveyor's scheduler is called. Instead, the mock popup event log reads: "
						+ popup.log.toString(), 0, popup.log.size());
		
		// Call the conveyor's scheduler
		conveyor.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock popup should have received message containing the glass. Event log: "
						+ popup.log.toString(), popup.log
						.containsString("Received message msgGlassOnPopup"));
		assertEquals(
				"Only 1 message should have been sent to the popup. Event log: "
						+ popup.log.toString(), 1, popup.log.size());
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		transducerLog.add(new LoggedEvent(
				"Received fired event: "+event.toString()+" on channel "+channel.toString()+" "+((Integer)args[0]).toString()));	
	}
}
