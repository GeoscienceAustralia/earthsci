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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.jar.Pack200.Unpacker;
import java.util.zip.GZIPOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task used to (re)pack200 JAR files. Repacking means packing and then
 * immediately unpacking. This is useful if you need to sign a JAR before
 * packing (as packing it changes the signature of the JAR file).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JarPack200 extends Task
{
	private Vector<FileSet> filesets = new Vector<FileSet>();
	private String todir;
	private boolean repack = false;
	private boolean preservelastmodified = true;
	private boolean force = false;

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	public void setTodir(String todir)
	{
		this.todir = todir;
	}

	public void setRepack(boolean repack)
	{
		this.repack = repack;
	}

	public void setPreservelastmodified(boolean preservelastmodified)
	{
		this.preservelastmodified = preservelastmodified;
	}

	public void setForce(boolean force)
	{
		this.force = force;
	}

	@Override
	public void execute() throws BuildException
	{
		Packer packer = Pack200.newPacker();
		Unpacker unpacker = Pack200.newUnpacker();

		File outputDir = todir == null || todir.length() == 0 ? null : new File(todir);
		if (outputDir != null)
		{
			outputDir.mkdirs();
		}

		try
		{
			for (FileSet fileset : filesets)
			{
				DirectoryScanner directoryScanner = fileset.getDirectoryScanner(getProject());
				String[] includedFiles = directoryScanner.getIncludedFiles();
				for (String filename : includedFiles)
				{
					filename = filename.replace('\\', '/');
					File base = directoryScanner.getBasedir();
					File file = new File(base, filename);
					File parent = outputDir == null ? file.getParentFile() : outputDir;
					File packFile = new File(parent, getPackFilename(file));
					File repackFile = new File(parent, file.getName());

					File output = repack ? repackFile : packFile;
					long lastModified = file.lastModified();
					boolean sameFile = file.equals(output);
					if (!force && !sameFile && output.exists() && output.lastModified() >= lastModified)
					{
						//not modified, skip
						continue;
					}

					System.out.println((repack ? "Re" : "") + "Pack200ing JAR: " + file
							+ (sameFile ? "" : " to " + output));

					pack(packer, file, packFile, !repack);
					if (repack)
					{
						unpack(unpacker, packFile, repackFile);
						packFile.delete();
					}

					if (preservelastmodified)
					{
						output.setLastModified(lastModified);
					}
				}
			}
		}
		catch (IOException e)
		{
			throw new BuildException(e);
		}
	}

	private void pack(Packer packer, File input, File output, boolean gzip) throws IOException
	{
		JarFile jarFile = null;
		OutputStream os = null;
		try
		{
			jarFile = new JarFile(input);
			os = new FileOutputStream(output);
			if (gzip)
			{
				os = new GZIPOutputStream(os);
			}
			os = new BufferedOutputStream(os);
			packer.pack(jarFile, os);
		}
		finally
		{
			if (jarFile != null)
			{
				jarFile.close();
			}
			if (os != null)
			{
				os.close();
			}
		}
	}

	private void unpack(Unpacker unpacker, File input, File output) throws IOException
	{
		JarOutputStream jos = null;
		try
		{
			jos = new JarOutputStream(new FileOutputStream(output));
			unpacker.unpack(input, jos);
		}
		finally
		{
			if (jos != null)
			{
				jos.close();
			}
		}
	}

	private String getPackFilename(File jarFile)
	{
		String suffix = repack ? ".pack" : ".pack.gz";
		return jarFile.getName() + suffix;
	}
}
