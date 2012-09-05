package au.gov.ga.earthsci.ant;

import java.util.HashMap;
import java.util.Map;

public class Util
{
	private static Map<String, String> osMap = new HashMap<String, String>();
	private static Map<String, String> archMap = new HashMap<String, String>();

	static
	{
		osMap.put("win32", "Windows");
		osMap.put("macosx", "Mac OS X");
		osMap.put("linux", "Linux");
	}

	public static String getJnlpOs(String os)
	{
		if (osMap.containsKey(os))
		{
			return osMap.get(os);
		}
		return os;
	}

	public static String getJnlpArch(String arch)
	{
		if (archMap.containsKey(arch))
		{
			return archMap.get(arch);
		}
		return arch;
	}
}
