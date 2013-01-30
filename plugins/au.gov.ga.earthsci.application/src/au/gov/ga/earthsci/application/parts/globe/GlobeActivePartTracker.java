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
package au.gov.ga.earthsci.application.parts.globe;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;

/**
 * Injected class that keeps track of the active WorldWindow, and notifies the
 * {@link WorldWindowRegistry}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GlobeActivePartTracker
{
	@Inject
	public void execute(@Optional @Active MPart activePart)
	{
		if (activePart != null && activePart.getObject() instanceof GlobePart)
		{
			GlobePart part = (GlobePart) activePart.getObject();
			WorldWindowRegistry.INSTANCE.setActive(part.getWorldWindow());
		}
	}
}
