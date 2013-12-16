package gui.panels.subcontrolpanels;

import engine.agent.Agent;

/**
 * TracePanelMessage represents a message and contains the agent
 * that sent the message.
 */
public class TracePanelMessage 
{
	/**
	 * The sender of the message.
	 */
	private Agent agent;
	/**
	 * The message sent
	 */
	private String message;
	/**
	 * Default constructor
	 */
	public TracePanelMessage()
	{	}
	
	/**
	 * Constructor that takes in the message and the agent who sent the 
	 * message.
	 * @param message The message the display
	 * @param agent The sender
	 */
	public TracePanelMessage(String message, Agent agent)
	{
		this.message = message;
		this.agent = agent;
	}
	
	/**
	 * Returns the sender of the message, an agent.
	 * @return the sender of the message
	 */
	public Agent getAgent()
	{
		return agent;
	}
	
	/**
	 * Returns the message that the agent sent.
	 * @return the message sent
	 */
	public String returnMessage()
	{
		return message;
	}
}
