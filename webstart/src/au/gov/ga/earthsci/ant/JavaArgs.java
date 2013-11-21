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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that generates the <code>&lt;j2se&gt;</code> element with a
 * <code>java-vm-args</code> with a custom set of arguments.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JavaArgs extends Task
{
	private String property;
	private String version;
	private List<FileSet> filesets = new ArrayList<FileSet>();
	private List<JavaArg> arguments = new ArrayList<JavaArg>();

	public void setProperty(String property)
	{
		this.property = property;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	public void addConfiguredJavaArg(JavaArg arg)
	{
		arguments.add(arg);
	}

	@Override
	public void execute() throws BuildException
	{
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
					uniqueOsArchs.add(osArch);
				}
			}
		}

		Set<String> topLevelOsArchs = new HashSet<String>();
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
				topLevelOsArchs.add(osArch);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String topLevelOsArch : topLevelOsArchs)
		{
			Pattern osPattern = Pattern.compile("os=\"([^\"]+)\"");
			Pattern archPattern = Pattern.compile("arch=\"([^\"]+)\"");
			Matcher osMatcher = osPattern.matcher(topLevelOsArch);
			Matcher archMatcher = archPattern.matcher(topLevelOsArch);
			String os = osMatcher.find() ? osMatcher.group(1) : null;
			String arch = archMatcher.find() ? archMatcher.group(1) : null;
			String initialHeapSize = null, maxHeapSize = null;

			sb.append("\t<resources");
			sb.append(topLevelOsArch);
			sb.append(">\n");
			sb.append("\t\t<j2se ");
			if (version != null)
			{
				sb.append("version=\"" + version + "\" ");
			}
			sb.append("java-vm-args=\"");
			boolean addedArgument = false;
			for (JavaArg arg : arguments)
			{
				boolean osMatches = arg.getOs() == null || (os != null && os.equals(arg.getOs()));
				boolean archMatches = arg.getArch() == null || (arch != null && arch.equals(arg.getArch()));
				if (osMatches && archMatches)
				{
					if (!Util.isEmpty(arg.getArgument()))
					{
						sb.append(arg.getArgument() + " ");
						addedArgument = true;
					}
					if (!Util.isEmpty(arg.getInitialheapsize()))
					{
						initialHeapSize = arg.getInitialheapsize();
					}
					if (!Util.isEmpty(arg.getMaxheapsize()))
					{
						maxHeapSize = arg.getMaxheapsize();
					}
				}
			}
			if (addedArgument)
			{
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("\"");
			if (initialHeapSize != null)
			{
				sb.append(" initial-heap-size=\"" + initialHeapSize + "\"");
			}
			if (maxHeapSize != null)
			{
				sb.append(" max-heap-size=\"" + maxHeapSize + "\"");
			}
			sb.append(" />\n");
			sb.append("\t</resources>\n");
		}

		getProject().setNewProperty(property, sb.toString());
	}
}
