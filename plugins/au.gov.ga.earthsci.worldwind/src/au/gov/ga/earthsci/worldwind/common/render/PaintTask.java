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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.render.DrawContext;

/**
 * Task which can be added to the {@link AnimatorSceneController} to run before
 * or after a repaint.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface PaintTask
{
	/**
	 * Run the task; called by the SceneController.
	 * 
	 * @param dc
	 *            Current {@link DrawContext}
	 */
	void run(DrawContext dc);
}
