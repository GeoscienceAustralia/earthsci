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
import gov.nasa.worldwind.ogc.kml.io.KMZFile;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The {@link RelativeKMZFile} class is a subclass of {@link KMZFile} that
 * supports better resolving of relative KML references.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RelativeKMZFile extends KMZFile implements RelativeKMLDoc
{
	private final File file;
	private final String href;
	private final KMLDoc parent;

	public RelativeKMZFile(File file, String href, KMLDoc parent) throws IOException
	{
		super(file);
		this.file = file;
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

		InputStream inputStream = super.getSupportFileStream(path);
		if (inputStream != null)
		{
			return inputStream;
		}

		File parentFile = file.getParentFile();
		if (parentFile != null)
		{
			File childFile = new File(parentFile, path);
			if (childFile.exists())
			{
				return new FileInputStream(childFile);
			}
		}

		//converting the path to a URL will only work if the path has a protocol
		URL url = WWIO.makeURL(path);
		if (url != null)
			return url.openStream();

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

		String superPath = super.getSupportFilePath(path);
		if (superPath != null)
		{
			return superPath;
		}

		File parentFile = file.getParentFile();
		if (parentFile != null)
		{
			File childFile = new File(parentFile, path);
			if (childFile.exists())
			{
				return childFile.getAbsolutePath();
			}
		}

		//converting the path to a URL will only work if the path has a protocol
		URL url = WWIO.makeURL(path);
		if (url != null)
			return url.toExternalForm();

		return null;
	}
}
