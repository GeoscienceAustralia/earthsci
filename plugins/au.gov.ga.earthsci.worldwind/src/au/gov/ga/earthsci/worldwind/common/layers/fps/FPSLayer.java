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
package au.gov.ga.earthsci.worldwind.common.layers.fps;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.util.OGLTextRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Layer that shows an FPS counter.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FPSLayer extends AbstractLayer
{
	private Long lastNanos;
	private int frameCount = 0;
	private int fps;

	// Display parameters - TODO: make configurable
	private Dimension size = new Dimension(150, 10);
	private Color color = Color.white;
	private int borderWidth = 20;
	private String position = AVKey.NORTHWEST;
	private String resizeBehavior = AVKey.RESIZE_SHRINK_ONLY;
	private Font defaultFont = Font.decode("Arial-PLAIN-12");
	private double toViewportScale = 0.2;

	private Vec4 locationCenter = null;
	private Vec4 locationOffset = new Vec4(-72, 5);
	private double pixelSize;

	// Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
	// TODO: Add general support for this common pattern.
	private OrderedIcon orderedImage = new OrderedIcon();

	private class OrderedIcon implements OrderedRenderable
	{
		@Override
		public double getDistanceFromEye()
		{
			return 0;
		}

		@Override
		public void pick(DrawContext dc, Point pickPoint)
		{
		}

		@Override
		public void render(DrawContext dc)
		{
			FPSLayer.this.draw(dc);
		}
	}

	/**
	 * Renders a scalebar graphic in a screen corner
	 */
	public FPSLayer()
	{
		setPickEnabled(false);
	}

	// Public properties

	/**
	 * Get the apparent pixel size in meter at the reference position.
	 * 
	 * @return the apparent pixel size in meter at the reference position.
	 */
	public double getPixelSize()
	{
		return this.pixelSize;
	}

	/**
	 * Get the scalebar graphic Dimension (in pixels)
	 * 
	 * @return the scalebar graphic Dimension
	 */
	public Dimension getSize()
	{
		return this.size;
	}

	/**
	 * Set the scalebar graphic Dimenion (in pixels)
	 * 
	 * @param size
	 *            the scalebar graphic Dimension
	 */
	public void setSize(Dimension size)
	{
		if (size == null)
		{
			String message = Logging.getMessage("nullValue.DimensionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		this.size = size;
	}

	/**
	 * Get the scalebar color
	 * 
	 * @return the scalebar Color
	 */
	public Color getColor()
	{
		return this.color;
	}

	/**
	 * Set the scalbar Color
	 * 
	 * @param color
	 *            the scalebar Color
	 */
	public void setColor(Color color)
	{
		if (color == null)
		{
			String msg = Logging.getMessage("nullValue.ColorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		this.color = color;
	}

	/**
	 * Returns the scalebar-to-viewport scale factor.
	 * 
	 * @return the scalebar-to-viewport scale factor
	 */
	public double getToViewportScale()
	{
		return toViewportScale;
	}

	/**
	 * Sets the scale factor applied to the viewport size to determine the
	 * displayed size of the scalebar. This scale factor is used only when the
	 * layer's resize behavior is AVKey.RESIZE_STRETCH or
	 * AVKey.RESIZE_SHRINK_ONLY. The scalebar's width is adjusted to occupy the
	 * proportion of the viewport's width indicated by this factor. The
	 * scalebar's height is adjusted to maintain the scalebar's Dimension aspect
	 * ratio.
	 * 
	 * @param toViewportScale
	 *            the scalebar to viewport scale factor
	 */
	public void setToViewportScale(double toViewportScale)
	{
		this.toViewportScale = toViewportScale;
	}

	public String getPosition()
	{
		return this.position;
	}

	/**
	 * Sets the relative viewport location to display the scalebar. Can be one
	 * of AVKey.NORTHEAST, AVKey.NORTHWEST, AVKey.SOUTHEAST (the default), or
	 * AVKey.SOUTHWEST. These indicate the corner of the viewport.
	 * 
	 * @param position
	 *            the desired scalebar position
	 */
	public void setPosition(String position)
	{
		if (position == null)
		{
			String msg = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		this.position = position;
	}

	/**
	 * Returns the current scalebar center location.
	 * 
	 * @return the current location center. May be null.
	 */
	public Vec4 getLocationCenter()
	{
		return locationCenter;
	}

	/**
	 * Specifies the screen location of the scalebar center. May be null. If
	 * this value is non-null, it overrides the position specified by
	 * #setPosition. The location is specified in pixels. The origin is the
	 * window's lower left corner. Positive X values are to the right of the
	 * origin, positive Y values are upwards from the origin. The final scalebar
	 * location will be affected by the currently specified location offset if a
	 * non-null location offset has been specified (see #setLocationOffset).
	 * 
	 * @param locationCenter
	 *            the scalebar center. May be null.
	 * @see #setPosition
	 * @see #setLocationOffset
	 */
	public void setLocationCenter(Vec4 locationCenter)
	{
		this.locationCenter = locationCenter;
	}

	/**
	 * Returns the current location offset. See #setLocationOffset for a
	 * description of the offset and its values.
	 * 
	 * @return the location offset. Will be null if no offset has been
	 *         specified.
	 */
	public Vec4 getLocationOffset()
	{
		return locationOffset;
	}

	/**
	 * Specifies a placement offset from the scalebar's position on the screen.
	 * 
	 * @param locationOffset
	 *            the number of pixels to shift the scalebar from its specified
	 *            screen position. A positive X value shifts the image to the
	 *            right. A positive Y value shifts the image up. If null, no
	 *            offset is applied. The default offset is null.
	 * @see #setLocationCenter
	 * @see #setPosition
	 */
	public void setLocationOffset(Vec4 locationOffset)
	{
		this.locationOffset = locationOffset;
	}

	/**
	 * Returns the layer's resize behavior.
	 * 
	 * @return the layer's resize behavior
	 */
	public String getResizeBehavior()
	{
		return resizeBehavior;
	}

	/**
	 * Sets the behavior the layer uses to size the scalebar when the viewport
	 * size changes, typically when the World Wind window is resized. If the
	 * value is AVKey.RESIZE_KEEP_FIXED_SIZE, the scalebar size is kept to the
	 * size specified in its Dimension scaled by the layer's current icon scale.
	 * If the value is AVKey.RESIZE_STRETCH, the scalebar is resized to have a
	 * constant size relative to the current viewport size. If the viewport
	 * shrinks the scalebar size decreases; if it expands then the scalebar
	 * enlarges. If the value is AVKey.RESIZE_SHRINK_ONLY (the default),
	 * scalebar sizing behaves as for AVKey.RESIZE_STRETCH but it will not grow
	 * larger than the size specified in its Dimension.
	 * 
	 * @param resizeBehavior
	 *            the desired resize behavior
	 */
	public void setResizeBehavior(String resizeBehavior)
	{
		this.resizeBehavior = resizeBehavior;
	}

	public int getBorderWidth()
	{
		return borderWidth;
	}

	/**
	 * Sets the scalebar offset from the viewport border.
	 * 
	 * @param borderWidth
	 *            the number of pixels to offset the scalebar from the borders
	 *            indicated by {@link #setPosition(String)}.
	 */
	public void setBorderWidth(int borderWidth)
	{
		this.borderWidth = borderWidth;
	}

	/**
	 * Get the scalebar legend Fon
	 * 
	 * @return the scalebar legend Font
	 */
	public Font getFont()
	{
		return this.defaultFont;
	}

	/**
	 * Set the scalebar legend Fon
	 * 
	 * @param font
	 *            the scalebar legend Font
	 */
	public void setFont(Font font)
	{
		if (font == null)
		{
			String msg = Logging.getMessage("nullValue.FontIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		this.defaultFont = font;
	}

	// Rendering
	@Override
	public void doRender(DrawContext dc)
	{
		dc.addOrderedRenderable(this.orderedImage);
	}

	@Override
	public void doPick(DrawContext dc, Point pickPoint)
	{
		// Delegate drawing to the ordered renderable list
		dc.addOrderedRenderable(this.orderedImage);
	}

	// Rendering
	public void draw(DrawContext dc)
	{
		if (dc.isPickingMode())
			return;

		frameCount++;
		long currentNanos = System.nanoTime();
		if (lastNanos == null)
		{
			lastNanos = currentNanos;
		}
		else if ((currentNanos - lastNanos) / 1e9d > 1d)
		{
			fps = frameCount;
			frameCount = 0;
			lastNanos = currentNanos;
		}


		GL2 gl = dc.getGL();

		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TRANSFORM_BIT);

			gl.glDisable(GL2.GL_DEPTH_TEST);

			double width = this.size.width;
			double height = this.size.height;

			// Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
			// into the GL projection matrix.
			java.awt.Rectangle viewport = dc.getView().getViewport();
			ogsh.pushProjectionIdentity(gl);
			double maxwh = width > height ? width : height;
			gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

			ogsh.pushModelviewIdentity(gl);

			// Scale to a width x height space
			// located at the proper position on screen
			double scale = this.computeScale(viewport);
			Vec4 locationSW = this.computeLocation(viewport, scale);
			gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
			gl.glScaled(scale, scale, 1);

			// Draw fps
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

			float[] colorRGB = this.color.getRGBColorComponents(null);
			gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity());

			// Draw label
			int divWidth = 0;
			String label = fps + " fps";
			gl.glLoadIdentity();
			gl.glDisable(GL2.GL_CULL_FACE);
			drawLabel(dc, label,
					locationSW.add3(new Vec4(divWidth * scale / 2 + (width - divWidth) / 2, height * scale, 0)));
		}
		finally
		{
			gl.glColor4d(1d, 1d, 1d, 1d); // restore the default OpenGL color
			gl.glEnable(GL2.GL_DEPTH_TEST);

			if (!dc.isPickingMode())
			{
				gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ZERO); // restore to default blend function
				gl.glDisable(GL2.GL_BLEND); // restore to default blend state
			}

			ogsh.pop(gl);
		}
	}

	// Draw the scale label
	private void drawLabel(DrawContext dc, String text, Vec4 screenPoint)
	{
		TextRenderer textRenderer =
				OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(), this.defaultFont);

		Rectangle2D nameBound = textRenderer.getBounds(text);
		int x = (int) (screenPoint.x() - nameBound.getWidth() / 2d);
		int y = (int) screenPoint.y();

		textRenderer.begin3DRendering();

		textRenderer.setColor(this.getBackgroundColor(this.color));
		textRenderer.draw(text, x + 1, y - 1);
		textRenderer.setColor(this.color);
		textRenderer.draw(text, x, y);

		textRenderer.end3DRendering();

	}

	private final float[] compArray = new float[4];

	// Compute background color for best contrast
	private Color getBackgroundColor(Color color)
	{
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
		if (compArray[2] > 0.5)
			return new Color(0, 0, 0, 0.7f);
		else
			return new Color(1, 1, 1, 0.7f);
	}

	private double computeScale(java.awt.Rectangle viewport)
	{
		if (this.resizeBehavior.equals(AVKey.RESIZE_SHRINK_ONLY))
		{
			return Math.min(1d, (this.toViewportScale) * viewport.width / this.size.width);
		}
		else if (this.resizeBehavior.equals(AVKey.RESIZE_STRETCH))
		{
			return (this.toViewportScale) * viewport.width / this.size.width;
		}
		else if (this.resizeBehavior.equals(AVKey.RESIZE_KEEP_FIXED_SIZE))
		{
			return 1d;
		}
		else
		{
			return 1d;
		}
	}

	private Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
	{
		double scaledWidth = scale * this.size.width;
		double scaledHeight = scale * this.size.height;

		double x;
		double y;

		if (this.locationCenter != null)
		{
			x = this.locationCenter.x - scaledWidth / 2;
			y = this.locationCenter.y - scaledHeight / 2;
		}
		else if (this.position.equals(AVKey.NORTHEAST))
		{
			x = viewport.getWidth() - scaledWidth - this.borderWidth;
			y = viewport.getHeight() - scaledHeight - this.borderWidth;
		}
		else if (this.position.equals(AVKey.SOUTHEAST))
		{
			x = viewport.getWidth() - scaledWidth - this.borderWidth;
			y = 0d + this.borderWidth;
		}
		else if (this.position.equals(AVKey.NORTHWEST))
		{
			x = 0d + this.borderWidth;
			y = viewport.getHeight() - scaledHeight - this.borderWidth;
		}
		else if (this.position.equals(AVKey.SOUTHWEST))
		{
			x = 0d + this.borderWidth;
			y = 0d + this.borderWidth;
		}
		else
		// use North East
		{
			x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
			y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
		}

		if (this.locationOffset != null)
		{
			x += this.locationOffset.x;
			y += this.locationOffset.y;
		}

		return new Vec4(x, y, 0);
	}
}
