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

public class OsgiBundles extends Task
{
	private String property;
	private List<FileSet> filesets = new ArrayList<FileSet>();
	private Map<String, Integer> startLevels = new HashMap<String, Integer>();

	public void setProperty(String property)
	{
		this.property = property;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}

	public void addConfiguredStartLevel(StartLevel startLevel)
	{
		startLevels.put(startLevel.getBundle(), startLevel.getLevel());
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
						Integer startLevel = startLevels.get(name);
						sb.append(name);
						sb.append(startLevel != null ? "@" + startLevel + ":start," : "@:start,");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append("\" />\n");
			sb.append("\t</resources>\n");
		}

		getProject().setNewProperty(property, sb.toString());
	}
}
