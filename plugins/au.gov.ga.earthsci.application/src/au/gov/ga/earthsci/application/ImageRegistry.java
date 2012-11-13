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

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The plugin image registry 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
@Creatable
@Singleton
public class ImageRegistry extends org.eclipse.jface.resource.ImageRegistry
{

	private static final ImageRegistry INSTANCE = new ImageRegistry();
	
	public static final String ICON_INFORMATION = "icon.information"; //$NON-NLS-1$
	public static final String ICON_INFORMATION_WHITE = "icon.information.white"; //$NON-NLS-1$
	public static final String ICON_LEGEND = "icon.legend"; //$NON-NLS-1$
	public static final String ICON_LEGEND_WHITE = "icon.legend.white"; //$NON-NLS-1$
	
	private ImageRegistry()
	{
		put(ICON_INFORMATION, ImageDescriptor.createFromURL(getClass().getResource("/icons/information.gif"))); //$NON-NLS-1$
		put(ICON_INFORMATION_WHITE, ImageDescriptor.createFromURL(getClass().getResource("/icons/information_white.gif"))); //$NON-NLS-1$
		put(ICON_LEGEND, ImageDescriptor.createFromURL(getClass().getResource("/icons/legend.gif"))); //$NON-NLS-1$
		put(ICON_LEGEND_WHITE, ImageDescriptor.createFromURL(getClass().getResource("/icons/legend_white.gif"))); //$NON-NLS-1$
	}

	public static ImageRegistry getInstance()
	{
		return INSTANCE;
	}
}
