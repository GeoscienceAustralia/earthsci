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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IImageTransformerDelegate;

/**
 * Applies a filter to the retrieved image tiles to remove striping noise using a 
 * combination of lowpass and highpass filtering.
 * <p/>
 * Based on the technique described at http://isis.astrogeology.usgs.gov/IsisWorkshop/index.php/Removing_Striping_Noise_from_Image_Data
 * <p/>
 * As described in the article, a good rule of thumb for the size of filter to apply is:
 * <ol>
 * 	<li>Make the size of the lowpass filter large enough to encompass the striping (e.g. twice the size of the striping)
 *  <li>Make the size of the highpass filter small enough to fit within the striping (e.g. half the size of the striping)
 * </ol>
 * <p/>
 * <code>&lt;Delegate&gt;StripingFilterTransformer(lowPassCols,lowPassRows,highPassCols,highPassRows)&lt;/Delegate&gt;</code>
 * <ul>
 * 	<li>lowPassCols = the filter size for the low pass filter applied to the columns of the image (integer number of pixels)
 *  <li>lowPassRows = the filter size for the low pass filter applied to the rows of the image (integer number of pixels)
 *  <li>highPassCols = the filter size for the high pass filter applied to the columns of the image (integer number of pixels)
 *  <li>highPassRows = the filter size for the high pass filter applied to the rows of the image (integer number of pixels)
 * </ul>
 * <b>Note:</b> If the lowPass and highPass filters are the same size for columns or rows, no effect will be seen in that direction of the image
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StripingFilterTransformerDelegate implements IImageTransformerDelegate
{
	private final static String DEFINITION_STRING = "StripingFilterTransformer";

	protected final int lowPassCols;
	protected final int lowPassRows;
	protected final int highPassCols;
	protected final int highPassRows;

	@SuppressWarnings("unused")
	private StripingFilterTransformerDelegate()
	{
		this(1, 1, 1, 1);
	}

	public StripingFilterTransformerDelegate(int lowPassCols, int lowPassRows, int highPassCols,
			int highPassRows)
	{
		this.lowPassCols = lowPassCols;
		this.lowPassRows = lowPassRows;
		this.highPassCols = highPassCols;
		this.highPassRows = highPassRows;
	}

	@Override
	public BufferedImage transformImage(BufferedImage image, IDelegatorTile tile)
	{
		return filter(image, lowPassCols, lowPassRows, highPassCols, highPassRows);
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\((\\d+),(\\d+),(\\d+),(\\d+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				int lowPassCols = Integer.parseInt(matcher.group(1));
				int lowPassRows = Integer.parseInt(matcher.group(2));
				int highPassCols = Integer.parseInt(matcher.group(3));
				int highPassRows = Integer.parseInt(matcher.group(4));
				return new StripingFilterTransformerDelegate(lowPassCols, lowPassRows,
						highPassCols, highPassRows);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + lowPassCols + "," + lowPassRows + "," + highPassCols + ","
				+ highPassRows + ")";
	}

	protected static BufferedImage filter(BufferedImage image, int lowPassCols, int lowPassRows,
			int highPassCols, int highPassRows)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		float[][][] array = imageToArray(image);

		float[][][] lowpass = average(array, lowPassCols, lowPassRows);
		float[][][] highpass = average(array, highPassCols, highPassRows);
		subtract(array, highpass, highpass);
		add(lowpass, highpass, array);

		return arrayToImage(array, width, height);
	}

	protected static float[][][] imageToArray(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();

		int bands = 4;
		float[][][] array = new float[width][height][bands];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int argb = image.getRGB(x, y);
				int a = (argb >> 24) & 0xff;
				int r = (argb >> 16) & 0xff;
				int g = (argb >> 8) & 0xff;
				int b = (argb) & 0xff;
				array[x][y][0] = (a / 255f) * 2f - 1f;
				array[x][y][1] = (r / 255f) * 2f - 1f;
				array[x][y][2] = (g / 255f) * 2f - 1f;
				array[x][y][3] = (b / 255f) * 2f - 1f;
			}
		}

		return array;
	}

	protected static BufferedImage arrayToImage(float[][][] array, int width, int height)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int a = (int) ((array[x][y][0] + 1f) / 2f * 255f);
				int r = (int) ((array[x][y][1] + 1f) / 2f * 255f);
				int g = (int) ((array[x][y][2] + 1f) / 2f * 255f);
				int b = (int) ((array[x][y][3] + 1f) / 2f * 255f);

				int argb = (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
				image.setRGB(x, y, argb);
			}
		}

		return image;
	}

	protected static float[][][] average(float[][][] image, int windowWidth, int windowHeight)
	{
		int width = image.length;
		int height = image[0].length;
		int bands = image[0][0].length;
		float[][][] array = new float[width][height][bands];

		float[][] windowHorizontalSum = new float[width][height];
		float[] windowVerticalEdgeSum = new float[height];

		//skip alpha
		for (int b = 1; b < bands; b++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (y == 0)
					{
						float sum = 0;
						for (int wy = 0; wy < windowHeight; wy++)
						{
							int sy = clamp(y + wy - windowHeight / 2, 0, height - 1);
							sum += image[x][sy][b];
						}
						windowHorizontalSum[x][y] = sum;
					}
					else
					{
						int ssy = clamp(y - windowHeight / 2 - 1, 0, height - 1);
						int say = clamp(y + windowHeight / 2, 0, height - 1);
						windowHorizontalSum[x][y] =
								windowHorizontalSum[x][y - 1] - image[x][ssy][b] + image[x][say][b];
					}
				}
			}

			for (int y = 0; y < height; y++)
			{
				float sum = 0;
				for (int wx = 0; wx < windowWidth; wx++)
				{
					int sx = clamp(wx - windowWidth / 2, 0, width - 1);
					sum += image[sx][y][b];
				}
				windowVerticalEdgeSum[y] = sum;
			}

			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (x == 0)
					{
						if (y == 0)
						{
							float sum = 0;
							for (int wx = 0; wx < windowWidth; wx++)
							{
								int sx = clamp(wx - windowWidth / 2, 0, width - 1);
								sum += windowHorizontalSum[sx][y];
							}
							array[x][y][b] = sum;
						}
						else
						{
							int ssy = clamp(y - windowHeight / 2 - 1, 0, height - 1);
							int say = clamp(y + windowHeight / 2, 0, height - 1);
							array[x][y][b] =
									array[x][y - 1][b] - windowVerticalEdgeSum[ssy]
											+ windowVerticalEdgeSum[say];
						}
					}
					else
					{
						int ssx = clamp(x - windowWidth / 2 - 1, 0, width - 1);
						int sax = clamp(x + windowWidth / 2, 0, width - 1);
						array[x][y][b] =
								array[x - 1][y][b] - windowHorizontalSum[ssx][y]
										+ windowHorizontalSum[sax][y];
					}
				}
			}

			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					array[x][y][b] /= windowWidth * windowHeight;
				}
			}
		}

		return array;
	}

	protected static int clamp(int value, int min, int max)
	{
		return value > max ? max : value < min ? min : value;
	}

	protected static float clamp(float value, float min, float max)
	{
		return value > max ? max : value < min ? min : value;
	}

	protected static void subtract(float[][][] image1, float[][][] image2, float[][][] store)
	{
		int width = image1.length;
		int height = image1[0].length;
		int bands = image1[0][0].length;

		//skip alpha
		for (int b = 1; b < bands; b++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					store[x][y][b] = clamp(image1[x][y][b] - image2[x][y][b], -1, 1);
				}
			}
		}
	}

	protected static void add(float[][][] image1, float[][][] image2, float[][][] store)
	{
		int width = image1.length;
		int height = image1[0].length;
		int bands = image1[0][0].length;

		//skip alpha
		for (int b = 1; b < bands; b++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					store[x][y][b] = clamp(image1[x][y][b] + image2[x][y][b], -1, 1);
				}
			}
		}
	}
}
