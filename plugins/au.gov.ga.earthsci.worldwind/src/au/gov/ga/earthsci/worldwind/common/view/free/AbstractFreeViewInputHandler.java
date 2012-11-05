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
package au.gov.ga.earthsci.worldwind.common.view.free;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.animation.AnimationController;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.Point;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract implementation of the {@link FreeViewInputHandler} interface;
 * implements much of the {@link ViewInputHandler} methods, but not the
 * mouse/keyboard input handling.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractFreeViewInputHandler implements FreeViewInputHandler, PropertyChangeListener
{
	private WorldWindow wwd;
	private boolean enableSmoothing;
	private boolean lockHeading;
	private boolean stopOnFocusLost;
	private double dragSlopeFactor;
	private ViewInputAttributes attributes;

	private static final String VIEW_ANIM_APP = "ViewAnimApp";
	private AnimationController animControl = new AnimationController();

	@Override
	public WorldWindow getWorldWindow()
	{
		return this.wwd;
	}

	@Override
	public void setWorldWindow(WorldWindow newWorldWindow)
	{
		if (newWorldWindow == this.wwd)
		{
			return;
		}

		if (this.wwd != null)
		{
			//this.wwd.removeRenderingListener(this);
			this.wwd.getSceneController().removePropertyChangeListener(this);
		}

		this.wwd = newWorldWindow;

		if (this.wwd != null)
		{
			//this.wwd.addRenderingListener(this);
			this.wwd.getSceneController().addPropertyChangeListener(this);
		}
	}

	protected View getView()
	{
		return wwd == null ? null : wwd.getView();
	}

	@Override
	public boolean isEnableSmoothing()
	{
		return enableSmoothing;
	}

	@Override
	public void setEnableSmoothing(boolean enableSmoothing)
	{
		this.enableSmoothing = enableSmoothing;
	}

	@Override
	public boolean isLockHeading()
	{
		return lockHeading;
	}

	@Override
	public void setLockHeading(boolean lockHeading)
	{
		this.lockHeading = lockHeading;
	}

	@Override
	public boolean isStopOnFocusLost()
	{
		return stopOnFocusLost;
	}

	@Override
	public void setStopOnFocusLost(boolean stopOnFocusLost)
	{
		this.stopOnFocusLost = stopOnFocusLost;
	}

	@Override
	public double getDragSlopeFactor()
	{
		return dragSlopeFactor;
	}

	@Override
	public void setDragSlopeFactor(double dragSlopeFactor)
	{
		this.dragSlopeFactor = dragSlopeFactor;
	}

	@Override
	public ViewInputAttributes getAttributes()
	{
		return attributes;
	}

	@Override
	public void setAttributes(ViewInputAttributes attributes)
	{
		this.attributes = attributes;
	}

	@Override
	public double computeDragSlope(Point point1, Point point2, Vec4 vec1, Vec4 vec2)
	{
		View view = this.getView();
		if (view == null)
		{
			return 0.0;
		}

		// Compute the screen space distance between point1 and point2.
		double dx = point2.getX() - point1.getX();
		double dy = point2.getY() - point1.getY();
		double pixelDistance = Math.sqrt(dx * dx + dy * dy);

		// Determine the distance from the eye to the point on the forward vector closest to vec1 and vec2
		double d = view.getEyePoint().distanceTo3(vec1);
		// Compute the size of a screen pixel at the nearest of the two distances.
		double pixelSize = view.computePixelSizeAtDistance(d);

		// Return the ratio of world distance to screen distance.
		double slope = vec1.distanceTo3(vec2) / (pixelDistance * pixelSize);
		if (slope < 1.0)
		{
			slope = 1.0;
		}

		return slope - 1.0;
	}

	@Override
	public void goTo(Position lookAtPos, double elevation)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void stopAnimators()
	{
		animControl.stopAnimations();
		animControl.clear();
	}

	@Override
	public boolean isAnimating()
	{
		return animControl.hasActiveAnimation();
	}

	@Override
	public void addAnimator(Animator animator)
	{
		animControl.put(VIEW_ANIM_APP, animator);
	}

	@Override
	public void apply()
	{
		View view = this.getView();
		if (view == null)
		{
			return;
		}
		if (animControl.stepAnimators())
		{
			view.firePropertyChange(AVKey.VIEW, null, view);
		}
	}

	@Override
	public void viewApplied()
	{
	}

	@Override
	public void focusGained(FocusEvent e)
	{
	}

	@Override
	public void focusLost(FocusEvent e)
	{
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
	}
}
