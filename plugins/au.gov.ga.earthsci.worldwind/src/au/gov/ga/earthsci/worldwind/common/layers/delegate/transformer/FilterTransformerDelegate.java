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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IImageTransformerDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.filters.TransparentMinimumFilter;
import au.gov.ga.earthsci.worldwind.common.layers.styled.PropertySetter;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndAttributeFactory;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

import com.jhlabs.image.AbstractBufferedImageOp;

/**
 * {@link IImageTransformerDelegate} implementation that uses implementations of
 * the {@link BufferedImageOp} interface to filter images. There are many such
 * implementations in the jhlabs filters library, which is included with this
 * project.
 * <p/>
 * <code>&lt;Delegate&gt;FilterTransformer&lt;/Delegate&gt;</code>
 * <p/>
 * When parsing from a layer definition file, the filter list must be provided
 * in the layer xml, as follows:
 * 
 * <pre>
 * &lt;Filters&gt;
 *   &lt;Filter name="classname"&gt;
 *     &lt;Property name="" value="" /&gt;
 *     &lt;Property name="" value="" type="" /&gt;
 *   &lt;/Filter&gt;
 *   &lt;Filter name="classname" /&gt;
 *   ...
 * &lt;/Filters&gt;
 * </pre>
 * 
 * Where:
 * <ul>
 * <li>classname = Class name of the {@link BufferedImageOp} implementation. If
 * the class is in the com.jhlabs.image package, the package prefix can be
 * optionally omitted.
 * <li>Property = Values to set on the instanciated class. The name attribute is
 * the name of the setter, and the value attribute is the value to set. If the
 * type attribute is included, the value is converted to type before setting,
 * otherwise the type of the setter is used.
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FilterTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "FilterTransformer";

	private final List<BufferedImageOp> filters;

	public FilterTransformerDelegate()
	{
		this(new ArrayList<BufferedImageOp>());
	}

	public FilterTransformerDelegate(List<BufferedImageOp> filters)
	{
		this.filters = filters;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().equals(DEFINITION_STRING.toLowerCase()))
		{
			List<BufferedImageOp> filters = new ArrayList<BufferedImageOp>();

			XPath xpath = XMLUtil.makeXPath();
			Element[] filterElements = XMLUtil.getElements(layerElement, "Filters/Filter", xpath);
			if (filterElements != null)
			{
				for (Element filterElement : filterElements)
				{
					String name = XMLUtil.getText(filterElement, "@name", xpath);
					try
					{
						Class<?> filterClass = null;
						try
						{
							//first try class name with the local .filter package prefix
							filterClass =
									Class.forName(TransparentMinimumFilter.class.getPackage().getName() + "." + name);
						}
						catch (ClassNotFoundException e)
						{
						}
						if (filterClass == null)
						{
							try
							{
								//next try class name with the com.jhlabs.image package prefix
								filterClass =
										Class.forName(AbstractBufferedImageOp.class.getPackage().getName() + "." + name);
							}
							catch (ClassNotFoundException e)
							{
							}
						}
						if (filterClass == null)
						{
							try
							{
								//if not found, simply try the name as the full class name
								filterClass = Class.forName(name);
							}
							catch (ClassNotFoundException e)
							{
								throw new Exception("Filter not found: " + name, e);
							}
						}

						Object filterObject = filterClass.newInstance();
						if (filterObject instanceof BufferedImageOp)
						{
							BufferedImageOp filter = (BufferedImageOp) filterObject;
							filters.add(filter);

							PropertySetter setter = new PropertySetter();
							StyleAndAttributeFactory.addProperties(filterElement, xpath, setter);
							setter.setPropertiesFromAttributes(null, null, filter);
						}
					}
					catch (Exception e)
					{
						//log exception
						e.printStackTrace();
					}
				}
			}

			return new FilterTransformerDelegate(filters);
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		if (filters != null)
		{
			for (BufferedImageOp filter : filters)
			{
				image = filter.filter(image, null);
			}
		}
		return image;
	}
}
