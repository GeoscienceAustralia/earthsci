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
package au.gov.ga.earthsci.core.mime;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.resolver.IContentTypeResolver;

/**
 * {@link IContentTypeResolver} implementation for http/https URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HttpContentTypeResolver implements IContentTypeResolver
{
	@Override
	public boolean supports(URL url, Intent intent)
	{
		String protocol = url.getProtocol();
		if (protocol == null)
		{
			return false;
		}
		protocol = protocol.toLowerCase();
		return "http".equals(protocol) || "https".equals(protocol); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IContentType resolve(URL url, Intent intent) throws IOException
	{
		InputStream is = null;
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) url.openConnection();

			String mimeType = connection.getContentType();
			if (mimeType != null)
			{
				int semicolonIndex = mimeType.indexOf(';');
				if (semicolonIndex >= 0)
				{
					mimeType = mimeType.substring(0, semicolonIndex);
				}

				//ignore types that are often used too generally (use the InputStream determination instead)
				if (!mimeType.equals("text/xml") && !mimeType.equals("application/xml")) //$NON-NLS-1$ //$NON-NLS-2$
				{
					IContentType contentType = MIMEHelper.getContentTypeForMIMEType(mimeType);
					if (contentType != null)
					{
						return contentType;
					}
				}
			}

			is = connection.getInputStream();
			return Platform.getContentTypeManager().findContentTypeFor(is, null);
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
				}
			}
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}
}
