package gui.components;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import shared.enums.ComponentOperations;
import transducer.TChannel;
import transducer.TEvent;

/**
 * GUIPart is a graphical representation of the glass that
 * is to be removed around on the conveyer
 * @author Anoop Kamboj
 *
 */
@SuppressWarnings("serial")
public class GUIGlass extends GuiComponent implements Serializable
{
	/**
	 * The rectangle of the glass pane
	 */
	Rectangle2D glassRect;
	/**
	 * Boolean to reflect if the part is broken or not. Initialized to false.
	 */
	boolean stateBroken = false;
	/**
	 * Enum that contains the last operation done to the glass
	 */
	ComponentOperations lastOperation;
	/**
	 * A number representing the part's position (block) on the conveyor
	 */
	int posInLine;
	/**
	 * The current angle of the part
	 */
	double currentAngle;
	/**
	 * The angle that the part needs to change to
	 */
	double targetAngle;
	/**
	 * Boolean to tell whether or not the part is done rotating
	 */
	boolean doneRotating;
	/**
	 * The speed that the part should rotate
	 */
	double rotationSpeed;
	/**
	 * File names for each of the various images for glass
	 */
	String filePathNONE = "imageicons/glassImage_NONE.png";//Base image
	/**
	 * Instances of image holder that hold the overlays for the glass for breakout
	 */
	ImageHolder imageBREAKOUT;
	/**
	 * Instances of image holder that hold the overlays for the glass for manual breakout
	 */
	ImageHolder imageMANUALBREAKOUT;
	/**
	 * Instances of image holder that hold the overlays for the glass for cross seamer
	 */
	ImageHolder imageCROSSSEAMER;
	/**
	 * Instances of image holder that hold the overlays for the glass for cutter
	 */
	ImageHolder imageCUTTER;
	/**
	 * Instances of image holder that hold the overlays for the glass for drill
	 */
	ImageHolder imageDRILL;
	/**
	 * Instances of image holder that hold the overlays for the glass for grinder
	 */
	ImageHolder imageGRINDER;
	/**
	 * Instances of image holder that hold the overlays for the glass for oven
	 */
	ImageHolder imageOVEN;
	/**
	 * Instances of image holder that hold the overlays for the glass for paint
	 */
	ImageHolder imagePAINT;
	/**
	 * Instances of image holder that hold the overlays for the glass for UVlamp
	 */
	ImageHolder imageUVLAMP;
	/**
	 * Instances of image holder that hold the overlays for the glass for washer
	 */
	ImageHolder imageWASHER;
	/**
	 * The current imageHolder overlay
	 */
	ImageHolder currentImageHolder;
	/**
	 * List of all image holders for the glass
	 */
	ArrayList<ImageHolder> imageHolders;
	/**
	 * The list of operations done on the part
	 */
	ArrayList<ComponentOperations> operationsOnPart;
	
	/**
	 * Public constructor for GUIPart
	 */
	public GUIGlass() 
	{
		super();
		imageHolders = new ArrayList<GUIGlass.ImageHolder>();
		operationsOnPart = new ArrayList<ComponentOperations>();
		currentAngle = 0;
		rotationSpeed = 3;
		doneRotating = true;
		glassRect = new Rectangle2D.Double();
	
		lastOperation = ComponentOperations.NONE;
		setIcon(new ImageIcon(filePathNONE));
		setSize(getIcon().getIconWidth(),getIcon().getIconHeight());
		setImageHolders();
	}
	/**
	 * Initializes the image holders
	 */
	public void setImageHolders()
	{
		imageBREAKOUT = new ImageHolder("imageicons/glassOverlays/glassImageHolder_BREAKOUT.png");
		imageHolders.add(imageBREAKOUT);
		imageCROSSSEAMER = new ImageHolder("imageicons/glassOverlays/glassImageHolder_CROSSSEAMER.png");
		imageHolders.add(imageCROSSSEAMER);
		imageCUTTER = new ImageHolder("imageicons/glassOverlays/glassImageHolder_CUTTER.png");
		imageHolders.add(imageCUTTER);
		imageDRILL = new ImageHolder("imageicons/glassOverlays/glassImageHolder_DRILL.png");
		imageHolders.add(imageDRILL);
		imageGRINDER = new ImageHolder("imageicons/glassOverlays/glassImageHolder_GRINDER.png");
		imageHolders.add(imageGRINDER);
		imageMANUALBREAKOUT = new ImageHolder("imageicons/glassOverlays/glassImageHolder_MANUALBREAKOUT.png");
		imageHolders.add(imageMANUALBREAKOUT);
		imageOVEN = new ImageHolder("imageicons/glassOverlays/glassImageHolder_OVEN.png");
		imageHolders.add(imageOVEN);
		imagePAINT = new ImageHolder("imageicons/glassOverlays/glassImageHolder_PAINT.png");
		imageHolders.add(imagePAINT);
		imageUVLAMP = new ImageHolder("imageicons/glassOverlays/glassImageHolder_UVLAMP.png");
		imageHolders.add(imageUVLAMP);
		imageWASHER = new ImageHolder("imageicons/glassOverlays/glassImageHolder_WASHER.png");
		imageHolders.add(imageWASHER);
	}
	/**
	 * Message to tell the part to rotate to an angle
	 * @param angle The angle that the part should rotate to
	 */
	public void msgRotatePart(double angle)
	{
		targetAngle = angle;
		doneRotating = false;
	}
	/**
	 * Method to animate rotating the part
	 */
	public void animate()
	{
		double angleDiff = targetAngle - currentAngle;
		if (currentAngle >= 360)
		{
			currentAngle -= 360;
		}
		else if (currentAngle <= -360)
		{
			currentAngle += 360;
		}
		if (angleDiff <= rotationSpeed)
		{
			currentAngle = targetAngle;
			doneRotating = true;
		}
		if (angleDiff == 0)
		{
			//Send message to agent
			doneRotating = true;
		}
		else if (angleDiff > 0)
		{
			currentAngle += rotationSpeed;
		}
		else 
		{
			currentAngle -= rotationSpeed;
		}
	}
	/**
	 * Changes the state of the display of the glass according to what the last operation
	 * was 
	 */
	public void changeState() 
	{
		switch (lastOperation)
		{
		case NONE:
			break;
		case BREAKOUT:
			imageBREAKOUT.display = true;
			break;
		case CROSSSEAMER:
			imageCROSSSEAMER.display = true;
			break;
		case CUTTER:
			imageCUTTER.display = true;
			break;
		case DRILL:
			imageDRILL.display = true;
			break;
		case GRINDER:
			imageGRINDER.display = true;
			break;
		case OVEN:
			imageOVEN.display = true;
			break;
		case PAINT:
			imagePAINT.display = true;
			break;
		case UVLAMP:
			imageUVLAMP.display = true;
			break;
		case WASHER:
			imageWASHER.display = true;
			break;
		}
	}
	/**
	 * Moves the glass's rectangle and sets its width and height to match that of the JLabel
	 */
	public void setupRect()
	{
		glassRect.setRect(getX(), getY(), getIcon().getIconWidth(), getIcon().getIconHeight());
	}
	
	/**
	 * Repaints, calls setupRect(), and calls changeState()
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		repaint();
		setupRect();
		if (!doneRotating)
		{
			animate();
		}
	}
	/**
	 * Sets the last operation done on the part so that a new image may be drawn
	 * @param operation
	 */
	public void setLastOperation(ComponentOperations operation)
	{
		lastOperation = operation;
		operationsOnPart.add(operation);
		changeState();
	}
	/**
	 * Draws the overlays over the part
	 * @param g2d The graphic that it should draw to
	 */
	public void drawOverlays(Graphics2D g2d)
	{
		if (imageBREAKOUT.display)
		{
			g2d.drawImage(imageBREAKOUT.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageMANUALBREAKOUT.display)
		{
			g2d.drawImage(imageMANUALBREAKOUT.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageCROSSSEAMER.display)
		{
			g2d.drawImage(imageCROSSSEAMER.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageCUTTER.display)
		{
			g2d.drawImage(imageCUTTER.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageDRILL.display)
		{
			g2d.drawImage(imageDRILL.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageGRINDER.display)
		{
			g2d.drawImage(imageGRINDER.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageOVEN.display)
		{
			g2d.drawImage(imageOVEN.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imagePAINT.display)
		{
			g2d.drawImage(imagePAINT.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageUVLAMP.display)
		{
			g2d.drawImage(imageUVLAMP.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
		if (imageWASHER.display)
		{
			g2d.drawImage(imageWASHER.holderImage.getImage(), 0, 0, getIcon().getIconWidth(), getIcon().getIconHeight(), this);
		}
	}

	/**
	 * Function to get the part's position in line
	 * @return The part's position in line
	 */
	public int getPosInLine()
	{
		return posInLine;
	}
	/**
	 * Sets the part's position in line
	 * @param newpos The part's new position in line
	 */
	public void setPosInLine(int newpos)
	{
		posInLine = newpos;
	}
	/**
	 * Sets the parts state to broken and changes its image icon to a broken one
	 */
	public void msgPartBroken()
	{
		stateBroken = true;
		for (int i = 0; i < imageHolders.size(); i++)
		{
			imageHolders.get(i).display = false;
		}
		setIcon( new ImageIcon("imageicons/glassImage_BROKEN.png"));
		lastOperation = ComponentOperations.SHATTERED;
	}
	/**
	 * Returns the current angle
	 * @return The part's current angle
	 */
	public double getCurrentAngle()
	{
		return currentAngle;
	}
	/**
	 * Increases the rotation by a number
	 * @param angle The amount to increase rotation
	 */
	public void rotatePartByAngle(double angle)
	{
		currentAngle += angle;
		targetAngle += angle;
	}
	/**
	 * Returns the last operation done on the GUIPart 
	 * @return The last operation done on the GUIPart
	 */
	public ComponentOperations getLastOperation()
	{
		return lastOperation;
	}
	/**
	 * Class to hold the image overlay for the glass
	 * @author Anoop Kamboj
	 *
	 */
	private class ImageHolder extends JLabel
	{
		ImageIcon holderImage;
		boolean display = false;
		
		public ImageHolder(String filePath)
		{
			holderImage = new ImageIcon(filePath);
		}
	}
	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		
	}
}
