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
import engine.agent.families.cf7.ConveyorAgent.ConveyorState;
import engine.agent.families.cf7.PopupAgent;
import engine.agent.families.cf7.test.mock.MockConveyor;
import engine.agent.families.cf7.test.mock.MockMachine;
import engine.agent.families.cf7.test.mock.MockSensor;

/**
 * This is a set of tests for the normal popup interaction cycle. This suite
 * tests all of the popup messages, with a single conveyor. Note, this does not
 * test multiple conveyors. These are contained in the
 * popupAgentTestsWithTwoconveyors. <br>
 * <br>
 * Note that the unit of test in this file is the individual message. We call
 * the appropriate functions to get the popupAgent ready to accept and process
 * a particular message and then make assert statements surrounding that
 * message. This breaks testing this one interaction up into 5 different test
 * methods. You could also integrate all of this into one large test method. We
 * do this when testing a popupAgent with multiple conveyors. Both styles are
 * valid and which one is best depends on the goal of your particular tests. <br>
 * This set of tests is invaluable when first writing your popupAgent. It will
 * validate that your popup implements the messaging contract described in the
 * interaction diagram. In building an agent system, I would actually write
 * these unit tests before writing the popupAgent itself. This practice is
 * called Test Driven Development (TDD).
 * 
 * @author Sean Turner
 * 
 */
public class PopupAgentTestsWithThreeGlass extends TestCase {

	/**
	 * This is the popupAgent to be tested.
	 */
	public PopupAgent popup;

	/**
	 * Test method for
	 * {@link restaurant.popupAgent#msgSitconveyorAtTable(restaurant.conveyorAgent, int)}
	 * .
	 * 
	 * This method creates a popupAgent and a Mockconveyor. The popup is
	 * messaged that the conveyor needs to be seated. The popup's scheduler is
	 * then called. The conveyor should receive msgFollowMeToTable after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgHereIsGlass() {
		
		popup = new PopupAgent(null, 1);
		
		// Create mock agents
		MockConveyor conveyor = new MockConveyor("Conveyor1");
		popup.setConveyor(conveyor);
		
		MockMachine machine1 = new MockMachine("Machine1");
		popup.setMachine(machine1, 0);
		
		MockMachine machine2 = new MockMachine("Machine2");
		popup.setMachine(machine2, 1);
		
		MockSensor backSensor = new MockSensor("Sensor1");
		popup.setBackSensor(backSensor);

		
		// Create a piece of glass
		Recipe recipe = new Recipe(false, false, false, false, true, false, false, false, false, false);
		Glass glass = new Glass(recipe);
		
		// Message the popup with a piece of glass
		popup.msgHereIsGlass(glass);
		

		// Simulate the glass passing through the popup
		Object args[] = { (Object) 1 };
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		popup.pickAndExecuteAnAction();
		
		assertTrue(
				"Mock conveyor should have received message to stop. Event log: "
						+ conveyor.log.toString(), conveyor.log
						.containsString("Received message msgStopConveyor."));
		
		// Move the popup up
		popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		// Make sure that the machines have empty logs
		assertEquals(
				"Mock Machine should have an empty event log before the popup's scheduler is called. Instead, the mock machine's event log reads: "
						+ machine1.log.toString(), 0, machine1.log.size());
		
		assertEquals(
				"Mock Machine should have an empty event log before the popup's scheduler is called. Instead, the mock machine's event log reads: "
						+ machine2.log.toString(), 0, machine2.log.size());
		
		popup.pickAndExecuteAnAction();
		popup.pickAndExecuteAnAction();
		

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock machine should have received message with glass. Event log: "
						+ machine1.log.toString(), machine1.log
						.containsString("Received message msgHereIsGlass from popup."));
		
		assertTrue(
				"Mock machine should have received message with glass. Event log: "
						+ machine1.log.toString(), machine1.log
						.containsString("Received message msgLoadGlass from popup."));
		
		
		assertEquals(
				"Only 2 message should have been sent to the machine. Event log: "
						+ machine1.log.toString(), 2, machine1.log.size());
		
		
		// Add a second piece of glass
		popup.msgHereIsGlass(glass);
		popup.pickAndExecuteAnAction();

		// Simulate the glass passing through the popup
		//popup.eventFired(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		popup.pickAndExecuteAnAction();
		
		popup.pickAndExecuteAnAction();
		popup.pickAndExecuteAnAction();
		

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock machine should have received message with glass. Event log: "
						+ machine2.log.toString(), machine2.log
						.containsString("Received message msgHereIsGlass from popup."));
		
		assertTrue(
				"Mock machine should have received message with glass. Event log: "
						+ machine2.log.toString(), machine2.log
						.containsString("Received message msgLoadGlass from popup."));
		
		
		assertEquals(
				"Only 2 message should have been sent to the machine. Event log: "
						+ machine2.log.toString(), 2, machine2.log.size());
		
	}
}
