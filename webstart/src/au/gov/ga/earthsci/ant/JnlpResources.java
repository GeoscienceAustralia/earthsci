package au.gov.ga.earthsci.ant;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class JnlpResources extends Task
{
	private String property;
	private String prefix;
	private Vector<FileSet> filesets = new Vector<FileSet>();

	public void setProperty(String property)
	{
		this.property = property;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	@Override
	public void execute() throws BuildException
	{
		MultiMap<String, String> resources = new MultiMap<String, String>();

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
					resources.putSingle(osArch, filename);
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

		getProject().setNewProperty(property, sb.toString());
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
			sb.append(prefix);
			sb.append(filename);
			sb.append("\" />\n");
		}
		sb.append("\t</resources>\n");
	}
}
