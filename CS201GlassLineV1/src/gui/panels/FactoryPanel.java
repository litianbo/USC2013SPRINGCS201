
package gui.panels;

import engine.agent.families.cf0.ConveyorFamilyZero;
import engine.agent.families.cf1.ConveyorFamilyOne;
import engine.agent.families.cf10.ConveyorAgentTen;
import engine.agent.families.cf11.ConveyorAgentEleven;
import engine.agent.families.cf12.ConveyorFamilyTwelve;
import engine.agent.families.cf13.ConveyorAgentThirteen;
import engine.agent.families.cf14.ConveyorAgentFourteen;
import engine.agent.families.cf2.ConveyorFamilyTwo;
import engine.agent.families.cf3.ConveyorFamilyThree;
import engine.agent.families.cf4.ConveyorFamilyFour;
import engine.agent.families.cf5.ConveyorFamilyInterfaceClass;
import engine.agent.families.cf6.ConveyorFamilySix;
import engine.agent.families.cf7.ConveyorFamilySeven;
import engine.agent.families.cf8.ConveyorAgentEight;
import engine.agent.families.cf9.ConveyorFamilyNine;
import gui.drivers.FactoryFrame;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import transducer.Transducer;

/**
 * The FactoryPanel is highest level panel in the actual kitting cell. The
 * FactoryPanel makes all the back end components, connects them to the
 * GuiComponents in the DisplayPanel. It is responsible for handing
 * communication between the back and front end.
 */
@SuppressWarnings("serial")
public class FactoryPanel extends JPanel
{
	/** The frame connected to the FactoryPanel */
	private FactoryFrame parent;

	/** The control system for the factory, displayed on right */
	private ControlPanel cPanel;

	/** The graphical representation for the factory, displayed on left */
	private DisplayPanel dPanel;

	/** Allows the control panel to communicate with the back end and give commands */
	private Transducer transducer;

	/**
	 * Constructor links this panel to its frame
	 */
	public FactoryPanel(FactoryFrame fFrame)
	{
		parent = fFrame;

		// initialize transducer
		transducer = new Transducer();
		transducer.startTransducer();

		// transducer.setDebugMode(TransducerDebugMode.EVENTS_AND_ACTIONS);
		
		// use default layout
		// dPanel = new DisplayPanel(this);
		// dPanel.setDefaultLayout();
		// dPanel.setTimerListeners();

		// initialize and run
		this.initialize();
		this.initializeBackEnd();
	}

	/**
	 * Initializes all elements of the front end, including the panels, and lays
	 * them out
	 */
	private void initialize()
	{
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// initialize control panel
		cPanel = new ControlPanel(this, transducer);

		// initialize display panel
		dPanel = new DisplayPanel(this, transducer);

		// add panels in
		// JPanel tempPanel = new JPanel();
		// tempPanel.setPreferredSize(new Dimension(830, 880));
		// this.add(tempPanel);

		this.add(dPanel);
		this.add(cPanel);
	}

	/**
	 * Feel free to use this method to start all the Agent threads at the same time
	 */
	private void initializeBackEnd()
	{
		/** Setup Non-Norm Panel **/
		/*NonNormFrame nonNormFrame = new NonNormFrame();
		nonNormFrame.setAllTransducer(transducer);
		
		nonNormFrame.showFrame();*/
		
		/** CREATE CONVEYOR FAMILIES **/
		ConveyorFamilyZero familyZero = new ConveyorFamilyZero("Cutter", this.transducer);
		ConveyorFamilyOne familyOne = new ConveyorFamilyOne("First Shuttle", transducer);
		ConveyorFamilyTwo familyTwo = new ConveyorFamilyTwo("Breakout", transducer);
		ConveyorFamilyThree familyThree = new ConveyorFamilyThree("Manual Breakout", transducer);
		ConveyorFamilyFour familyFour = new ConveyorFamilyFour("Second Shuttle", transducer);
		ConveyorFamilyInterfaceClass familyFive = new ConveyorFamilyInterfaceClass(transducer);
		ConveyorFamilySix familySix = new ConveyorFamilySix(transducer);
		ConveyorFamilySeven familySeven = new ConveyorFamilySeven(transducer);		
		ConveyorAgentEight familyEight = new ConveyorAgentEight(transducer);
		ConveyorFamilyNine familyNine = new ConveyorFamilyNine(transducer);
		ConveyorAgentTen familyTen = new ConveyorAgentTen(transducer);
		ConveyorAgentEleven familyEleven = new ConveyorAgentEleven(transducer);
		ConveyorFamilyTwelve familyTwelve = new ConveyorFamilyTwelve(transducer);
		ConveyorAgentThirteen familyThirteen = new ConveyorAgentThirteen(transducer);
		ConveyorAgentFourteen familyFourteen = new ConveyorAgentFourteen(transducer);
		
		/** SET NEIGHBORING FAMILIES **/
		familyZero.setNextFamily(familyOne);
		familyOne.setPreviousFamily(familyZero);
		familyOne.setNextFamily(familyTwo);
		familyTwo.setPreviousFamily(familyOne);
		familyTwo.setNextFamily(familyThree);
		familyThree.setPreviousFamily(familyTwo);
		familyThree.setNextFamily(familyFour);
		familyFour.setPreviousFamily(familyThree);
		familyFour.setNextFamily(familyFive);
		familyFive.setPreviousFamily(familyFour);
		familyFive.setNextFamily(familySix);
		familySix.setPreviousFamily(familyFive);
		familySix.setNextFamily(familySeven);
		familySeven.setPreviousFamily(familySix);
		familySeven.setNextFamily(familyEight);		
		familyEight.setPreviousFamily(familySeven);
		familyEight.setNextFamily(familyNine);
		familyNine.setPreviousFamily(familyEight);
		familyNine.setNextFamily(familyTen);
		familyTen.setPreviousFamily(familyNine);
		familyTen.setNextFamily(familyEleven);
		familyEleven.setPreviousFamily(familyTen);
		familyEleven.setNextFamily(familyTwelve);
		familyTwelve.setPreviousFamily(familyEleven);
		familyTwelve.setNextFamily(familyThirteen);
		familyThirteen.setPreviousFamily(familyTwelve);
		familyThirteen.setNextFamily(familyFourteen);
		familyFourteen.setPreviousFamily(familyThirteen);		

		/** START CONVEYOR FAMILY AGENT THREADS **/
		familyZero.startThread();
		familyOne.startThread();
		familyTwo.startThread();
		familyThree.startThread();
		familyFour.startThread();
		familySix.startAgentThreads();
		// family seven's threads are started in the constructor
		familyEight.startThread();
		familyNine.startThread();
		familyTen.startThread();
		familyEleven.startThread();
		familyTwelve.startThread();
		familyThirteen.startThread();
		familyFourteen.startThread();
		
		//Glass testGlass = new Glass(new Recipe(true, true, true, true, true, true, true, true, true, true));
		//familyZero.msgHereIsGlass(temp);
		
		System.out.println("Back end initialization finished.");
	}

	/**
	 * Returns the parent frame of this panel
	 * 
	 * @return the parent frame
	 */
	public FactoryFrame getGuiParent()
	{
		return parent;
	}

	/**
	 * Returns the control panel
	 * 
	 * @return the control panel
	 */
	public ControlPanel getControlPanel()
	{
		return cPanel;
	}

	/**
	 * Returns the display panel
	 * 
	 * @return the display panel
	 */
	public DisplayPanel getDisplayPanel()
	{
		return dPanel;
	}
}
