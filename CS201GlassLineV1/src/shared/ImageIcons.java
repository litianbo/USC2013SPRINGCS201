
package shared;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

public class ImageIcons
{
	File dir;

	static Map<String, List<ImageIcon>> animations = new HashMap<String, List<ImageIcon>>();

	public ImageIcons(String dirName)
	{
		dir = new File(dirName);
		processDirectory(dir);
	}

	public void processDirectory(File dir)
	{
		if (dir.isDirectory())
		{
			animations.put(dir.getName(), new ArrayList<ImageIcon>());
			File[] children = dir.listFiles();
			for (int i = 0; i < children.length; i++)
			{
				if (children[i].getName().equals(".svn"))
				{
					continue;
				}
				processDirectory(children[i]);
			}
		}
		else
		{
			if (!dir.isHidden())
			{
				addToMap(dir.getParentFile().getName(), dir.getPath());
			}
		}
	}

	public void addToMap(String key, String icon)
	{
		System.out.println("Adding " + icon + " to " + key);
		ImageIcon temp = new ImageIcon(icon);
		animations.get(key).add(temp);
	}

	public static List<ImageIcon> getIconList(String key) throws NullPointerException
	{
		return animations.get(key);
	}
}
