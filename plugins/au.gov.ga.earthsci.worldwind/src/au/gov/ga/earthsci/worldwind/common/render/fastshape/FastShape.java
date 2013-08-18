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
package au.gov.ga.earthsci.worldwind.common.render.fastshape;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.WWTexture;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import au.gov.ga.earthsci.worldwind.common.layers.Bounded;
import au.gov.ga.earthsci.worldwind.common.layers.Wireframeable;

import com.jogamp.opengl.util.texture.Texture;

/**
 * The FastShape class is a representation of a piece of geometry. It is useful
 * for meshes or points or lines with a large number of vertices, as the vertex
 * positions aren't updated every frame (instead they are updated in a vertex
 * updater thread).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FastShape implements OrderedRenderable, Cacheable, Bounded, Wireframeable
{
	protected final static SingleTaskRunner VertexUpdater = new SingleTaskRunner(FastShape.class.getName()
			+ " VertexUpdater"); //$NON-NLS-1$
	protected final static SingleTaskRunner IndexUpdater = new SingleTaskRunner(FastShape.class.getName()
			+ " IndexUpdater"); //$NON-NLS-1$

	protected final ReadWriteLock positionLock = new ReentrantReadWriteLock();
	protected final PickSupport pickSupport = new PickSupport();
	protected Layer pickLayer = null;

	protected List<Position> positions;
	protected ReadWriteLock positionsLock = new ReentrantReadWriteLock();
	protected String name = "Shape";
	protected final int mode;

	//calculated:
	protected final FloatVBO vertexVBO = new FloatVBO(3);
	protected final FloatVBO normalVBO = new FloatVBO(3);
	protected final IntIndexVBO sortedIndexVBO = new IntIndexVBO();

	//set:
	protected final IntIndexVBO indexVBO = new IntIndexVBO();
	protected final FloatVBO colorVBO = new FloatVBO(3);
	protected final FloatVBO pickingColorVBO = new FloatVBO(3);
	protected final FloatVBO textureCoordinateVBO = new FloatVBO(2);

	protected boolean useOrderedRendering = false;
	protected double distanceFromEye = 0;
	protected double alphaForOrderedRenderingMode;

	protected Sphere boundingSphere;
	protected Sphere modBoundingSphere;
	protected Sector sector;

	protected boolean colorBufferEnabled = true;
	protected Color color = Color.white;
	protected double opacity = 1;
	protected boolean followTerrain = false;
	protected long followTerrainUpdateFrequency = 2000; //ms

	protected Globe lastGlobe = null;
	protected boolean verticesDirty = true;
	protected Vec4 lastEyePoint = null;
	protected double lastVerticalExaggeration = -Double.MAX_VALUE;

	protected double elevation = 0d;
	protected boolean elevationChanged = false;
	protected boolean calculateNormals = false;
	protected boolean reverseNormals = false;
	protected boolean fogEnabled = false;
	protected boolean wireframe = false;
	protected boolean lighted = false;
	protected boolean sortTransparentPrimitives = true;
	protected boolean forceSortedPrimitives = false;
	protected boolean backfaceCulling = false;
	protected boolean enabled = true;
	protected boolean twoSidedLighting = false;

	protected Double lineWidth;
	protected Double pointSize;
	protected Double pointMinSize;
	protected Double pointMaxSize;
	protected Double pointConstantAttenuation;
	protected Double pointLinearAttenuation;
	protected Double pointQuadraticAttenuation;
	protected boolean pointSprite = false;

	protected URL pointTextureUrl;
	protected WWTexture pointTexture;
	protected WWTexture blankTexture;
	protected boolean textured = true; //only actually textured if texture is not null
	protected Texture texture;
	protected double[] textureMatrix;

	protected Layer lastLayer;
	protected long lastFollowTerrainUpdateTime;

	protected final List<FastShapeRenderListener> renderListeners = new ArrayList<FastShapeRenderListener>();

	public FastShape(List<Position> positions, int mode)
	{
		this(positions, null, mode);
	}

	public FastShape(List<Position> positions, int[] indices, int mode)
	{
		this.mode = mode;
		setPositions(positions);
		setIndices(indices);
	}

	@Override
	public double getDistanceFromEye()
	{
		return distanceFromEye;
	}

	@Override
	public void pick(DrawContext dc, Point pickPoint)
	{
		if (useOrderedRendering && !dc.isOrderedRenderingMode())
		{
			render(dc);
			return;
		}

		Color color = getColor();
		boolean lighted = isLighted();
		boolean textured = isTextured();
		boolean deepPicking = dc.isDeepPickingEnabled();

		try
		{
			Color uniqueColor = dc.getUniquePickColor();
			pickSupport.clearPickList();
			pickSupport.addPickableObject(uniqueColor.getRGB(), this);
			setColor(uniqueColor);
			setLighted(false);
			setTextured(false);
			dc.setDeepPickingEnabled(true);

			try
			{
				pickSupport.beginPicking(dc);
				render(dc);
			}
			finally
			{
				pickSupport.endPicking(dc);
				pickSupport.resolvePick(dc, pickPoint, pickLayer);
			}
		}
		finally
		{
			setColor(color);
			setLighted(lighted);
			setTextured(textured);
			dc.setDeepPickingEnabled(deepPicking);
		}
	}

	@Override
	public void render(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		if (!dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
		{
			String message = "Vertex Buffer Objects are disabled or unsupported by your graphics card."; //$NON-NLS-1$
			Logging.logger().severe(message);
			setEnabled(false);
			return;
		}

		if (dc.getCurrentLayer() != null)
		{
			lastLayer = dc.getCurrentLayer();
		}

		//Store all the parameters locally, so they don't change in the middle of rendering.
		//This means we don't have to lock the writeLock when changing render parameters.
		boolean backfaceCulling = isBackfaceCulling();
		Sphere boundingSphere = this.boundingSphere;
		Color color = getColor();
		boolean colorBufferEnabled = isColorBufferEnabled();
		boolean fogEnabled = isFogEnabled();
		boolean forceSortedPrimitives = isForceSortedPrimitives();
		boolean lighted = isLighted();
		Double lineWidth = getLineWidth();
		int mode = getMode();
		Double pointConstantAttenuation = getPointConstantAttenuation();
		Double pointLinearAttenuation = getPointLinearAttenuation();
		Double pointQuadraticAttenuation = getPointQuadraticAttenuation();
		Double pointMinSize = getPointMinSize();
		Double pointMaxSize = getPointMaxSize();
		Double pointSize = getPointSize();
		boolean pointSprite = isPointSprite();
		WWTexture pointTexture = this.pointTexture;
		URL pointTextureUrl = getPointTextureUrl();
		boolean sortTransparentPrimitives = isSortTransparentPrimitives();
		Texture texture = getTexture();
		boolean textured = isTextured();
		double[] textureMatrix = getTextureMatrix();
		boolean twoSidedLighting = isTwoSidedLighting();
		boolean wireframe = isWireframe();
		boolean willCalculateNormals = willCalculateNormals();
		boolean useOrderedRenderingMode = isUseOrderedRendering();

		double alpha = getOpacity();
		if (dc.getCurrentLayer() != null)
		{
			alpha *= dc.getCurrentLayer().getOpacity();
		}

		recalculateIfRequired(dc, alpha);

		if (positions.isEmpty() || vertexVBO.getBuffer() == null)
		{
			return;
		}

		if (boundingSphere == null || !dc.getView().getFrustumInModelCoordinates().intersects(boundingSphere))
		{
			return;
		}

		if (useOrderedRenderingMode)
		{
			if (!dc.isOrderedRenderingMode())
			{
				alphaForOrderedRenderingMode = alpha;
				pickLayer = dc.getCurrentLayer();
				dc.addOrderedRenderable(this);
				return;
			}
			alpha = alphaForOrderedRenderingMode;
		}

		if (dc.isPickingMode())
		{
			alpha = 1;
		}

		GL2 gl = dc.getGL().getGL2();
		OGLStackHandler stack = new OGLStackHandler();

		try
		{
			notifyRenderListenersOfPreRender(dc);

			boolean colorBufferContainsAlpha = colorVBO.getBuffer() != null && colorVBO.getElementStride() > 3;
			boolean willUseSortedIndices =
					(forceSortedPrimitives || (sortTransparentPrimitives && alpha < 1.0))
							&& sortedIndexVBO.getBuffer() != null;
			boolean willUsePointSprite = mode == GL2.GL_POINTS && pointSprite && pointTextureUrl != null;
			boolean willUseTextureBlending = (alpha < 1.0 || color != null) && colorBufferContainsAlpha;

			if (willUsePointSprite && pointTexture == null)
			{
				try
				{
					BufferedImage image = ImageIO.read(pointTextureUrl.openStream());
					pointTexture = new BasicWWTexture(image, true);
				}
				catch (IOException e)
				{
					e.printStackTrace();
					willUsePointSprite = false;
				}
			}
			if (willUseTextureBlending && blankTexture == null)
			{
				BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
				blankTexture = new BasicWWTexture(image, true);
			}

			int attributesToPush = GL2.GL_CURRENT_BIT | GL2.GL_POINT_BIT;
			if (!fogEnabled)
			{
				attributesToPush |= GL2.GL_FOG_BIT;
			}
			if (wireframe || backfaceCulling)
			{
				attributesToPush |= GL2.GL_POLYGON_BIT;
			}
			if (lighted)
			{
				attributesToPush |= GL2.GL_LIGHTING_BIT;
			}
			if (willUseSortedIndices)
			{
				attributesToPush |= GL2.GL_DEPTH_BUFFER_BIT;
			}
			if (lineWidth != null)
			{
				attributesToPush |= GL2.GL_LINE_BIT;
			}
			if (willUsePointSprite || willUseTextureBlending || (textured && texture != null))
			{
				attributesToPush |= GL2.GL_TEXTURE_BIT;
			}

			stack.pushAttrib(gl, attributesToPush);
			stack.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
			Vec4 referenceCenter = boundingSphere.getCenter();
			dc.getView().pushReferenceCenter(dc, referenceCenter);

			if (lineWidth != null)
			{
				gl.glLineWidth(lineWidth.floatValue());
			}
			if (pointSize != null)
			{
				gl.glPointSize(pointSize.floatValue());
			}
			if (pointMinSize != null)
			{
				gl.glPointParameterf(GL2.GL_POINT_SIZE_MIN, pointMinSize.floatValue());
			}
			if (pointMaxSize != null)
			{
				gl.glPointParameterf(GL2.GL_POINT_SIZE_MAX, pointMaxSize.floatValue());
			}
			if (pointConstantAttenuation != null || pointLinearAttenuation != null || pointQuadraticAttenuation != null)
			{
				float ca = pointConstantAttenuation != null ? pointConstantAttenuation.floatValue() : 1f;
				float la = pointLinearAttenuation != null ? pointLinearAttenuation.floatValue() : 0f;
				float qa = pointQuadraticAttenuation != null ? pointQuadraticAttenuation.floatValue() : 0f;
				gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, new float[] { ca, la, qa }, 0);
			}
			if (!fogEnabled)
			{
				gl.glDisable(GL2.GL_FOG);
			}
			if (wireframe)
			{
				gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
			}
			if (backfaceCulling)
			{
				gl.glEnable(GL2.GL_CULL_FACE);
				gl.glCullFace(GL2.GL_BACK);
			}
			if (lighted)
			{
				Vec4 cameraPosition = dc.getView().getEyePoint();
				Vec4 lightPos = cameraPosition.subtract3(referenceCenter);
				float[] lightPosition = { (float) lightPos.x, (float) lightPos.y, (float) lightPos.z, 1.0f };
				float[] lightAmbient = { 0.0f, 0.0f, 0.0f, 1.0f };
				float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
				float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
				float[] modelAmbient = { 0.3f, 0.3f, 0.3f, 1.0f };
				gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, modelAmbient, 0);
				gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition, 0);
				gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse, 0);
				gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient, 0);
				gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightSpecular, 0);
				gl.glDisable(GL2.GL_LIGHT0);
				gl.glEnable(GL2.GL_LIGHT1);
				gl.glEnable(GL2.GL_LIGHTING);
				gl.glEnable(GL2.GL_COLOR_MATERIAL);
				gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, twoSidedLighting ? GL2.GL_TRUE : GL2.GL_FALSE);
			}

			if (willUsePointSprite)
			{
				gl.glEnable(GL2.GL_POINT_SMOOTH);
				gl.glEnable(GL2.GL_POINT_SPRITE);

				//stage 0: previous (color) * texture

				gl.glActiveTexture(GL2.GL_TEXTURE0);
				gl.glEnable(GL2.GL_TEXTURE_2D);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
				gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);

				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);

				//TODO consider (instead of 2 calls above):
				//gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
				//gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_SRC1_RGB, GL.GL_TEXTURE);
				//gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_MODULATE);

				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PREVIOUS);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2GL3.GL_SRC1_ALPHA, GL2.GL_TEXTURE);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);

				pointTexture.bind(dc);
			}

			if (willUseTextureBlending)
			{
				float r = 1, g = 1, b = 1;
				if (color != null)
				{
					r = color.getRed() / 255f;
					g = color.getGreen() / 255f;
					b = color.getBlue() / 255f;
				}

				//stage 1: previous (color) * texture envionment color

				gl.glActiveTexture(willUsePointSprite ? GL2.GL_TEXTURE1 : GL2.GL_TEXTURE0);
				gl.glEnable(GL2.GL_TEXTURE_2D);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);

				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC1_RGB, GL2.GL_CONSTANT);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_MODULATE);

				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_ALPHA, GL2.GL_PREVIOUS);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2GL3.GL_SRC1_ALPHA, GL2.GL_CONSTANT);
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GL2.GL_MODULATE);

				gl.glTexEnvfv(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_COLOR, new float[] { r, g, b, (float) alpha }, 0);
				blankTexture.bind(dc);
			}

			if (textured && texture != null)
			{
				gl.glActiveTexture(GL2.GL_TEXTURE0);
				gl.glEnable(GL2.GL_TEXTURE_2D);
				texture.bind(gl);
			}

			if (textureMatrix != null && textureMatrix.length >= 16)
			{
				stack.pushTexture(gl);
				gl.glLoadMatrixd(textureMatrix, 0);
			}

			if (colorBufferEnabled)
			{
				FloatVBO vbo = dc.isPickingMode() && pickingColorVBO.getBuffer() != null ? pickingColorVBO : colorVBO;
				if (vbo.getBuffer() != null)
				{
					gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
					vbo.bind(gl);
					gl.glColorPointer(vbo.getElementStride(), GL2.GL_FLOAT, 0, 0);
				}
			}

			if (color != null && !willUseTextureBlending)
			{
				float r = color.getRed() / 255f, g = color.getGreen() / 255f, b = color.getBlue() / 255f;
				gl.glColor4f(r, g, b, (float) alpha);
			}

			if (textureCoordinateVBO.getBuffer() != null)
			{
				gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
				textureCoordinateVBO.bind(gl);
				gl.glTexCoordPointer(textureCoordinateVBO.getElementStride(), GL2.GL_FLOAT, 0, 0);
			}

			if (alpha < 1.0 || colorBufferContainsAlpha || willUseSortedIndices)
			{
				gl.glEnable(GL2.GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			}

			gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
			vertexVBO.bind(gl);
			gl.glVertexPointer(vertexVBO.getElementStride(), GL2.GL_FLOAT, 0, 0);

			if (willCalculateNormals)
			{
				gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
				normalVBO.bind(gl);
				gl.glNormalPointer(GL2.GL_FLOAT, 0, 0);
			}

			if (willUseSortedIndices)
			{
				gl.glDepthMask(false);
				sortedIndexVBO.bind(gl);
				gl.glDrawElements(mode, sortedIndexVBO.getBuffer().length, GL2.GL_UNSIGNED_INT, 0);
			}
			else if (indexVBO.getBuffer() != null)
			{
				indexVBO.bind(gl);
				gl.glDrawElements(mode, indexVBO.getBuffer().length, GL2.GL_UNSIGNED_INT, 0);
			}
			else
			{
				gl.glDrawArrays(mode, 0, vertexVBO.getBuffer().length / vertexVBO.getElementStride());
			}

			//unbind the buffers
			gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
			gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		finally
		{
			stack.pop(gl);
			dc.getView().popReferenceCenter(dc);

			notifyRenderListenersOfPostRender(dc);
		}
	}

	protected void recalculateIfRequired(DrawContext dc, double alpha)
	{
		boolean followTerrainRecalculationRequired = false;
		if (followTerrain)
		{
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastFollowTerrainUpdateTime > getFollowTerrainUpdateFrequency())
			{
				lastFollowTerrainUpdateTime = currentTime;
				followTerrainRecalculationRequired = true;
			}
		}

		boolean recalculateVertices =
				followTerrainRecalculationRequired || elevationChanged || verticesDirty || lastGlobe != dc.getGlobe()
						|| lastVerticalExaggeration != dc.getVerticalExaggeration();
		if (recalculateVertices)
		{
			boolean willRecalculate = recalculateVertices(dc, false);
			if (willRecalculate)
			{
				lastGlobe = dc.getGlobe();
				verticesDirty = false;
				elevationChanged = false;
				lastVerticalExaggeration = dc.getVerticalExaggeration();
			}
		}

		Vec4 eyePoint = dc.getView().getEyePoint();
		boolean recalculateIndices =
				(forceSortedPrimitives || (sortTransparentPrimitives && alpha < 1.0))
						&& (mode == GL2.GL_TRIANGLES || mode == GL2.GL_POINTS) && !eyePoint.equals(lastEyePoint);
		if (recalculateIndices)
		{
			lastEyePoint = eyePoint;
			resortIndices(dc, lastEyePoint);
		}
	}

	protected boolean recalculateVertices(final DrawContext dc, boolean runNow)
	{
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				positionsLock.readLock().lock();
				try
				{
					int size = positions.size() * 3;
					float[] vertices;

					vertexVBO.lock();
					try
					{
						vertices = vertexVBO.getBuffer();
						if (vertices == null || vertices.length != size)
						{
							vertices = new float[size];
						}
						calculateVertices(dc, vertices);
						vertexVBO.setBuffer(vertices);
					}
					finally
					{
						vertexVBO.unlock();
					}

					if (willCalculateNormals())
					{
						normalVBO.lock();
						try
						{
							float[] normals = normalVBO.getBuffer();
							if (normals == null || normals.length != size)
							{
								normals = new float[size];
							}
							calculateNormals(vertices, normals);
							normalVBO.setBuffer(normals);
						}
						finally
						{
							normalVBO.unlock();
						}
					}

					Sphere temp = boundingSphere;
					boundingSphere = modBoundingSphere;
					modBoundingSphere = temp;
				}
				finally
				{
					positionsLock.readLock().unlock();
				}

				//when the vertices have been recalculated, trigger a render of the layer
				if (lastLayer != null)
				{
					lastLayer.firePropertyChange(AVKey.LAYER, null, lastLayer);
				}
			}
		};

		if (runNow)
		{
			runnable.run();
			return true;
		}
		else
		{
			return VertexUpdater.run(this, runnable);
		}
	}

	protected synchronized void calculateVertices(DrawContext dc, float[] vertices)
	{
		int index = 0;
		for (LatLon position : positions)
		{
			Vec4 v = calculateVertex(dc, position);
			vertices[index++] = (float) v.x;
			vertices[index++] = (float) v.y;
			vertices[index++] = (float) v.z;
		}

		BufferWrapper wrapper = new BufferWrapper.FloatBufferWrapper(FloatBuffer.wrap(vertices));
		modBoundingSphere = createBoundingSphere(wrapper);

		//prevent NullPointerExceptions when there's no vertices:
		if (modBoundingSphere == null)
		{
			modBoundingSphere = new Sphere(Vec4.ZERO, 1);
		}

		for (int i = 0; i < vertices.length; i += 3)
		{
			vertices[i + 0] -= (float) modBoundingSphere.getCenter().x;
			vertices[i + 1] -= (float) modBoundingSphere.getCenter().y;
			vertices[i + 2] -= (float) modBoundingSphere.getCenter().z;
		}
	}

	protected Vec4 calculateVertex(DrawContext dc, LatLon position)
	{
		double elevation = this.elevation;
		if (followTerrain)
		{
			elevation += dc.getGlobe().getElevation(position.getLatitude(), position.getLongitude());
		}
		elevation += calculateElevationOffset(position);
		elevation *= dc.getVerticalExaggeration();
		elevation = Math.max(elevation, -dc.getGlobe().getMaximumRadius());
		return dc.getGlobe().computePointFromPosition(position.add(calculateLatLonOffset()), elevation);
	}

	protected double calculateElevationOffset(LatLon position)
	{
		if (position instanceof Position)
		{
			return ((Position) position).elevation;
		}
		return 0;
	}

	protected LatLon calculateLatLonOffset()
	{
		return LatLon.ZERO;
	}

	protected static Sphere createBoundingSphere(BufferWrapper wrapper)
	{
		//the Sphere.createBoundingSphere() function doesn't ensure that the radius is at least 1, causing errors
		Vec4[] extrema = Vec4.computeExtrema(wrapper);
		if (extrema == null)
		{
			return null;
		}
		Vec4 center =
				new Vec4((extrema[0].x + extrema[1].x) / 2.0, (extrema[0].y + extrema[1].y) / 2.0,
						(extrema[0].z + extrema[1].z) / 2.0);
		double radius = Math.max(1, extrema[0].distanceTo3(extrema[1]) / 2.0);
		return new Sphere(center, radius);
	}

	protected void calculateNormals(float[] vertices, float[] normals)
	{
		int size = normals.length / 3;
		int[] count = new int[size];
		Vec4[] verts = new Vec4[size];
		Vec4[] norms = new Vec4[size];

		for (int i = 0, j = 0; i < vertices.length; i += 3, j++)
		{
			verts[j] = new Vec4(vertices[i + 0], vertices[i + 1], vertices[i + 2]);
			norms[j] = new Vec4(0);
		}

		int[] indices = indexVBO.getBuffer();
		boolean hasIndices = indices != null;
		int loopLimit = hasIndices ? indices.length : size;
		int loopIncrement = 3;
		if (mode == GL2.GL_TRIANGLE_STRIP)
		{
			loopLimit -= 2;
			loopIncrement = 1;
		}

		for (int i = 0; i < loopLimit; i += loopIncrement)
		{
			//don't touch indices's position/mark, because it may currently be in use by OpenGL thread
			int index0 = hasIndices ? indices[i + 0] : i + 0;
			int index1 = hasIndices ? indices[i + 1] : i + 1;
			int index2 = hasIndices ? indices[i + 2] : i + 2;
			Vec4 v0 = verts[index0];
			Vec4 v1 = verts[index1];
			Vec4 v2 = verts[index2];

			Vec4 e1 = v1.subtract3(v0);
			Vec4 e2 = mode == GL2.GL_TRIANGLE_STRIP && i % 2 == 0 ? v0.subtract3(v2) : v2.subtract3(v0);
			Vec4 N = reverseNormals ? e2.cross3(e1).normalize3() : e1.cross3(e2).normalize3();

			// if N is 0, the triangle is degenerate
			if (N.getLength3() > 0)
			{
				norms[index0] = norms[index0].add3(N);
				norms[index1] = norms[index1].add3(N);
				norms[index2] = norms[index2].add3(N);

				count[index0]++;
				count[index1]++;
				count[index2]++;
			}
		}

		for (int i = 0, j = 0; i < normals.length; i += 3, j++)
		{
			int c = count[j] > 0 ? count[j] : 1; //prevent divide by zero
			normals[i + 0] = (float) norms[j].x / c;
			normals[i + 1] = (float) norms[j].y / c;
			normals[i + 2] = (float) norms[j].z / c;
		}
	}

	protected synchronized void resortIndices(final DrawContext dc, final Vec4 eyePoint)
	{
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				float[] vertices = vertexVBO.getBuffer();
				if (vertices == null)
				{
					return;
				}

				int[] indices = indexVBO.getBuffer();
				int size = indices != null ? indices.length : vertices.length / 3;
				sortedIndexVBO.lock();
				try
				{
					int[] sortedIndices = sortedIndexVBO.getBuffer();
					if (sortedIndices == null || sortedIndices.length != size)
					{
						sortedIndices = new int[size];
					}
					sortIndices(dc, eyePoint, vertices, indices, sortedIndices);
					sortedIndexVBO.setBuffer(sortedIndices);
				}
				finally
				{
					sortedIndexVBO.unlock();
				}
			}
		};

		IndexUpdater.run(this, runnable);
	}

	protected void sortIndices(DrawContext dc, Vec4 eyePoint, float[] vertices, int[] indices, int[] sortedIndices)
	{
		int size = vertices.length / 3;
		Vec4[] verts = new Vec4[size];

		for (int i = 0, j = 0; i < vertices.length; i += 3, j++)
		{
			verts[j] = new Vec4(vertices[i + 0], vertices[i + 1], vertices[i + 2]);
		}

		if (boundingSphere != null)
		{
			eyePoint = eyePoint.subtract3(boundingSphere.getCenter());
		}

		if (mode == GL2.GL_TRIANGLES)
		{
			boolean hasIndices = indices != null;
			int triangleCountBy3 = hasIndices ? indices.length : size;
			IndexAndDistance[] distances = new IndexAndDistance[triangleCountBy3 / 3];

			for (int i = 0, j = 0; i < triangleCountBy3; i += 3, j++)
			{
				int index0 = hasIndices ? indices[i + 0] : i + 0;
				int index1 = hasIndices ? indices[i + 1] : i + 1;
				int index2 = hasIndices ? indices[i + 2] : i + 2;
				Vec4 v0 = verts[index0];
				Vec4 v1 = verts[index1];
				Vec4 v2 = verts[index2];
				double distance =
						v0.distanceToSquared3(eyePoint) + v1.distanceToSquared3(eyePoint)
								+ v2.distanceToSquared3(eyePoint);
				distances[j] = new IndexAndDistance(distance, i);
			}

			Arrays.sort(distances);
			IndexAndDistance closest = distances[distances.length - 1];
			Vec4 closestPoint = verts[hasIndices ? indices[closest.index] : closest.index];
			distanceFromEye = closestPoint.distanceTo3(eyePoint);

			for (int i = 0, j = 0; i < triangleCountBy3; i += 3, j++)
			{
				IndexAndDistance distance = distances[j];
				sortedIndices[i + 0] = hasIndices ? indices[distance.index + 0] : distance.index + 0;
				sortedIndices[i + 1] = hasIndices ? indices[distance.index + 1] : distance.index + 1;
				sortedIndices[i + 2] = hasIndices ? indices[distance.index + 2] : distance.index + 2;
			}
		}
		else if (mode == GL2.GL_POINTS)
		{
			IndexAndDistance[] distances = new IndexAndDistance[size];
			for (int i = 0; i < size; i++)
			{
				double distance = verts[i].distanceToSquared3(eyePoint);
				distances[i] = new IndexAndDistance(distance, i);
			}

			Arrays.sort(distances);
			IndexAndDistance closest = distances[distances.length - 1];
			Vec4 closestPoint = verts[closest.index];
			distanceFromEye = closestPoint.distanceTo3(eyePoint);

			for (int i = 0; i < size; i++)
			{
				sortedIndices[i] = distances[i].index;
			}
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public float[] getColorBuffer()
	{
		return colorVBO.getBuffer();
	}

	public void setColorBuffer(float[] colorBuffer)
	{
		colorVBO.setBuffer(colorBuffer);
	}

	public int getColorBufferElementSize()
	{
		return colorVBO.getElementStride();
	}

	public void setColorBufferElementSize(int colorBufferElementSize)
	{
		colorVBO.setElementStride(colorBufferElementSize);
	}

	public float[] getPickingColorBuffer()
	{
		return pickingColorVBO.getBuffer();
	}

	public void setPickingColorBuffer(float[] pickingColorBuffer)
	{
		pickingColorVBO.setBuffer(pickingColorBuffer);
	}

	public int getPickingColorBufferElementSize()
	{
		return pickingColorVBO.getElementStride();
	}

	public void setPickingColorBufferElementSize(int pickingColorBufferElementSize)
	{
		pickingColorVBO.setElementStride(pickingColorBufferElementSize);
	}

	public boolean isColorBufferEnabled()
	{
		return colorBufferEnabled;
	}

	public void setColorBufferEnabled(boolean useColorBuffer)
	{
		this.colorBufferEnabled = useColorBuffer;
	}

	public float[] getTextureCoordinateBuffer()
	{
		return textureCoordinateVBO.getBuffer();
	}

	public void setTextureCoordinateBuffer(float[] textureCoordinateBuffer)
	{
		textureCoordinateVBO.setBuffer(textureCoordinateBuffer);
	}

	public double getOpacity()
	{
		return opacity;
	}

	public void setOpacity(double opacity)
	{
		this.opacity = opacity;
	}

	public List<Position> getPositions()
	{
		return positions;
	}

	public void setPositions(List<Position> positions)
	{
		positionsLock.writeLock().lock();
		try
		{
			this.positions = positions;
			verticesDirty = true;

			sector = null;
			for (Position position : positions)
			{
				sector =
						sector != null ? sector.union(position.latitude, position.longitude) : new Sector(
								position.latitude, position.latitude, position.longitude, position.longitude);
			}
		}
		finally
		{
			positionsLock.writeLock().unlock();
		}
	}

	public int[] getIndices()
	{
		return indexVBO.getBuffer();
	}

	public void setIndices(int[] indices)
	{
		indexVBO.setBuffer(indices);
	}

	public boolean isFollowTerrain()
	{
		return followTerrain;
	}

	public void setFollowTerrain(boolean followTerrain)
	{
		this.followTerrain = followTerrain;
		verticesDirty = true;
	}

	public long getFollowTerrainUpdateFrequency()
	{
		return followTerrainUpdateFrequency;
	}

	public void setFollowTerrainUpdateFrequency(long followTerrainUpdateFrequency)
	{
		this.followTerrainUpdateFrequency = followTerrainUpdateFrequency;
	}

	public int getMode()
	{
		return mode;
	}

	public double getElevation()
	{
		return elevation;
	}

	public void setElevation(double elevation)
	{
		elevationChanged = this.elevation != elevation;
		this.elevation = elevation;
	}

	public boolean isCalculateNormals()
	{
		return calculateNormals;
	}

	public void setCalculateNormals(boolean calculateNormals)
	{
		this.calculateNormals = calculateNormals;
	}

	public boolean isReverseNormals()
	{
		return reverseNormals;
	}

	public void setReverseNormals(boolean reverseNormals)
	{
		this.reverseNormals = reverseNormals;
		verticesDirty = true;
	}

	protected boolean willCalculateNormals()
	{
		return isCalculateNormals() && (getMode() == GL2.GL_TRIANGLES || getMode() == GL2.GL_TRIANGLE_STRIP);
	}

	public boolean isFogEnabled()
	{
		return fogEnabled;
	}

	public void setFogEnabled(boolean fogEnabled)
	{
		this.fogEnabled = fogEnabled;
	}

	@Override
	public boolean isWireframe()
	{
		return wireframe;
	}

	@Override
	public void setWireframe(boolean wireframe)
	{
		this.wireframe = wireframe;
	}

	public boolean isBackfaceCulling()
	{
		return backfaceCulling;
	}

	public void setBackfaceCulling(boolean backfaceCulling)
	{
		this.backfaceCulling = backfaceCulling;
	}

	public boolean isLighted()
	{
		return lighted;
	}

	public void setLighted(boolean lighted)
	{
		this.lighted = lighted;
	}

	public boolean isTwoSidedLighting()
	{
		return twoSidedLighting;
	}

	public void setTwoSidedLighting(boolean twoSidedLighting)
	{
		this.twoSidedLighting = twoSidedLighting;
	}

	public boolean isSortTransparentPrimitives()
	{
		return sortTransparentPrimitives;
	}

	public void setSortTransparentPrimitives(boolean sortTransparentPrimitives)
	{
		this.sortTransparentPrimitives = sortTransparentPrimitives;
	}

	public boolean isForceSortedPrimitives()
	{
		return forceSortedPrimitives;
	}

	public void setForceSortedPrimitives(boolean forceSortedPrimitives)
	{
		this.forceSortedPrimitives = forceSortedPrimitives;
	}

	public Double getLineWidth()
	{
		return lineWidth;
	}

	public void setLineWidth(Double lineWidth)
	{
		this.lineWidth = lineWidth;
	}

	public Double getPointSize()
	{
		return pointSize;
	}

	public void setPointSize(Double pointSize)
	{
		this.pointSize = pointSize;
	}

	public Double getPointMinSize()
	{
		return pointMinSize;
	}

	public void setPointMinSize(Double pointMinSize)
	{
		this.pointMinSize = pointMinSize;
	}

	public Double getPointMaxSize()
	{
		return pointMaxSize;
	}

	public void setPointMaxSize(Double pointMaxSize)
	{
		this.pointMaxSize = pointMaxSize;
	}

	public Double getPointConstantAttenuation()
	{
		return pointConstantAttenuation;
	}

	public void setPointConstantAttenuation(Double pointConstantAttenuation)
	{
		this.pointConstantAttenuation = pointConstantAttenuation;
	}

	public Double getPointLinearAttenuation()
	{
		return pointLinearAttenuation;
	}

	public void setPointLinearAttenuation(Double pointLinearAttenuation)
	{
		this.pointLinearAttenuation = pointLinearAttenuation;
	}

	public Double getPointQuadraticAttenuation()
	{
		return pointQuadraticAttenuation;
	}

	public void setPointQuadraticAttenuation(Double pointQuadraticAttenuation)
	{
		this.pointQuadraticAttenuation = pointQuadraticAttenuation;
	}

	public boolean isPointSprite()
	{
		return pointSprite;
	}

	public void setPointSprite(boolean pointSprite)
	{
		this.pointSprite = pointSprite;
	}

	public URL getPointTextureUrl()
	{
		return pointTextureUrl;
	}

	public void setPointTextureUrl(URL pointTextureUrl)
	{
		this.pointTextureUrl = pointTextureUrl;
		pointTexture = null;
	}

	public Texture getTexture()
	{
		return texture;
	}

	public void setTexture(Texture texture)
	{
		this.texture = texture;
	}

	public boolean isTextured()
	{
		return textured;
	}

	public void setTextured(boolean textured)
	{
		this.textured = textured;
	}

	public double[] getTextureMatrix()
	{
		return textureMatrix;
	}

	public void setTextureMatrix(double[] textureMatrix)
	{
		this.textureMatrix = textureMatrix;
	}

	public boolean isUseOrderedRendering()
	{
		return useOrderedRendering;
	}

	public void setUseOrderedRendering(boolean useOrderedRendering)
	{
		this.useOrderedRendering = useOrderedRendering;
	}

	@Override
	public long getSizeInBytes()
	{
		//very approximate, measured by checking JVM memory usage over many object creations
		return 500 + 80 * getPositions().size();
	}

	/**
	 * @return The extent of this shape. This is calculated by
	 *         {@link FastShape#render(DrawContext)}, so don't use this for
	 *         frustum culling.
	 */
	public Extent getExtent()
	{
		return boundingSphere;
	}

	@Override
	public Sector getSector()
	{
		positionsLock.readLock().lock();
		try
		{
			return sector;
		}
		finally
		{
			positionsLock.readLock().unlock();
		}
	}

	public void addRenderListener(FastShapeRenderListener renderListener)
	{
		renderListeners.add(renderListener);
	}

	public void removeRenderListener(FastShapeRenderListener renderListener)
	{
		renderListeners.remove(renderListener);
	}

	protected void notifyRenderListenersOfPreRender(DrawContext dc)
	{
		for (int i = renderListeners.size() - 1; i >= 0; i--)
		{
			renderListeners.get(i).shapePreRender(dc, this);
		}
	}

	protected void notifyRenderListenersOfPostRender(DrawContext dc)
	{
		for (int i = renderListeners.size() - 1; i >= 0; i--)
		{
			renderListeners.get(i).shapePostRender(dc, this);
		}
	}

	public static float[] color4ToFloats(List<Color> colors)
	{
		return color4ToFloats(colors, new float[colors.size() * 4]);
	}

	public static float[] color4ToFloats(List<Color> colors, float[] floats)
	{
		int i = 0;
		for (Color color : colors)
		{
			floats[i++] = color.getRed() / 255f;
			floats[i++] = color.getGreen() / 255f;
			floats[i++] = color.getBlue() / 255f;
			floats[i++] = color.getAlpha() / 255f;
		}
		return floats;
	}

	public static float[] color3ToFloats(List<Color> colors)
	{
		return color3ToFloats(colors, new float[colors.size() * 3]);
	}

	public static float[] color3ToFloats(List<Color> colors, float[] floats)
	{
		int i = 0;
		for (Color color : colors)
		{
			floats[i++] = color.getRed() / 255f;
			floats[i++] = color.getGreen() / 255f;
			floats[i++] = color.getBlue() / 255f;
		}
		return floats;
	}
}
