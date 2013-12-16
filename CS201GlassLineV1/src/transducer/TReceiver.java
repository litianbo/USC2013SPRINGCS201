
package transducer;

/**
 * The superclass for all transducer recievers. Any class implementing TReceiver can
 * register itself with the transducer in order to send and listen to events. All
 * classes in the factory that need to communicate across the front/back end should
 * implement this class and use the transducer to fire events.
 */
public interface TReceiver
{
	/**
	 * Called every time the transducer is fired. Subclasses should parse out the
	 * channel and message and respond accordingly. It is heavily recommended that
	 * subclasses synchronize this method implementation.
	 * 
	 * @param channel
	 *        the channel the event was fired on
	 * @param event
	 *        the event type
	 * @param args
	 *        any arguments the message needs
	 */
	public void eventFired(TChannel channel, TEvent event, Object[] args);
}
