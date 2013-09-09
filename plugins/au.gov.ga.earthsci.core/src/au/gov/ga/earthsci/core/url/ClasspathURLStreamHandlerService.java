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
package au.gov.ga.earthsci.core.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * {@link URLStreamHandlerService} that handles classpath://resource URLs, which
 * provide access to resources on the classpath using
 * {@link Class#getResource(String)}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ClasspathURLStreamHandlerService extends AbstractURLStreamHandlerService
{
	@Override
	public URLConnection openConnection(URL u) throws IOException
	{
		return new URLConnection(u)
		{
			@Override
			public void connect() throws IOException
			{
			}

			@Override
			public String getContentType()
			{
				return "image/png"; //$NON-NLS-1$
			}

			@Override
			public InputStream getInputStream() throws IOException
			{
				String path = blankNullString(getURL().getHost()) + blankNullString(getURL().getPath());
				if (!path.startsWith("/")) //$NON-NLS-1$
				{
					path = "/" + path; //$NON-NLS-1$
				}
				return getClass().getResourceAsStream(path);
			}

			private String blankNullString(String s)
			{
				return s == null ? "" : s; //$NON-NLS-1$
			}
		};
	}
}