package au.gov.ga.earthsci.ant;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BundleProperties
{
	private final String symbolicName;
	private final String platformFilter;
	private String[] osArchs = null;

	public BundleProperties(File jarFile)
	{
		String platformFilter = null;
		String symbolicName = null;
		try
		{
			URL url = new URL("jar:" + jarFile.toURI().toURL() + "!/META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());
			Attributes attributes = manifest.getMainAttributes();
			platformFilter = attributes.getValue("Eclipse-PlatformFilter");
			symbolicName = attributes.getValue("Bundle-SymbolicName");

			int indexOfSemiColon = symbolicName.indexOf(';');
			if (indexOfSemiColon >= 0)
			{
				symbolicName = symbolicName.substring(0, indexOfSemiColon);
			}
		}
		catch (Exception e)
		{
		}

		this.platformFilter = platformFilter;
		this.symbolicName = symbolicName;
	}

	public String getSymbolicName()
	{
		return symbolicName;
	}

	public String getPlatformFilter()
	{
		return platformFilter;
	}

	public String[] getOsArchs()
	{
		if (osArchs == null)
		{
			osArchs = calculateOsArchs();
		}
		return osArchs;
	}

	private String[] calculateOsArchs()
	{
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
			//on some 64-bit platforms, webstart uses x86_64, others use amd64, so add both
			if (archs != null)
			{
				boolean x86_64 = archs.contains("x86_64");
				boolean amd64 = archs.contains("amd64");
				if (x86_64 || amd64)
				{
					if (!x86_64)
					{
						archs.add("x86_64");
					}
					if (!amd64)
					{
						archs.add("amd64");
					}
				}
			}
			if (oss != null && !oss.isEmpty())
			{
				String[] combinations = new String[oss.size() * Math.max(1, archs == null ? 0 : archs.size())];
				int i = 0;
				for (String os : oss)
				{
					os = Util.getJnlpOs(os);
					if (archs != null && !archs.isEmpty())
					{
						for (String arch : archs)
						{
							arch = Util.getJnlpArch(arch);
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
		return new String[] { "" };
	}
}
