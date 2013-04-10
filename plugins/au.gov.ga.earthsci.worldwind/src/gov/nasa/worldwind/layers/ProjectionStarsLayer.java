package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;

/**
 * An extension of the WW default {@link StarsLayer} that extracts projection
 * matrix initialisation into a method that can be overridden by subclasses that
 * wish to change the default projection matrix (e.g. for stereo rendering etc)
 */
public class ProjectionStarsLayer extends StarsLayer
{
	protected void applyDrawProjection(DrawContext dc, OGLStackHandler ogsh)
	{
		//copied from super's doRender() function

		ogsh.pushProjectionIdentity(dc.getGL().getGL2());
		double distanceFromOrigin = dc.getView().getEyePoint().getLength3();
		double near = distanceFromOrigin;
		double far = this.radius + distanceFromOrigin;
		dc.getGLU().gluPerspective(dc.getView().getFieldOfView().degrees,
				dc.getView().getViewport().getWidth() / dc.getView().getViewport().getHeight(), near, far);
	}

	//COPIED FROM StarsLayer (replaced setting the projection matrix with a call to the function above):

	@Override
	public void doRender(DrawContext dc)
	{
		// Load or reload stars if not previously loaded
		if (this.starsBuffer == null || this.rebuild)
		{
			this.loadStars();
			this.rebuild = false;
		}

		// Still no stars to render ?
		if (this.starsBuffer == null)
		{
			return;
		}

		GL2 gl = dc.getGL().getGL2();
		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			gl.glDisable(GL2.GL_DEPTH_TEST);

			// CHANGE HERE
			applyDrawProjection(dc, ogsh);
			// CHANGE HERE

			// Rotate sphere
			ogsh.pushModelview(gl);
			gl.glRotatef((float) this.longitudeOffset.degrees, 0.0f, 1.0f, 0.0f);
			gl.glRotatef((float) -this.latitudeOffset.degrees, 1.0f, 0.0f, 0.0f);

			// Draw
			ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

			if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
			{
				if (!this.drawWithVBO(dc))
				{
					this.drawWithVertexArray(dc);
				}
			}
			else
			{
				this.drawWithVertexArray(dc);
			}
		}
		finally
		{
			dc.restoreDefaultDepthTesting();
			ogsh.pop(gl);
		}
	}
}
