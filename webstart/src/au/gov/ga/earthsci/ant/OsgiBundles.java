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
package au.gov.ga.earthsci.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that generates the <code>osgi.bundles</code> property for a FileSet
 * of JAR files. Uses the {@link BundleProperties} class to determine the
 * os/arch of each JAR file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OsgiBundles extends Task
{
	private String property;
	
	private String defaultStartLevel = "4";
	private boolean defaultAutoStart = false;
	
	private List<FileSet> filesets = new ArrayList<FileSet>();
	private Map<String, StartLevel> startLevels = new HashMap<String, StartLevel>();

	public void setProperty(String property)
	{
		this.property = property;
	}

	public void setDefaultStartLevel(String startLevel)
	{
		this.defaultStartLevel = startLevel;
	}
	
	public void setDefaultAutoStart(boolean autoStart)
	{
		this.defaultAutoStart = autoStart;
	}
	
	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	public void addConfiguredStartLevel(StartLevel startLevel)
	{
		startLevels.put(startLevel.getBundle(), startLevel);
	}

	@Override
	public void execute() throws BuildException
	{
		MultiMap<String, BundleProperties> resources = new MultiMap<String, BundleProperties>();
		Set<String> uniqueOsArchs = new HashSet<String>();

		for (FileSet fileset : filesets)
		{
			DirectoryScanner directoryScanner = fileset.getDirectoryScanner(getProject());
			String[] includedFiles = directoryScanner.getIncludedFiles();
			for (String filename : includedFiles)
			{
				filename = filename.replace('\\', '/');
				File base = directoryScanner.getBasedir();
				File file = new File(base, filename);
				BundleProperties bundleProperties = new BundleProperties(file);
				String[] osArchs = bundleProperties.getOsArchs();
				for (String osArch : osArchs)
				{
					resources.putSingle(osArch, bundleProperties);
					uniqueOsArchs.add(osArch);
				}
			}
		}

		MultiMap<String, String> topLevelOsArchs = new MultiMap<String, String>();
		for (String osArch : uniqueOsArchs)
		{
			boolean lowestLevel = true;
			for (String test : uniqueOsArchs)
			{
				//skip itself
				if (test == osArch)
					continue;

				if (test.contains(osArch))
				{
					lowestLevel = false;
					break;
				}
			}

			if (lowestLevel)
			{
				for (String test : uniqueOsArchs)
				{
					if (osArch.contains(test))
					{
						topLevelOsArchs.putSingle(osArch, test);
					}
				}
			}
		}

		StringBuilder sb = new StringBuilder();

		for (Entry<String, List<String>> topLevelOsArch : topLevelOsArchs.entrySet())
		{
			sb.append("\t<resources");
			sb.append(topLevelOsArch.getKey());
			sb.append(">\n");
			sb.append("\t\t<property name=\"osgi.bundles\" value=\"");
			for (String osArch : topLevelOsArch.getValue())
			{
				List<BundleProperties> bundles = resources.get(osArch);
				if (bundles != null)
				{
					for (BundleProperties bundle : bundles)
					{
						String name = bundle.getSymbolicName();
						sb.append(name).append("@");
						
						String startLevel = getStartLevel(name);
						boolean autoStart = isAutoStart(name);
						
						if (startLevel != null)
						{
							sb.append(startLevel);
						}
						if (startLevel != null && autoStart)
						{
							sb.append("\\");
						}
						if (autoStart)
						{
							sb.append(":start");
						}
						sb.append(',');
					}
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\" />\n");
			sb.append("\t</resources>\n");
		}

		getProject().setNewProperty(property, sb.toString());
	}
	
	private boolean isAutoStart(String bundle)
	{
		if (startLevels.containsKey(bundle))
		{
			return startLevels.get(bundle).isAutoStart();
		}
		return defaultAutoStart;
	}
	
	private String getStartLevel(String bundle)
	{
		if (startLevels.containsKey(bundle))
		{
			return startLevels.get(bundle).getLevel();
		}
		return defaultStartLevel;
	}
}
