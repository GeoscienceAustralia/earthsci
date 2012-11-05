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
package au.gov.ga.earthsci.worldwind.common.layers.earthquakes;

import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.geom.LatLon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Calendar;

/**
 * Helper class used for converting the large historic earthquakes shapefile
 * into a smaller double array, which is used by the
 * {@link HistoricEarthquakesLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HistoricEarthquakesShapefileConverter
{
	public static void main(String[] args) throws IOException
	{
		File file = new File("D:/Earthquakes/quakes.shp");
		File output = new File("D:/Earthquakes/quakes.dat");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(output));
		Shapefile shapefile = new Shapefile(file);

		while (shapefile.hasNext())
		{
			ShapefileRecord record = shapefile.nextRecord();
			DBaseRecord attributes = record.getAttributes();
			LatLon latlon = record.getPointBuffer(0).getLocation(0);

			//date string is in (-)ymmdd format, where y can be 1 or more characters in length
			String dateString = attributes.getValue("DATE_").toString();
			int day = Integer.valueOf(dateString.substring(dateString.length() - 2, dateString.length()));
			int month = Integer.valueOf(dateString.substring(dateString.length() - 4, dateString.length() - 2));
			int year = Integer.valueOf(dateString.substring(0, dateString.length() - 4));
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day);
			long timeInMillis = calendar.getTimeInMillis();

			double magnitude = getDoubleFromObject(attributes.getValue("MAG"), 0);
			double elevation = getDoubleFromObject(attributes.getValue("DEPTH"), 0) * -1000;

			//although we don't particularly need double/long accuracy, once the binary file
			//is zipped there is not much difference in size between the two (around 15%)
			oos.writeDouble(latlon.getLatitude().degrees);
			oos.writeDouble(latlon.getLongitude().degrees);
			oos.writeDouble(elevation);
			oos.writeDouble(magnitude);
			oos.writeLong(timeInMillis);
		}

		oos.close();
	}

	protected static double getDoubleFromObject(Object object, double defalt)
	{
		try
		{
			if (Double.class.isAssignableFrom(object.getClass()))
			{
				return ((Double) object).doubleValue();
			}
			else
			{
				return Double.parseDouble(object.toString());
			}
		}
		catch (Exception e)
		{
			return defalt;
		}
	}
}
