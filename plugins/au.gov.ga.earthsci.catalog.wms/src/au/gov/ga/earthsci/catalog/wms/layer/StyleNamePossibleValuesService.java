/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.catalog.wms.layer;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSCapabilityInformation;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;

import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.services.PossibleValuesService;

import au.gov.ga.earthsci.editable.EditableModelResource;

/**
 * Sapphire {@link PossibleValuesService} for the style names available for a
 * layer on a WMS server.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StyleNamePossibleValuesService extends PossibleValuesService
{
	@Override
	protected void init()
	{
		final Listener listener = new FilteredListener<PropertyContentEvent>()
		{
			@Override
			protected void handleTypedEvent(final PropertyContentEvent event)
			{
				broadcast();
			}
		};

		final Element model = context(Element.class);
		model.attach(listener, WMSLayerModelElement.PROP_LAYER_NAME.name());
	}

	@Override
	protected void fillPossibleValues(Set<String> values)
	{
		final Element model = context(Element.class);

		Resource resource = model.resource();
		if (!(resource instanceof EditableModelResource))
		{
			return;
		}

		EditableModelResource<?> modelResource = (EditableModelResource<?>) resource;
		Object object = modelResource.getObject();
		if (!(object instanceof WMSLayer))
		{
			return;
		}

		WMSLayer layer = (WMSLayer) object;
		WMSCapabilities capabilities = layer.getCapabilities();
		if (capabilities == null)
		{
			return;
		}

		WMSCapabilityInformation information = capabilities.getCapabilityInformation();
		if (information == null)
		{
			return;
		}

		List<WMSLayerCapabilities> layerCapabilitiesList = information.getLayerCapabilities();
		if (layerCapabilitiesList == null)
		{
			return;
		}

		WMSLayerCapabilities found = null;
		for (WMSLayerCapabilities layerCapabilities : layerCapabilitiesList)
		{
			found = FindLayers(layerCapabilities, layer.getLayerName());
		}
		if (found == null)
		{
			return;
		}

		Set<WMSLayerStyle> styles = found.getStyles();
		if (styles == null)
		{
			return;
		}

		for (WMSLayerStyle style : styles)
		{
			if (style.getName() != null)
			{
				values.add(style.getName());
			}
		}
	}

	private WMSLayerCapabilities FindLayers(WMSLayerCapabilities capabilities, String layerName)
	{
		if (layerName.equals(capabilities.getName()))
		{
			return capabilities;
		}
		List<WMSLayerCapabilities> layers = capabilities.getLayers();
		if (layers != null)
		{
			for (WMSLayerCapabilities layer : layers)
			{
				WMSLayerCapabilities found = FindLayers(layer, layerName);
				if (found != null)
				{
					return found;
				}
			}
		}
		return null;
	}

	@Override
	public String getInvalidValueMessage(final String invalidValue)
	{
		return NLS.bind("\"{0}\" is not a valid style name for the selected layer name.", invalidValue);
	}
}
