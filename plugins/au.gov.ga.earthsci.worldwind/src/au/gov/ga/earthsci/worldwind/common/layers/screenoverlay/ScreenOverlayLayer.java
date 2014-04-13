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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.util.Logging;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.DoubleBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;
import javax.swing.SwingUtilities;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * A layer that can display html formatted text and images overlayed on the
 * screen.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ScreenOverlayLayer extends AbstractLayer
{
	private ScreenOverlayAttributes attributes;

	private ScreenOverlay overlay = new ScreenOverlay();

	private BufferedImage image = null;

	private boolean imageLoading = false;

	/**
	 * Create a new {@link ScreenOverlayLayer} with the given source data and
	 * the default attribute values
	 */
	public ScreenOverlayLayer(URL sourceUrl)
	{
		Validate.notNull(sourceUrl, "Source data URL is required");
		this.attributes = new MutableScreenOverlayAttributesImpl(sourceUrl);
	}

	/**
	 * Create a new {@link ScreenOverlayLayer} with the given overlay attributes
	 */
	public ScreenOverlayLayer(ScreenOverlayAttributes attributes)
	{
		Validate.notNull(attributes, "Overlay attributes are required");
		this.attributes = attributes;
	}

	/**
	 * Create a new {@link ScreenOverlayLayer} with parameters provided in the
	 * given {@link AVList}
	 */
	public ScreenOverlayLayer(AVList params)
	{
		Validate.notNull(params, "Initialisation parameters are required");
		this.attributes = new MutableScreenOverlayAttributesImpl(params);
		super.setValues(params);
	}

	/**
	 * The ordered renderable with eye distance 0 that will render the layer on
	 * top of most other layers
	 */
	protected class ScreenOverlay implements OrderedRenderable
	{
		@Override
		public double getDistanceFromEye()
		{
			return 0;
		}

		@Override
		public void render(DrawContext dc)
		{
			ScreenOverlayLayer.this.draw(dc);
		}

		@Override
		public void pick(DrawContext dc, Point pickPoint)
		{
			ScreenOverlayLayer.this.draw(dc);
		}

	}

	public void setAttributes(ScreenOverlayAttributes attributes)
	{
		Validate.notNull(attributes, "Attributes are required");
		this.attributes = attributes;
	}

	public ScreenOverlayAttributes getAttributes()
	{
		return this.attributes;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		dc.addOrderedRenderable(this.overlay);
	}

	@Override
	protected void doPick(DrawContext dc, Point pickPoint)
	{
		dc.addOrderedRenderable(this.overlay);
	}

	protected void draw(DrawContext dc)
	{
		if (attributes == null)
		{
			return;
		}

		GL2 gl = dc.getGL().getGL2();

		boolean attribsPushed = false;
		boolean modelviewPushed = false;
		boolean projectionPushed = false;

		try
		{
			gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT
					| GL2.GL_TRANSFORM_BIT | GL2.GL_VIEWPORT_BIT | GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT);
			attribsPushed = true;

			gl.glDisable(GL2.GL_DEPTH_TEST);

			Rectangle viewport = dc.getView().getViewport();
			Rectangle overlay =
					new Rectangle((int) attributes.getWidth(viewport.width),
							(int) attributes.getHeight(viewport.height));

			// Parallel projection 
			gl.glMatrixMode(GL2.GL_PROJECTION);
			gl.glPushMatrix();
			projectionPushed = true;
			gl.glLoadIdentity();
			double maxwh = Math.max(1, Math.max(overlay.width, overlay.height));
			gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

			// Translate to the correct position
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glPushMatrix();
			modelviewPushed = true;
			gl.glLoadIdentity();
			Vec4 location = computeLocation(viewport, overlay);
			gl.glTranslated(location.x, location.y, location.z);

			if (!dc.isPickingMode())
			{
				if (attributes.isDrawBorder())
				{
					drawBorder(dc, overlay);
				}

				drawOverlay(dc, overlay);
			}
		}
		finally
		{
			if (projectionPushed)
			{
				gl.glMatrixMode(GL2.GL_PROJECTION);
				gl.glPopMatrix();
			}
			if (modelviewPushed)
			{
				gl.glMatrixMode(GL2.GL_MODELVIEW);
				gl.glPopMatrix();
			}
			if (attribsPushed)
			{
				gl.glPopAttrib();
			}
		}
	}

	private void drawBorder(DrawContext dc, Rectangle overlay)
	{
		DoubleBuffer buffer = null;
		buffer =
				FrameFactory.createShapeBuffer(AVKey.SHAPE_RECTANGLE,
						(overlay.width + attributes.getBorderWidth() * 2),
						(overlay.height + attributes.getBorderWidth() * 2), 0, buffer);
		GL2 gl = dc.getGL().getGL2();

		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);

		gl.glLineWidth(attributes.getBorderWidth());

		gl.glEnable(GL2.GL_BLEND);
		float[] compArray = new float[4];
		attributes.getBorderColor().getRGBComponents(compArray);
		compArray[3] = (float) this.getOpacity();
		gl.glColor4fv(compArray, 0);

		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

		gl.glTranslated(-attributes.getBorderWidth() / 2, -attributes.getBorderWidth() / 2, 0);
		FrameFactory.drawBuffer(dc, GL2.GL_LINE_STRIP, buffer.remaining() / 2, buffer);
		gl.glTranslated(attributes.getBorderWidth(), attributes.getBorderWidth(), 0);
	}

	private void drawOverlay(DrawContext dc, Rectangle overlay)
	{
		Texture overlayTexture = getTexture(dc, overlay);
		if (overlayTexture != null)
		{
			GL2 gl = dc.getGL().getGL2();
			gl.glEnable(GL2.GL_TEXTURE_2D);
			overlayTexture.bind(gl);

			gl.glColor4d(1d, 1d, 1d, this.getOpacity());
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			TextureCoords texCoords = overlayTexture.getImageTexCoords();
			gl.glScaled(overlay.width, overlay.height, 1d);
			dc.drawUnitQuad(texCoords);
		}
	}

	private Texture getTexture(DrawContext dc, final Rectangle overlay)
	{
		Texture texture = dc.getGpuResourceCache().getTexture(attributes.getSourceId());
		if (!textureNeedsReloading(texture, overlay))
		{
			return texture;
		}

		if (image == null)
		{
			if (!imageLoading)
			{
				imageLoading = true;
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							if (attributes.isSourceHtml())
							{
								if (attributes.getSourceUrl() != null)
								{
									image =
											HtmlToImage.createImageFromHtml(attributes.getSourceUrl(), overlay.width,
													overlay.height);
								}
								else
								{
									image =
											HtmlToImage.createImageFromHtml(attributes.getSourceHtml(), overlay.width,
													overlay.height);
								}
							}
							else
							{
								image = ImageIO.read(attributes.getSourceUrl());
							}
							imageLoading = false;
						}
						catch (Exception e)
						{
							String msg = Logging.getMessage("layers.IOExceptionDuringInitialization");
							Logging.logger().severe(msg);
							throw new WWRuntimeException(msg, e);
						}
					}
				});
			}
			return null;
		}

		texture = AWTTextureIO.newTexture(GLProfile.get(GLProfile.GL2), image, false);
		dc.getTextureCache().put(attributes.getSourceId(), texture);

		GL2 gl = dc.getGL().getGL2();
		gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		int[] maxAnisotropy = new int[1];
		gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);

		return texture;

	}

	private boolean textureNeedsReloading(Texture texture, Rectangle overlay)
	{
		if (texture == null)
		{
			return true;
		}

		if (attributes.isSourceImage())
		{
			// Images will simply be rescaled - don't need to reload for a dimension change
			return false;
		}

		return texture.getImageHeight() != overlay.height || texture.getImageWidth() != overlay.width;
	}

	private Vec4 computeLocation(Rectangle viewport, Rectangle overlay)
	{
		double x = 0d;
		double y = 0d;

		switch (attributes.getPosition())
		{
		case CENTER:
		{
			x = ((viewport.width - overlay.width) / 2) - attributes.getBorderWidth();
			y = ((viewport.height - overlay.height) / 2) - attributes.getBorderWidth();
			break;
		}

		case NORTH:
		{
			x = ((viewport.width - overlay.width) / 2) - attributes.getBorderWidth();
			y = viewport.height - overlay.height - attributes.getBorderWidth();
			break;
		}

		case NORTHEAST:
		{
			x = viewport.width - overlay.width - attributes.getBorderWidth();
			y = viewport.height - overlay.height - attributes.getBorderWidth();
			break;
		}

		case EAST:
		{
			x = viewport.width - overlay.width - attributes.getBorderWidth();
			y = ((viewport.height - overlay.height) / 2) - attributes.getBorderWidth();
			break;
		}

		case SOUTHEAST:
		{
			x = viewport.width - overlay.width - attributes.getBorderWidth();
			y = attributes.getBorderWidth();
			break;
		}

		case SOUTH:
		{
			x = ((viewport.width - overlay.width) / 2) - attributes.getBorderWidth();
			y = attributes.getBorderWidth();
			break;
		}

		case SOUTHWEST:
		{
			x = attributes.getBorderWidth();
			y = attributes.getBorderWidth();
			break;
		}

		case WEST:
		{
			x = attributes.getBorderWidth();
			y = ((viewport.height - overlay.height) / 2) - attributes.getBorderWidth();
			break;
		}

		case NORTHWEST:
		{
			x = attributes.getBorderWidth();
			y = viewport.height - overlay.height - attributes.getBorderWidth();
			break;
		}
		}

		return new Vec4(x, y, 0);
	}
}
