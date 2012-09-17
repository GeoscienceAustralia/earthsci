package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.model.layer.TreeLayerList;

public class WorldWindModel extends BasicModel
{
	public WorldWindModel()
	{
		super(createGlobe(), createLayerList());
	}

	protected static Globe createGlobe()
	{
		return (Globe) WorldWind.createConfigurationComponent(AVKey.GLOBE_CLASS_NAME);
	}

	protected static LayerList createLayerList()
	{
		Element element = Configuration.getElement("./LayerList"); //$NON-NLS-1$
		return createLayersFromElementStatic(element);
	}

	protected static LayerList createLayersFromElementStatic(Element element)
	{
		Object o = BasicFactory.create(AVKey.LAYER_FACTORY, element);

		if (o instanceof LayerList)
			return (LayerList) o;

		if (o instanceof Layer)
			return new TreeLayerList(new Layer[] { (Layer) o });

		if (o instanceof LayerList[])
		{
			LayerList[] lists = (LayerList[]) o;
			if (lists.length > 0)
				return LayerList.collapseLists((LayerList[]) o);
		}

		return null;
	}
}
