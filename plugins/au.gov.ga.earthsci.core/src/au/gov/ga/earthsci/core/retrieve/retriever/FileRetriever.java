/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.core.retrieve.retriever;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.osgi.util.NLS;

import au.gov.ga.earthsci.core.retrieve.IRetriever;

/**
 * An {@link IRetriever} that can handle requests for {@code file://} URLs
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FileRetriever extends AbstractRetriever implements IRetriever
{
	
	public static final String FILE_PROTOCOL = "file"; //$NON-NLS-1$

	@Override
	public boolean supports(URL url)
	{
		if (url == null)
		{
			return false;
		}
		return url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL);
	}
	
	@Override
	protected String getMessageForRetrievalException(URLConnection connection, Exception e)
	{
		if (e instanceof FileNotFoundException)
		{
			return NLS.bind(Messages.FileRetriever_FileNotFoundMessage, connection.getURL().getPath());
		}
		return super.getMessageForRetrievalException(connection, e);
	}

}
