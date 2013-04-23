package au.gov.ga.earthsci.core.model.render;

import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexBasedGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexColouredGeometry;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;

/**
 * A basic {@link IModelGeometryRenderer} that supports
 * {@link IVertexBasedGeometry} and {@link IVertexColouredGeometry} instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class BasicRenderer implements IModelGeometryRenderer
{

	private static final Logger logger = LoggerFactory.getLogger(BasicRenderer.class);

	private WorldWindowRegistry wwRegistry;
	private IVertexBasedGeometry geometry;

	private ModelDataVBO vertexVBO;

	/**
	 * Create a new instance of the renderer for the given geometry
	 * 
	 * @param geometry
	 *            The geometry to create the renderer for
	 * 
	 * @param wwRegistry
	 *            The {@link WorldWindowRegistry} used to obtain the current
	 *            view and globe etc.
	 */
	public BasicRenderer(IVertexBasedGeometry geometry, WorldWindowRegistry wwRegistry)
	{
		Validate.notNull(geometry, "A geometry is required"); //$NON-NLS-1$
		this.geometry = geometry;
		this.wwRegistry = wwRegistry;
	}

	@Override
	public void render()
	{
		logger.trace("Rendering {} ({})", geometry.getName(), geometry.getId()); //$NON-NLS-1$

		GLContext context = GLContext.getCurrent();
		if (context == null)
		{
			logger.debug("No current GL context found - aborting render"); //$NON-NLS-1$
			return;
		}
		GL2 gl = (GL2) context.getGL();

		if (vertexVBO == null)
		{
			vertexVBO = ModelDataVBO.createDataVBO(geometry.getVertices());
		}

		OGLStackHandler stack = new OGLStackHandler();
		stack.pushAttrib(gl, GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT);
		stack.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
		try
		{
			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			vertexVBO.bind(gl);
			gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
			gl.glDrawArrays(GL2.GL_POINTS, 0, vertexVBO.getBuffer().limit() / 3);
		}
		finally
		{
			stack.pop(gl);
		}

		//		// TODO REALLY REALLY INNEFICIENT!!! TESTING ONLY!!!
		//		ByteBuffer vertices = geometry.getVertices().getSource();
		//		BufferType type = geometry.getVertices().getBufferType();
		//
		//		Globe globe = wwRegistry.getRendering().getView().getGlobe();
		//
		//		gl.glBegin(GL2.GL_POINTS);
		//		while (vertices.hasRemaining())
		//		{
		//			float lon = type.getValueFrom(vertices).floatValue();
		//			float lat = type.getValueFrom(vertices).floatValue();
		//			float elevation = type.getValueFrom(vertices).floatValue();
		//
		//			Vec4 point = globe.computePointFromPosition(Angle.fromDegrees(lat), Angle.fromDegrees(lon), elevation);
		//
		//
		//			gl.glVertex3d(point.x, point.y, point.z);
		//		}
		//		gl.glEnd();

		// TODO 
	}

	@Override
	public IModelGeometry getGeometry()
	{
		return geometry;
	}

}
