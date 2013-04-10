package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;

import javax.media.opengl.GL2;

/**
 * Sets fog range/density according to view altitude.
 * 
 * @Author: Patrick Murris
 * @version $Id: FogLayer.java 5178 2008-04-25 21:51:20Z patrickmurris $
 */
public class FogLayer extends RenderableLayer
{
	private float fogColor[] = new float[] { 0.66f, 0.70f, 0.81f, 1.0f };
	private float nearFactor = 1.1f; // Applies to the view altitude
	private float farFactor = 1.1f; // Applies to the distance to the horizon

	/**
	 * Sets fog range/density according to view altitude
	 */
	public FogLayer()
	{
		this.setName("Fog layer");
	}

	// Public properties

	/**
	 * Get the fog color
	 * 
	 * @return the fog color
	 */
	public Color getColor()
	{
		return new Color(this.fogColor[0], this.fogColor[1], this.fogColor[2], this.fogColor[3]);
	}

	/**
	 * Set the fog color
	 * 
	 * @param color
	 *            the fog color
	 */
	public void setColor(Color color)
	{
		if (color == null)
		{
			String msg = Logging.getMessage("nullValue.ColorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		color.getColorComponents(this.fogColor);
	}

	/**
	 * Get the near distance factor that is applied to the view altitude.
	 * 
	 * @return the near factor
	 */
	public float getNearFactor()
	{
		return this.nearFactor;
	}

	/**
	 * Set the near distance factor applied to the view altitude
	 * 
	 * @param factor
	 *            the factor to apply to the view altitude
	 */
	public void setNearFactor(float factor)
	{
		this.nearFactor = factor;
	}

	/**
	 * Get the far distance factor that is applied to the eye distance to the
	 * horizon.
	 * 
	 * @return the far factor
	 */
	public float getFarFactor()
	{
		return this.farFactor;
	}

	/**
	 * Set the far distance factor applied to the eye distance to the horizon
	 * 
	 * @param factor
	 *            the factor to apply to the eye distance to the horizon
	 */
	public void setFarFactor(float factor)
	{
		this.farFactor = factor;
	}

	@Override
	public void doRender(DrawContext dc)
	{
		Position eyePos = dc.getView().getEyePosition();
		if (eyePos == null)
		{
			return;
		}
		// View altitude
		float alt = (float) eyePos.getElevation();
		alt = alt < 100 ? 100 : alt; // Clamp altitudes below 100m
		// Start based on view altitude
		float start = alt * this.nearFactor;
		// End based on distance to horizon
		float end = (float) (dc.getView().getHorizonDistance() * this.farFactor);
		// Set GL fog
		GL2 gl = dc.getGL().getGL2();
		gl.glFogfv(GL2.GL_FOG_COLOR, fogColor, 0); // Set fog color
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR); // Set fog mode
		gl.glFogf(GL2.GL_FOG_START, start); // Set fog start distance
		gl.glFogf(GL2.GL_FOG_END, end); // Set fog end distance
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_DONT_CARE);// Set fog hint
		gl.glEnable(GL2.GL_FOG); // Enable fog
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
