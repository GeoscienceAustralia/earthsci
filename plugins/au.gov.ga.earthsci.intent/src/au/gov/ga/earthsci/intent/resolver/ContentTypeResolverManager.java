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
package au.gov.ga.earthsci.intent.resolver;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.internal.content.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.intent.Intent;

/**
 * Manages the set of {@link IContentTypeResolver}s, registered using the
 * {@value #CONTENT_TYPE_RESOLVERS_ID} extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ContentTypeResolverManager
{
	public static final String CONTENT_TYPE_RESOLVERS_ID = "au.gov.ga.earthsci.intent.contentTypeResolvers"; //$NON-NLS-1$

	private static final Set<IContentTypeResolver> resolvers = new HashSet<IContentTypeResolver>();
	private static final Logger logger = LoggerFactory.getLogger(ContentTypeResolverManager.class);

	static
	{
		IConfigurationElement[] config =
				RegistryFactory.getRegistry().getConfigurationElementsFor(CONTENT_TYPE_RESOLVERS_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				IContentTypeResolver resolver = (IContentTypeResolver) element.createExecutableExtension("class"); //$NON-NLS-1$
				resolvers.add(resolver);
			}
			catch (CoreException e)
			{
				logger.error("Error registering content type resolver", e); //$NON-NLS-1$
			}
		}
	}

	public static IContentType resolveContentType(URL url, Intent intent) throws IOException
	{
		nullSaxParserFactoryWorkaround();

		for (IContentTypeResolver resolver : resolvers)
		{
			if (resolver.supports(url, intent))
			{
				IContentType contentType = resolver.resolve(url, intent);
				return contentType;
			}
		}
		return null;
	}

	/**
	 * Make a call into the Eclipse content activator to make sure the
	 * SAXParserFactory is non-null. Sometimes on startup, this is null, which
	 * causes the XML content type describers to fail.
	 */
	private static void nullSaxParserFactoryWorkaround()
	{
		//FIXME This is a workaround for a bug which is probably threading related, fix properly.

		int tries = 0;
		while (Activator.getDefault().getFactory() == null)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
			}
			if (tries++ > 50)
			{
				//5 seconds
				break;
			}
		}
	}
}
