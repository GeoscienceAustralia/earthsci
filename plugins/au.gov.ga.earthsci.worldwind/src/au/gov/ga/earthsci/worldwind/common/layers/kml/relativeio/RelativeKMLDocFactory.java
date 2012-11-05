/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.kml.relativeio;

import gov.nasa.worldwind.exception.WWUnrecognizedException;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * This class acts as a Factory for {@link RelativeKMLDoc} subclasses. Most of
 * the code is from the KMLRoot class, but modified to pass in the parameters
 * required to instanciate {@link RelativeKMLDoc} objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RelativeKMLDocFactory
{
	/**
	 * Create a RelativeKMLDoc from a docSource.
	 * 
	 * @param docSource
	 *            either a {@link File}, a {@link URL}, or an
	 *            {@link InputStream}, or a {@link String} identifying a file
	 *            path or URL.
	 * @param contentType
	 *            the content type of the data. Specify
	 *            {@link KMLConstants#KML_MIME_TYPE} for plain KML and
	 *            {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. The content is
	 *            treated as KML for any other value or a value of null.
	 * @param href
	 *            the original address of the document if the file is a
	 *            retrieved and cached file.
	 * @param parent
	 *            the {@link KMLDoc} from which this new {@link KMLDoc} was
	 *            referenced (null if this is root)
	 * @return A new {@link RelativeKMLDoc}.
	 */
	public static RelativeKMLDoc createKMLDoc(Object docSource, String contentType, String href, KMLDoc parent)
			throws IOException
	{
		if (docSource == null)
		{
			String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (docSource instanceof File)
			return createDocSource((File) docSource, href, parent);
		else if (docSource instanceof URL)
			return createDocSource((URL) docSource, contentType, href, parent);
		else if (docSource instanceof InputStream)
			return createDocSource((InputStream) docSource, contentType, href, parent);
		else if (docSource instanceof String)
		{
			File file = new File((String) docSource);
			if (file.exists())
				return createKMLDoc(file, contentType, href, parent);

			URL url = WWIO.makeURL(docSource);
			if (url != null)
				return createKMLDoc(url, contentType, href, parent);
		}

		return null;
	}

	protected static RelativeKMLDoc createDocSource(File docSource, String href, KMLDoc parent) throws IOException
	{
		if (WWIO.isContentType(docSource, KMLConstants.KML_MIME_TYPE))
			return new RelativeKMLFile(docSource, href, parent);
		else if (WWIO.isContentType(docSource, KMLConstants.KMZ_MIME_TYPE))
		{
			try
			{
				return new RelativeKMZFile(docSource, href, parent);
			}
			catch (ZipException e)
			{
				// We've encountered some zip files that will not open with ZipFile, but will open
				// with ZipInputStream. Try again, this time opening treating the file as a stream.
				// See WWJINT-282.
				return new RelativeKMZInputStream(new FileInputStream(docSource), WWIO.makeURI(docSource), href, parent);
			}
		}
		else
			throw new WWUnrecognizedException(Logging.getMessage("KML.UnrecognizedKMLFileType"));
	}

	protected static RelativeKMLDoc createDocSource(InputStream docSource, String contentType, String href,
			KMLDoc parent) throws IOException
	{
		if (contentType != null && contentType.equals(KMLConstants.KMZ_MIME_TYPE))
			return new RelativeKMZInputStream(docSource, null, href, parent);
		else if (contentType == null && docSource instanceof ZipInputStream)
			return new RelativeKMZInputStream(docSource, null, href, parent);
		else
			return new RelativeKMLInputStream(docSource, null, href, parent);
	}

	protected static RelativeKMLDoc createDocSource(URL docSource, String contentType, String href, KMLDoc parent)
			throws IOException
	{
		URLConnection conn = docSource.openConnection();
		if (contentType == null)
			contentType = conn.getContentType();

		if (!(KMLConstants.KMZ_MIME_TYPE.equals(contentType) || KMLConstants.KML_MIME_TYPE.equals(contentType)))
			contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(docSource.getPath()));

		if (KMLConstants.KMZ_MIME_TYPE.equals(contentType))
			return new RelativeKMZInputStream(conn.getInputStream(), WWIO.makeURI(docSource), href, parent);
		else
			return new RelativeKMLInputStream(conn.getInputStream(), WWIO.makeURI(docSource), href, parent);
	}
}
