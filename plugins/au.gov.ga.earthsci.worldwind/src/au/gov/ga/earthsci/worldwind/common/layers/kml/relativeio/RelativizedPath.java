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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;

/**
 * This class is a wrapper around a {@link String} and {@link KMLDoc} object. It
 * allows one to store a relative path, and the {@link KMLDoc} it is relative
 * to.
 * 
 * It also contains helper functions for normalizing and relativizing paths.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RelativizedPath
{
	public final String path;
	public final KMLDoc relativeTo;

	public RelativizedPath(String path, KMLDoc relativeTo)
	{
		this.path = path;
		this.relativeTo = relativeTo;
	}

	/**
	 * Relativize {@code path} from the {@code document} point in the KML tree.
	 * 
	 * @param path
	 *            Path to relativize.
	 * @param document
	 *            Point in the KML tree to relative with respect to.
	 * @return Relativized path, and the KMLDoc that the relativized path is
	 *         relativized to (usually the {@code document} passed).
	 */
	public static RelativizedPath relativizePath(String path, RelativeKMLDoc document)
	{
		/*
		 * Example:
		 * - dir/sydney.kmz contains model/model.kml
		 * - model/model.kml points to ../icon/pin.png
		 * = should resolve to dir/sydney.kmz/icon/pin.png (dir/sydney.kmz/model/../icon/pin.png)
		 * = so sydney.kmz doc should return its icon/pin.png
		 * 
		 * Example 2:
		 * - dir/sydney.kmz contains model/model.kml
		 * - model/model.kml points to ../../icon/pin.png
		 * = should resolve to dir/icon/pin.png (dir/sydney.kmz/model/../../icon/pin.png)
		 * = so sydney.kmz doc should return the dir/icon/pin.png path
		 * 
		 * Example 3:
		 * - dir1/dir2/sydney.kmz contains dir3/dir4/melbourne.kmz
		 * - dir3/dir4/melbourne.kmz contains model/model.kml
		 * - model/model.kml points to ../icon/pin.png
		 * = should resolve to dir1/dir2/sydney.kmz/dir3/dir4/melbourne.kmz/icon/pin.png (dir1/dir2/sydney.kmz/dir3/dir4/melbourne.kmz/model/../icon/pin.png)
		 * = so melbourne.kmz doc should return its icon/pin.png
		 * 
		 * Example 4:
		 * - dir1/dir2/sydney.kmz contains dir3/dir4/melbourne.kmz
		 * - dir3/dir4/melbourne.kmz contains model/model.kml
		 * - model/model.kml points to ../../../../icon/pin.png
		 * = should resolve to dir1/dir2/sydney.kmz/icon/pin.png (dir1/dir2/sydney.kmz/dir3/dir4/melbourne.kmz/model/../../../../icon/pin.png)
		 * = so sydney.kmz doc should return its icon/pin.png
		 * 
		 * Example 5:
		 * - dir1/dir2/sydney.kmz contains dir3/dir4/melbourne.kmz
		 * - dir3/dir4/melbourne.kmz contains model/model.kml
		 * - model/model.kml points to ../../../../../../icon/pin.png
		 * = should resolve to dir1/icon/pin.png (dir1/dir2/sydney.kmz/dir3/dir4/melbourne.kmz/model/../../../../../../icon/pin.png)
		 * = so sydney.kmz doc should return the dir1/icon/pin.png path
		 */

		path = normalizePath(path);
		String href = document.getHref();
		KMLDoc parent = document.getParent();

		boolean complete = document.isContainer() && (path.length() < 3 || !path.substring(3).contains(".."));
		if (complete || parent == null || !(parent instanceof RelativeKMLDoc) || href == null)
		{
			//this is as relative as we can go!

			//If the document is a container (KMZ), a ../ at the start of a path refers to the
			//KMZ as a directory, so remove it and carry on
			
			//Unfortunately, Google Earth supports treating both the KMZ file as the base directory and treating
			//the KMZ's parent directory as the base directory (it first checks the first, and if it results in
			//a HTTP error, checks the second). This means there are a lot of KMZ files that don't follow spec;
			//we should probably add support for this at some stage.
			
			if (document.isContainer() && path.startsWith("../"))
			{
				path = path.substring(3);
			}
			return new RelativizedPath(path, document);
		}

		RelativeKMLDoc relativeParent = (RelativeKMLDoc) parent;
		if (document.isContainer())
		{
			//don't remove the filename from the end of href, as KMZ files (containers) should be treated as a directory
			path = normalizePath(href + "/" + path);
			return relativizePath(path, relativeParent); //recurse
		}

		href = normalizePath(href);
		int indexOfLastSlash = href.lastIndexOf('/');
		String parentHref = indexOfLastSlash >= 0 ? href.substring(0, indexOfLastSlash + 1) : "/";
		path = normalizePath(parentHref + path);
		return relativizePath(path, relativeParent); //recurse
	}

	/**
	 * Normalize a path. This:
	 * <ul>
	 * <li>replaces back slashes '\' with forward slashes '/'</li>
	 * <li>removed repeating slashes '//' (except after a colon ':')</li>
	 * <li>normalizes the path (dir1/dir2/../dir3 becomes dir1/dir3)</li>
	 * </ul>
	 * 
	 * @param path
	 * @return
	 */
	public static String normalizePath(String path)
	{
		if (path == null)
		{
			return null;
		}

		//remove any back slashes and double slashes (but not slashes after a ':')
		path = path.replaceAll("\\\\+", "/");
		path = path.replaceAll("(:/+)|(/)/*", "$1$2");

		//remove prefix slash
		if (path.startsWith("/"))
		{
			path = path.substring(1);
		}

		//fix relative paths that point to a parent directory (with a ".." in the path)
		//this acts like unix pathname normalization: dir1/dir2/../dir3 becomes dir1/dir3
		if (path.contains(".."))
		{
			List<String> parts = new ArrayList<String>(Arrays.asList(path.split("/", -1)));
			for (int i = 0; i < parts.size(); i++)
			{
				if (parts.get(i).equals(".."))
				{
					if (i > 0 && !parts.get(i - 1).equals(".."))
					{
						parts.remove(i - 1);
						parts.remove(i - 1);
						i -= 2;
					}
				}
			}
			if (!parts.isEmpty())
			{
				StringBuilder sb = new StringBuilder();
				for (String s : parts)
				{
					sb.append("/" + s);
				}
				path = sb.substring(1);
			}
		}
		return path;
	}
}
