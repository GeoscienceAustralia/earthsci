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
package au.gov.ga.earthsci.catalog.model;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentManager;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentFilter;

/**
 * Dummy intent manager for testing.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DummyIntentManager implements IIntentManager
{
	@Override
	public void start(Intent intent, IIntentCallback callback, IEclipseContext context)
	{
	}

	@Override
	public IntentFilter findFilter(Intent intent)
	{
		return null;
	}

	@Override
	public void addFilter(IntentFilter filter)
	{
	}

	@Override
	public void removeFilter(IntentFilter filter)
	{
	}
}
