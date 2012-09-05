package au.gov.ga.earthsci.ant;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class JnlpResources extends Task
{
	private String property;
	private String prefix;
	private Vector<FileSet> filesets = new Vector<FileSet>();
	private static Map<String, String> osMap = new HashMap<String, String>();
	private static Map<String, String> archMap = new HashMap<String, String>();

	static
	{
		osMap.put("win32", "Windows");
		osMap.put("macosx", "Mac OS X");
		osMap.put("linux", "Linux");
	}

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
				String[] osArchs = getOsArchs(file);
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

	private String[] getOsArchs(File file)
	{
		try
		{
			URL url = new URL("jar:" + file.toURI().toURL() + "!/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attributes = manifest.getMainAttributes();
			String platformFilter = attributes.getValue("Eclipse-PlatformFilter");
			if (platformFilter != null)
			{
				Pattern propertyPattern = Pattern.compile("\\(([\\w.]+)=([\\w.]+)\\)");
				Matcher matcher = propertyPattern.matcher(platformFilter);
				int start = 0;
				MultiMap<String, String> properties = new MultiMap<String, String>();
				while (matcher.find(start))
				{
					properties.putSingle(matcher.group(1), matcher.group(2));
					start = matcher.end();
				}

				List<String> oss = properties.get("osgi.os");
				List<String> archs = properties.get("osgi.arch");
				if (oss != null && !oss.isEmpty())
				{
					String[] combinations = new String[oss.size() * Math.max(1, archs.size())];
					int i = 0;
					for (String os : oss)
					{
						if (osMap.containsKey(os))
						{
							os = osMap.get(os);
						}
						if (archs != null && !archs.isEmpty())
						{
							for (String arch : archs)
							{
								if (archMap.containsKey(arch))
								{
									arch = archMap.get(arch);
								}
								combinations[i++] = " os=\"" + os + "\" arch=\"" + arch + "\"";
							}
						}
						else
						{
							combinations[i++] = " os=\"" + os + "\"";
						}
					}
					return combinations;
				}
			}
		}
		catch (Exception e)
		{
		}
		return new String[] { "" };
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

	private static class MultiMap<K, V> extends HashMap<K, List<V>>
	{
		public void putSingle(K key, V value)
		{
			List<V> values = null;
			if (containsKey(key))
			{
				values = get(key);
			}
			else
			{
				values = new ArrayList<V>();
				put(key, values);
			}
			values.add(value);
		}
	}
}
