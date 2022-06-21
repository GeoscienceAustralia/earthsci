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
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that automatically creates a list of <code>&lt;resources&gt;</code>
 * elements for a fileset of JAR files. Uses the {@link BundleProperties} class
 * to determine the os/arch of each JAR file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JnlpResources extends Task
{
	private String property1;
	private String property2;
	private String basedir;
	private Vector<FileSet> filesets = new Vector<FileSet>();

	public void setProperty1(String property1)
	{
		this.property1 = property1;
	}
	
	public void setProperty2(String property2)
	{
		this.property2 = property2;
	}

	public void setBasedir(String basedir)
	{
		this.basedir = basedir;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	@Override
	public void execute() throws BuildException
	{
		MultiMap<String, String> resources = new MultiMap<String, String>();

		File relativeBase = new File(basedir);
		for (FileSet fileset : filesets)
		{
			DirectoryScanner directoryScanner = fileset.getDirectoryScanner(getProject());
			String[] includedFiles = directoryScanner.getIncludedFiles();
			for (String filename : includedFiles)
			{
				File base = directoryScanner.getBasedir();
				File file = new File(base, filename);
				BundleProperties bundleProperties = new BundleProperties(file);
				String[] osArchs = bundleProperties.getOsArchs();
				try
				{
					File relative = Util.getRelativeFile(file, relativeBase);
					for (String osArch : osArchs)
					{
						resources.putSingle(osArch, relative.getPath().replace('\\', '/'));
					}
					if (filename.startsWith("au.gov.ga.earthsci.application")) {
						getProject().setNewProperty(property2, filename);
					}
				}
				catch (IOException e)
				{
					throw new BuildException("Error generating relative path", e);
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		appendResources(sb, "", resources.get(""));
		for (Entry<String, List<String>> entry : resources.entrySet())
		{
			if ("".equals(entry.getKey()))
				continue;
			appendResources(sb, entry.getKey(), entry.getValue());
		}

		getProject().setNewProperty(property1, sb.toString());
	}

	private void appendResources(StringBuilder sb, String osArch, List<String> filenames)
	{
		if (filenames == null || filenames.isEmpty())
			return;

		sb.append("\t<resources");
		sb.append(osArch);
		sb.append(">\n");
		for (String filename : filenames)
		{
			sb.append("\t\t<jar href=\"");
			sb.append(filename);
			sb.append("\" />\n");
		}
		sb.append("\t</resources>\n");
	}
}
