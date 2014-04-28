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
package au.gov.ga.earthsci.worldwind.common.util;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * {@link AVKey} extension with extra keys used for this project.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AVKeyMore extends AVKey
{
	final static String CONTEXT_URL = "au.gov.ga.worldwind.AVKeyMore.ContextURL";
	final static String LEGEND_URL = "au.gov.ga.worldwind.AVKeyMore.LegendURL";
	final static String DELEGATE_KIT = "au.gov.ga.worldwind.AVKeyMore.DelegateKit";
	final static String DOWNLOADER_CONNECT_TIMEOUT = "au.gov.ga.worldwind.AVKeyMore.DownloaderConnectTimeout";
	final static String DOWNLOADER_READ_TIMEOUT = "au.gov.ga.worldwind.AVKeyMore.DownloaderReadTimeout";
	final static String EXPIRY_TIMESPAN = "au.gov.ga.worldwind.AVKeyMore.ExpiryTimespan";
	final static String EXPIRY_START_TIME = "au.gov.ga.worldwind.AVKeyMore.ExpiryStartTime";
	final static String MINIMUM_DISTANCE = "au.gov.ga.worldwind.AVKeyMore.MinimumDistance";
	final static String LINE_WIDTH = "au.gov.ga.worldwind.AVKeyMore.LineWidth";
	final static String POINT_SIZE = "au.gov.ga.worldwind.AVKeyMore.PointSize";
	final static String POINT_MIN_SIZE = "au.gov.ga.worldwind.AVKeyMore.PointMinSize";
	final static String POINT_MAX_SIZE = "au.gov.ga.worldwind.AVKeyMore.PointMaxSize";
	final static String POINT_SPRITE = "au.gov.ga.worldwind.AVKeyMore.PointSprite";
	final static String POINT_CONSTANT_ATTENUATION = "au.gov.ga.worldwind.AVKeyMore.PointConstantAttenuation";
	final static String POINT_LINEAR_ATTENUATION = "au.gov.ga.worldwind.AVKeyMore.PointLinearAttenuation";
	final static String POINT_QUADRATIC_ATTENUATION = "au.gov.ga.worldwind.AVKeyMore.PointQuadraticAttenuation";
	final static String COLOR_MAP = "au.gov.ga.worldwind.AVKeyMore.ColorMap";
	final static String REVERSE_NORMALS = "au.gov.ga.worldwind.AVKeyMore.ReverseNormals";
	final static String PAINTED_VARIABLE = "au.gov.ga.worldwind.AVKeyMore.PaintedVariable";
	final static String ORDERED_RENDERING = "au.gov.ga.worldwind.AVKeyMore.OrderedRendering";
	final static String DELEGATE_VIEW_DELEGATE_CLASS_NAME = "au.gov.ga.worldwind.AVKeyMore.DelegateViewDelegateClassName";

	//curtain layer
	final static String FULL_WIDTH = "au.gov.ga.worldwind.AVKeyMore.FullWidth";
	final static String FULL_HEIGHT = "au.gov.ga.worldwind.AVKeyMore.FullHeight";
	final static String LEVEL_WIDTH = "au.gov.ga.worldwind.AVKeyMore.LevelWidth";
	final static String LEVEL_HEIGHT = "au.gov.ga.worldwind.AVKeyMore.LevelHeight";
	final static String POSITIONS = "au.gov.ga.worldwind.AVKeyMore.Positions";
	final static String CURTAIN_TOP = "au.gov.ga.worldwind.AVKeyMore.CurtainTop";
	final static String CURTAIN_BOTTOM = "au.gov.ga.worldwind.AVKeyMore.CurtainBottom";
	final static String FOLLOW_TERRAIN = "au.gov.ga.worldwind.AVKeyMore.FollowTerrain";
	final static String SUBSEGMENTS = "au.gov.ga.worldwind.AVKeyMore.Subsegments";
	final static String PATH = "au.gov.ga.worldwind.AVKeyMore.Path";

	//data layers
	final static String DATA_LAYER_PROVIDER = "au.gov.ga.worldwind.AVKeyMore.DataLayerProvider";
	final static String DATA_LAYER_STYLES = "au.gov.ga.worldwind.AVKeyMore.DataLayerStyles";
	final static String DATA_LAYER_ATTRIBUTES = "au.gov.ga.worldwind.AVKeyMore.DataLayerAttributes";

	//geometry layer
	final static String SHAPE_TYPE = "au.gov.ga.worldwind.AVKeyMore.ShapeType";

	//borehole layer
	final static String BOREHOLE_SAMPLE_STYLES = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleStyles";
	final static String BOREHOLE_SAMPLE_ATTRIBUTES = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleAttributes";
	final static String BOREHOLE_UNIQUE_IDENTIFIER_ATTRIBUTE = "au.gov.ga.worldwind.AVKeyMore.BoreholeUniqueIdentifierAttribute";
	final static String BOREHOLE_SAMPLE_DEPTH_FROM_ATTRIBUTE = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleDepthFromAttribute";
	final static String BOREHOLE_SAMPLE_DEPTH_TO_ATTRIBUTE = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleDepthToAttribute";
	final static String BOREHOLE_SAMPLE_DEPTH_ATTRIBUTES_POSITIVE = "au.gov.ga.worldwind.AVKeyMore.BoreholeSampleDepthAttributesPositive";

	//historic earthquakes layer
	final static String COLORING = "au.gov.ga.worldwind.AVKeyMore.Coloring";
	final static String COLORING_MIN_DATE = "au.gov.ga.worldwind.AVKeyMore.ColoringMinDate";
	final static String COLORING_MAX_DATE = "au.gov.ga.worldwind.AVKeyMore.ColoringMaxDate";

	//crust layer
	final static String SCALE = "au.gov.ga.worldwind.AVKeyMore.Scale";
	final static String WRAP = "au.gov.ga.worldwind.AVKeyMore.Wrap";

	//elevation model
	final static String EXTRACT_ZIP_ENTRY = "au.gov.ga.worldwind.AVKeyMore.ExtractZipEntry";

	//voxet model
	final static String BILINEAR_MINIFICATION = "au.gov.ga.worldwind.AVKeyMore.BilinearMinification";
	final static String SUBSAMPLING_U = "au.gov.ga.worldwind.AVKeyMore.SubsamplingU";
	final static String SUBSAMPLING_V = "au.gov.ga.worldwind.AVKeyMore.SubsamplingV";
	final static String SUBSAMPLING_W = "au.gov.ga.worldwind.AVKeyMore.SubsamplingW";
	final static String DYNAMIC_SUBSAMPLING = "au.gov.ga.worldwind.AVKeyMore.DynamicSubsampling";
	final static String DYNAMIC_SUBSAMPLING_SAMPLES_PER_AXIS = "au.gov.ga.worldwind.AVKeyMore.DynamicSubsamplingSamplesPerAxis";

	//volume layer
	final static String MAX_VARIANCE = "au.gov.ga.worldwind.AVKeyMore.MaxVariance";
	final static String NO_DATA_COLOR = "au.gov.ga.worldwind.AVKeyMore.NoDataColor";
	final static String INITIAL_OFFSET_MIN_U = "au.gov.ga.worldwind.AVKeyMore.InitialOffsetMinU";
	final static String INITIAL_OFFSET_MAX_U = "au.gov.ga.worldwind.AVKeyMore.InitialOffsetMaxU";
	final static String INITIAL_OFFSET_MIN_V = "au.gov.ga.worldwind.AVKeyMore.InitialOffsetMinV";
	final static String INITIAL_OFFSET_MAX_V = "au.gov.ga.worldwind.AVKeyMore.InitialOffsetMaxV";
	final static String INITIAL_OFFSET_MIN_W = "au.gov.ga.worldwind.AVKeyMore.InitialOffsetMinW";
	final static String INITIAL_OFFSET_MAX_W = "au.gov.ga.worldwind.AVKeyMore.InitialOffsetMaxW";
	final static String FORCE_TWO_SIDED_LIGHTING = "au.gov.ga.worldwind.AVKeyMore.Force2SidedLighting";

	//model layer
	final static String TARGET_BAND = "au.gov.ga.worldwind.AVKeyMore.TargetBand";
	final static String OFFSET = "au.gov.ga.worldwind.AVKeyMore.Offset";
	
	//sphere layer
	final static String SPHERE_RADIUS = "au.gov.worldwind.AVKeyMore.SphereRadius";
	final static String SPHERE_SLICES = "au.gov.worldwind.AVKeyMore.SphereSlices";
	final static String SPHERE_STACKS = "au.gov.worldwind.AVKeyMore.SphereStacks";
}
