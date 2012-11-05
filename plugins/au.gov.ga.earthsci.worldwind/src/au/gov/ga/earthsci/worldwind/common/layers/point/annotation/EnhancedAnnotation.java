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
package au.gov.ga.earthsci.worldwind.common.layers.point.annotation;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.util.WWMath;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.media.opengl.GL2;

/**
 * An enhanced annotation that supports:
 * <ul>
 * <li>Fade in/out based on eye elevation</li>
 * <li>Fade in/out based on eye distance</li>
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EnhancedAnnotation extends GlobeAnnotation
{
	public EnhancedAnnotation(String text, Position position, AnnotationAttributes defaults)
	{
		super(text, position, defaults);
	}

	public EnhancedAnnotation(String text, Position position, Font font, Color textColor)
	{
		super(text, position, font, textColor);
	}

	public EnhancedAnnotation(String text, Position position, Font font)
	{
		super(text, position, font);
	}

	public EnhancedAnnotation(String text, Position position)
	{
		super(text, position);
	}

	@Override
	public void setAttributes(AnnotationAttributes attributes)
	{
		if (attributes instanceof EnhancedAnnotationAttributes)
		{
			super.setAttributes(attributes);
		}
		else
		{
			super.setAttributes(new EnhancedAnnotationAttributes(attributes));
		}
	}

	/**
	 * Set the enhanced attributes associated with this annotation
	 */
	public void setAttributes(EnhancedAnnotationAttributes attributes)
	{
		super.setAttributes(attributes);
	}

	/**
	 * @return The enhanced attributes associated with this annotation
	 */
	public EnhancedAnnotationAttributes getEnhancedAttributes()
	{
		AnnotationAttributes result = getAttributes();
		if (result instanceof EnhancedAnnotationAttributes)
		{
			return (EnhancedAnnotationAttributes) result;
		}

		setAttributes(new EnhancedAnnotationAttributes(result));
		return (EnhancedAnnotationAttributes) getAttributes();
	}

	@Override
	protected void doRenderNow(DrawContext dc)
	{
		if (dc.isPickingMode() && this.getPickSupport() == null)
		{
			return;
		}

		Vec4 point = this.getAnnotationDrawPoint(dc);
		if (point == null || pointIsBehindView(dc, point))
		{
			return;
		}

		Vec4 screenPoint = dc.getView().project(point);
		if (screenPoint == null)
		{
			return;
		}

		Dimension size = this.getPreferredSize(dc);
		Position pos = dc.getGlobe().computePositionFromPoint(point);

		// Determine scale and opacity factors based on distance from eye vs the distance to the look at point.
		double lookAtDistance = this.computeLookAtDistance(dc);
		double eyeDistance = dc.getView().getEyePoint().distanceTo3(point);
		double distanceFactor = Math.sqrt(lookAtDistance / eyeDistance);
		double scale =
				WWMath.clamp(distanceFactor, this.attributes.getDistanceMinScale(),
						this.attributes.getDistanceMaxScale());

		double opacity = computeOpacity(eyeDistance, distanceFactor);
		if (opacity < 0.1)
		{
			getAttributes().setHighlighted(false);
		}

		// Don't draw picking if annotation is not visible
		if (opacity <= 0.0 && dc.isPickingMode())
		{
			return;
		}

		this.setDepthFunc(dc, screenPoint);
		this.drawTopLevelAnnotation(dc, screenPoint.x, screenPoint.y, size.width, size.height, scale, opacity, pos);
	}

	/**
	 * Compute the opacity of the annotation based on the distance between the
	 * eye and the marker point
	 * <p/>
	 * Applies a linear fade based on the set <code>fadeDistance</code>
	 */
	private double computeOpacity(double eyeDistance, double distanceFactor)
	{
		double opacity = WWMath.clamp(distanceFactor, this.attributes.getDistanceMinOpacity(), 1);

		if (isLessThanMinDistance(eyeDistance) || isGreaterThanMaxDistance(eyeDistance))
		{
			return 0.0;
		}

		// Use a linear fadeout: 
		// y = m*x + d where m = 1/fadeDistance, x = eyeDistance and d = -m * minDistance 
		// y = (1/fadeDistance) * eyeDistance - (1/fadeDistance) * minDistance
		// y = (1/fadeDistance)*(eyeDistance - minDistance)
		double fadeRate = 1 / getEnhancedAttributes().getFadeDistance();

		Double minEyeDistance = getEnhancedAttributes().getMinEyeDistance();
		if (minEyeDistance != null)
		{
			opacity = WWMath.clamp(fadeRate * (eyeDistance - minEyeDistance), 0d, opacity);
		}

		Double maxEyeDistance = getEnhancedAttributes().getMaxEyeDistance();
		if (maxEyeDistance != null)
		{
			opacity = WWMath.clamp(fadeRate * (maxEyeDistance - eyeDistance), 0d, opacity);
		}

		return opacity;
	}

	private boolean isLessThanMinDistance(double eyeDistance)
	{
		return getEnhancedAttributes().getMinEyeDistance() != null
				&& eyeDistance < getEnhancedAttributes().getMinEyeDistance();
	}

	private boolean isGreaterThanMaxDistance(double eyeDistance)
	{
		return getEnhancedAttributes().getMaxEyeDistance() != null
				&& eyeDistance > getEnhancedAttributes().getMaxEyeDistance();
	}

	private boolean pointIsBehindView(DrawContext dc, Vec4 point)
	{
		return dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(point) < 0;
	}

	protected void drawTopLevelAnnotation(DrawContext dc, double x, double y, int width, int height, double scale,
			double opacity, Position pickPosition)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		OGLStackHandler stackHandler = new OGLStackHandler();
		this.beginDraw(dc, stackHandler);
		try
		{
			this.applyScreenTransform(dc, x, y, width, height, scale);
			this.draw(dc, width, height, opacity, pickPosition);
		}
		finally
		{
			this.endDraw(dc, stackHandler);
		}
	}

	protected void applyScreenTransform(DrawContext dc, double x, double y, double width, double height, double scale)
	{
		double finalScale = scale * this.computeScale(dc);
		java.awt.Point offset = this.getAttributes().getDrawOffset();

		GL2 gl = dc.getGL();
		gl.glTranslated(x, y, 0);
		gl.glScaled(finalScale, finalScale, 1);
		gl.glTranslated(offset.x, offset.y, 0);
		gl.glTranslated(-width / 2, 0, 0);
	}
}
