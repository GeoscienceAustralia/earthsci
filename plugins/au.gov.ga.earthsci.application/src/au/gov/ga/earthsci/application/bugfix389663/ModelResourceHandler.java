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
package au.gov.ga.earthsci.application.bugfix389663;

import java.io.File;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.InjectorFactory;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
import org.eclipse.e4.ui.internal.workbench.ResourceHandler;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * Workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=389663,
 * until 4.3M7 is released.
 * 
 * Fragments are by default loaded every time the application loads, even when
 * restoring from a previous state. This causes NPEs on load. This fix ignores
 * any fragments if the model is being restored from a previous state.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ModelResourceHandler extends ResourceHandler
{
	@Inject
	@Named(E4Workbench.INSTANCE_LOCATION)
	private Location instanceLocation;

	private final boolean saveAndRestore;
	private final boolean clearPersistedState;

	@Inject
	public ModelResourceHandler(@Named(IWorkbench.PERSIST_STATE) boolean saveAndRestore,
			@Named(IWorkbench.CLEAR_PERSISTED_STATE) boolean clearPersistedState,
			@Named(E4Workbench.DELTA_RESTORE) boolean deltaRestore)
	{
		super(saveAndRestore, clearPersistedState, deltaRestore);
		this.saveAndRestore = saveAndRestore;
		this.clearPersistedState = clearPersistedState;
	}

	@Override
	public Resource loadMostRecentModel()
	{
		//check if the model will be restored from a previous state, and if so, replace
		//the standard ModelAssembler with one that ignores fragments

		/* *******************************
		 * COPIED FROM SUPER CLASS START *
		 ******************************* */
		File workbenchData = getWorkbenchSaveLocation();

		if (clearPersistedState && workbenchData.exists())
		{
			workbenchData.delete();
		}

		URI restoreLocation = null;
		if (saveAndRestore)
		{
			restoreLocation = URI.createFileURI(workbenchData.getAbsolutePath());
		}

		// last stored time-stamp
		long restoreLastModified =
				restoreLocation == null ? 0L : new File(restoreLocation.toFileString()).lastModified();

		// See bug 380663, bug 381219
		// long lastApplicationModification = getLastApplicationModification();
		// boolean restore = restoreLastModified > lastApplicationModification;
		boolean restore = restoreLastModified > 0;

		Resource resource = null;
		if (restore && saveAndRestore)
		{
			resource = loadResource(restoreLocation);
		}
		/* *****************************
		 * COPIED FROM SUPER CLASS END *
		 ***************************** */

		if (resource != null)
		{
			InjectorFactory.getDefault().addBinding(ModelAssembler.class).implementedBy(NoOpModelAssembler.class);
		}
		return super.loadMostRecentModel();
	}

	private File getWorkbenchSaveLocation()
	{
		try
		{
			Method method = getClass().getSuperclass().getDeclaredMethod("getWorkbenchSaveLocation"); //$NON-NLS-1$
			method.setAccessible(true);
			return (File) method.invoke(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private Resource loadResource(URI uri)
	{
		try
		{
			Method method = getClass().getSuperclass().getDeclaredMethod("loadResource", URI.class); //$NON-NLS-1$
			method.setAccessible(true);
			return (Resource) method.invoke(this, uri);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
