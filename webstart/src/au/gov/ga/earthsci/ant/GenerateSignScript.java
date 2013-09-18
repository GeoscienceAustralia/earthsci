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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that generates a script that can sign (and optionally pack) a
 * fileset of jars using a keystore.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GenerateSignScript extends Task
{
	private String filename;
	private boolean pack;
	private List<FileSet> filesets = new ArrayList<FileSet>();
	private String keystore;
	private String alias;
	private String storepass;
	private String firstline;

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setPack(boolean pack)
	{
		this.pack = pack;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	public void setKeystore(String keystore)
	{
		this.keystore = keystore;
	}

	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	public void setStorepass(String password)
	{
		this.storepass = password;
	}

	public void setFirstline(String firstline)
	{
		this.firstline = firstline;
	}

	@Override
	public void execute() throws BuildException
	{
		if (filename == null || filename.isEmpty())
		{
			throw new BuildException("Sign script filename not specified");
		}
		if (keystore == null || keystore.isEmpty())
		{
			throw new BuildException("Keystore not specified");
		}
		if (alias == null || alias.isEmpty())
		{
			throw new BuildException("Alias not specified");
		}

		File scriptFile = new File(filename);
		File scriptDirectory = scriptFile.getParentFile();

		List<File> files = new ArrayList<File>();
		for (FileSet fileset : filesets)
		{
			DirectoryScanner directoryScanner = fileset.getDirectoryScanner(getProject());
			String[] includedFiles = directoryScanner.getIncludedFiles();
			for (String filename : includedFiles)
			{
				if (!filename.toLowerCase().endsWith(".jar"))
				{
					//skip non-jar files
					continue;
				}

				File base = directoryScanner.getBasedir();
				File file = new File(base, filename);
				File relative;
				try
				{
					relative = Util.getRelativeFile(file, scriptDirectory);
				}
				catch (IOException e)
				{
					throw new BuildException("Error generating relative path", e);
				}
				files.add(relative);
			}
		}

		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		if (firstline != null && !firstline.isEmpty())
		{
			sb.append(firstline + newline + newline);
		}

		String keystore = this.keystore;
		try
		{
			File relativeKeystore = Util.getRelativeFile(new File(keystore), scriptDirectory);
			keystore = relativeKeystore.getPath();
		}
		catch (IOException e)
		{
			throw new BuildException("Error generating relative keystore path", e);
		}
		String jarsignerPrefix = "jarsigner -keystore \"" + keystore + "\" ";
		if (storepass != null && !storepass.isEmpty())
		{
			jarsignerPrefix += "-storepass \"" + storepass.replaceAll("\"", "\\\"") + "\" ";
		}
		String jarsignerSuffix = " " + "\"" + alias + "\"";

		for (File file : files)
		{
			sb.append(jarsignerPrefix + "\"" + file.getPath() + "\"" + jarsignerSuffix + newline);
		}
		if (pack)
		{
			sb.append(newline);
			for (File file : files)
			{
				String path = file.getPath();
				String packPath = path.substring(0, path.length() - 4) + ".pack.gz";
				sb.append("pack200 \"" + packPath + "\" \"" + path + "\"" + newline);
			}
		}

		try
		{
			FileOutputStream fos = new FileOutputStream(scriptFile);
			fos.write(sb.toString().getBytes());
			fos.close();
		}
		catch (IOException e)
		{
			throw new BuildException("Error writing sign script file", e);
		}
	}
}
