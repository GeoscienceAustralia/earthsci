/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.view.orbit;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.view.BasicView;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import javax.media.opengl.GL2;

/**
 * Abstract implementation of the {@link View} interface.
 * <p/>
 * Largely copied from the {@link BasicView} class, but with the
 * heading/pitch/roll storage removed. Concrete subclasses can choose how to
 * store view state, as there are many different ways that this state can be
 * stored. Some examples:
 * <ul>
 * <li>center/heading/pitch/roll/zoom (like the {@link BasicOrbitView})</li>
 * <li>eye/heading/pitch/roll (like the {@link BasicFlyView})</li>
 * <li>center/rotation/distance (representing the rotation as a quaternion would
 * result in more rotation freedom)</li>
 * <li>center/eye/roll</li>
 * <li>eye/rotation</li>
 * </ul>
 * You may also choose to represent your center/eye points as {@link Position}s
 * (geographic) or {@link Vec4}s (cartesian), depending on your movement type.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("nls")
public abstract class AbstractView extends WWObjectImpl implements View
{
	/** The field of view in degrees. */
	protected Angle fieldOfView = Angle.fromDegrees(45);
	// Provide reasonable default values for the near and far clip distances. By default, BasicView automatically
	// updates these values each frame based on the current eye position relative to the surface. These default values
	// are provided for two reasons:
	// * The view can provide a reasonable value to the application until the first frame.
	// * Subclass implementations which may override the automatic update of clipping plane distances have reasonable
	//   default values to fall back on.
	protected double nearClipDistance = MINIMUM_NEAR_DISTANCE;
	protected double farClipDistance = MINIMUM_FAR_DISTANCE;
	protected Matrix modelview = Matrix.IDENTITY;
	protected Matrix modelviewInv = Matrix.IDENTITY;
	protected Matrix projection = Matrix.IDENTITY;
	protected java.awt.Rectangle viewport = new java.awt.Rectangle();
	protected Frustum frustum = new Frustum();
	protected Frustum lastFrustumInModelCoords = null;

	protected DrawContext dc;
	protected boolean detectCollisions = true;
	protected boolean hadCollisions;
	protected ViewInputHandler viewInputHandler;
	protected Globe globe;
	protected double horizonDistance;

	/**
	 * Identifier for the modelview matrix state. This number is incremented
	 * when one of the fields that affects the modelview matrix is set.
	 */
	protected long viewStateID;

	// TODO: make configurable
	protected static final double MINIMUM_NEAR_DISTANCE = 2;
	protected static final double MINIMUM_FAR_DISTANCE = 100;
	protected static final double COLLISION_THRESHOLD = 10;
	protected static final int COLLISION_NUM_ITERATIONS = 4;


	@Override
	public Globe getGlobe()
	{
		return this.globe;
	}

	/**
	 * Set the globe associated with this view. Note that the globe is reset
	 * each frame.
	 * 
	 * @param globe
	 *            New globe.
	 */
	public void setGlobe(Globe globe)
	{
		this.globe = globe;
	}

	public DrawContext getDC()
	{
		return (this.dc);
	}

	@Override
	public ViewInputHandler getViewInputHandler()
	{
		return viewInputHandler;
	}

	public void setViewInputHandler(ViewInputHandler viewInputHandler)
	{
		this.viewInputHandler = viewInputHandler;
	}

	public boolean isDetectCollisions()
	{
		return this.detectCollisions;
	}

	public void setDetectCollisions(boolean detectCollisions)
	{
		this.detectCollisions = detectCollisions;
	}

	public boolean hadCollisions()
	{
		boolean result = this.hadCollisions;
		this.hadCollisions = false;
		return result;
	}

	@Override
	public void copyViewState(View view)
	{
		this.globe = view.getGlobe();
		Vec4 center = view.getCenterPoint();
		if (center == null)
		{
			Vec4 eyePoint = view.getCurrentEyePoint();
			center = eyePoint.add3(view.getForwardVector());
		}
		setOrientation(view.getCurrentEyePosition(), globe.computePositionFromPoint(center));
	}

	@Override
	public void apply(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (dc.getGlobe() == null)
		{
			String message = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		
		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();

		// Update DrawContext and Globe references.
		this.dc = dc;
		this.globe = this.dc.getGlobe();

		if (this.viewInputHandler != null)
		{
			this.viewInputHandler.apply();
		}

		doApply(dc);

		if (this.viewInputHandler != null)
		{
			this.viewInputHandler.viewApplied();
		}
	}

	protected abstract void doApply(DrawContext dc);

	@Override
	public void stopMovement()
	{
		this.firePropertyChange(VIEW_STOPPED, null, this);
	}

	@Override
	public java.awt.Rectangle getViewport()
	{
		// java.awt.Rectangle is mutable, so we defensively copy the viewport.
		return new java.awt.Rectangle(this.viewport);
	}

	@Override
	public Frustum getFrustum()
	{
		return this.frustum;
	}

	@Override
	public Frustum getFrustumInModelCoordinates()
	{
		if (this.lastFrustumInModelCoords == null)
		{
			Matrix modelviewTranspose = this.modelview.getTranspose();
			if (modelviewTranspose != null)
			{
				this.lastFrustumInModelCoords = this.frustum.transformBy(modelviewTranspose);
			}
			else
			{
				this.lastFrustumInModelCoords = this.frustum;
			}
		}
		return this.lastFrustumInModelCoords;
	}

	@Override
	public void setFieldOfView(Angle fieldOfView)
	{
		if (fieldOfView == null)
		{
			String message = Logging.getMessage("nullValue.AngleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.fieldOfView = fieldOfView;
	}

	@Override
	public double getNearClipDistance()
	{
		return this.nearClipDistance;
	}

	protected void setNearClipDistance(double clipDistance)
	{
		this.nearClipDistance = clipDistance;
	}

	@Override
	public double getFarClipDistance()
	{
		return this.farClipDistance;
	}

	protected void setFarClipDistance(double clipDistance)
	{
		this.farClipDistance = clipDistance;
	}

	@Override
	public Matrix getModelviewMatrix()
	{
		return this.modelview;
	}

	/** {@inheritDoc} */
	@Override
	public long getViewStateID()
	{
		return this.viewStateID;
	}

	@Override
	public Angle getFieldOfView()
	{
		return this.fieldOfView;
	}

	@Override
	public Vec4 project(Vec4 modelPoint)
	{
		if (modelPoint == null)
		{
			String message = Logging.getMessage("nullValue.Vec4IsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return this.project(modelPoint, this.modelview, this.projection, this.viewport);
	}

	@Override
	public Vec4 unProject(Vec4 windowPoint)
	{
		if (windowPoint == null)
		{
			String message = Logging.getMessage("nullValue.Vec4IsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		return unProject(windowPoint, this.modelview, this.projection, this.viewport);
	}

	@Override
	public void stopAnimations()
	{
		viewInputHandler.stopAnimators();
	}

	@Override
	public boolean isAnimating()
	{
		return viewInputHandler.isAnimating();
	}

	@Override
	public void goTo(Position position, double distance)
	{
		viewInputHandler.goTo(position, distance);
	}

	@Override
	public Line computeRayFromScreenPoint(double x, double y)
	{
		return ViewUtil.computeRayFromScreenPoint(this, x, y,
				this.modelview, this.projection, this.viewport);
	}

	@Override
	public Position computePositionFromScreenPoint(double x, double y)
	{
		if (this.globe != null)
		{
			Line ray = computeRayFromScreenPoint(x, y);
			if (ray != null)
			{
				return this.globe.getIntersectionPosition(ray);
			}
		}

		return null;
	}

	@Override
	public double computePixelSizeAtDistance(double distance)
	{
		return ViewUtil.computePixelSizeAtDistance(distance, this.fieldOfView, this.viewport);
	}

	protected Position computeEyePositionFromModelview()
	{
		if (this.globe != null)
		{
			Vec4 eyePoint = Vec4.UNIT_W.transformBy4(this.modelviewInv);
			return this.globe.computePositionFromPoint(eyePoint);
		}

		return Position.ZERO;
	}

	@Override
	public double getHorizonDistance()
	{
		return this.horizonDistance;
	}

	protected double computeHorizonDistance()
	{
		return this.computeHorizonDistance(computeEyePositionFromModelview());
	}

	protected double computeHorizonDistance(Position eyePosition)
	{
		if (this.globe != null && eyePosition != null)
		{
			double elevation = eyePosition.getElevation();
			double elevationAboveSurface = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
			return ViewUtil.computeHorizonDistance(this.globe, Math.max(elevation, elevationAboveSurface));
		}

		return 0;
	}

	protected double computeNearClipDistance()
	{
		return computeNearDistance(getCurrentEyePosition());
	}

	protected double computeFarClipDistance()
	{
		return computeFarDistance(getCurrentEyePosition());
	}

	protected double computeNearDistance(Position eyePosition)
	{
		double near = 0;
		if (eyePosition != null && this.dc != null)
		{
			double elevation = ViewUtil.computeElevationAboveSurface(this.dc, eyePosition);
			double tanHalfFov = this.fieldOfView.tanHalfAngle();
			near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
		}
		return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
	}

	protected double computeFarDistance(Position eyePosition)
	{
		double far = 0;
		if (eyePosition != null)
		{
			far = computeHorizonDistance(eyePosition);
		}

		return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
	}

	@Override
	public Matrix getProjectionMatrix()
	{
		return this.projection;
	}

	@Override
	public String getRestorableState()
	{
		RestorableSupport rs = RestorableSupport.newRestorableSupport();
		// Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
		if (rs == null)
		{
			return null;
		}

		this.doGetRestorableState(rs, null);

		return rs.getStateAsXml();
	}

	@Override
	public void restoreState(String stateInXml)
	{
		if (stateInXml == null)
		{
			String message = Logging.getMessage("nullValue.StringIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		RestorableSupport rs;
		try
		{
			rs = RestorableSupport.parse(stateInXml);
		}
		catch (Exception e)
		{
			// Parsing the document specified by stateInXml failed.
			String message = Logging.getMessage("generic.ExceptionAttemptingToParseStateXml", stateInXml);
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message, e);
		}

		this.doRestoreState(rs, null);
	}

	/**
	 * Update the modelview state identifier. This method should be called
	 * whenever one of the fields that affects the modelview matrix is changed.
	 */
	protected void updateModelViewStateID()
	{
		this.viewStateID++;
	}

	//**************************************************************//
	//******************** Restorable State  ***********************//
	//**************************************************************//

	protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
	{
		this.getViewPropertyLimits().getRestorableState(rs, rs.addStateObject(context, "viewPropertyLimits"));

		rs.addStateValueAsBoolean(context, "detectCollisions", this.isDetectCollisions());

		if (this.getFieldOfView() != null)
		{
			rs.addStateValueAsDouble(context, "fieldOfView", this.getFieldOfView().getDegrees());
		}

		rs.addStateValueAsDouble(context, "nearClipDistance", this.getNearClipDistance());
		rs.addStateValueAsDouble(context, "farClipDistance", this.getFarClipDistance());

		if (this.getEyePosition() != null)
		{
			rs.addStateValueAsPosition(context, "eyePosition", this.getEyePosition());
		}

		if (this.getHeading() != null)
		{
			rs.addStateValueAsDouble(context, "heading", this.getHeading().getDegrees());
		}

		if (this.getPitch() != null)
		{
			rs.addStateValueAsDouble(context, "pitch", this.getPitch().getDegrees());
		}
	}

	protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
	{
		// Restore the property limits and collision detection flags before restoring the view's position and
		// orientation. This has the effect of ensuring that the view's position and orientation are consistent with the
		// current property limits and the current surface collision state.

		RestorableSupport.StateObject so = rs.getStateObject(context, "viewPropertyLimits");
		if (so != null)
		{
			this.getViewPropertyLimits().restoreState(rs, so);
		}

		Boolean b = rs.getStateValueAsBoolean(context, "detectCollisions");
		if (b != null)
		{
			this.setDetectCollisions(b);
		}

		Double d = rs.getStateValueAsDouble(context, "fieldOfView");
		if (d != null)
		{
			this.setFieldOfView(Angle.fromDegrees(d));
		}

		d = rs.getStateValueAsDouble(context, "nearClipDistance");
		if (d != null)
		{
			this.setNearClipDistance(d);
		}

		d = rs.getStateValueAsDouble(context, "farClipDistance");
		if (d != null)
		{
			this.setFarClipDistance(d);
		}

		Position p = rs.getStateValueAsPosition(context, "eyePosition");
		if (p != null)
		{
			this.setEyePosition(p);
		}

		d = rs.getStateValueAsDouble(context, "heading");
		if (d != null)
		{
			this.setHeading(Angle.fromDegrees(d));
		}

		d = rs.getStateValueAsDouble(context, "pitch");
		if (d != null)
		{
			this.setPitch(Angle.fromDegrees(d));
		}
	}

	@Override
	public Matrix pushReferenceCenter(DrawContext dc, Vec4 referenceCenter)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (referenceCenter == null)
		{
			String message = Logging.getMessage("nullValue.PointIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Matrix modelview = getModelviewMatrix();

		// Compute a new model-view matrix with origin at referenceCenter.
		Matrix matrix = null;
		if (modelview != null)
		{
			matrix = modelview.multiply(Matrix.fromTranslation(referenceCenter));
		}

		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		// Store the current matrix-mode state.
		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TRANSFORM_BIT);

			gl.glMatrixMode(GL2.GL_MODELVIEW);

			// Push and load a new model-view matrix to the current OpenGL context held by 'dc'.
			gl.glPushMatrix();
			if (matrix != null)
			{
				double[] matrixArray = new double[16];
				matrix.toArray(matrixArray, 0, false);
				gl.glLoadMatrixd(matrixArray, 0);
			}
		}
		finally
		{
			ogsh.pop(gl);
		}

		return matrix;
	}

	@Override
	public Matrix setReferenceCenter(DrawContext dc, Vec4 referenceCenter)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (referenceCenter == null)
		{
			String message = Logging.getMessage("nullValue.PointIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Matrix modelview = getModelviewMatrix();

		// Compute a new model-view matrix with origin at referenceCenter.
		Matrix matrix = null;
		if (modelview != null)
		{
			matrix = modelview.multiply(Matrix.fromTranslation(referenceCenter));
		}
		if (matrix == null)
		{
			return null;
		}

		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		gl.glMatrixMode(GL2.GL_MODELVIEW);

		double[] matrixArray = new double[16];
		matrix.toArray(matrixArray, 0, false);
		gl.glLoadMatrixd(matrixArray, 0);

		return matrix;
	}

	/**
	 * Removes the model-view matrix on top of the matrix stack, and restores
	 * the original matrix.
	 * 
	 * @param dc
	 *            the current World Wind drawing context on which the original
	 *            matrix will be restored.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>dc</code> is null, or if the <code>Globe</code> or
	 *             <code>GL</code> instances in <code>dc</code> are null.
	 */
	@Override
	public void popReferenceCenter(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

		// Store the current matrix-mode state.
		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TRANSFORM_BIT);

			gl.glMatrixMode(GL2.GL_MODELVIEW);

			// Pop the top model-view matrix.
			gl.glPopMatrix();
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	/**
	 * Transforms the specified object coordinates into window coordinates using
	 * the given modelview and projection matrices, and viewport.
	 * 
	 * @param point
	 *            The object coordinate to transform
	 * @param modelview
	 *            The modelview matrix
	 * @param projection
	 *            The projection matrix
	 * @param viewport
	 *            The viewport
	 * 
	 * @return the transformed coordinates
	 */
	public Vec4 project(Vec4 point, Matrix modelview, Matrix projection, java.awt.Rectangle viewport)
	{
		if (point == null)
		{
			String message = Logging.getMessage("nullValue.PointIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (modelview == null || projection == null)
		{
			String message = Logging.getMessage("nullValue.MatrixIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (viewport == null)
		{
			String message = Logging.getMessage("nullValue.RectangleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// GLU expects matrices as column-major arrays.
		double[] modelviewArray = new double[16];
		double[] projectionArray = new double[16];
		modelview.toArray(modelviewArray, 0, false);
		projection.toArray(projectionArray, 0, false);
		// GLU expects the viewport as a four-component array.
		int[] viewportArray = new int[] { viewport.x, viewport.y, viewport.width, viewport.height };

		double[] result = new double[3];
		if (!this.dc.getGLU().gluProject(
				point.x, point.y, point.z,
				modelviewArray, 0,
				projectionArray, 0,
				viewportArray, 0,
				result, 0))
		{
			return null;
		}

		return Vec4.fromArray3(result, 0);
	}

	/**
	 * Maps the given window coordinates into model coordinates using the given
	 * matrices and viewport.
	 * 
	 * @param windowPoint
	 *            the window point
	 * @param modelview
	 *            the modelview matrix
	 * @param projection
	 *            the projection matrix
	 * @param viewport
	 *            the window viewport
	 * 
	 * @return the unprojected point
	 */
	public Vec4 unProject(Vec4 windowPoint, Matrix modelview, Matrix projection, java.awt.Rectangle viewport)
	{
		if (windowPoint == null)
		{
			String message = Logging.getMessage("nullValue.PointIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (modelview == null || projection == null)
		{
			String message = Logging.getMessage("nullValue.MatrixIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (viewport == null)
		{
			String message = Logging.getMessage("nullValue.RectangleIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		// GLU expects matrices as column-major arrays.
		double[] modelviewArray = new double[16];
		double[] projectionArray = new double[16];
		modelview.toArray(modelviewArray, 0, false);
		projection.toArray(projectionArray, 0, false);
		// GLU expects the viewport as a four-component array.
		int[] viewportArray = new int[] { viewport.x, viewport.y, viewport.width, viewport.height };

		double[] result = new double[3];
		if (!this.dc.getGLU().gluUnProject(
				windowPoint.x, windowPoint.y, windowPoint.z,
				modelviewArray, 0,
				projectionArray, 0,
				viewportArray, 0,
				result, 0))
		{
			return null;
		}

		return Vec4.fromArray3(result, 0);
	}

	/**
	 * Sets the the opengl modelview and projection matrices to the given
	 * matrices.
	 * 
	 * @param dc
	 *            the drawing context
	 * @param modelview
	 *            the modelview matrix
	 * @param projection
	 *            the projection matrix
	 */
	public static void loadGLViewState(DrawContext dc, Matrix modelview, Matrix projection)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}
		if (dc.getGL() == null)
		{
			String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}
		if (modelview == null)
		{
			Logging.logger().fine("nullValue.ModelViewIsNull");
		}
		if (projection == null)
		{
			Logging.logger().fine("nullValue.ProjectionIsNull");
		}

		double[] matrixArray = new double[16];

		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
		// Store the current matrix-mode state.
		OGLStackHandler ogsh = new OGLStackHandler();

		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TRANSFORM_BIT);

			// Apply the model-view matrix to the current OpenGL context.
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			if (modelview != null)
			{
				modelview.toArray(matrixArray, 0, false);
				gl.glLoadMatrixd(matrixArray, 0);
			}
			else
			{
				gl.glLoadIdentity();
			}

			// Apply the projection matrix to the current OpenGL context.
			gl.glMatrixMode(GL2.GL_PROJECTION);
			if (projection != null)
			{
				projection.toArray(matrixArray, 0, false);
				gl.glLoadMatrixd(matrixArray, 0);
			}
			else
			{
				gl.glLoadIdentity();
			}
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	/**
	 * Add an animator to the this View. The View does not start the animator.
	 * 
	 * @param animator
	 *            the {@link gov.nasa.worldwind.animation.Animator} to be added
	 */
	@Override
	public void addAnimator(Animator animator)
	{
		viewInputHandler.addAnimator(animator);
	}
}
