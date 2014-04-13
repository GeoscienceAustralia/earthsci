/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.input.hydra;

import com.sixense.ControllerData;

/**
 * An event from the Razer Hydra containing controller data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HydraEvent
{
	/**
	 * Controller 1's data.
	 */
	public final ControllerData controller1;
	/**
	 * Controller 2's data.
	 */
	public final ControllerData controller2;

	public HydraEvent(ControllerData controller1, ControllerData controller2)
	{
		this.controller1 = controller1;
		this.controller2 = controller2;
	}
}
