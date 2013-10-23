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
package au.gov.ga.earthsci.layer.intent;

import gov.nasa.worldwind.layers.Layer;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;

/**
 * {@link IIntentHandler} that handles class:// layer URIs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ClassLayerIntentHandler implements IIntentHandler
{
	@Override
	public void handle(Intent intent, IIntentCallback callback)
	{
		String className = intent.getURI().getAuthority();
		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends Layer> c = (Class<? extends Layer>) Class.forName(className);
			Layer layer = c.newInstance();
			callback.completed(layer, intent);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}
}
