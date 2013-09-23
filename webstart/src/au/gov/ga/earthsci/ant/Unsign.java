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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task that unsigns a FileSet of JAR files, saving the unsigned versions in
 * a directory.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Unsign extends Task
{
	private String todir;
	private Vector<FileSet> filesets = new Vector<FileSet>();
	private boolean preserveLastModified = true;
	private boolean force = false;

	public void setTodir(String todir)
	{
		this.todir = todir;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	public void setPreserveLastModified(boolean preserveLastModified)
	{
		this.preserveLastModified = preserveLastModified;
	}

	public void setForce(boolean force)
	{
		this.force = force;
	}

	@Override
	public void execute() throws BuildException
	{
		File outputDir = new File(todir);
		outputDir.mkdirs();

		for (FileSet fileset : filesets)
		{
			DirectoryScanner directoryScanner = fileset.getDirectoryScanner(getProject());
			String[] includedFiles = directoryScanner.getIncludedFiles();
			for (String filename : includedFiles)
			{
				filename = filename.replace('\\', '/');
				File base = directoryScanner.getBasedir();
				File file = new File(base, filename);
				File output = new File(outputDir, file.getName());

				long lastModified = file.lastModified();
				boolean sameFile = file.equals(output);
				if (!force && !sameFile && output.exists())
				{
					if (output.lastModified() >= lastModified)
					{
						//destination file is newer than new file, so skip
						System.out.println("JAR is not newer, skipping: " + file);
						continue;
					}

					BundleProperties srcBundle = new BundleProperties(file);
					BundleProperties dstBundle = new BundleProperties(output);
					String srcVersion = srcBundle.getVersion();
					String dstVersion = dstBundle.getVersion();
					if (srcVersion != null && srcVersion.length() > 0 && dstVersion != null && dstVersion.length() > 0)
					{
						if (srcVersion.equals(dstVersion))
						{
							//bundle versions are the same, so skip
							System.out.println("Skipping JAR with same version (" + srcVersion + ") as destination: "
									+ file);
							continue;
						}
						//We cannot perform version comparison here to see which one is newer, because the
						//tycho-buildtimestamp-jgit plugin will fall back onto the default build timestamp
						//provider if the working copy is dirty. This will mean that, if the user builds with
						//a dirty working copy, and then discards changes, the build timestamp will revert to
						//the last commit on that project, which will be older than the timestamp used when
						//building with the dirty working copy.
					}
				}

				System.out.println("Unsigning JAR: " + file + (sameFile ? "" : " to " + output));

				try
				{
					unsign(file, output);
				}
				catch (IOException e)
				{
					throw new BuildException(e);
				}

				if (preserveLastModified)
				{
					output.setLastModified(lastModified);
				}
			}
		}
	}

	private void unsign(File input, File output) throws IOException
	{
		JarInputStream jis = null;
		JarOutputStream jos = null;

		try
		{
			InputStream is = new FileInputStream(input);
			OutputStream os = new FileOutputStream(output);

			jis = new JarInputStream(is);
			Manifest manifest = jis.getManifest();
			if (manifest == null)
			{
				jos = new JarOutputStream(os);
			}
			else
			{
				manifest.getEntries().clear();
				jos = new JarOutputStream(os, manifest);
			}

			JarEntry entry;
			while ((entry = jis.getNextJarEntry()) != null)
			{
				String name = entry.getName();
				String lower = name.toLowerCase();
				if (!(lower.startsWith("meta-inf") && (lower.endsWith(".rsa") || lower.endsWith(".dsa") || lower
						.endsWith(".sf"))))
				{
					JarEntry dest = new JarEntry(name);
					jos.putNextEntry(dest);
					copyContent(jis, jos);
				}
			}
		}
		finally
		{
			if (jos != null)
			{
				jos.close();
			}
			if (jis != null)
			{
				jis.close();
			}
		}
	}

	private static void copyContent(JarInputStream jis, JarOutputStream jos) throws IOException
	{
		byte[] buffer = new byte[10240];
		int len = 0;
		while ((len = jis.read(buffer)) != -1)
		{
			jos.write(buffer, 0, len);
		}
	}

}
