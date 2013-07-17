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
package au.gov.ga.earthsci.discovery.csw;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import au.gov.ga.earthsci.common.util.Util;


/**
 * An enumeration of different CSW implementations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public enum CSWFormat
{
	/**
	 * <a href="http://sourceforge.net/projects/catogc/">http://sourceforge.net/
	 * projects/catogc/</a>
	 */
	CATOGC("catogc", "2.0.2", "CatOGC"),
	/**
	 * <a href="http://www.conterra.de/en/products/sdi/terracatalog/index.shtm">
	 * http://www.conterra.de/en/products/sdi/terracatalog/index.shtm< /a>
	 */
	CONTERRA("conterra", "2.0.2", "con terra"),
	/**
	 * <a href="http://www.deegree.org/">http://www.deegree.org/</a>
	 */
	DEEGREE("deegree", "2.0.2", "deegree"),
	/**
	 * <a
	 * href="http://gdsc.nlr.nl/gdsc/en/software/excat/manual">http://gdsc.nlr
	 * .nl/gdsc/en/software/excat/manual</a>
	 */
	EXCAT("excat", "2.0.2", "eXcat"),
	/**
	 * <a
	 * href="http://geospatial.intergraph.com/products/GeoMedia/Details.aspx">
	 * http://geospatial.intergraph.com/products/GeoMedia/Details.aspx </a>
	 */
	GEOMEDIA0("geomedia", "2.0.0", "GeoMedia 2.0.0"),
	/**
	 * <a
	 * href="http://geospatial.intergraph.com/products/GeoMedia/Details.aspx">
	 * http://geospatial.intergraph.com/products/GeoMedia/Details.aspx </a>
	 */
	GEOMEDIA2("geomedia", "2.0.2", "GeoMedia 2.0.2"),
	/**
	 * <a href="http://geonetwork-opensource.org/">http://geonetwork-opensource.
	 * org/</a>
	 */
	GEONETWORK1("geonetwork", "2.0.1", "GeoNetwork 2.0.1"),
	/**
	 * <a href="http://geonetwork-opensource.org/">http://geonetwork-opensource.
	 * org/</a>
	 */
	GEONETWORK2("geonetwork", "2.0.2", "GeoNetwork 2.0.2"),
	/**
	 * <a href="http://www.geofabrics.com/downloads/dload.htm?name=gpt9">http://
	 * www.geofabrics.com/downloads/dload.htm?name=gpt9</a>
	 */
	GPT9("gpt9", "2.0.2", "GPT9"),
	/**
	 * <a href="http://www.galdosinc.com/products/indicio">http://www.galdosinc.
	 * com/products/indicio</a>
	 */
	INDICIO0("indicio", "2.0.0", "INdicio 2.0.0"),
	/**
	 * <a href="http://www.galdosinc.com/products/indicio">http://www.galdosinc.
	 * com/products/indicio</a>
	 */
	INDICIO1("indicio", "2.0.1", "INdicio 2.0.1");

	public final String directory;
	public final String version;
	public final String label;

	private CSWFormat(String directory, String version, String label)
	{
		this.directory = directory;
		this.version = version;
		this.label = label;
	}

	/**
	 * Generate a CSW request string (for the HTTP POST payload) for the given
	 * CSW format.
	 * 
	 * @param parameters
	 *            CSW query parameters
	 * @param startPosition
	 *            Start record position (1-indexed)
	 * @param maxRecords
	 *            Maximum number of records to return (can be 0)
	 * @return CSW request string
	 */
	public String generateRequest(CSWRequestParameters parameters, int startPosition, int maxRecords)
	{
		if (startPosition <= 0)
		{
			startPosition = 1;
		}
		if (maxRecords < 0)
		{
			maxRecords = 10;
		}

		int count = 0;

		//String query = "";
		String body = "";

		boolean bboxDefined = parameters.bbox != null && parameters.bbox.length == 4;
		if (bboxDefined)
		{
			String template = getTemplate("bbox");
			template = template.replace("$XMIN", Double.toString(parameters.bbox[0]));
			template = template.replace("$YMIN", Double.toString(parameters.bbox[1]));
			template = template.replace("$XMAX", Double.toString(parameters.bbox[2]));
			template = template.replace("$YMAX", Double.toString(parameters.bbox[3]));
			body += template;
			//query += "&BBOX=" + parameters.bbox[0] + "," + parameters.bbox[1] + "," + parameters.bbox[2] + "," + parameters.bbox[3];
			count++;
		}

		if (!Util.isEmpty(parameters.title))
		{
			body += getTemplate("title").replace("$VALUE", parameters.title);
			//query += "&TITLE=" + URLEncoder.encode(parameters.title, "UTF-8");
			count++;
		}

		if (!Util.isEmpty(parameters.description))
		{
			body += getTemplate("description").replace("$VALUE", parameters.description);
			//query += "&DESCRIPTION=" + URLEncoder.encode(parameters.description, "UTF-8");
			count++;
		}

		if (!Util.isEmpty(parameters.subject))
		{
			body += getTemplate("subject").replace("$VALUE", parameters.subject);
			//query += "&SUBJECT=" + URLEncoder.encode(parameters.subject, "UTF-8");
			count++;
		}

		if (!Util.isEmpty(parameters.any))
		{
			body += getTemplate("any").replace("$VALUE", parameters.any);
			//query += "&ANY=" + URLEncoder.encode(parameters.any, "UTF-8");
			count++;
		}

		if (!Util.isEmpty(parameters.organisation))
		{
			body += getTemplate("organisation").replace("$VALUE", parameters.organisation);
			//query += "&ORGANISATION=" + URLEncoder.encode(parameters.organisation, "UTF-8");
			count++;
		}

		if (!Util.isEmpty(parameters.language))
		{
			body += getTemplate("language").replace("$VALUE", parameters.language);
			//query += "&LANGUAGE=" + URLEncoder.encode(parameters.language, "UTF-8");
			count++;
		}

		boolean indicio = this.name().toLowerCase().contains("indicio");

		if (count == 0)
		{
			body += getTemplate("title").replace("$VALUE", "");
		}
		else if (count >= 2 && !indicio)
		{
			//indicio templates already have the ogc:And element
			body = "<ogc:And>" + body + "</ogc:And>";
		}

		String request = getTemplate("body").replace("$FILTER", body);


		request = request.replace("$STARTPOSITION", Integer.toString(startPosition));
		//query += "&STARTPOSITION=" + startPosition;

		request = request.replace("$MAXRECORDS", Integer.toString(maxRecords));
		//query += "&MAXRECORDS=" + maxRecords;


		//crazy hacks for indicio
		if (indicio)
		{
			if (!Util.isEmpty(parameters.organisation))
			{
				request =
						request.replace("csw:Query typeNames=\"",
								"csw:Query typeNames=\"Organization=o Association=a1 ");
			}
			if (bboxDefined)
			{
				request = request.replace("d/rim:Slot=", "d/rim:Slot=slotBbox,");
			}
			if (!Util.isEmpty(parameters.subject))
			{
				request = request.replace("d/rim:Slot=", "d/rim:Slot=keywordSlot,");
			}
		}


		return request;
	}

	private String getTemplate(String parameter)
	{
		String filename = "catalogs/" + directory + "/" + version + "/" + parameter + ".xml";
		InputStream is = getClass().getResourceAsStream(filename);
		try
		{
			return Util.readStreamToString(is, "UTF-8");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	/**
	 * Transform the CSW response to a common XML document using this format's
	 * XSLT transform.
	 * 
	 * @param response
	 *            Response to transform
	 * @return Transformed response (XML)
	 */
	public String transformResponse(InputStream response)
	{
		String filename = "catalogs/" + directory + "/" + version + "/response.xsl";
		InputStream xslis = getClass().getResourceAsStream(filename);
		Source xslSource = new StreamSource(xslis);

		Source xmlSource = new StreamSource(response);

		StringWriter stringWriter = new StringWriter();
		Result result = new StreamResult(stringWriter);

		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer(xslSource);
			transformer.transform(xmlSource, result);
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				xslis.close();
			}
			catch (IOException e)
			{
			}
		}
		return stringWriter.toString();
	}
}
