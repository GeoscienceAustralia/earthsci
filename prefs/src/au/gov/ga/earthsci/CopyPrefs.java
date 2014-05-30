/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility to copy the Eclipse project preferences from this project to all
 * plugin projects, excluding a blacklist.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public class CopyPrefs
{
	public static final Set<String> BLACKLIST = new HashSet<String>()
	{
		{
			add("au.gov.ga.earthsci.eclipse.extras");
			add("au.gov.ga.earthsci.jface.extras");
			add("org.eclipse.ui.workbench.compatibility");
		}
	};

	public static void main(String[] args) throws IOException
	{
		File prefsDir = new File(".");
		File projectDir = new File(prefsDir, "../");
		File pluginsDir = new File(projectDir, "plugins");
		File settingsDir = new File(prefsDir, ".settings");
		for (File plugin : pluginsDir.listFiles())
		{
			if (plugin.isDirectory() && !BLACKLIST.contains(plugin.getName()))
			{
				System.out.println("Processing " + plugin.getName());
				File pluginSettingsDir = new File(plugin, ".settings");
				for (File settings : settingsDir.listFiles())
				{
					if (settings.isFile() && settings.getName().toLowerCase().endsWith(".prefs"))
					{
						File pluginSettings = new File(pluginSettingsDir, settings.getName());
						pluginSettingsDir.mkdirs();
						System.out.println("Copying " + settings + " to " + pluginSettings);
						copyFile(settings, pluginSettings);
					}
				}
			}
		}
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException
	{
		if (!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try
		{
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally
		{
			if (source != null)
			{
				source.close();
			}
			if (destination != null)
			{
				destination.close();
			}
		}
	}
}
