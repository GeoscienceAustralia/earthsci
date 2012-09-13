package au.gov.ga.earthsci.worldwind;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.worldwind.layers.LayerFactory;

@Creatable
@Singleton
public class WorldWindConfigurator
{
	public WorldWindConfigurator()
	{
		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());
		Configuration.setValue(AVKey.MODEL_CLASS_NAME, WorldWindModel.class.getName());
	}
}
