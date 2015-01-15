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
package au.gov.ga.earthsci.worldwind.common.view.delegate;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitView;
import au.gov.ga.earthsci.worldwind.common.render.DrawableSceneController;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.view.target.TargetOrbitView;

/**
 * {@link OrbitView} implementation of {@link IDelegateView}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DelegateOrbitView extends TargetOrbitView implements IDelegateView
{
	private IViewDelegate delegate;
	private IViewDelegate currentDelegate;
	private boolean callingBeforeComputeMatrices = false;
	private boolean callingComputeModelView = false;
	private boolean callingGetPretransformedModelView = false;
	private boolean callingComputeProjection = false;
	private boolean callingDraw = false;
	private Vec4 up = null;
	private Vec4 forward = null;
	private Matrix postTransform = Matrix.IDENTITY;

	public DelegateOrbitView()
	{
		String delegateClassName = Configuration.getStringValue(AVKeyMore.DELEGATE_VIEW_DELEGATE_CLASS_NAME);
		if (!Util.isBlank(delegateClassName))
		{
			try
			{
				IViewDelegate delegate = (IViewDelegate) WorldWind.createComponent(delegateClassName);
				setDelegate(delegate);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public IViewDelegate getDelegate()
	{
		return delegate;
	}

	@Override
	public void setDelegate(IViewDelegate delegate)
	{
		if (this.delegate != delegate)
		{
			if (this.delegate != null)
			{
				this.delegate.uninstalled(this);
			}
			this.delegate = delegate;
			if (this.delegate != null)
			{
				this.delegate.installed(this);
			}
			firePropertyChange(AVKey.VIEW, null, this);
		}
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		currentDelegate = delegate;
		super.doApply(dc);
		postTransform = modelviewInv.multiply(getPretransformedModelView());
		up = null;
		forward = null;
	}

	@Override
	public Vec4 getUpVector()
	{
		if (up == null)
		{
			up = super.getUpVector();
			up = up.transformBy3(postTransform);
		}
		return up;
	}

	@Override
	public Vec4 getForwardVector()
	{
		if (forward == null)
		{
			forward = super.getForwardVector();
			forward = forward.transformBy3(postTransform);
		}
		return forward;
	}

	@Override
	public void beforeComputeMatrices()
	{
		if (currentDelegate == null || callingBeforeComputeMatrices)
		{
			super.beforeComputeMatrices();
			return;
		}

		try
		{
			callingBeforeComputeMatrices = true;
			currentDelegate.beforeComputeMatrices(this);
		}
		finally
		{
			callingBeforeComputeMatrices = false;
		}
	}

	@Override
	public Matrix computeModelView()
	{
		if (currentDelegate == null || callingComputeModelView)
		{
			return super.computeModelView();
		}

		try
		{
			callingComputeModelView = true;
			return currentDelegate.computeModelView(this);
		}
		finally
		{
			callingComputeModelView = false;
		}
	}

	@Override
	public Matrix getPretransformedModelView()
	{
		if (currentDelegate == null || callingGetPretransformedModelView)
		{
			return computeModelView();
		}

		try
		{
			callingGetPretransformedModelView = true;
			return currentDelegate.getPretransformedModelView(this);
		}
		finally
		{
			callingGetPretransformedModelView = false;
		}
	}

	@Override
	public Matrix computeProjection(double nearDistance, double farDistance)
	{
		return computeProjection(getFieldOfView(), nearDistance, farDistance);
	}

	@Override
	public Matrix computeProjection(Angle horizontalFieldOfView, double nearDistance, double farDistance)
	{
		if (currentDelegate == null || callingComputeProjection)
		{
			return super.computeProjection(horizontalFieldOfView, nearDistance, farDistance);
		}

		try
		{
			callingComputeProjection = true;
			return currentDelegate.computeProjection(this, horizontalFieldOfView, nearDistance, farDistance);
		}
		finally
		{
			callingComputeProjection = false;
		}
	}

	@Override
	public void draw(DrawContext dc, DrawableSceneController sc)
	{
		if (currentDelegate == null || callingDraw)
		{
			sc.draw(dc);
			return;
		}

		try
		{
			callingDraw = true;
			currentDelegate.draw(this, dc, sc);
		}
		finally
		{
			callingDraw = false;
		}
	}
}
