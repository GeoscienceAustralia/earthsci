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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.program.Program;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * {@link URLStreamHandlerService} that handles extensionicon://extension URLs,
 * which provide access to the default system icon for filename extensions.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtensionIconURLStreamHandlerService extends AbstractURLStreamHandlerService
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
				String extension = getURL().getHost();
				return getImageInputStreamForExtension(extension);
			}
		};
	}

	public static InputStream getImageInputStreamForExtension(String extension) throws IOException
	{
		Program program = Program.findProgram(extension);
		if (program == null)
		{
			throw new IOException("Program not found for extension: " + extension); //$NON-NLS-1$
		}
		ImageData data = program.getImageData();
		if (data == null)
		{
			throw new IOException("Icon not found for program for extension: " + extension); //$NON-NLS-1$
		}
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { data };
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		loader.save(baos, SWT.IMAGE_PNG);
		baos.close();
		return new ByteArrayInputStream(baos.toByteArray());
	}
}
