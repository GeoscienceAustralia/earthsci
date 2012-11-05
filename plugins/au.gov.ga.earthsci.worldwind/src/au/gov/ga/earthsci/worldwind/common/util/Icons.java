/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.worldwind.common.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * An accessor class that allows easy access to icon resources.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Icons
{
	public static final Icons about = new Icons("about.gif");
	public static final Icons add = new Icons("add.gif");
	public static final Icons bookmark = new Icons("bookmark.gif");
	public static final Icons category = new Icons("category.gif");
	public static final Icons check = new Icons("check.gif");
	public static final Icons checkall = new Icons("checkall.gif");
	public static final Icons checkboxes = new Icons("checkboxes.gif");
	public static final Icons collapse = new Icons("collapse.gif");
	public static final Icons collapseall = new Icons("collapseall.gif");
	public static final Icons compass = new Icons("compass.gif");
	public static final Icons copy = new Icons("copy.gif");
	public static final Icons crosshair45 = new Icons("crosshair45.gif");
	public static final Icons crosshair = new Icons("crosshair.gif");
	public static final Icons crosshairwhite = new Icons("crosshairwhite.gif");
	public static final Icons cut = new Icons("cut.gif");
	public static final Icons cutdelete = new Icons("cutdelete.gif");
	public static final Icons datasets = new Icons("datasets.gif");
	public static final Icons delete = new Icons("delete.gif");
	public static final Icons deleteall = new Icons("deleteall.gif");	
	public static final Icons deletevalue = new Icons("deletevalue.gif");
	public static final Icons down = new Icons("down.gif");
	public static final Icons earth32 = new Icons("earth32.png");
	public static final Icons earth = new Icons("earth.png");
	public static final Icons edit = new Icons("edit.gif");
	public static final Icons error = new Icons("error.gif");
	public static final Icons escape = new Icons("escape.gif");
	public static final Icons exaggeration = new Icons("exaggeration.png");
	public static final Icons expand = new Icons("expand.gif");
	public static final Icons expandall = new Icons("expandall.gif");
	public static final Icons export = new Icons("export.gif");
	public static final Icons file = new Icons("file.gif");
	public static final Icons find = new Icons("find.gif");
	public static final Icons flag = new Icons("flag.gif");
	public static final Icons folder = new Icons("folder.gif");
	public static final Icons graticule = new Icons("graticule.png");
	public static final Icons help = new Icons("help.gif");
	public static final Icons hierarchy = new Icons("hierarchy.gif");
	public static final Icons home = new Icons("home.gif");
	public static final Icons image = new Icons("image.gif");
	public static final Icons imporrt = new Icons("import.gif");
	public static final Icons info = new Icons("info.gif");
	public static final Icons infowhite = new Icons("infowhite.gif");
	public static final Icons keyboard = new Icons("keyboard.gif");
	public static final Icons legend = new Icons("legend.gif");
	public static final Icons legendwhite = new Icons("legendwhite.gif");
	public static final Icons list = new Icons("list.gif");
	public static final Icons monitor = new Icons("monitor.gif");
	public static final Icons navigation = new Icons("navigation.png");
	public static final Icons newfile = new Icons("newfile.gif");
	public static final Icons newfolder = new Icons("newfolder.gif");
	public static final Icons offline = new Icons("offline.gif");
	public static final Icons overview = new Icons("overview.gif");
	public static final Icons partialCheck = new Icons("partialcheck.gif");
	public static final Icons paste = new Icons("paste.gif");
	public static final Icons pause = new Icons("pause.gif");
	public static final Icons properties = new Icons("properties.gif");
	public static final Icons refresh = new Icons("refresh.gif");
	public static final Icons reload = new Icons("reload.gif");
	public static final Icons remove = new Icons("remove.gif");
	public static final Icons run = new Icons("run.gif");
	public static final Icons save = new Icons("save.gif");
	public static final Icons scalebar = new Icons("scalebar.gif");
	public static final Icons screenshot = new Icons("screenshot.gif");
	public static final Icons search = new Icons("search.gif");
	public static final Icons settings = new Icons("settings.gif");
	public static final Icons skirts = new Icons("skirts.png");
	public static final Icons stop = new Icons("stop.gif");
	public static final Icons uncheck = new Icons("uncheck.gif");
	public static final Icons uncheckall = new Icons("uncheckall.gif");
	public static final Icons up = new Icons("up.gif");
	public static final Icons updown = new Icons("updown.gif");
	public static final Icons view = new Icons("view.gif");
	public static final Icons wireframe = new Icons("wireframe.png");
	public static final Icons world = new Icons("world.gif");
	public static final Icons zwireframe = new Icons("zwireframe.png");
	public static final Icons wmsbrowser = new Icons("wms-browser.gif");
	
	/**
	 * @return A new loading icon instance
	 */
	public static ImageIcon newLoadingIcon()
	{
		return loadIcon(createURL(DEFAULT_ICON_DIRECTORY, "progress.gif"));
	}
	
	private static URL createURL(String directory, String filename)
	{
		return Icons.class.getResource(directory + filename);
	}

	private static ImageIcon loadIcon(URL url)
	{
		try
		{
			InputStream is = url.openStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) >= 0)
			{
				baos.write(buffer, 0, read);
			}
			return new ImageIcon(baos.toByteArray());
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	private static final String DEFAULT_ICON_DIRECTORY = "/images/icons/";
	private ImageIcon icon;
	private URL url;

	/**
	 * Create an icon accessor using the default directory ({@value #DEFAULT_ICON_DIRECTORY})
	 */
	protected Icons(String filename)
	{
		url = createURL(DEFAULT_ICON_DIRECTORY, filename);
	}

	/**
	 * Create an icon accessor using the provided directory
	 */
	protected Icons(String directory, String filename)
	{
		url = createURL(directory, filename);
	}
	
	public ImageIcon getIcon()
	{
		if (icon == null)
		{
			icon = loadIcon(url);
		}
		return icon;
	}

	public URL getURL()
	{
		return url;
	}
}
