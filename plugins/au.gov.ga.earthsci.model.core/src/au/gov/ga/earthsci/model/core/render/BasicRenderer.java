package au.gov.ga.earthsci.model.core.render;

import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLUniformData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.model.geometry.IMeshGeometry;
import au.gov.ga.earthsci.model.geometry.IModelGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexBasedGeometry;
import au.gov.ga.earthsci.model.geometry.IVertexColouredGeometry;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.AbstractVBO;

import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

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

	private static final String FRAGMENT_SHADER = "BasicRenderer.fp"; //$NON-NLS-1$
	private static final String VERTEX_SHADER = "BasicRenderer.vp"; //$NON-NLS-1$
	private static final String VE = "ve"; //$NON-NLS-1$
	private static final String ES = "es"; //$NON-NLS-1$
	private static final String RADIUS = "radius"; //$NON-NLS-1$

	private VerticalExaggerationService veService = VerticalExaggerationService.INSTANCE;
	private WorldWindowRegistry wwRegistry;
	private IVertexBasedGeometry geometry;

	private AbstractVBO<?> vertexVBO;
	private AbstractVBO<?> edgesVBO;

	private Integer renderMode;

	private ShaderState shaderState;
	private ShaderProgram shaderProgram;
	private ShaderCode vertexShader;
	private ShaderCode fragmentShader;

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
		if (edgesVBO == null && geometry instanceof IMeshGeometry && ((IMeshGeometry) geometry).hasEdgeIndices())
		{
			edgesVBO = ModelDataVBO.createIndexVBO(((IMeshGeometry) geometry).getEdgeIndices());
		}
		if (renderMode == null)
		{
			renderMode = getModeForGeometry();
		}

		OGLStackHandler stack = new OGLStackHandler();
		stack.pushAttrib(gl, GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT | GL2.GL_POLYGON_BIT);
		stack.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
		try
		{
			initShader(gl);

			shaderState.useProgram(gl, true);

			Globe globe = wwRegistry.getRenderingView().getGlobe();
			boolean uniformsSet = true;
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(RADIUS, (float) globe.getRadius()));
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(ES, (float) globe.getEccentricitySquared()));
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(VE, (float) veService.get()));
			if (!uniformsSet)
			{
				throw new IllegalStateException("Uniforms not set correctly."); //$NON-NLS-1$
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
		}
		finally
		{
			shaderState.useProgram(gl, false);
			stack.pop(gl);
		}
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
			case GL2.GL_STACK_OVERFLOW:
				buf.append("GL_STACK_OVERFLOW "); //$NON-NLS-1$
				break;
			case GL2.GL_STACK_UNDERFLOW:
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

	private void initShader(GL2 gl)
	{
		// TODO: Move shader codes to a repository so the same shader can be shared amongst programs
		try
		{
			if (shaderProgram == null)
			{
				vertexShader = ShaderCode.create(gl, GL2.GL_VERTEX_SHADER, 1, getClass(),
						new String[] { VERTEX_SHADER }, false);

				fragmentShader = ShaderCode.create(gl, GL2.GL_FRAGMENT_SHADER, 1, getClass(),
						new String[] { FRAGMENT_SHADER }, false);

				shaderProgram = new ShaderProgram();
				shaderProgram.add(gl, vertexShader, null);
				shaderProgram.add(gl, fragmentShader, null);
			}

			if (shaderState == null)
			{
				shaderState = new ShaderState();
			}

			shaderState.attachShaderProgram(gl, shaderProgram, true);
		}
		catch (Exception e)
		{
			logger.error("Unable to initialise shader", e); //$NON-NLS-1$
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

	@Override
	public IModelGeometry getGeometry()
	{
		return geometry;
	}
}
