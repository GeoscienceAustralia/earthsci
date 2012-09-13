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
package au.gov.ga.earthsci.worldwind.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Arrays;

import org.w3c.dom.Element;

/**
 * Abstract {@link BasicLayerFactory} subclass that allows configuration of the
 * LayerList implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractLayerFactory extends BasicLayerFactory
{
	/**
	 * @return New LayerList instance
	 */
	protected abstract LayerList createLayerList();

	////////////////////////////////////////////////////////////////////////////////////////////
	// BELOW IS COPIED DIRECTLY FROM BasicLayerFactory, BUT CALLING createLayerList() INSTEAD //
	////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected LayerList createLayerList(Element[] layerElements, AVList params)
	{
		LayerList layerList = createLayerList();

		for (Element element : layerElements)
		{
			try
			{
				layerList.add(this.createFromLayerDocument(element, params));
			}
			catch (Exception e)
			{
				Logging.logger().log(java.util.logging.Level.WARNING, e.getMessage(), e);
				// keep going to create other layers
			}
		}

		return layerList;
	}

	@Override
	protected LayerList[] createLayerLists(Element[] elements, AVList params)
	{
		ArrayList<LayerList> layerLists = new ArrayList<LayerList>();

		for (Element element : elements)
		{
			try
			{
				String href = WWXML.getText(element, "@href"); //$NON-NLS-1$
				if (href != null && href.length() > 0)
				{
					Object o = this.createFromConfigSource(href, params);
					if (o == null)
						continue;

					if (o instanceof Layer)
					{
						LayerList ll = createLayerList();
						ll.add((Layer) o);
						o = ll;
					}

					if (o instanceof LayerList)
					{
						LayerList list = (LayerList) o;
						if (list != null && list.size() > 0)
							layerLists.add(list);
					}
					else if (o instanceof LayerList[])
					{
						LayerList[] lists = (LayerList[]) o;
						if (lists != null && lists.length > 0)
							layerLists.addAll(Arrays.asList(lists));
					}
					else
					{
						String msg = Logging.getMessage("LayerFactory.UnexpectedTypeForLayer", o.getClass().getName()); //$NON-NLS-1$
						Logging.logger().log(java.util.logging.Level.WARNING, msg);
					}

					continue;
				}

				String title = WWXML.getText(element, "@title"); //$NON-NLS-1$
				Element[] children = WWXML.getElements(element, "./Layer", null); //$NON-NLS-1$
				if (children != null && children.length > 0)
				{
					LayerList list = this.createLayerList(children, params);
					if (list != null && list.size() > 0)
					{
						layerLists.add(list);
						if (title != null && title.length() > 0)
							list.setValue(AVKey.DISPLAY_NAME, title);
					}
				}
			}
			catch (Exception e)
			{
				Logging.logger().log(java.util.logging.Level.WARNING, e.getMessage(), e);
				// keep going to create other layers
			}
		}

		return layerLists.toArray(new LayerList[layerLists.size()]);
	}
}
