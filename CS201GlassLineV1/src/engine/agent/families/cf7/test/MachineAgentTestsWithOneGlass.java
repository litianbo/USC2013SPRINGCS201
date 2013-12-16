/**
 * 
 */
package engine.agent.families.cf7.test;

import org.junit.Test;

import junit.framework.TestCase;
import transducer.TChannel;
import transducer.TEvent;
import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf7.MachineAgent;
import engine.agent.families.cf7.test.mock.MockPopup;

/**
 * This is a set of tests for the normal machine interaction cycle. This suite
 * tests all of the machine messages, with a single popup. Note, this does not
 * test multiple popups. These are contained in the
 * machineAgentTestsWithTwopopups. <br>
 * <br>
 * Note that the unit of test in this file is the individual message. We call
 * the appropriate functions to get the machineAgent ready to accept and process
 * a particular message and then make assert statements surrounding that
 * message. This breaks testing this one interaction up into 5 different test
 * methods. You could also integrate all of this into one large test method. We
 * do this when testing a machineAgent with multiple popups. Both styles are
 * valid and which one is best depends on the goal of your particular tests. <br>
 * This set of tests is invaluable when first writing your machineAgent. It will
 * validate that your machine implements the messaging contract described in the
 * interaction diagram. In building an agent system, I would actually write
 * these unit tests before writing the machineAgent itself. This practice is
 * called Test Driven Development (TDD).
 * 
 * @author Sean Turner
 * 
 */
public class MachineAgentTestsWithOneGlass extends TestCase {

	/**
	 * This is the machineAgent to be tested.
	 */
	public MachineAgent machine;

	/**
	 * Test method for
	 * {@link restaurant.machineAgent#msgSitpopupAtTable(restaurant.popupAgent, int)}
	 * .
	 * 
	 * This method creates a machineAgent and a Mockpopup. The machine is
	 * messaged that the popup needs to be seated. The machine's scheduler is
	 * then called. The popup should receive msgFollowMeToTable after the
	 * scheduler is called.
	 */
	@Test
	public void testMsgHereIsGlass() {
		
		machine = new MachineAgent(null, 1);
		
		// Create mock agents
		MockPopup popup = new MockPopup("Popup1");
		machine.setPopup(popup);
		
		// Create a piece of glass
		Recipe recipe = new Recipe(false, false, false, false, true, false, false, false, false, false);
		Glass glass = new Glass(recipe);
		
		// Message the machine with a piece of glass
		machine.msgHereIsGlass(glass);
		
		// This will check that you're not messaging the popup in the
		// machine's message reception.
		assertEquals(
				"Mock Popup should have an empty event log before the machine's scheduler is called. Instead, the mock Popup's event log reads: "
						+ popup.log.toString(), 0, popup.log.size());
		
		machine.pickAndExecuteAnAction();
	
		assertEquals(
				"Mock Popup should have an empty event log before the machine's scheduler is called. Instead, the mock Popup's event log reads: "
						+ popup.log.toString(), 0, popup.log.size());
		
		Object args[] = { (Object) 1 };
		machine.eventFired(TChannel.DRILL, TEvent.WORKSTATION_GUI_ACTION_FINISHED, args);
		machine.pickAndExecuteAnAction();
		
		assertEquals(
				"Mock Popup should have an empty event log before the machine's scheduler is called. Instead, the mock Popup's event log reads: "
						+ popup.log.toString(), 0, popup.log.size());
		
		machine.pickAndExecuteAnAction();
		
		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.

		assertTrue(
				"Mock Popup should have received message with glass. Event log: "
						+ popup.log.toString(), popup.log
						.containsString("Received message msgGlassMachined from workstation."));
		
		assertEquals(
				"Only 1 message should have been sent to the popup. Event log: "
						+ popup.log.toString(), 1, popup.log.size());
	}
}
