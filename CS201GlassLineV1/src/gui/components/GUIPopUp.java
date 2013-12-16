package gui.components;

import gui.panels.DisplayPanel;
import gui.panels.FactoryPanel;

import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.ImageIcon;

import shared.enums.ConveyorDirections;
import shared.enums.MachineType;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

/**
 * GUIPopUp is a graphical representation of the popup component that transfers
 * glass off the conveyor
 */
@SuppressWarnings("serial")
public class GUIPopUp extends GuiComponent implements Serializable {
	ConveyorDirections direction;

	/**
	 * Factory frame parent
	 */
	FactoryPanel fp;

	/**
	 * Boolean of whether or not to do pop up animation
	 */
	boolean doPopUpAnimation;

	/**
	 * Boolean of whether or not to do pop down animation
	 */
	boolean doPopDownAnimation;

	/**
	 * Pop ups part
	 */
	GUIGlass guiPart;

	GUIComponentOffline[] pairedMachines = new GUIComponentOffline[2];

	/**
	 * Image icons
	 */
	public static ImageIcon iconWithoutPart = new ImageIcon(
			"imageicons/popup.png");

	static ImageIcon iconWithPart = new ImageIcon("imageicons/popup-good.png");

	private enum PopUpLoadState {
		LOADING, EMPTY, FULL, RELEASING
	};

	private enum PopUpHeightState {
		RISING, DROPPING, UP, DOWN
	};

	PopUpLoadState loadState = PopUpLoadState.EMPTY;

	PopUpHeightState heightState = PopUpHeightState.DOWN;

	Integer index;

	/**
	 * Constructor for GUIPopUp
	 * 
	 * @param p
	 *            The panel that displays the factory
	 * @param popUp
	 *            The popUp agents
	 */
	public GUIPopUp(DisplayPanel p, Transducer t) {
		super(p);
		doPopUpAnimation = false;
		doPopDownAnimation = false;
		setIcon(iconWithoutPart);
		this.setSize(iconWithoutPart.getIconHeight(),
				iconWithoutPart.getIconWidth());
		this.setIcon(iconWithoutPart);
		transducer = t;
		transducer.register(this, TChannel.POPUP);
	}

	public void setPairedMachineType(MachineType type) {
		if (type.equals(MachineType.DRILL)) {
			transducer.register(this, TChannel.DRILL);
		}
		if (type.equals(MachineType.GRINDER)) {
			transducer.register(this, TChannel.GRINDER);
		}
		if (type.equals(MachineType.CROSS_SEAMER)) {
			transducer.register(this, TChannel.CROSS_SEAMER);
		}

	}

	public void setPairedMachine(GUIComponentOffline machine, int index) {
		pairedMachines[index] = machine;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	/**
	 * Method to do the pop-up animation
	 */
	public void popUpAnimation() {
		setIcon(iconWithPart);
		heightState = PopUpHeightState.UP;
		Object[] args = new Object[1];
		args[0] = index;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_UP, args);
	}

	/**
	 * Method to do pop-down animation
	 */
	public void popDownAnimation() {
		setIcon(iconWithoutPart);
		heightState = PopUpHeightState.DOWN;
		Object[] args = new Object[1];
		args[0] = index;
		transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_GUI_MOVED_DOWN, args);
	}

	@Override
	public void addPart(GUIGlass part) {
		this.part = part;
		loadState = PopUpLoadState.LOADING;
	}

	/**
	 * Method that is called by the timer, calls the animation methods
	 * 
	 * @param e
	 *            ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (loadState == PopUpLoadState.LOADING) {
			movePartIn();
		} else if (heightState == PopUpHeightState.RISING) {
			popUpAnimation();
		} else if (heightState == PopUpHeightState.DROPPING) {
			popDownAnimation();
		} else if (loadState == PopUpLoadState.RELEASING) {

			movePartOut();
			if (!part.getBounds().intersects(getBounds())) {

				nextComponent.addPart(part);
				part = null;
				loadState = PopUpLoadState.EMPTY;
				Object[] args = new Object[1];
				args[0] = index;
				transducer.fireEvent(TChannel.POPUP,
						TEvent.POPUP_GUI_RELEASE_FINISHED, args);
			}
		}
		if (heightState == PopUpHeightState.UP
				&& loadState == PopUpLoadState.FULL) {
			if (!part.getBounds().intersects(getBounds())) {
				part = null;
				loadState = PopUpLoadState.EMPTY;
			}
		}

	}

	/**
	 * Called from the actionPerformed method.
	 * 
	 * For the GUIPart in the movingPosition of the GUIConveyor, increments its
	 * position toward partFinalPos. Also increments the position of the
	 * GUIConveyorArrow(s) in the same direction and magnitude as the GUIParts.
	 * Upon completion of a "one block" movement of the parts, informs the
	 * agent.
	 */
	private void movePartIn() {
		if (part.getCenterX() < getCenterX())
			part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
		else if (part.getCenterX() > getCenterX())
			part.setCenterLocation(part.getCenterX() - 1, part.getCenterY());

		if (part.getCenterY() < getCenterY())
			part.setCenterLocation(part.getCenterX(), part.getCenterY() + 1);
		else if (part.getCenterY() > getCenterY())
			part.setCenterLocation(part.getCenterX(), part.getCenterY() - 1);

		if (part.getCenterX() == getCenterX()
				&& part.getCenterY() == getCenterY()) {
			loadState = PopUpLoadState.FULL;
			Object[] args = new Object[1];
			args[0] = index;
			transducer.fireEvent(TChannel.POPUP,
					TEvent.POPUP_GUI_LOAD_FINISHED, args);
		}
	}

	private void movePartOut() {
		part.setCenterLocation(part.getCenterX() + 1, part.getCenterY());
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		//System.out.println(channel + " + " + event + " " + args[0] + " "
		//		+ index);
		if (channel == TChannel.POPUP && event == TEvent.POPUP_DO_MOVE_UP
				&& ((Integer) args[0]).equals(index)) {
			heightState = PopUpHeightState.RISING;
			return;
		} else if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_DO_MOVE_DOWN
				&& ((Integer) args[0]).equals(index)) {
			heightState = PopUpHeightState.DROPPING;
			return;
		} else if (channel == TChannel.POPUP
				&& event == TEvent.POPUP_RELEASE_GLASS
				&& ((Integer) args[0]).equals(index)) {

			loadState = PopUpLoadState.RELEASING;
			//System.out.println(loadState);
			return;
		} else if (event == TEvent.WORKSTATION_DO_LOAD_GLASS) {
			pairedMachines[((Integer) args[0])].addPart(part);
		}
	}
}
