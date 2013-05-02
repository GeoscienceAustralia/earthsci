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
package au.gov.ga.earthsci.common.spatial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gdal.GDALDataSetup;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that gives access to the list of available known spatial reference
 * systems (SRS).
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SpatialReferences
{

	private static Logger logger = LoggerFactory.getLogger(SpatialReferences.class);

	/**
	 * Provides summary information for a spatial reference system (SRS).
	 */
	public static final class SpatialReferenceSummary implements Comparable<SpatialReferenceSummary>
	{
		public static final SpatialReferenceSummary WGS84 = new SpatialReferenceSummary("EPSG:4326", "WGS 84"); //$NON-NLS-1$//$NON-NLS-2$

		private final String epsg;
		private final String name;

		public SpatialReferenceSummary(String epsg, String name)
		{
			super();
			this.epsg = epsg;
			this.name = name;
		}

		public String getEpsg()
		{
			return epsg;
		}

		public String getName()
		{
			return name;
		}

		/**
		 * Create a new {@link SpatialReference} instance from this summary
		 * object
		 * 
		 * @return The new spatial reference instance or <code>null</code> if
		 *         one could not be created.
		 */
		public SpatialReference createReference()
		{
			SpatialReference result = new SpatialReference();
			int code = result.SetFromUserInput(epsg);
			if (code != ogrConstants.OGRERR_NONE)
			{
				return null;
			}
			return result;
		}

		@Override
		public int compareTo(SpatialReferenceSummary o)
		{
			if (epsg.length() == o.epsg.length())
			{
				return epsg.compareTo(o.epsg);
			}
			return epsg.length() - o.epsg.length();
		}

		@Override
		public String toString()
		{
			return epsg + " (" + name + ")"; //$NON-NLS-1$//$NON-NLS-2$
		}
	}

	private static List<SpatialReferenceSummary> references;

	public static List<SpatialReferenceSummary> get()
	{
		if (references == null)
		{
			loadReferences();
		}

		return Collections.unmodifiableList(references);
	}

	private static void loadReferences()
	{
		try
		{
			references = new ArrayList<SpatialReferenceSummary>();
			loadFrom(GDALDataSetup.getDataFile("gcs.csv")); //$NON-NLS-1$
			loadFrom(GDALDataSetup.getDataFile("pcs.csv")); //$NON-NLS-1$
			Collections.sort(references);
		}
		catch (Exception e)
		{
			logger.error("Unable to read spatial references", e); //$NON-NLS-1$
		}

	}

	private static void loadFrom(File file) throws Exception
	{
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));

			// Consume the first header line
			reader.readLine();

			String line = null;
			while ((line = reader.readLine()) != null)
			{
				String[] vals = line.split(","); //$NON-NLS-1$
				references.add(new SpatialReferenceSummary("EPSG:" + vals[0], vals[1].replace("\"", ""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
		}
	}
}
