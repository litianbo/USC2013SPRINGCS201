package engine.agent.families.cf5.test;

import org.junit.Test;

import junit.framework.TestCase;
import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.families.Glass;
import engine.agent.families.Recipe;
import engine.agent.families.cf5.ConveyorFamilyInterfaceClass;
import engine.agent.families.cf5.test.mock.EventLog;
import engine.agent.families.cf5.test.mock.LoggedEvent;
import engine.agent.families.cf5.test.mock.MockConveyorFamily;

public class BlackBoxTest extends TestCase implements TReceiver {

	/**
	 * This is the ConveyorFamilyInterface to be tested.
	 */
	public ConveyorFamilyInterfaceClass conveyorFamilyIntefaceClass;
	
	public Transducer myTransducer;
	
	public EventLog transducerLog = new EventLog();
	
	public MockConveyorFamily previousConveyorFamily;
	public MockConveyorFamily nextConveyorFamily;
	
	public void setup(){
		// initialize the transducer
		myTransducer = new Transducer();
		myTransducer.startTransducer();
		
		// Register to popup channel
		myTransducer.register(this, TChannel.POPUP);
		
		// Register to conveyor channel
		myTransducer.register(this, TChannel.CONVEYOR);
		
		// Register to sensor channel
		myTransducer.register(this, TChannel.SENSOR);
		
		// Register to drill channel
		myTransducer.register(this, TChannel.DRILL);
		
		// Create a MockConveyorFamily
		previousConveyorFamily = new MockConveyorFamily("Conveyor 4");
		nextConveyorFamily = new MockConveyorFamily("Conveyor 6");
		
		conveyorFamilyIntefaceClass = new ConveyorFamilyInterfaceClass(myTransducer);
		conveyorFamilyIntefaceClass.setPreviousFamily(previousConveyorFamily);
		conveyorFamilyIntefaceClass.setNextFamily(nextConveyorFamily);
	}
	
	/**
	 * This method creates a ConveyorFamilyInterface and runs a black box test
	 * involving one glass pane which does not need processing
	 * @throws InterruptedException 
	 */
	@Test
	public void testOneGlassNoneProcessing() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe());
		
		setup();
		
		//Message conveyor family that there is incoming glass
		conveyorFamilyIntefaceClass.msgHereIsGlass(testGlass);
		
		/**
		 * Simulate glass passing over entry sensor
		 */
		
		// Send SENSOR_GUI_PRESSED fired event
		Integer[] args = new Integer[1];
		args[0] = 10;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Send SENSOR_GUI_PRESSED fired event
		args = new Integer[1];
		args[0] = 10;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		/**
		 * Simulate glass passing over popup sensor
		 */
		
		// Send SENSOR_GUI_PRESSED fired event
		args = new Integer[1];
		args[0] = 11;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Send SENSOR_GUI_PRESSED fired event
		args = new Integer[1];
		args[0] = 11;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		/**
		 * Simulate glass passing over onto popup
		 */
		
		// Send POPUP_GUI_LOAD_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		
		// Send POPUP_GUI_MOVED_DOWN fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue(
				"Next Conveyor Family should have received a hereIsGlass message. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue(
				"Glass state should not have changed. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("state is start"));
		
		assertTrue("Next Conveyor Family should have only received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);

	}
	
	/**
	 * This method creates a ConveyorFamilyInterface and runs a black box test
	 * involving one glass pane which does need processing
	 * @throws InterruptedException 
	 */
	@Test
	public void testOneGlassNeedProcessing() throws InterruptedException {

		Glass testGlass = new Glass(new Recipe(false, false, false, false, true, false, false, false, false, false));
		
		setup();
		
		//Message conveyor family that there is incoming glass
		conveyorFamilyIntefaceClass.msgHereIsGlass(testGlass);
		
		/**
		 * Simulate glass passing over entry sensor
		 */
		
		// Send SENSOR_GUI_PRESSED fired event
		Integer[] args = new Integer[1];
		args[0] = 10;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Send SENSOR_GUI_PRESSED fired event
		args = new Integer[1];
		args[0] = 10;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		/**
		 * Simulate glass passing over popup sensor
		 */
		
		// Send SENSOR_GUI_PRESSED fired event
		args = new Integer[1];
		args[0] = 11;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_PRESSED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Send SENSOR_GUI_PRESSED fired event
		args = new Integer[1];
		args[0] = 11;
		myTransducer.fireEvent(TChannel.SENSOR, TEvent.SENSOR_GUI_RELEASED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		/**
		 * Simulate glass passing over onto popup
		 */
		
		// Send POPUP_GUI_LOAD_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);

		// Now, make asserts to make sure that the scheduler did what it was
		// supposed to.
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_UP"));
		
		// Send POPUP_GUI_MOVED_UP fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: WORKSTATION_DO_LOAD_GLASS"));
		
		// Send WORKSTATION_LOAD_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_LOAD_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: WORKSTATION_DO_ACTION"));
		
		// Send WORKSTATION_GUI_ACTION_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_GUI_ACTION_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: WORKSTATION_RELEASE_GLASS"));
		
		// Send WORKSTATION_RELEASE_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_RELEASE_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		// Send POPUP_GUI_LOAD_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_LOAD_FINISHED, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_DO_MOVE_DOWN"));
		
		// Send POPUP_GUI_LOAD_FINISHED fired event
		args = new Integer[1];
		args[0] = 0;
		myTransducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
		
		//sleep needed for agent to check fired events
		Thread.sleep(100);
		
		assertTrue(
				"Transducer should have received fired event. Event log: "
						+ getLogs(), transducerLog
						.containsString("Received fired event: POPUP_RELEASE_GLASS"));
		assertTrue(
				"Next Conveyor Family should have received a hereIsGlass message. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("Received message msgHereIsGlass"));
		assertTrue(
				"Glass state should not have changed. Event log: "
						+ getLogs(), nextConveyorFamily.log
						.containsString("state is doneDrilling"));
		
		assertTrue("Next Conveyor Family should have only received 1 message."
				+ getLogs(), nextConveyorFamily.log.size() == 1);

	}
	
	public String getLogs() {
		StringBuilder sb = new StringBuilder();
		String newLine = System.getProperty("line.separator");
		
		sb.append("-------Next Conveyor Family Log-------");
		sb.append(newLine);
		sb.append(nextConveyorFamily.log.toString());
		sb.append("-------Next Conveyor Family Log-------");
		
		sb.append(newLine);
		
		sb.append("-------Previous Conveyor Family Log-------");
		sb.append(newLine);
		sb.append(previousConveyorFamily.log.toString());
		sb.append("-------Previous Conveyor Family Log-------");
		
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
