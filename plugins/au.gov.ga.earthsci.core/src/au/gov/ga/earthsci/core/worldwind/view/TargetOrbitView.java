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
package au.gov.ga.earthsci.core.worldwind.view;

import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitView;

/**
 * {@link OrbitView} extension that allows the user to optionally modify the
 * center of rotation point, instead of keeping the center point fixed to the
 * earth's surface, which is the default.
 * <p/>
 * Also draws an optional axis marker whenever the view changes to indicate the
 * current center of rotation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TargetOrbitView extends BaseOrbitView
{
	protected boolean targetMode = false;
	protected boolean drawAxisMarker = true;
	protected final AxisRenderable axisMarker = new AxisRenderable();
	protected final RenderableScreenCreditDelegate axisScreenCredit = new RenderableScreenCreditDelegate(axisMarker);

	@Override
	protected ViewInputHandler createViewInputHandler()
	{
		return new TargetOrbitViewInputHandler();
	}

	/**
	 * @return Is target mode enabled?
	 */
	public boolean isTargetMode()
	{
		return targetMode;
	}

	/**
	 * Enable/disable target mode. When enabled, the user can modify the center
	 * point, instead of fixing it to the earth's surface.
	 * 
	 * @param targetMode
	 */
	public void setTargetMode(boolean targetMode)
	{
		this.targetMode = targetMode;
	}

	/**
	 * @return Should the axis marker be drawn when the view changes?
	 */
	public boolean isDrawAxisMarker()
	{
		return drawAxisMarker;
	}

	/**
	 * Enable/disable the axis marker that is drawn when the view changes.
	 * 
	 * @param drawAxisMarker
	 */
	public void setDrawAxisMarker(boolean drawAxisMarker)
	{
		this.drawAxisMarker = drawAxisMarker;
	}

	/**
	 * @return Axis marker that is drawn when the view changes
	 */
	public AxisRenderable getAxisMarker()
	{
		return axisMarker;
	}

	@Override
	public void focusOnViewportCenter()
	{
		if (isTargetMode())
		{
			//if we are in target mode, the center point can be changed by the user, so don't change it automatically
			return;
		}

		super.focusOnViewportCenter();
	}

	@Override
	protected void doApply(DrawContext dc)
	{
		Vec4 beforeApply = Vec4.UNIT_W.transformBy4(this.modelview);

		super.doApply(dc);

		Vec4 afterApply = Vec4.UNIT_W.transformBy4(this.modelview);
		if (beforeApply.distanceToSquared3(afterApply) > 10)
		{
			//view has changed, so show the axis marker
			axisMarker.trigger();
		}
		//the screen credits are stored in a map, so adding this each frame is not a problem
		dc.addScreenCredit(axisScreenCredit);
	}
}
