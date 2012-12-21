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
package au.gov.ga.earthsci.application;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plugin image registry.
 * <p/>
 * Provides convenient access to the reusable icon and image elements in the
 * application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ImageRegistry extends org.eclipse.jface.resource.ImageRegistry
{
	private final static ImageRegistry INSTANCE = new ImageRegistry();

	private static final Logger logger = LoggerFactory.getLogger(ImageRegistry.class);

	public static final String ICON_INFORMATION = "icon.information"; //$NON-NLS-1$
	public static final String ICON_INFORMATION_WHITE = "icon.information.white"; //$NON-NLS-1$
	public static final String ICON_LEGEND = "icon.legend"; //$NON-NLS-1$
	public static final String ICON_LEGEND_WHITE = "icon.legend.white"; //$NON-NLS-1$
	public static final String ICON_ERROR = "icon.error"; //$NON-NLS-1$
	public static final String ICON_WARNING = "icon.warning"; //$NON-NLS-1$
	public static final String ICON_REPOSITORY = "icon.repo"; //$NON-NLS-1$
	public static final String ICON_FOLDER = "icon.folder"; //$NON-NLS-1$
	public static final String ICON_LAYER = "icon.layer"; //$NON-NLS-1$
	public static final String ICON_FILE = "icon.file"; //$NON-NLS-1$
	public static final String ICON_ADD = "icon.add"; //$NON-NLS-1$
	public static final String ICON_REMOVE = "icon.remove"; //$NON-NLS-1$
	public static final String ICON_TRANSPARENT = "icon.transparent"; //$NON-NLS-1$
	public static final String ICON_APPLY = "icon.apply"; //$NON-NLS-1$
	public static final String ICON_EDIT = "icon.edit"; //$NON-NLS-1$
	public static final String ICON_LOADING = "icon.loading"; //$NON-NLS-1$
	public static final String DECORATION_INCLUDED = "decoration.included"; //$NON-NLS-1$
	
	private final Map<String, URL> urlMap = new ConcurrentHashMap<String, URL>(); 
	
	public static ImageRegistry getInstance()
	{
		return INSTANCE;
	}

	private ImageRegistry()
	{
		putResource(ICON_INFORMATION, "/icons/information.gif"); //$NON-NLS-1$
		putResource(ICON_INFORMATION_WHITE, "/icons/information_white.gif"); //$NON-NLS-1$

		putResource(ICON_LEGEND, "/icons/legend.gif"); //$NON-NLS-1$
		putResource(ICON_LEGEND_WHITE, "/icons/legend_white.gif"); //$NON-NLS-1$

		putResource(ICON_ERROR, "/icons/error.gif"); //$NON-NLS-1$

		putResource(ICON_WARNING, "/icons/warning.gif"); //$NON-NLS-1$

		putResource(ICON_REPOSITORY, "/icons/repo.gif"); //$NON-NLS-1$

		putResource(ICON_FOLDER, "/icons/folder.gif"); //$NON-NLS-1$
		putResource(ICON_FILE, "/icons/file_obj.gif"); //$NON-NLS-1$
		putResource(ICON_LAYER, "/icons/layer.png"); //$NON-NLS-1$

		putResource(ICON_ADD, "/icons/add.gif"); //$NON-NLS-1$
		putResource(ICON_REMOVE, "/icons/remove.gif"); //$NON-NLS-1$
		
		putResource(ICON_APPLY, "/icons/apply.gif"); //$NON-NLS-1$

		putResource(ICON_EDIT, "/icons/edit.gif"); //$NON-NLS-1$
		
		putResource(ICON_TRANSPARENT, "/icons/transparent.gif"); //$NON-NLS-1$

		putResource(DECORATION_INCLUDED, "/icons/included_dec.gif"); //$NON-NLS-1$

		putAnimatedResource(ICON_LOADING, "/icons/loading.gif"); //$NON-NLS-1$
	}

	protected void putResource(String key, String resourceName)
	{
		URL url = getClass().getResource(resourceName);
		put(key, ImageDescriptor.createFromURL(url));
		
		urlMap.put(key, url);
	}

	protected void putAnimatedResource(String key, String resourceName)
	{
		putAnimated(key, getClass().getResource(resourceName));
	}

	public void putAnimated(String key, URL url)
	{
		try
		{
			Image[] images = loadAnimated(url);
			for (int i = 0; i < images.length; i++)
			{
				String frameKey = key + "." + i; //$NON-NLS-1$
				put(frameKey, images[i]);
			}
		}
		catch (IOException e)
		{
			logger.error("Error loading animated image", e); //$NON-NLS-1$
		}
	}

	public Image[] getAnimated(String key)
	{
		List<Image> images = new ArrayList<Image>();
		for (int i = 0;; i++)
		{
			String frameKey = key + "." + i; //$NON-NLS-1$
			Image image = get(frameKey);
			if (image == null)
			{
				break;
			}
			images.add(image);
		}
		return images.toArray(new Image[images.size()]);
	}

	private Image[] loadAnimated(URL url) throws IOException
	{
		Display display = Display.getDefault();
		ImageLoader imageLoader = new ImageLoader();
		imageLoader.load(url.openStream());
		Image[] images = new Image[imageLoader.data.length];
		for (int i = 0; i < imageLoader.data.length; ++i)
		{
			ImageData nextFrameData = imageLoader.data[i];
			images[i] = new Image(display, nextFrameData);
		}
		return images;
	}
	
	/**
	 * Return the URL for the image resource with the given key, if one exists
	 * 
	 * @param key The key for the image URL to load
	 * 
	 * @return the URL for the image resource with the given key. 
	 * <code>null</code> if no resource exists or no URL is available for the resource.
	 */
	public URL getURL(String key)
	{
		return urlMap.get(key);
	}
}
