package au.gov.ga.earthsci.verifier;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Custom Ant task for EarthSci that checks each plugin in the plugins directory
 * for correct configuration. Each plugin must:
 * <ul>
 * <li>Contain a Maven pom.xml file</li>
 * <li>Be listed in the pom.xml file in the plugins/ directory</li>
 * <li>Include sensible files in the bin.includes property in its
 * build.properties file</li>
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class VerifyPlugins extends Task
{
	private String pluginsdir;

	public void setPluginsdir(String pluginsdir)
	{
		this.pluginsdir = pluginsdir;
	}

	@Override
	public void execute() throws BuildException
	{
		File directory = new File(pluginsdir);
		File pomFile = new File(directory, "pom.xml");
		Set<String> modules = new HashSet<String>();
		Set<String> plugins = new HashSet<String>();
		List<File> pluginDirectories = new ArrayList<File>();

		File[] files = directory.listFiles();
		for (File file : files)
		{
			if (file.isDirectory() && new File(file, ".project").exists())
			{
				plugins.add(file.getName());
				pluginDirectories.add(file);

				File pluginPom = new File(file, "pom.xml");
				if (!pluginPom.exists())
				{
					throw new BuildException(pluginPom + " not found");
				}
			}
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(pomFile);
			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression expression = xpath.compile("//modules/module");
			NodeList moduleElements = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < moduleElements.getLength(); i++)
			{
				Element moduleElement = (Element) moduleElements.item(i);
				String module = moduleElement.getTextContent();
				if (modules.contains(module))
				{
					throw new BuildException("Module defined more than once: " + module);
				}
				modules.add(module);
			}
		}
		catch (Exception e)
		{
			throw new BuildException("Error parsing plugins POM file", e);
		}

		Set<String> missingModules = new HashSet<String>(plugins);
		missingModules.removeAll(modules);
		if (!missingModules.isEmpty())
		{
			StringBuilder sb = new StringBuilder(pomFile + " is missing the following plugin modules:");
			for (String missingModule : missingModules)
			{
				sb.append("\n\t" + missingModule);
			}
			throw new BuildException(sb.toString());
		}

		for (File pluginDirectory : pluginDirectories)
		{
			File buildFile = new File(pluginDirectory, "build.properties");
			if (!buildFile.exists())
			{
				throw new BuildException(buildFile + " not found");
			}

			Properties properties = new Properties();
			try
			{
				FileInputStream fis = new FileInputStream(buildFile);
				try
				{
					properties.load(fis);
				}
				finally
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				throw new BuildException("Error reading " + buildFile, e);
			}

			String binIncludes = properties.getProperty("bin.includes");
			String[] split = binIncludes.split(",");
			Set<String> includes = new HashSet<String>(Arrays.asList(split));

			String[] patterns =
					new String[] { "META-INF", "icons", "css", "OSGI-INF", "schema", "*.e4xmi" };
			FileFilter filter = new WildcardFileFilter(patterns);
			File[] matchingFiles = pluginDirectory.listFiles(filter);
			for (File matchingFile : matchingFiles)
			{
				String match = matchingFile.getName();
				if (matchingFile.isDirectory())
				{
					match += "/";
				}
				if (!includes.contains(match))
				{
					throw new BuildException(buildFile + " missing '" + match + "' from it's bin.includes property");
				}
			}
		}
	}
}
