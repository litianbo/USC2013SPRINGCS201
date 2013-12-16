/**
 * 
 */
package engine.agent.families.cf7.test;

import junit.framework.TestCase;

import org.junit.Test;

import transducer.TChannel;
import transducer.TEvent;
import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf7.SensorAgent;
import engine.agent.families.cf7.SensorAgent.SensorType;
import engine.agent.families.cf7.test.mock.MockConveyor;

/**
 * This is a set of tests for the normal sensor interaction cycle. This suite
 * tests all of the sensor messages, with a single conveyor. Note, this does not
 * test multiple conveyors. These are contained in the
 * sensorAgentTestsWithTwoconveyors. <br>
 * <br>
 * Note that the unit of test in this file is the individual message. We call
 * the appropriate functions to get the sensorAgent ready to accept and process
 * a particular message and then make assert statements surrounding that
 * message. This breaks testing this one interaction up into 5 different test
 * methods. You could also integrate all of this into one large test method. We
 * do this when testing a sensorAgent with multiple conveyors. Both styles are
 * valid and which one is best depends on the goal of your particular tests. <br>
 * This set of tests is invaluable when first writing your sensorAgent. It will
 * validate that your sensor implements the messaging contract described in the
 * interaction diagram. In building an agent system, I would actually write
 * these unit tests before writing the sensorAgent itself. This practice is
 * called Test Driven Development (TDD).
 * 
 * @author Sean Turner
 * 
 */
public class SensorAgentTestsWithOneGlass extends TestCase {

	/**
	 * This is the SensorAgent to be tested.
	 */
	public SensorAgent sensor;

	/**
	 * Test method for
	 * {@link restaurant.sensorAgent#msgSitconveyorAtTable(restaurant.conveyorAgent, int)}
	 * .
	 * 
	 * This method creates a sensorAgent and a Mockconveyor. The sensor is
	 * messaged that the conveyor needs to be seated. The sensor's scheduler is
	 * then called. The conveyor should receive msgFollowMeToTable after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgHereIsGlass() {
		
		sensor = new SensorAgent(null, 1);
		sensor.setPopup(null);
		sensor.type = SensorType.FRONT;

		// Create a MockConveyor
		MockConveyor conveyor = new MockConveyor("Conveyor1");
		sensor.setConveyor(conveyor);
		
		// Create a piece of glass
		Recipe recipe = new Recipe(false, false, false, false, true, false, false, false, false, false);
		Glass glass = new Glass(recipe);
		
		// Message the sensor with a piece of glass
		sensor.msgHereIsGlass(glass);
		
		// This will check that you're not messaging the conveyor in the
		// sensor's message reception.
		assertEquals(
				"Mock Conveyor should have an empty event log before the Sensor's scheduler is called. Instead, the mock conveyor's event log reads: "
						+ conveyor.log.toString(), 0, conveyor.log.size());
		
		// Simulate the glass passing through the sensor
		Object args[] = { (Object) 1 };
		sensor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		sensor.pickAndExecuteAnAction();
		
		sensor.eventFired(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		sensor.pickAndExecuteAnAction();

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock conveyor should have received message with glass. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgStopConveyor."));
		
		assertTrue(
				"Mock conveyor should have received message with glass. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgHereIsGlass from front sensor."));
		
		assertTrue(
				"Mock conveyor should have received message with glass. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgStartConveyor."));
		
		assertEquals(
				"Only 1 message should have been sent to the conveyor. Event log: "
						+ conveyor.log.toString(), 3, conveyor.log.size());
	}
}
