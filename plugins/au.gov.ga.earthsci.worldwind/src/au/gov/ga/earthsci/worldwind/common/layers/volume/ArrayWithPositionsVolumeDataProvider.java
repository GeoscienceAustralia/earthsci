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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * {@link VolumeDataProvider} which reads its data from a custom object array
 * file, which includes the latitude/longitude of each position in the data.
 * <p>
 * Any {@link AbstractVolumeDataProvider} instance can be converted to a file
 * which this class supports, using the
 * {@link ArrayWithPositionsVolumeDataProvider#saveVolumeDataProviderToArrayFile(AbstractVolumeDataProvider, File)}
 * function.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ArrayWithPositionsVolumeDataProvider extends AbstractVolumeDataProvider
{
	@Override
	protected boolean doLoadData(URL url, VolumeLayer layer)
	{
		try
		{
			InputStream is = null;
			if (url.toString().toLowerCase().endsWith(".zip"))
			{
				ZipInputStream zis = new ZipInputStream(url.openStream());
				zis.getNextEntry();
				is = zis;
			}
			else
			{
				is = url.openStream();
			}

			ObjectInputStream ois = new ObjectInputStream(is);
			try
			{
				xSize = ois.readInt();
				ySize = ois.readInt();
				zSize = ois.readInt();
				double minLatitude = ois.readDouble();
				double maxLatitude = ois.readDouble();
				double minLongitude = ois.readDouble();
				double maxLongitude = ois.readDouble();
				sector = Sector.fromDegrees(minLatitude, maxLatitude, minLongitude, maxLongitude);
				top = ois.readDouble();
				depth = ois.readDouble();
				noDataValue = ois.readFloat();
				minValue = Float.MAX_VALUE;
				maxValue = -Float.MAX_VALUE;

				positions = new ArrayList<Position>(xSize * ySize);
				for (int i = 0; i < xSize * ySize; i++)
				{
					positions.add(Position.fromDegrees(ois.readDouble(), ois.readDouble(), ois.readDouble()));
				}

				data = FloatBuffer.allocate(xSize * ySize * zSize);
				for (int i = 0; i < data.limit(); i++)
				{
					float value = ois.readFloat();
					data.put(value);
					minValue = Math.min(minValue, value);
					maxValue = Math.max(maxValue, value);
				}
				data.rewind();
			}
			finally
			{
				ois.close();
			}

			layer.dataAvailable(this);
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Save the given {@link AbstractVolumeDataProvider} to a File which this
	 * class supports reading. If the file extension is .zip, a zip file is
	 * written.
	 * 
	 * @param provider
	 *            {@link AbstractVolumeDataProvider} to write to a file.
	 * @param file
	 *            {@link File} to write to.
	 */
	public static void saveVolumeDataProviderToArrayFile(AbstractVolumeDataProvider provider, File file)
	{
		try
		{
			OutputStream os;
			if (file.getName().toLowerCase().endsWith(".zip"))
			{
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
				zos.putNextEntry(new ZipEntry("data"));
				os = zos;
			}
			else
			{
				os = new FileOutputStream(file);
			}
			ObjectOutputStream oos = new ObjectOutputStream(os);
			try
			{
				oos.writeInt(provider.xSize);
				oos.writeInt(provider.ySize);
				oos.writeInt(provider.zSize);
				oos.writeDouble(provider.sector.getMinLatitude().degrees);
				oos.writeDouble(provider.sector.getMaxLatitude().degrees);
				oos.writeDouble(provider.sector.getMinLongitude().degrees);
				oos.writeDouble(provider.sector.getMaxLongitude().degrees);
				oos.writeDouble(provider.top);
				oos.writeDouble(provider.depth);
				oos.writeFloat(provider.noDataValue);
				for (Position position : provider.positions)
				{
					oos.writeDouble(position.latitude.degrees);
					oos.writeDouble(position.longitude.degrees);
					oos.writeDouble(position.elevation);
				}
				for (int i = 0; i < provider.data.limit(); i++)
				{
					oos.writeFloat(provider.data.get(i));
				}
			}
			finally
			{
				oos.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
