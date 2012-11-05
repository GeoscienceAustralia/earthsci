package au.gov.ga.earthsci.worldwind.common.layers.sphere;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A simple layer that renders a sphere at the Earth's centre with a specified radius and colour.
 * <p/>
 * Useful for representing Earth core boundaries etc.
 * <p/>
 * Uses simple GLU geometry for improved performance.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SphereLayer extends AbstractLayer
{
	private Vec4 center = Vec4.ZERO;
	private double radius;
	private Color color = Color.white;
	private int slices = 10;
	private int stacks = 10;

	/**
	 * Create a new {@link SphereLayer} with the properties in the provided params list 
	 */
	public SphereLayer(AVList params)
	{
		Validate.notNull(params.getValue(AVKeyMore.SPHERE_RADIUS), "A sphere radius is required.");
		
		this.radius = (Double) params.getValue(AVKeyMore.SPHERE_RADIUS);
		
		if (params.hasKey(AVKeyMore.COLOR))
		{
			this.color = (Color) params.getValue(AVKeyMore.COLOR);
		}
		if (params.hasKey(AVKeyMore.SPHERE_SLICES))
		{
			this.slices = (Integer) params.getValue(AVKeyMore.SPHERE_SLICES);
		}
		if (params.hasKey(AVKeyMore.SPHERE_STACKS))
		{
			this.stacks = (Integer) params.getValue(AVKeyMore.SPHERE_STACKS);
		}
		this.setValues(params);
	}

	/**
	 * Create a new {@link SphereLayer} with the given radius, using defaults for other properties.
	 * 
	 * @param radius The radius of the sphere, in metres.
	 */
	public SphereLayer(double radius)
	{
		this.radius = radius;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		GL2 gl = dc.getGL();
		GLU glu = dc.getGLU();
		
		OGLStackHandler ogsh = new OGLStackHandler();
		
		try
		{
			ogsh.pushModelview(gl);
			ogsh.pushProjection(gl);
			ogsh.pushAttrib(gl, GL2.GL_TEXTURE_BIT
								| GL2.GL_ENABLE_BIT
								| GL2.GL_CURRENT_BIT);
			
			setupProjectionMatrix(dc, gl);
	        
			gl.glDisable(GL2.GL_TEXTURE_2D);
			
			gl.glColor4ub((byte) color.getRed(), 
						  (byte) color.getGreen(),
						  (byte) color.getBlue(), 
						  (byte) (getOpacity() * 255));
	
			if (getOpacity() < 1.0)
			{
				gl.glEnable(GL2.GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			}
			
			gl.glDepthMask(false);
	
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			
			gl.glPushMatrix();
			gl.glTranslated(center.x, center.y, center.z);
			GLUquadric quadric = glu.gluNewQuadric();
			glu.gluSphere(quadric, radius, slices, stacks);
			gl.glPopMatrix();
			
			glu.gluDeleteQuadric(quadric);
			
			gl.glDepthMask(true);
	
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	private void setupProjectionMatrix(DrawContext dc, GL2 gl)
	{
		// Compute a projection matrix with no far clipping
		Matrix projection = Matrix.fromPerspective(dc.getView().getFieldOfView(),
												   dc.getView().getViewport().getWidth(),
												   dc.getView().getViewport().getHeight(),
												   1e3,
												   dc.getGlobe().getDiameter() * 1.5);
		// Apply the projection matrix to the current OpenGL context.
		gl.glMatrixMode(GL2.GL_PROJECTION);
		double[] matrixArray = new double[16];
		if (projection != null)
		{
		    projection.toArray(matrixArray, 0, false);
		    gl.glLoadMatrixd(matrixArray, 0);
		}
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public Vec4 getCenter()
	{
		return center;
	}

	public void setCenter(Vec4 center)
	{
		this.center = center;
	}

	public double getRadius()
	{
		return radius;
	}

	public void setRadius(double radius)
	{
		this.radius = radius;
	}

	public int getSlices()
	{
		return slices;
	}

	public void setSlices(int slices)
	{
		this.slices = slices;
	}

	public int getStacks()
	{
		return stacks;
	}

	public void setStacks(int stacks)
	{
		this.stacks = stacks;
	}
}
