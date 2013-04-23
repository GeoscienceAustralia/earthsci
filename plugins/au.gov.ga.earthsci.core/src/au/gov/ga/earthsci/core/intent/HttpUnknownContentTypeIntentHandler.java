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
package au.gov.ga.earthsci.core.intent;

import java.net.URL;

import javax.inject.Inject;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.core.mime.MIMEHelper;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentManager;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentFilter;
import au.gov.ga.earthsci.intent.IntentManager;

/**
 * Intent handler that handles HTTP and HTTPS intents with no content type.
 * Retrieves the data from the server and queries the content/MIME type; if it
 * is a known content type the intent's content type will be set and forwarded
 * back to the {@link IIntentManager}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpUnknownContentTypeIntentHandler extends AbstractRetrieveIntentHandler
{
	@Inject
	private IEclipseContext context;

	private boolean handled = false;

	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback)
	{
		//ensure that the intent isn't fowarded twice (once for cache, once for updated)
		synchronized (this)
		{
			if (handled)
			{
				return;
			}
			handled = true;
		}

		String mimeType = data.getContentType();
		if (mimeType != null)
		{
			int semicolonIndex = mimeType.indexOf(';');
			if (semicolonIndex >= 0)
			{
				mimeType = mimeType.substring(0, semicolonIndex);
			}
		}
		IContentType contentType = MIMEHelper.getContentTypeForMIMEType(mimeType);
		if (contentType == null)
		{
			callback.error(new Exception("Could not determine content type for MIME type: " + mimeType), intent); //$NON-NLS-1$
			return;
		}
		//set the content type and forward it back to the intent manager
		intent.setContentType(contentType);
		IntentFilter filter = IntentManager.getInstance().findFilter(intent);
		//don't allow this class to handle the intent again (causing a infinite intent loop)
		if (filter != null && !getClass().equals(filter.getHandler()))
		{
			IntentManager.getInstance().start(intent, callback, context);
		}
		else
		{
			callback.error(new Exception("Could not find filter to handle intent for MIME type: " + mimeType), intent); //$NON-NLS-1$
		}
	}
}
