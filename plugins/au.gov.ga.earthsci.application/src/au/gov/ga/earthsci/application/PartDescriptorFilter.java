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
package au.gov.ga.earthsci.application;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An addon for the application model that cleans up unwanted contributed part
 * descriptors from the application model before it is sent for instantiation.
 * <p/>
 * Used to remove the welcome part etc. which are contributed by external
 * plugins but are not wanted in the platform.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class PartDescriptorFilter
{
	private final static Logger logger = LoggerFactory.getLogger(PartDescriptorFilter.class);

	@SuppressWarnings("nls")
	/**
	 * The blacklist of {@link MPartDescriptor} element IDs that are to be filtered
	 */
	private static final Set<String> BLACKLIST = new HashSet<String>()
	{
		{
			add("org.eclipse.ui.internal.introview");
			add("org.eclipse.e4.ui.compatibility.editor");
		}
	};

	public static void run(final MApplication application, final EModelService service)
	{
		Iterator<MPartDescriptor> it = application.getDescriptors().iterator();
		while (it.hasNext())
		{
			MPartDescriptor d = it.next();
			if (BLACKLIST.contains(d.getElementId()))
			{
				logger.trace("Filtering descriptor: " + d.getElementId()); //$NON-NLS-1$
				it.remove();
			}
		}
	}
}
