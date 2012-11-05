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

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.ogc.kml.io.KMZInputStream;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * The {@link RelativeKMZInputStream} class is a subclass of
 * {@link KMZInputStream} that supports better resolving of relative KML
 * references.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RelativeKMZInputStream extends KMZInputStream implements RelativeKMLDoc
{
	/** The URI of this KMZ document. May be {@code null}. */
	protected URI uri;
	private final String href;
	private final KMLDoc parent;

	public RelativeKMZInputStream(InputStream sourceStream, URI uri, String href, KMLDoc parent) throws IOException
	{
		super(sourceStream);
		this.uri = uri;
		this.href = href;
		this.parent = parent;
	}

	@Override
	public String getHref()
	{
		return href;
	}

	@Override
	public KMLDoc getParent()
	{
		return parent;
	}

	@Override
	public boolean isContainer()
	{
		return true;
	}

	@Override
	public synchronized InputStream getSupportFileStream(String path) throws IOException
	{
		path = RelativizedPath.normalizePath(path);
		RelativizedPath relativized = RelativizedPath.relativizePath(path, this);
		path = relativized.path;
		if (relativized.relativeTo != this)
		{
			return relativized.relativeTo.getSupportFileStream(path);
		}

		//first search for path within KMZ zip file
		InputStream inputStream = super.getSupportFileStream(path);
		if (inputStream != null)
		{
			return inputStream;
		}

		//the following is copied from KMLInputStream:
		String ref = this.getSupportFilePath(path);
		if (ref != null)
		{
			URL url = WWIO.makeURL(ref);
			if (url != null)
				return url.openStream();
		}
		return null;
	}

	@Override
	public synchronized String getSupportFilePath(String path) throws IOException
	{
		path = RelativizedPath.normalizePath(path);
		RelativizedPath relativized = RelativizedPath.relativizePath(path, this);
		path = relativized.path;
		if (relativized.relativeTo != this)
		{
			return relativized.relativeTo.getSupportFilePath(path);
		}

		//first search for path within KMZ zip file
		String superPath = super.getSupportFilePath(path);
		if (superPath != null)
		{
			return superPath;
		}

		//the following is copied from KMLInputStream:
		if (this.uri != null)
		{
			URI remoteFileURI = uri.resolve(path);
			if (isNotFileOrExistingFile(remoteFileURI))
			{
				return remoteFileURI.toString();
			}
		}
		
		//converting the path to a URL will only work if the path has a protocol
		URL url = WWIO.makeURL(path);
		if (url != null)
			return url.toExternalForm();
		
		return null;
	}

	protected static boolean isNotFileOrExistingFile(URI uri)
	{
		if (!uri.isAbsolute())
			return false;
		return !uri.getScheme().toLowerCase().equals("file") || new File(uri).exists();
	}
}
