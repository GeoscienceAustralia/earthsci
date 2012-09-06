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
				if (!force && !sameFile && output.exists() && output.lastModified() >= lastModified)
				{
					//not modified, skip
					continue;
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
