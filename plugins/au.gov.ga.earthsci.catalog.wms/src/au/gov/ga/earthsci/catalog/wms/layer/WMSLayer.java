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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.catalog.wms.WMSHelper;
import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.core.model.IModelStatus;
import au.gov.ga.earthsci.core.model.IStatused;
import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.layer.IPersistentLayer;
import au.gov.ga.earthsci.layer.delegator.LayerDelegator;
import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class WMSLayer extends LayerDelegator implements IPersistentLayer, IInformationed, IStatused
{
	private static final Logger logger = LoggerFactory.getLogger(WMSLayer.class);

	private final static String URI_ELEMENT = "uri"; //$NON-NLS-1$
	private final static String LAYER_ELEMENT = "layer"; //$NON-NLS-1$
	private final static String STYLE_ELEMENT = "style"; //$NON-NLS-1$

	private URI capabilitiesURI;
	private WMSCapabilities capabilities;
	private String layerName;
	private String styleName;

	private boolean loading = false;
	private IModelStatus status = ModelStatus.ok();

	public WMSLayer(URI capabilitiesURI, WMSCapabilities capabilities, String layerName, String styleName)
	{
		this.capabilitiesURI = capabilitiesURI;
		this.capabilities = capabilities;
		this.layerName = layerName;
		this.styleName = styleName;

		recreateLayer();
	}

	@SuppressWarnings("unused")
	private WMSLayer()
	{
	}

	private void recreateLayer()
	{
		AVList params = new AVListImpl();
		params.setValue(AVKey.LAYER_NAMES, layerName);
		params.setValue(AVKey.STYLE_NAMES, styleName);

		URL informationURL = WMSHelper.getInformationURL(capabilities, layerName);
		URL legendURL = WMSHelper.getLegendURL(capabilities, layerName, styleName);
		if (legendURL != null)
		{
			params.setValue(AVKeyMore.LEGEND_URL, legendURL);
		}

		Layer layer = new InformationedWMSTiledImageLayer(capabilities, params, informationURL);
		setLayer(layer);
	}

	private void loadCapabilities()
	{
		if (capabilitiesURI == null)
		{
			return;
		}

		try
		{
			URL url = capabilitiesURI.toURL();
			IRetrieval retrieval = RetrievalServiceFactory.getServiceInstance().retrieve(this, url);
			retrieval.addListener(new RetrievalAdapter()
			{
				@Override
				public void complete(IRetrieval retrieval)
				{
					setLoading(false);
					IRetrievalResult result = retrieval.getResult();
					try
					{
						if (!result.isSuccessful())
						{
							throw result.getError();
						}
						capabilities = new WMSCapabilities(result.getData().getInputStream()).parse();
						recreateLayer();
					}
					catch (Exception e)
					{
						String message = "Error loading WMS capabilities"; //$NON-NLS-1$
						setStatus(ModelStatus.error(message, e));
						logger.error(message, e);
					}
				}
			});
			setLoading(true);
			retrieval.start();
		}
		catch (MalformedURLException e)
		{
			logger.error("Capabilities URI error", e); //$NON-NLS-1$
		}
	}

	public URI getCapabilitiesURI()
	{
		return capabilitiesURI;
	}

	public WMSCapabilities getCapabilities()
	{
		return capabilities;
	}

	public String getLayerName()
	{
		return layerName;
	}

	public void setLayerName(String layerName)
	{
		firePropertyChange("layerName", getLayerName(), this.layerName = layerName); //$NON-NLS-1$
		recreateLayer();
	}

	public String getStyleName()
	{
		return styleName;
	}

	public void setStyleName(String styleName)
	{
		firePropertyChange("styleName", getStyleName(), this.styleName = styleName); //$NON-NLS-1$
		recreateLayer();
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	protected void setLoading(boolean loading)
	{
		firePropertyChange("loading", isLoading(), this.loading = loading); //$NON-NLS-1$
	}

	@Override
	public IModelStatus getStatus()
	{
		return status;
	}

	@Override
	public void setStatus(IModelStatus status)
	{
		firePropertyChange("status", getStatus(), this.status = status); //$NON-NLS-1$
	}

	@Override
	public void save(Element parent)
	{
		XmlUtil.setTextElement(parent, URI_ELEMENT, capabilitiesURI.toString());
		XmlUtil.setTextElement(parent, LAYER_ELEMENT, layerName);
		XmlUtil.setTextElement(parent, STYLE_ELEMENT, styleName);
	}

	@Override
	public void load(Element parent)
	{
		String uriText = XmlUtil.getText(parent, URI_ELEMENT);
		if (uriText != null)
		{
			try
			{
				capabilitiesURI = new URI(uriText);
			}
			catch (URISyntaxException e)
			{
			}
		}
		layerName = XmlUtil.getText(parent, LAYER_ELEMENT);
		styleName = XmlUtil.getText(parent, STYLE_ELEMENT);

		loadCapabilities();
	}

	@Override
	public void initialize(ILayerNode node, IEclipseContext context)
	{
	}

	@Override
	public URL getInformationURL()
	{
		Layer layer = getLayer();
		if (layer instanceof IInformationed)
		{
			return ((IInformationed) layer).getInformationURL();
		}
		return null;
	}

	@Override
	public String getInformationString()
	{
		Layer layer = getLayer();
		if (layer instanceof IInformationed)
		{
			return ((IInformationed) layer).getInformationString();
		}
		return null;
	}
}
