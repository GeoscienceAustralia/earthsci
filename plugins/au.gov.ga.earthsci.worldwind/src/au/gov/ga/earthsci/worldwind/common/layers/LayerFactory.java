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
package au.gov.ga.earthsci.worldwind.common.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.crust.CrustLayer;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.BasicTiledCurtainLayer;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.delegate.DelegatorTiledCurtainLayer;
import au.gov.ga.earthsci.worldwind.common.layers.earthquakes.HistoricEarthquakesLayer;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.GeometryLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.kml.KMLLayer;
import au.gov.ga.earthsci.worldwind.common.layers.mercator.delegate.DelegatorMercatorTiledImageLayer;
import au.gov.ga.earthsci.worldwind.common.layers.model.ModelLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.screenoverlay.ScreenOverlayLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.shapefile.surfaceshape.ShapefileLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.sphere.SphereLayerFactory;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.DelegatorTiledImageLayer;
import au.gov.ga.earthsci.worldwind.common.layers.transform.TransformSkyGradientLayer;
import au.gov.ga.earthsci.worldwind.common.layers.transform.TransformStarsLayer;
import au.gov.ga.earthsci.worldwind.common.layers.volume.VolumeLayerFactory;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Extension to World Wind's {@link BasicLayerFactory} which allows creation of
 * extra layer types.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerFactory extends BasicLayerFactory
{
	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		//overridden to allow extra layer types

		String layerType = WWXML.getText(domElement, "@layerType");
		if ("SurfaceShapeShapefileLayer".equals(layerType))
		{
			return ShapefileLayerFactory.createLayer(domElement, params);
		}
		if ("PointLayer".equals(layerType))
		{
			return PointLayerFactory.createPointLayer(domElement, params);
		}
		if ("KMLLayer".equals(layerType))
		{
			return new KMLLayer(domElement, params);
		}
		if ("CurtainImageLayer".equals(layerType))
		{
			return createTiledCurtainLayer(domElement, params);
		}
		if ("MercatorImageLayer".equals(layerType))
		{
			return createTiledMercatorLayer(domElement, params);
		}
		if ("HistoricEarthquakesLayer".equals(layerType))
		{
			return new HistoricEarthquakesLayer(domElement, params);
		}
		if ("CrustLayer".equals(layerType))
		{
			return new CrustLayer(domElement, params);
		}
		if ("GeometryLayer".equalsIgnoreCase(layerType))
		{
			return GeometryLayerFactory.createGeometryLayer(domElement, params);
		}
		if ("BoreholeLayer".equalsIgnoreCase(layerType))
		{
			return BoreholeLayerFactory.createBoreholeLayer(domElement, params);
		}
		if ("ModelLayer".equalsIgnoreCase(layerType))
		{
			return ModelLayerFactory.createModelLayer(domElement, params);
		}
		if ("VolumeLayer".equalsIgnoreCase(layerType))
		{
			return VolumeLayerFactory.createVolumeLayer(domElement, params);
		}
		if ("SphereLayer".equalsIgnoreCase(layerType))
		{
			return SphereLayerFactory.createSphereLayer(domElement, params);
		}
		if ("ScreenOverlayLayer".equals(layerType))
		{
			return ScreenOverlayLayerFactory.createScreenOverlayLayer(domElement, params);
		}

		String className = WWXML.getText(domElement, "@className");
		if (className != null && className.length() > 0)
		{
			try
			{
				Class<?> c = Class.forName(className);
				if (c.equals(StarsLayer.class))
				{
					className = TransformStarsLayer.class.getName();
				}
				else if (c.equals(SkyGradientLayer.class))
				{
					className = TransformSkyGradientLayer.class.getName();
				}
			}
			catch (ClassNotFoundException e)
			{
				if (className.startsWith("au.gov.ga.worldwind"))
				{
					className = className.replace("au.gov.ga.worldwind", "au.gov.ga.earthsci.worldwind");
				}
			}

			domElement.setAttribute("className", className);

			//try instantiate from this plugin, in case WorldWind plugin cannot find the class:
			try
			{
				Class<?> c = Class.forName(className.trim());
				Layer layer = (Layer) c.newInstance();
				String actuate = WWXML.getText(domElement, "@actuate");
				layer.setEnabled(WWUtil.isEmpty(actuate) || actuate.equals("onLoad"));
				WWXML.invokePropertySetters(layer, domElement);
				return layer;
			}
			catch (Exception e)
			{
			}
		}

		return super.createFromLayerDocument(domElement, params);
	}

	protected Layer createTiledCurtainLayer(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if ("DelegatorTileService".equals(serviceName))
		{
			layer = new DelegatorTiledCurtainLayer(domElement, params);
		}
		else
		{
			layer = new BasicTiledCurtainLayer(domElement, params);
		}

		params = TimedExpirationHandler.getExpirationParams(domElement, params);
		TimedExpirationHandler.registerLayer(layer, params);

		return layer;
	}

	protected Layer createTiledMercatorLayer(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if ("DelegatorTileService".equals(serviceName))
		{
			layer = new DelegatorMercatorTiledImageLayer(domElement, params);
		}
		else
		{
			layer = new BasicMercatorTiledImageLayer(params);
		}

		params = TimedExpirationHandler.getExpirationParams(domElement, params);
		TimedExpirationHandler.registerLayer(layer, params);

		return layer;
	}

	@Override
	protected Layer createTiledImageLayer(Element domElement, AVList params)
	{
		//overridden to allow extra service names for the TiledImageLayer type

		if (params == null)
		{
			params = new AVListImpl();
		}

		Layer layer;
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if ("DelegatorTileService".equals(serviceName) || OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
		{
			layer = new DelegatorTiledImageLayer(domElement, params);
		}
		else
		{
			layer = super.createTiledImageLayer(domElement, params);
			if (params.getValue(AVKey.SECTOR) != null)
			{
				layer.setValue(AVKey.SECTOR, params.getValue(AVKey.SECTOR));
			}
		}

		params = TimedExpirationHandler.getExpirationParams(domElement, params);
		TimedExpirationHandler.registerLayer(layer, params);

		return layer;
	}
}
