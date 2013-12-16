
package gui.util;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Toolbox for ImageIcons
 */
public class ImageIconUtil
{
	/**
	 * It gets an ImageIcon creates an new on with new dimension and returns it
	 * @param icon
	 *        the icon that needs to be resized
	 * @param width
	 *        new width of the icon
	 * @param height
	 *        new height of the icon
	 * @return new ImageIcon with the same image as icno but with new dimensions
	 */
	public static ImageIcon resizeImageIcon(ImageIcon icon, int width, int height)
	{
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH));
	}

	/**
	 * it gets a string which is is a path to a specific image. and returns an image icon with that image and passed
	 * dimension
	 * @param path
	 *        path to the image file
	 * @param width
	 *        required width of the ImageIcon
	 * @param height
	 *        required height of the ImageIcon
	 * @return new Image Icon created
	 */
	public static ImageIcon getImageIcon(String path, int width, int height)
	{
		try
		{
			return new ImageIcon(ImageIO.read(new File(path)).getScaledInstance(width, height,
				java.awt.Image.SCALE_SMOOTH));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
