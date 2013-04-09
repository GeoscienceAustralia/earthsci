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
package au.gov.ga.earthsci.core.retrieve.result;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;

/**
 * Abstract implementation of {@link IRetrievalData}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractRetrievalData implements IRetrievalData
{
	protected final URL url;
	protected final long contentLength;
	protected final String contentType;

	private Logger logger = LoggerFactory.getLogger(AbstractRetrievalData.class);

	public AbstractRetrievalData(URL url, long contentLength, String contentType)
	{
		this.url = url;
		this.contentLength = contentLength;
		this.contentType = contentType;
	}

	@Override
	public long getContentLength()
	{
		return contentLength;
	}

	@Override
	public String getContentType()
	{
		return contentType;
	}

	@Override
	public File getFile()
	{
		//first check if the URL is already a file URL
		if ("file".equalsIgnoreCase(url.getProtocol())) //$NON-NLS-1$
		{
			try
			{
				return new File(url.toURI());
			}
			catch (URISyntaxException e)
			{
			}
		}

		//next try the Eclipse FileLocator
		try
		{
			URL fileURL = FileLocator.toFileURL(url);
			if (fileURL != null && "file".equalsIgnoreCase(fileURL.getProtocol())) //$NON-NLS-1$
			{
				return new File(fileURL.toURI());
			}
		}
		catch (Exception e)
		{
		}

		//finally just save the InputStream to a temporary file
		try
		{
			InputStream is = getInputStream();
			try
			{
				String prefix = getClass().getSimpleName();
				String suffix = Util.getExtension(url.getPath());
				suffix = Util.blankNullString(suffix);
				return Util.writeInputStreamToTemporaryFile(is, prefix, suffix);
			}
			finally
			{
				is.close();
			}
		}
		catch (Exception e)
		{
			logger.error("Error converting url to file", e); //$NON-NLS-1$
		}
		return null;
	}

}
