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
package au.gov.ga.earthsci.gitinfo;

import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

/**
 * Initializes system properties containing the properties within the generated
 * .properties file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PropertyInitializer
{
	@PostConstruct
	public void init()
	{
		if (GitInformation.isSet())
		{
			Properties properties = GitInformation.getProperties();
			Set<String> names = properties.stringPropertyNames();
			for (String name : names)
			{
				String value = properties.getProperty(name);
				System.setProperty(name, value);
			}
		}
		else
		{
			System.setProperty(GitInformation.GIT_DESCRIBE_PROPERTY_KEY, "<Git version information unavailable>"); //$NON-NLS-1$
		}
	}
}
