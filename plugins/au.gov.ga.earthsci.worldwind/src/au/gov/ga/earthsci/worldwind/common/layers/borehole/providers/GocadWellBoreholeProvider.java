/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.borehole.providers;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.Borehole;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeImpl;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeLayer;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeMarkerImpl;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeProvider;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeSampleImpl;
import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;

/**
 * {@link BoreholeProvider} that creates {@link Borehole}s from a GOCAD wells
 * file.
 *
 * @author Michael de Hoog
 */
public class GocadWellBoreholeProvider extends AbstractDataProvider<BoreholeLayer> implements BoreholeProvider
{
	public final static Pattern headerPattern = Pattern.compile("GOCAD\\s+Well\\s+.*");
	private final static Pattern namePattern = Pattern.compile("name:(.*)\\s*");
	private final static Pattern endPattern = Pattern.compile("END\\s*");

	private final static Pattern wrefPattern = Pattern
			.compile("WREF\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern vrtxPattern = Pattern
			.compile("VRTX\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern stationPattern = Pattern
			.compile("STATION\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern pathPattern = Pattern
			.compile("PATH\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern tvssPathPattern = Pattern
			.compile("TVSS_PATH\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern tvdPathPattern = Pattern
			.compile("TVD_PATH\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");

	private final static Pattern zonePattern = Pattern
			.compile("ZONE\\s+([^\\s]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([^\\s]+)\\s*");

	private final static Pattern mrkrPattern = Pattern.compile("MRKR\\s+([^\\s]+)\\s+([^\\s]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern dipPattern = Pattern.compile("DIP\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern dipDegPattern = Pattern.compile("DIPDEG\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");
	private final static Pattern normPattern = Pattern
			.compile("NORM\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s*");

	private Bounds bounds;
	private List<BoreholeData> data = new ArrayList<BoreholeData>();
	private BoreholeData currentData;

	@Override
	public Bounds getBounds()
	{
		return bounds;
	}

	@Override
	public boolean isFollowTerrain()
	{
		return false;
	}

	@Override
	protected boolean doLoadData(URL url, BoreholeLayer layer)
	{
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				parseLine(line, layer);
			}
			generateBoreholes(data, layer);
			layer.loadComplete();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		return true;
	}

	protected void parseLine(String line, BoreholeLayer layer)
	{
		Matcher matcher = headerPattern.matcher(line);
		if (matcher.matches())
		{
			currentData = new BoreholeData();
			data.add(currentData);
			return;
		}

		//from now on, must be in a well
		if (currentData == null)
		{
			return;
		}

		matcher = endPattern.matcher(line);
		if (matcher.matches())
		{
			currentData = null;
			return;
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			currentData.name = matcher.group(1);
			return;
		}

		matcher = wrefPattern.matcher(line);
		if (matcher.matches())
		{
			double x = Double.valueOf(matcher.group(1));
			double y = Double.valueOf(matcher.group(2));
			double z = Double.valueOf(matcher.group(3));
			currentData.wref = new Vec4(x, y, z);
			return;
		}

		matcher = vrtxPattern.matcher(line);
		if (matcher.matches())
		{
			double x = Double.valueOf(matcher.group(1));
			double y = Double.valueOf(matcher.group(2));
			double z = Double.valueOf(matcher.group(3));
			Vec4 vrtx = new Vec4(x, y, z);
			BoreholeDataPathItem lastItem =
					!currentData.path.isEmpty() ? currentData.path.get(currentData.path.size() - 1) : null;
			double lastDepth = lastItem != null ? lastItem.depth : 0;
			Vec4 lastVrtx = lastItem != null ? lastItem.vertex : currentData.wref;
			double depth = lastDepth + lastVrtx.distanceTo3(vrtx);
			currentData.path.add(new BoreholeDataPathItem(depth, vrtx));
			return;
		}

		matcher = stationPattern.matcher(line);
		if (matcher.matches())
		{
			double depth = Double.valueOf(matcher.group(1));
			Angle pitch = Angle.fromDegrees(Double.valueOf(matcher.group(2)));
			Angle heading = Angle.fromDegrees(Double.valueOf(matcher.group(3)));
			BoreholeDataPathItem lastItem =
					!currentData.path.isEmpty() ? currentData.path.get(currentData.path.size() - 1) : null;
			double lastDepth = lastItem != null ? lastItem.depth : 0;
			Vec4 lastVrtx = lastItem != null ? lastItem.vertex : currentData.wref;
			Angle currentPitch =
					currentData.lastStationPitch != null ? Angle.mix(0.5, currentData.lastStationPitch, pitch) : pitch;
			Angle currentHeading =
					currentData.lastStationHeading != null ? Angle.mix(0.5, currentData.lastStationHeading, heading)
							: heading;
			Vec4 vrtx = new Vec4(0, 0, lastDepth - depth);
			Quaternion q = Quaternion.fromRotationXYZ(currentPitch, Angle.ZERO, currentHeading.multiply(-1));
			vrtx = vrtx.transformBy3(q);
			vrtx = lastVrtx.add3(vrtx);
			currentData.path.add(new BoreholeDataPathItem(depth, vrtx));
			currentData.lastStationPitch = pitch;
			currentData.lastStationHeading = heading;
		}

		matcher = pathPattern.matcher(line);
		if (matcher.matches())
		{
			double depth = Double.valueOf(matcher.group(1));
			double z = Double.valueOf(matcher.group(2));
			double x = Double.valueOf(matcher.group(3));
			double y = Double.valueOf(matcher.group(4));
			Vec4 path = new Vec4(x + currentData.wref.x, y + currentData.wref.y, z);
			currentData.path.add(new BoreholeDataPathItem(depth, path));
			return;
		}

		matcher = tvssPathPattern.matcher(line);
		if (matcher.matches())
		{
			double depth = Double.valueOf(matcher.group(1));
			double z = Double.valueOf(matcher.group(2));
			double x = Double.valueOf(matcher.group(3));
			double y = Double.valueOf(matcher.group(4));
			Vec4 path = new Vec4(x + currentData.wref.x, y + currentData.wref.y, z);
			currentData.path.add(new BoreholeDataPathItem(depth, path));
			return;
		}

		matcher = tvdPathPattern.matcher(line);
		if (matcher.matches())
		{
			double depth = Double.valueOf(matcher.group(1));
			double z = Double.valueOf(matcher.group(2));
			double x = Double.valueOf(matcher.group(3));
			double y = Double.valueOf(matcher.group(4));
			Vec4 path = new Vec4(x + currentData.wref.x, y + currentData.wref.y, z - currentData.wref.z);
			currentData.path.add(new BoreholeDataPathItem(depth, path));
			return;
		}

		matcher = zonePattern.matcher(line);
		if (matcher.matches())
		{
			String name = matcher.group(1);
			double depth1 = Double.valueOf(matcher.group(2));
			double depth2 = Double.valueOf(matcher.group(3));
			currentData.zones.add(new BoreholeDataZone(name, depth1, depth2));
			return;
		}

		matcher = mrkrPattern.matcher(line);
		if (matcher.matches())
		{
			String name = matcher.group(1);
			double depth = Double.valueOf(matcher.group(3));
			currentData.markers.add(new BoreholeDataMarker(name, depth));
			return;
		}

		matcher = normPattern.matcher(line);
		if (matcher.matches())
		{
			if (currentData.markers.isEmpty())
			{
				return;
			}
			BoreholeDataMarker marker = currentData.markers.get(currentData.markers.size() - 1);
			double x = Double.valueOf(matcher.group(1));
			double y = Double.valueOf(matcher.group(2));
			double z = Double.valueOf(matcher.group(3));
			Vec4 norm = new Vec4(x, y, z).normalize3();
			marker.azimuth = Angle.fromRadians(Math.atan2(norm.x, norm.y)); //reversed x,y
			marker.dip = Angle.fromRadians(Math.atan2(Math.sqrt(norm.x * norm.x + norm.y * norm.y), norm.z));
			return;
		}

		matcher = dipPattern.matcher(line);
		if (matcher.matches())
		{
			if (currentData.markers.isEmpty())
			{
				return;
			}
			BoreholeDataMarker marker = currentData.markers.get(currentData.markers.size() - 1);
			//in gradians, convert to degrees
			marker.azimuth = Angle.fromDegrees(Double.valueOf(matcher.group(1)) * 90d / 100d);
			marker.dip = Angle.fromDegrees(Double.valueOf(matcher.group(2)) * 90d / 100d);
			return;
		}

		matcher = dipDegPattern.matcher(line);
		if (matcher.matches())
		{
			if (currentData.markers.isEmpty())
			{
				return;
			}
			BoreholeDataMarker marker = currentData.markers.get(currentData.markers.size() - 1);
			marker.azimuth = Angle.fromDegrees(Double.valueOf(matcher.group(1)));
			marker.dip = Angle.fromDegrees(Double.valueOf(matcher.group(2)));
			return;
		}
	}

	protected void generateBoreholes(List<BoreholeData> dataList, BoreholeLayer layer)
	{
		CoordinateTransformation transformation = layer.getCoordinateTransformation();
		ColorMap colorMap = layer.getColorMap();

		//use same color for same property names (zone/marker) in each borehole
		Map<String, Color> colors = new HashMap<String, Color>();
		for (BoreholeData data : dataList)
		{
			for (BoreholeDataZone zone : data.zones)
			{
				colors.put(zone.name, null);
			}
			for (BoreholeDataMarker m : data.markers)
			{
				colors.put(m.name, null);
			}
		}
		int i = 0;
		int colorCount = Math.max(1, colors.size() - (colorMap != null ? 1 : 0));
		for (Entry<String, Color> entry : colors.entrySet())
		{
			float hue = i++ / (float) colorCount;
			entry.setValue(colorMap != null ? colorMap.calculateColor(hue) : generateColor(hue, entry.getKey()));
		}

		for (BoreholeData data : dataList)
		{
			Position position = vertexToPosition(data.wref, transformation);
			bounds = Bounds.union(bounds, position);

			MarkerAttributes markerAttributes = new BasicMarkerAttributes();
			markerAttributes.setShapeType(BasicMarkerShape.CONE);
			markerAttributes.setMarkerPixels(4);
			BoreholeImpl borehole = new BoreholeImpl(position, markerAttributes);
			borehole.setTooltipText(data.name);
			layer.addBorehole(borehole);

			for (BoreholeDataPathItem item : data.path)
			{
				Position pathPosition = vertexToPosition(item.vertex, transformation);
				borehole.addPath(item.depth, pathPosition);
			}

			for (BoreholeDataZone zone : data.zones)
			{
				BoreholeSampleImpl sample = new BoreholeSampleImpl(borehole);
				sample.setText(zone.name);
				sample.setColor(colors.get(zone.name));
				sample.setDepthFrom(zone.depth1);
				sample.setDepthTo(zone.depth2);
				borehole.addSample(sample);
			}

			for (BoreholeDataMarker m : data.markers)
			{
				Position markerPosition = borehole.getPath().getPosition(m.depth);
				BoreholeMarkerImpl marker = new BoreholeMarkerImpl(borehole, markerPosition);
				marker.setDepth(m.depth);
				marker.setTooltipText(m.name);
				marker.setColor(colors.get(m.name));
				marker.setAzimuth(m.azimuth);
				marker.setDip(m.dip);
				borehole.addMarker(marker);
			}
		}
	}

	protected Position vertexToPosition(Vec4 vertex, CoordinateTransformation transformation)
	{
		if (transformation != null)
		{
			double[] transformed = new double[3];
			transformation.TransformPoint(transformed, vertex.x, vertex.y, vertex.z);
			return Position.fromDegrees(transformed[1], transformed[0], transformed[2]);
		}
		return Position.fromDegrees(vertex.y, vertex.x, vertex.z);
	}

	protected Color generateColor(float hue, String s)
	{
		int hash = s.hashCode();
		Random random = new Random(hash);
		int fourteenbit = 0x4000;
		int number = random.nextInt(fourteenbit);
		int sat = 127 + (number & 0x7f); //7 bits
		int val = 127 + ((number >> 7) & 0x7f); //7 bits
		return new Color(Color.HSBtoRGB(hue, sat / 255f, val / 255f));
	}

	private static class BoreholeData
	{
		public Vec4 wref;
		public String name;
		public final List<BoreholeDataPathItem> path = new ArrayList<BoreholeDataPathItem>();
		public final List<BoreholeDataMarker> markers = new ArrayList<BoreholeDataMarker>();
		public final List<BoreholeDataZone> zones = new ArrayList<BoreholeDataZone>();
		public Angle lastStationPitch;
		public Angle lastStationHeading;
	}

	private static class BoreholeDataPathItem
	{
		public final double depth;
		public final Vec4 vertex;

		public BoreholeDataPathItem(double depth, Vec4 vertex)
		{
			this.depth = depth;
			this.vertex = vertex;
		}
	}

	private static class BoreholeDataMarker
	{
		public final String name;
		public final double depth;
		public Angle azimuth;
		public Angle dip;

		public BoreholeDataMarker(String name, double depth)
		{
			this.name = name;
			this.depth = depth;
		}
	}

	private static class BoreholeDataZone
	{
		public final String name;
		public final double depth1;
		public final double depth2;

		public BoreholeDataZone(String name, double depth1, double depth2)
		{
			this.name = name;
			this.depth1 = depth1;
			this.depth2 = depth2;
		}
	}
}
