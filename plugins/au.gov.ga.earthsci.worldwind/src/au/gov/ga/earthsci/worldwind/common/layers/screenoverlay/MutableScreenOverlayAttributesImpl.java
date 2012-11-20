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
package au.gov.ga.earthsci.worldwind.common.layers.screenoverlay;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import au.gov.ga.earthsci.worldwind.common.util.FileUtil;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A default mutable implementation of the {@link ScreenOverlayAttributes} interface.
 * <p/>
 * Provides some sensible defaults that can be used with minimal configuration.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class MutableScreenOverlayAttributesImpl implements ScreenOverlayAttributes
{

	private static final ScreenOverlayPosition DEFAULT_OVERLAY_POSITION = ScreenOverlayPosition.CENTER;
	private static final LengthExpression DEFAULT_LENGTH = new LengthExpression("80%");
	private static final int DEFAULT_BORDER_WIDTH = 2;
	private static final int NO_BORDER_WIDTH = 0;
	private static final Color DEFAULT_BORDER_COLOR = Color.GRAY;
	
	private static final String ID_PREFIX = "ScreenOverlay";
	
	private URL sourceUrl;
	private String sourceHtml;
	private String sourceId;
	
	private ScreenOverlayPosition position = DEFAULT_OVERLAY_POSITION;
	
	private LengthExpression minHeight = DEFAULT_LENGTH;
	private LengthExpression maxHeight = null; // Fixed height
	private LengthExpression minWidth = DEFAULT_LENGTH;
	private LengthExpression maxWidth = null; // Fixed width
	
	private boolean drawBorder = true;
	private Color borderColor = DEFAULT_BORDER_COLOR;
	private int borderWidth = DEFAULT_BORDER_WIDTH;
	
	// Constructor used for testing etc.
	MutableScreenOverlayAttributesImpl(){}
	
	public MutableScreenOverlayAttributesImpl(URL sourceUrl)
	{
		Validate.notNull(sourceUrl, "A source URL is required");
		this.sourceUrl = sourceUrl;
	}
	
	public MutableScreenOverlayAttributesImpl(String sourceHtml)
	{
		Validate.notBlank(sourceHtml, "Source html is required");
		this.sourceHtml = sourceHtml.trim();
	}
	
	@Override
	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	@Override
	public String getSourceHtml()
	{
		return sourceHtml;
	}

	@Override
	public String getSourceId()
	{
		if (sourceId == null)
		{
			CRC32 checksum = new CRC32();
			if (sourceUrl != null)
			{
				checksum.update((sourceUrl.toExternalForm()).getBytes());
			}
			else
			{
				checksum.update(sourceHtml.getBytes());
			}
			sourceId = ID_PREFIX + checksum.getValue();
		}
		return sourceId;
	}
	
	@Override
	public boolean isSourceHtml()
	{
		// Assume content is HTML if not an image...
		return !isSourceImage();
	}
	
	@Override
	public boolean isSourceImage()
	{
		if (sourceUrl == null)
		{
			return false;
		}
		
		String urlSuffix = FileUtil.getExtension(sourceUrl.toExternalForm());
		for (String suffix : ImageIO.getReaderFileSuffixes())
		{
			if (suffix.equalsIgnoreCase(urlSuffix))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ScreenOverlayPosition getPosition()
	{
		return position;
	}

	public void setPosition(ScreenOverlayPosition position)
	{
		this.position = (position == null ? DEFAULT_OVERLAY_POSITION : position);
	}
	
	@Override
	public LengthExpression getMinHeight()
	{
		return minHeight;
	}
	
	public void setMinHeight(String expression)
	{
		setMinHeight(new LengthExpression(expression));
	}
	
	public void setMinHeight(LengthExpression minHeight)
	{
		if (minHeight == null)
		{
			minHeight = maxHeight == null ? DEFAULT_LENGTH : maxHeight;
		}
		
		this.minHeight = minHeight;
	}

	@Override
	public LengthExpression getMaxHeight()
	{
		return maxHeight == null ? minHeight : maxHeight;
	}

	public void setMaxHeight(String expression)
	{
		setMaxHeight(new LengthExpression(expression));
	}
	
	public void setMaxHeight(LengthExpression maxHeight)
	{
		this.maxHeight = maxHeight;
	}
	
	@Override
	public float getHeight(float screenHeight)
	{
		return clampDimension(screenHeight, getMinHeight().getLength(screenHeight), getMaxHeight().getLength(screenHeight));
	}

	@Override
	public LengthExpression getMinWidth()
	{
		return minWidth;
	}
	
	public void setMinWidth(String expression)
	{
		setMinWidth(new LengthExpression(expression));
	}
	
	public void setMinWidth(LengthExpression minWidth)
	{
		this.minWidth = minWidth;
	}

	@Override
	public LengthExpression getMaxWidth()
	{
		return maxWidth == null ? minWidth : maxWidth;
	}
	
	public void setMaxWidth(String expression)
	{
		setMaxWidth(new LengthExpression(expression));
	}
	
	public void setMaxWidth(LengthExpression maxWidth)
	{
		this.maxWidth = maxWidth;
	}

	@Override
	public float getWidth(float screenWidth)
	{
		return clampDimension(screenWidth, getMinWidth().getLength(screenWidth), getMaxWidth().getLength(screenWidth));
	}

	@Override
	public boolean isDrawBorder()
	{
		return drawBorder;
	}

	public void setDrawBorder(boolean drawBorder)
	{
		this.drawBorder = drawBorder;
	}
	
	@Override
	public Color getBorderColor()
	{
		return drawBorder ? borderColor : null;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = (borderColor == null ? DEFAULT_BORDER_COLOR : borderColor);
	}
	
	@Override
	public int getBorderWidth()
	{
		return drawBorder ? borderWidth : NO_BORDER_WIDTH;
	}
	
	public void setBorderWidth(int borderWidth)
	{
		this.borderWidth = (borderWidth < 0 ? DEFAULT_BORDER_WIDTH : borderWidth);
	}

	
	private float clampDimension(float screenDimension, float min, float max)
	{
		screenDimension = Math.max(1, screenDimension);
		if (screenDimension < min) return min;
		if (screenDimension > max) return max;
		return screenDimension;
	}
	
	/**
	 * Create a new {@link MutableScreenOverlayAttributesImpl} from the provided parameters
	 */
	public MutableScreenOverlayAttributesImpl(AVList params)
	{
		Validate.notNull(params, "Initialisation parameters are required");
		if (params.hasKey(ScreenOverlayKeys.URL))
		{
			Object urlEntry = params.getValue(ScreenOverlayKeys.URL);
			if (urlEntry instanceof URL)
			{
				// If its a URL, use it
				this.sourceUrl = (URL)urlEntry;
			}
			else
			{
				// If its a String, use it along with a context URL (if provided)
				URL context = (URL)params.getValue(ScreenOverlayKeys.CONTEXT_URL);
				try
				{
					this.sourceUrl = new URL(context, (String)urlEntry);
				}
				catch (MalformedURLException e)
				{
					throw new IllegalArgumentException("Unable to create source URL from params.", e);
				}
			}
		}
		else if (params.hasKey(ScreenOverlayKeys.OVERLAY_CONTENT))
		{
			this.sourceHtml = params.getStringValue(ScreenOverlayKeys.OVERLAY_CONTENT).trim();
		}
		else
		{
			throw new IllegalArgumentException("No overlay content or source URL found. At least one is required.");
		}
		
		if (params.hasKey(ScreenOverlayKeys.MIN_HEIGHT))
		{
			Object minHeight = params.getValue(ScreenOverlayKeys.MIN_HEIGHT);
			if (minHeight instanceof LengthExpression)
			{
				this.minHeight = (LengthExpression)minHeight;
			}
			else if (minHeight instanceof String)
			{
				this.minHeight = new LengthExpression((String)minHeight);
			}
			else if (minHeight == null)
			{
				this.minHeight = null;
			}
			else
			{
				throw new IllegalArgumentException("Min height must be one of String or LengthExpression");
			}
		}
		
		if (params.hasKey(ScreenOverlayKeys.MAX_HEIGHT))
		{
			Object maxHeight = params.getValue(ScreenOverlayKeys.MAX_HEIGHT);
			if (maxHeight instanceof LengthExpression)
			{
				this.maxHeight = (LengthExpression)maxHeight;
			}
			else if (maxHeight instanceof String)
			{
				this.maxHeight = new LengthExpression((String)maxHeight);
			}
			else if (maxHeight == null)
			{
				this.maxHeight = null;
			}
			else
			{
				throw new IllegalArgumentException("Max height must be one of String or LengthExpression");
			}
		}
		
		if (params.hasKey(ScreenOverlayKeys.MIN_WIDTH))
		{
			Object minWidth = params.getValue(ScreenOverlayKeys.MIN_WIDTH);
			if (minWidth instanceof LengthExpression)
			{
				this.minWidth = (LengthExpression)minWidth;
			}
			else if (minWidth instanceof String)
			{
				this.minWidth = new LengthExpression((String)minWidth);
			}
			else if (minWidth == null)
			{
				this.minWidth = null;
			}
			else
			{
				throw new IllegalArgumentException("Min height must be one of String or LengthExpression");
			}
		}
		
		if (params.hasKey(ScreenOverlayKeys.MAX_WIDTH))
		{
			Object maxWidth = params.getValue(ScreenOverlayKeys.MAX_WIDTH);
			if (maxWidth instanceof LengthExpression)
			{
				this.maxWidth = (LengthExpression)maxWidth;
			}
			else if (maxWidth instanceof String)
			{
				this.maxWidth = new LengthExpression((String)maxWidth);
			}
			else if (maxWidth == null)
			{
				this.maxWidth = null;
			}
			else
			{
				throw new IllegalArgumentException("Max height must be one of String or LengthExpression");
			}
		}
		
		if (params.hasKey(ScreenOverlayKeys.DRAW_BORDER))
		{
			this.drawBorder = (Boolean)params.getValue(ScreenOverlayKeys.DRAW_BORDER);
		}
		
		if (params.hasKey(ScreenOverlayKeys.BORDER_WIDTH))
		{
			this.borderWidth = (Integer)params.getValue(ScreenOverlayKeys.BORDER_WIDTH);
		}
		
		if (params.hasKey(ScreenOverlayKeys.BORDER_COLOR))
		{
			this.borderColor = (Color)params.getValue(ScreenOverlayKeys.BORDER_COLOR);
		}
		
		if (params.hasKey(ScreenOverlayKeys.POSITION))
		{
			Object position = params.getValue(ScreenOverlayKeys.POSITION);
			if (position instanceof ScreenOverlayPosition)
			{
				this.position = (ScreenOverlayPosition)position;
			}
			else if (position instanceof String)
			{
				this.position = ScreenOverlayPosition.valueOf(((String)position).toUpperCase());
			}
			else
			{
				throw new IllegalArgumentException("Position must be one of String or ScreenOverlayPosition");
			}
		}
	}
}
