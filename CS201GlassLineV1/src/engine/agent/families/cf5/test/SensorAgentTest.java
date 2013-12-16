package engine.agent.families.cf5.test;

import org.junit.Test;

import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf5.SensorAgent;
import engine.agent.families.cf5.test.mock.MockConveyor;
import engine.agent.families.cf5.test.mock.MockConveyorFamily;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

import junit.framework.TestCase;

public class SensorAgentTest extends TestCase implements TReceiver {

	/**
	 * This is the SensorAgent to be tested.
	 */
	public SensorAgent sensor;
	
	/**
	 * Test method for
	 * {@link factory.SensorAgent#msgHereIsGlass(factory.Glass)}
	 * .
	 * 
	 * This method creates a SensorAgent, a Transducer and a mockConveyor. The sensor is
	 * messaged that a glass pane is approaching the entry sensor. The sensor's scheduler is
	 * then called. The conveyor should receive msgGlassOnConveyor after the
	 * scheduler is called.
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgSenseGlassEntry() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe());

		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to sensor channel
		myTransducer.register(this, TChannel.SENSOR);
		
		// Create a entry SensorAgent
		SensorAgent entrySensor = new SensorAgent("Entry Sensor", "Entry", 0, myTransducer);
		
		// Create a MockConveyor
		MockConveyor conveyor = new MockConveyor("Conveyor");
		
		entrySensor.setConveyor(conveyor);

		// Message the sensor that it will be receiving glass
		entrySensor.msgHereIsGlass(testGlass);
		
		// Send SENSOR_GUI_PRESSED fired event
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		// Send SENSOR_GUI_RELEASED fired event
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);

		// This will check that you're not messaging the conveyor in the
		// sensor's message reception.
		assertEquals(
				"Mock Conveyor should have an empty event log before the Sensor's scheduler is called. Instead, the mock conveyor's event log reads: "
						+ conveyor.log.toString(), 0, conveyor.log.size());

		// Call the sensor's scheduler
		entrySensor.pickAndExecuteAnAction();
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock conveyor should have received message containing the glass. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgGlassOnConveyor"));
		assertEquals(
				"Only 1 message should have been sent to the conveyor. Event log: "
						+ conveyor.log.toString(), 1, conveyor.log.size());
	}

	/**
	 * Test method for
	 * {@link factory.SensorAgent#msgSenseGlass(factory.Glass)}
	 * .
	 * 
	 * This method creates a SensorAgent, a Transducer and a mockConveyor. The sensor is
	 * messaged that a glass pane is approaching the popup sensor. The sensor's scheduler is
	 * then called. The conveyor should receive msgGlassApproachingPopup after the
	 * scheduler is called.
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgSenseGlassPopup() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe());

		// Create and initialize a transducer
		Transducer myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to sensor channel
		myTransducer.register(this, TChannel.SENSOR);
		
		// Create a popup SensorAgent
		SensorAgent popupSensor = new SensorAgent("Popup Sensor", "Popup", 0, myTransducer);
		
		// Create a MockConveyor
		MockConveyor conveyor = new MockConveyor("Conveyor");
		
		popupSensor.setConveyor(conveyor);

		// Message the sensor that it will be receiving glass
		popupSensor.msgSenseGlass(testGlass);
		
		// Send SENSOR_GUI_PRESSED fired event
		Integer[] args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);

		// This will check that you're not messaging the conveyor in the
		// sensor's message reception.
		assertEquals(
				"Mock Conveyor should have an empty event log before the Sensor's scheduler is called. Instead, the mock conveyor's event log reads: "
						+ conveyor.log.toString(), 0, conveyor.log.size());

		// Call the sensor's scheduler
		popupSensor.pickAndExecuteAnAction();
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock conveyor should have received message containing the glass. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgGlassApproachingPopup"));
		assertEquals(
				"Only 1 message should have been sent to the conveyor. Event log: "
						+ conveyor.log.toString(), 1, conveyor.log.size());
	}
	
	/**
	 * Test method for
	 * {@link factory.SensorAgent#msgStartFactory(factory.Glass)}
	 * .
	 * 
	 * This method creates a SensorAgent and a mockConveyorFamily. The sensor is
	 * messaged to restart the previous conveyor family. The conveyor family should receive msgStartConveyor after the
	 * scheduler is called.
	 * @throws InterruptedException 
	 */
	@Test
	public void testMsgStartConveyor() throws InterruptedException {
		
		// Create a popup SensorAgent
		SensorAgent popupSensor = new SensorAgent("Entry Sensor", "Entry", 0, null);
		
		// Create a MockConveyorFamily
		MockConveyorFamily previousConveyor = new MockConveyorFamily("Previous conveyor");
		
		popupSensor.setPreviousConveyor(previousConveyor);

		// Message the sensor that it will be receiving glass
		popupSensor.msgStartConveyor();

		// This will check that you're not messaging the conveyor in the
		// sensor's message reception.
		assertEquals(
				"Mock Conveyor Family should have an empty event log before the Sensor's scheduler is called. Instead, the mock conveyor family's event log reads: "
						+ previousConveyor.log.toString(), 0, previousConveyor.log.size());

		// Call the sensor's scheduler
		popupSensor.pickAndExecuteAnAction();
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock conveyor family should have received start conveyor message. Event log: "
						+ previousConveyor.log.toString(), previousConveyor.log
						.containsString("Received message msgStartConveyor"));
		assertEquals(
				"Only 1 message should have been sent to the conveyor family. Event log: "
						+ previousConveyor.log.toString(), 1, previousConveyor.log.size());
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		
	}
}
