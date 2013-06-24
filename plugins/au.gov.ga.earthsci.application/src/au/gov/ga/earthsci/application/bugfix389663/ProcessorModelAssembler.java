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

import java.lang.reflect.Method;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.internal.workbench.ModelAssembler;

/**
 * {@link ModelAssembler} subclass that only looks at 'processor' extension
 * points, and ignores 'fragment's.
 * 
 * Workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=389663,
 * until 4.3M7 is released.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ProcessorModelAssembler extends ModelAssembler
{
	final private static String extensionPointID = "org.eclipse.e4.workbench.model"; //$NON-NLS-1$

	@Override
	public void processModel()
	{
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointID);
		IExtension[] extensions = topoSort(extPoint.getExtensions());

		//only process 'processor' extension points; ignore 'fragment' points
		for (IExtension extension : extensions)
		{
			IConfigurationElement[] ces = extension.getConfigurationElements();
			for (IConfigurationElement ce : ces)
			{
				if (!"processor".equals(ce.getName())) //$NON-NLS-1$
				{
					continue;
				}
				runProcessor(ce);
			}
		}
	}

	private IExtension[] topoSort(IExtension[] extensions)
	{
		//call the superclass' topoSort method using reflection:
		try
		{
			Method topoSortMethod = getClass().getSuperclass().getDeclaredMethod("topoSort", IExtension[].class); //$NON-NLS-1$
			topoSortMethod.setAccessible(true);
			return (IExtension[]) topoSortMethod.invoke(this, new Object[] { extensions });
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private void runProcessor(IConfigurationElement ce)
	{
		//call the superclass' runProcessor method using reflection:
		try
		{
			Method runProcessorMethod =
					getClass().getSuperclass().getDeclaredMethod("runProcessor", IConfigurationElement.class); //$NON-NLS-1$
			runProcessorMethod.setAccessible(true);
			runProcessorMethod.invoke(this, ce);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
