
package transducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * This class is the fundamental support for front/back end communication.
 * Any class implementing the TReceiver interface can use the Transducer to
 * listen to events on any property channel.
 */
public class Transducer
{
	/**
	 * Switches what debug prints are on
	 */
	private static volatile boolean debugEvents = false, debugActions = false, debugScheduler = false;

	/**
	 * Semaphore that holds whether the internal state has changed
	 */
	private Semaphore stateChange = new Semaphore(1, true);

	/**
	 * Thread that processes transducer events
	 */
	private Thread transducerThread;

	/**
	 * A mapping of all the properties and Agents that are registered
	 */
	private Map<TChannel, Set<TReceiver>> diverter;

	/**
	 * The synchronized queue of TReceivers waiting to be registered
	 */
	private Queue<RegistrationEvent> toBeRegistered;

	/**
	 * The synchronized queue of TRecievers waiting to be unregistered
	 */
	private Queue<RegistrationEvent> toBeUnregistered;

	/**
	 * The synchronized queue of events that need to be fired
	 */
	private Queue<TransducerEvent> toBeFired;
	
	/**
	 * Internal class to package register events
	 */
	private class RegistrationEvent
	{
		TReceiver receiver;

		TChannel channel;

		public RegistrationEvent(TReceiver r, TChannel c)
		{
			receiver = r;
			channel = c;
		}
	}

	/**
	 * Internal class to package transducer events
	 */
	private class TransducerEvent
	{
		TChannel channel;

		TEvent event;

		Object[] args;

		public TransducerEvent(TChannel c, TEvent e, Object[] a)
		{
			channel = c;
			event = e;
			args = a;
		}
	}

	/**
	 * Internal thread that processes events
	 */
	private class TransducerThread extends Thread
	{
		private volatile boolean goOn = false;

		private TransducerThread()
		{
			super();
		}

		public void run()
		{
			goOn = true;

			while (goOn)
			{
				try
				{
					stateChange.acquire();
					while (processNextEvent())
						;
				}
				catch (Exception e)
				{
					System.err.println("Exception in transducer: " + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}

	/**
	 * Default constructor creates an empty map
	 */
	public Transducer()
	{
		diverter = new HashMap<TChannel, Set<TReceiver>>();
		toBeRegistered = new LinkedBlockingQueue<RegistrationEvent>();
		toBeUnregistered = new LinkedBlockingQueue<RegistrationEvent>();
		toBeFired = new LinkedBlockingQueue<TransducerEvent>();
	}

	/**
	 * Sets what kind of debug messages to print
	 */
	public static void setDebugMode(TransducerDebugMode tdm)
	{
		if(tdm == TransducerDebugMode.NONE)
		{
			debugEvents = false;
			debugActions = false;
			debugScheduler = false;
		}
		else if(tdm == TransducerDebugMode.EVENTS_ONLY)
		{
			debugEvents = true;
			debugActions = false;
			debugScheduler = false;
		}
		else if(tdm == TransducerDebugMode.EVENTS_AND_ACTIONS)
		{
			debugEvents = true;
			debugActions = true;
			debugScheduler = false;
		}
		else if(tdm == TransducerDebugMode.EVENTS_AND_ACTIONS_AND_SCHEDULER)
		{
			debugEvents = true;
			debugActions = true;
			debugScheduler = true;
		}
	}

	/**
	 * Starts up the transducer, should only be called once at init time.
	 */
	public synchronized void startTransducer()
	{
		if (transducerThread == null)
		{
			transducerThread = new TransducerThread();
			transducerThread.start();

			if (debugActions)
				System.out.println("Transducer: " + "STARTED");
		}
		else
		{
			transducerThread.interrupt();
		}
	}

	/**
	 * Processes the next event in the transducer's queue
	 */
	private boolean processNextEvent()
	{
		if (debugScheduler) System.out.println("Transducer: " + "processing next event...");

		boolean active = false;

		if (!toBeRegistered.isEmpty())
		{
			performRegister(toBeRegistered.poll());
			active = true;
		}
		else if (!toBeUnregistered.isEmpty())
		{
			performUnregister(toBeUnregistered.poll());
			active = true;
		}
		else if (!toBeFired.isEmpty())
		{
			performFireEvent(toBeFired.poll());
			active = true;
		}
		else
		{
			if (debugScheduler) System.out.println("Transducer: " + "no events to process!");
		}

		return active;
	}

	/**
	 * Called internally to signal an internal state change
	 */
	private void stateChanged()
	{
		stateChange.release();
	}

	/**
	 * Registers an Receiver with the Transducer so that they can receive messages.
	 * The Receiver will receive messages everytime an event with the corresponding
	 * property is activated.
	 * 
	 * @param toRegister
	 *        the Receiver to register
	 * @param channel
	 *        the channel to listen to
	 */
	public void register(TReceiver toRegister, TChannel channel)
	{
		if (debugActions)
			System.out.println("Transducer: " + "Queueing registration of " + toRegister + " on channel " + channel);
		RegistrationEvent next = new RegistrationEvent(toRegister, channel);
		toBeRegistered.add(next);
		this.stateChanged();
	}

	/**
	 * Internal helper method that actually performs registration
	 */
	private boolean performRegister(RegistrationEvent next)
	{
		TReceiver toRegister = next.receiver;
		TChannel channel = next.channel;

		if (debugEvents)
			System.out.println("Transducer: " + "Registering " + toRegister + " on channel " + channel + "...");

		if (toRegister == null)
		{
			throw new NullPointerException("Cannot register null receivers!");
		}
		else if (channel == null)
		{
			throw new NullPointerException("Cannot register null channels!");
		}

		Set<TReceiver> prevRegistered = diverter.get(channel);
		boolean success = false;

		// No Receiver registered to property, create a new entry
		if (prevRegistered == null)
		{
			if(debugActions) 
				System.out.println("Transducer: " + "Creating new channel entry " + channel + " for " + toRegister);

			// create new Receiver set and add it to diverter
			prevRegistered = new HashSet<TReceiver>();
			prevRegistered.add(toRegister);
			diverter.put(channel, prevRegistered);
			success = true;
		}
		// Other Agents registered to event, attempt to add new Receiver
		else
		{
			if(debugActions)
				System.out.println("Transducer: " + "Attempting to add " + toRegister + " to channel entry " + channel);

			// add Receiver to existing set in diverter
			success = prevRegistered.add(toRegister);
		}

		if (success)
		{
			if(debugActions) 
				System.out.println("Transducer: " + toRegister + " successfully registered on channel " + channel);
		}

		return success;
	}

	/**
	 * Unregisters an Receiver with the Transducer so that they stop receiving messages for a specific property.
	 * 
	 * @param toUnregister
	 *        the Receiver to unregister
	 * @param channel
	 *        the channel to stop listening to
	 */
	public void unregister(TReceiver toUnregister, TChannel channel)
	{
		if (debugActions)
			System.out.println("Transducer: " + "Adding unregistration of " + toUnregister + " on channel " + channel
				+ " to queue.");
		RegistrationEvent next = new RegistrationEvent(toUnregister, channel);
		toBeUnregistered.add(next);
		this.stateChanged();
	}

	/**
	 * Internal helper method that actually performs unregistration
	 */
	private boolean performUnregister(RegistrationEvent next)
	{
		TReceiver toUnregister = next.receiver;
		TChannel channel = next.channel;

		if (debugEvents)
			System.out.println("Transducer: " + "Unregistering " + toUnregister + " on channel " + channel + "...");

		if (toUnregister == null)
		{
			throw new NullPointerException("Cannot unregister null receivers!");
		}
		else if (channel == null)
		{
			throw new NullPointerException("Cannot unregister null channels!");
		}

		Set<TReceiver> prevRegistered = diverter.get(channel);
		boolean success;

		// No Receiver registered to property
		if (prevRegistered == null)
		{
			success = false;
		}
		// Agents registered to property, attempt removal of current Receiver
		else
		{
			success = prevRegistered.remove(toUnregister);
		}

		if (success)
		{
			if(debugActions)
				System.out.println("Transducer: " + toUnregister + " successfully unregistered from channel " + channel);
		}

		return success;
	}

	/**
	 * Fires an event on the Transducer.
	 * All TReceivers listening to the property will be notified.
	 * It is the TReceiver's responsibility to parse the event in a meaningful way.
	 * 
	 * @param channel
	 *        the channel to fire the event on
	 * @param event
	 *        the event to fire
	 * @param args
	 *        any additional arguments associated with the event
	 */
	public void fireEvent(TChannel channel, TEvent event, Object[] args)
	{
		if (debugActions)
			System.out.println("Transducer: " + "Adding event " + event + " on channel " + channel + " to queue.");
		TransducerEvent next = new TransducerEvent(channel, event, args);
		toBeFired.add(next);
		this.stateChanged();
	}

	/**
	 * Internal helper method that actually fires event
	 */
	private int performFireEvent(TransducerEvent next)
	{
		TEvent event = next.event;
		TChannel channel = next.channel;
		Object[] args = next.args;

		if (debugEvents)
			System.out.println("Transducer: " + "Firing event " + event + " on channel " + channel);

		if (channel == null)
		{
			throw new NullPointerException("Cannot fire events on null channels!");
		}
		else if (event == null)
		{
			throw new NullPointerException("Cannot fire null events!");
		}

		// look for Receivers listening to the desired channel
		Set<TReceiver> toNotify = diverter.get(channel);
		int numNotified = 0;

		if (toNotify == null)
		{
			if (debugActions)
				System.out.println("Transducer: " + "Channel " + channel + " has never been registered!");
		}
		else
		{
			// fire events to all listening Receivers, if any exist
			if (!toNotify.isEmpty())
			{
				for (TReceiver r : toNotify)
				{
					r.eventFired(channel, event, args);
				}
			}
		}

		return numNotified;
	}
	
	//sleep transducer thread
	public void sleepTransducerThread(int time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
