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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Contains information about the current git commit, read from a .properties
 * file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GitInformation
{
	private static final String PROPERTIES_FILE = "/gitinfo.properties"; //$NON-NLS-1$
	private static Properties properties = new Properties();
	private static boolean set = false;

	static
	{
		InputStream is = GitInformation.class.getResourceAsStream(PROPERTIES_FILE);
		if (is != null)
		{
			try
			{
				properties.load(is);
				set = true;
			}
			catch (IOException e)
			{
			}
			finally
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	public static boolean isSet()
	{
		return set;
	}

	public static Properties getProperties()
	{
		return properties;
	}

	public static String getCommitId()
	{
		return properties.getProperty("git.commit.id"); //$NON-NLS-1$
	}

	public static String getCommitIdAbbreviated()
	{
		return properties.getProperty("git.commit.id.abbrev"); //$NON-NLS-1$
	}

	public static String getCommitMessage()
	{
		return properties.getProperty("git.commit.message.full"); //$NON-NLS-1$
	}

	public static String getCommitMessageShort()
	{
		return properties.getProperty("git.commit.message.short"); //$NON-NLS-1$
	}

	public static String getCommitUserEmail()
	{
		return properties.getProperty("git.commit.user.email"); //$NON-NLS-1$
	}

	public static String getCommitUserName()
	{
		return properties.getProperty("git.commit.user.name"); //$NON-NLS-1$
	}

	public static String getBuildUserName()
	{
		return properties.getProperty("git.build.user.name"); //$NON-NLS-1$
	}

	public static String getGitDescribe()
	{
		return properties.getProperty("git.commit.id.describe"); //$NON-NLS-1$
	}

	public static String getBuildUserEmail()
	{
		return properties.getProperty("git.build.user.email"); //$NON-NLS-1$
	}

	public static String getBranch()
	{
		return properties.getProperty("git.branch"); //$NON-NLS-1$
	}

	public static String getCommitTime()
	{
		return properties.getProperty("git.commit.time"); //$NON-NLS-1$
	}

	public static String getBuildTime()
	{
		return properties.getProperty("git.build.time"); //$NON-NLS-1$
	}

	public static String getRemoteOriginUrl()
	{
		return properties.getProperty("git.remote.origin.url"); //$NON-NLS-1$
	}
}
