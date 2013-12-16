/**
 * 
 */
package engine.agent.families.cf7.test;

import org.junit.Test;

import junit.framework.TestCase;
import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf7.ConveyorAgent;
import engine.agent.families.cf7.ConveyorAgent.ConveyorState;
import engine.agent.families.cf7.test.mock.MockPopup;
import engine.agent.families.cf7.test.mock.MockSensor;

/**
 * This is a set of tests for the normal conveyor interaction cycle. This suite
 * tests all of the conveyor messages, with a single customer. Note, this does not
 * test multiple customers. These are contained in the
 * conveyorAgentTestsWithTwoCustomers. <br>
 * <br>
 * Note that the unit of test in this file is the individual message. We call
 * the appropriate functions to get the conveyorAgent ready to accept and process
 * a particular message and then make assert statements surrounding that
 * message. This breaks testing this one interaction up into 5 different test
 * methods. You could also integrate all of this into one large test method. We
 * do this when testing a conveyorAgent with multiple customers. Both styles are
 * valid and which one is best depends on the goal of your particular tests. <br>
 * This set of tests is invaluable when first writing your conveyorAgent. It will
 * validate that your conveyor implements the messaging contract described in the
 * interaction diagram. In building an agent system, I would actually write
 * these unit tests before writing the conveyorAgent itself. This practice is
 * called Test Driven Development (TDD).
 * 
 * @author Sean Turner
 * 
 */
public class ConveyorAgentTestsWithOneGlass extends TestCase {

	/**
	 * This is the conveyorAgent to be tested.
	 */
	public ConveyorAgent conveyor;

	/**
	 * Test method for
	 * {@link restaurant.conveyorAgent#msgSitCustomerAtTable(restaurant.CustomerAgent, int)}
	 * .
	 * 
	 * This method creates a conveyorAgent and a MockCustomer. The conveyor is
	 * messaged that the customer needs to be seated. The conveyor's scheduler is
	 * then called. The customer should receive msgFollowMeToTable after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgHereIsGlass() {

		MockPopup popup = new MockPopup("Popup1");

		MockSensor backSensor = new MockSensor("BackSensor1");

		MockSensor frontSensor = new MockSensor("FrontSensor1");

		// Create a conveyorAgent
		ConveyorAgent conveyor = new ConveyorAgent(null, popup, frontSensor, backSensor, 1);
		
		// Create a piece of glass
		Recipe recipe = new Recipe(false, false, false, false, true, false, false, false, false, false);
		Glass glass = new Glass(recipe);
		

		// Simulate the glass passing through the conveyor
		conveyor.state = ConveyorState.STARTED;
		conveyor.glassOnBackSensor = true;
		conveyor.msgHereIsGlass(glass);
		
		
		// This will check that you're not messaging the conveyor in the
		// popup's message reception.
		assertEquals(
				"Mock Sensor should have an empty event log before the conveyor's scheduler is called. Instead, the mock sensor's event log reads: "
						+ backSensor.log.toString(), 0, backSensor.log.size());
		 
		
		conveyor.pickAndExecuteAnAction();
		

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock sensor should have received message with glass. Event log: "
						+ backSensor.log.toString(), backSensor.log
						.containsString("Received message msgHereIsGlass."));
		
		assertEquals(
				"Only 1 message should have been sent to the sensor. Event log: "
						+ backSensor.log.toString(), 1, backSensor.log.size());
	}
	
	@Test
	public void testMsgSendGlass() {

		MockPopup popup = new MockPopup("Popup1");

		MockSensor backSensor = new MockSensor("BackSensor1");

		MockSensor frontSensor = new MockSensor("FrontSensor1");

		// Create a conveyorAgent
		ConveyorAgent conveyor = new ConveyorAgent(null, popup, frontSensor, backSensor, 1);
		
		// Create a piece of glass
		Recipe recipe = new Recipe(false, false, false, false, true, false, false, false, false, false);
		Glass glass = new Glass(recipe);
		

		// Simulate the glass passing through the conveyor
		conveyor.state = ConveyorState.STARTED;
		conveyor.msgHereIsGlass(glass);
		conveyor.msgSendGlass();
		
		
		// This will check that you're not messaging the conveyor in the
		// popup's message reception.
		assertEquals(
				"Mock Sensor should have an empty event log before the conveyor's scheduler is called. Instead, the mock sensor's event log reads: "
						+ backSensor.log.toString(), 0, backSensor.log.size());
		 
		
		conveyor.pickAndExecuteAnAction();
		

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock sensor should have received message with glass. Event log: "
						+ backSensor.log.toString(), backSensor.log
						.containsString("Received message msgHereIsGlass."));
		
		assertEquals(
				"Only 1 message should have been sent to the sensor. Event log: "
						+ backSensor.log.toString(), 1, backSensor.log.size());
	}
}
