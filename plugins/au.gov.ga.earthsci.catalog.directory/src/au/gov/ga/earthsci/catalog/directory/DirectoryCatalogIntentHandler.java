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
package au.gov.ga.earthsci.catalog.directory;

import java.io.File;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;

/**
 * Intent handler for directory catalogs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DirectoryCatalogIntentHandler implements IIntentHandler
{
	@Inject
	private IEclipseContext context;

	@Override
	public void handle(Intent intent, IIntentCallback callback)
	{
		File directory = new File(intent.getURI());
		if (directory.isDirectory())
		{
			DirectoryCatalogTreeNode node = new DirectoryCatalogTreeNode(directory.toURI(), true, context);
			callback.completed(node, intent);
		}
		else
		{
			callback.error(new Exception("URI is not a directory: " + intent.getURI()), intent); //$NON-NLS-1$
		}
	}
}
