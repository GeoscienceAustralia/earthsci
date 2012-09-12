package au.gov.ga.earthsci.application;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import au.gov.ga.earthsci.model.DataModelImpl;
import au.gov.ga.earthsci.model.IDataModel;

public class LifeCycleManager
{
	@Inject
	private IEclipseContext context;
	
	@Inject
	private ProxyConfigurator proxyConfigurator;

	@PostContextCreate
	void postContextCreate()
	{
		context.set(Model.class, (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME));
		context.set(IDataModel.class, new DataModelImpl());
	}
}
