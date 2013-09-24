package au.gov.ga.earthsci.model.core.render;

import gov.nasa.worldwind.util.OGLStackHandler;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.color.ColorType;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.model.geometry.IMeshGeometry;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexBasedGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexColourMappedGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexColouredGeometry;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.AbstractVBO;

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

	private VerticalExaggerationService veService = VerticalExaggerationService.INSTANCE;
	private WorldWindowRegistry wwRegistry;
	private IVertexBasedGeometry geometry;

	private AtomicBoolean isInitialised = new AtomicBoolean(false);

	private AbstractVBO<?> vertexVBO;
	private AbstractVBO<?> vertexColourVBO;
	private AbstractVBO<?> edgesVBO;

	private Integer renderMode;

	private BasicRendererShader shader = new BasicRendererShader();

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

		init();

		OGLStackHandler stack = new OGLStackHandler();
		stack.pushAttrib(gl, GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT | GL2.GL_POLYGON_BIT | GL2.GL_ALPHA_BITS);
		stack.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
		try
		{
			shader.setGlobe(wwRegistry.getRenderingView().getGlobe());
			shader.setVerticalExaggeration((float) veService.get());
			shader.setNodata((Float) geometry.getVertices().getNoDataValue());
			shader.setOpacity((float) geometry.getOpacity());

			boolean bound = shader.bind(gl);
			if (!bound)
			{
				logger.debug("Unable to bind shader. Aborting.", shader.getLastError()); //$NON-NLS-1$
				return;
			}

			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			if (vertexColourVBO != null)
			{
				gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
				vertexColourVBO.bind(gl);
				gl.glColorPointer(getColourTypeForGeometry().getNumComponents(), GL2.GL_FLOAT, 0, 0);
			}

			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			vertexVBO.bind(gl);
			gl.glVertexPointer(geometry.getVertices().getGroupSize(), GL2.GL_FLOAT, 0, 0);

			if (edgesVBO != null)
			{
				edgesVBO.bind(gl);
				gl.glDrawElements(renderMode, ((IMeshGeometry) geometry).getEdgeIndices().getNumberOfValues(),
						GL2.GL_UNSIGNED_INT, 0);
			}
			else
			{
				gl.glDrawArrays(renderMode, 0, geometry.getVertices().getNumberOfGroups());
			}

			checkForError(gl);

			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		finally
		{
			shader.unbind(gl);
			stack.pop(gl);
		}
	}

	private void init()
	{
		if (isInitialised.get())
		{
			return;
		}

		if (vertexVBO == null)
		{
			vertexVBO = ModelDataVBO.createDataVBO(geometry.getVertices());
		}
		if (edgesVBO == null && geometryHasEdges())
		{
			edgesVBO = ModelDataVBO.createIndexVBO(((IMeshGeometry) geometry).getEdgeIndices());
		}
		if (vertexColourVBO == null && geometryHasVertexColours())
		{
			vertexColourVBO = ModelDataVBO.createDataVBO(((IVertexColouredGeometry) geometry).getVertexColour());
		}
		if (renderMode == null)
		{
			renderMode = getModeForGeometry();
		}

		shader.setUseVertexColouring(geometryHasVertexColours());

		isInitialised.set(true);
	}

	private boolean geometryHasEdges()
	{
		return geometry instanceof IMeshGeometry && ((IMeshGeometry) geometry).hasEdgeIndices();
	}

	private boolean geometryHasVertexColours()
	{
		return geometry instanceof IVertexColouredGeometry && ((IVertexColouredGeometry) geometry).hasVertexColour();
	}

	private boolean geometryHasColourMap()
	{
		return geometry instanceof IVertexColourMappedGeometry
				&& ((IVertexColourMappedGeometry) geometry).hasColorMap();
	}

	private void checkForError(GL2 gl)
	{
		int error = gl.glGetError();
		if (error != GL2.GL_NO_ERROR)
		{
			StringBuffer buf = new StringBuffer("OpenGL error detected: "); //$NON-NLS-1$
			switch (error)
			{
			case GL2.GL_INVALID_ENUM:
				buf.append("GL_INVALID_ENUM "); //$NON-NLS-1$
				break;
			case GL2.GL_INVALID_VALUE:
				buf.append("GL_INVALID_VALUE "); //$NON-NLS-1$
				break;
			case GL2.GL_INVALID_OPERATION:
				buf.append("GL_INVALID_OPERATION "); //$NON-NLS-1$
				break;
			case GL2GL3.GL_STACK_OVERFLOW:
				buf.append("GL_STACK_OVERFLOW "); //$NON-NLS-1$
				break;
			case GL2GL3.GL_STACK_UNDERFLOW:
				buf.append("GL_STACK_UNDERFLOW "); //$NON-NLS-1$
				break;
			case GL2.GL_OUT_OF_MEMORY:
				buf.append("GL_OUT_OF_MEMORY "); //$NON-NLS-1$
				break;
			default:
				buf.append("Unknown (0x").append(Integer.toHexString(error)).append(")"); //$NON-NLS-1$//$NON-NLS-2$
			}
			buf.append(". Relaunch with -Djogl.debug=all and -Dnewt.debug=all for more information."); //$NON-NLS-1$
			logger.error(buf.toString());
		}
	}

	private int getModeForGeometry()
	{
		int mode = GL2.GL_POINTS;

		if (geometry instanceof IMeshGeometry)
		{
			switch (((IMeshGeometry) geometry).getFaceType())
			{
			case QUADS:
				mode = GL2.GL_QUADS;
				break;
			case QUAD_STRIP:
				mode = GL2.GL_QUAD_STRIP;
				break;
			case TRIANGLES:
				mode = GL2.GL_TRIANGLES;
				break;
			case TRIANGLE_FAN:
				mode = GL2.GL_TRIANGLE_FAN;
				break;
			case TRIANGLE_STRIP:
				mode = GL2.GL_TRIANGLE_STRIP;
				break;
			case LINES:
				mode = GL2.GL_LINES;
				break;
			case LINE_STRIP:
				mode = GL2.GL_LINE_STRIP;
				break;
			case LINE_LOOP:
				mode = GL2.GL_LINE_LOOP;
				break;
			default:
				mode = GL2.GL_POINTS;
			}
		}
		return mode;
	}

	private ColorType getColourTypeForGeometry()
	{
		if (geometry instanceof IVertexColouredGeometry)
		{
			return ((IVertexColouredGeometry) geometry).getColourType();
		}
		return ColorType.RGB;
	}

	@Override
	public IModelGeometry getGeometry()
	{
		return geometry;
	}
}
