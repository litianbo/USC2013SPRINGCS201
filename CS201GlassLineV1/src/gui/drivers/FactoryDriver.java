package gui.drivers;

import transducer.Transducer;
import transducer.TransducerDebugMode;

/**
 * The FactoryDriver class is the main class for running the factory program.
 * 
 * It creates a Frame, adds the factory components and agents in, and shows the
 * window.
 */
public class FactoryDriver
{
	/**
	 * Main method to run the program.
	 */
	public static void main(String[] args)
	{		
		TransducerDebugMode mode = TransducerDebugMode.NONE;
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equalsIgnoreCase("tdebug1"))
			{
				mode = TransducerDebugMode.EVENTS_ONLY;
			}
			else if(args[i].equalsIgnoreCase("tdebug2"))
			{
				mode = TransducerDebugMode.EVENTS_AND_ACTIONS;
			}
			else if(args[i].equalsIgnoreCase("tdebug3"))
			{
				mode = TransducerDebugMode.EVENTS_AND_ACTIONS_AND_SCHEDULER;
			}
		}
		Transducer.setDebugMode(mode);
		
		FactoryFrame myFactory = new FactoryFrame();
		//moved to FactoryPanel to inherit transducer
		
		myFactory.setLocation(50, 50);
		myFactory.showFrame();

		System.out.println("Factory frame displayed.");
	}
}
