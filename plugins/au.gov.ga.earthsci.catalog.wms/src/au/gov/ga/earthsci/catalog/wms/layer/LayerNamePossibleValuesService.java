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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.PossibleValuesService;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.Status;

import au.gov.ga.earthsci.editable.EditableResource;

/**
 * Sapphire {@link PossibleValuesService} for the layer names available on a WMS
 * server.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerNamePossibleValuesService extends PossibleValuesService
{
	@Override
	protected void initPossibleValuesService()
	{
		super.initPossibleValuesService();

		final Listener listener = new FilteredListener<PropertyContentEvent>()
		{
			@Override
			protected void handleTypedEvent(final PropertyContentEvent event)
			{
				broadcast();
			}
		};

		final Element model = context(Element.class);
		model.attach(listener, WMSLayerElement.PROP_STYLE_NAME.name());

		this.invalidValueMessage = "\"${Entity}\" is not a valid layer name for the WMS server.";
		this.invalidValueSeverity = Status.Severity.ERROR;
	}

	@Override
	protected void compute(Set<String> values)
	{
		final Element model = context(Element.class);

		Resource resource = model.resource();
		if (!(resource instanceof EditableResource))
		{
			return;
		}

		EditableResource<?> modelResource = (EditableResource<?>) resource;
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

		for (WMSLayerCapabilities layerCapabilities : layerCapabilitiesList)
		{
			AddLayers(layerCapabilities, values);
		}
	}

	private void AddLayers(WMSLayerCapabilities capabilities, Collection<String> collection)
	{
		if (capabilities.getName() != null)
		{
			collection.add(capabilities.getName());
		}
		List<WMSLayerCapabilities> layers = capabilities.getLayers();
		if (layers != null)
		{
			for (WMSLayerCapabilities layer : layers)
			{
				AddLayers(layer, collection);
			}
		}
	}
}
