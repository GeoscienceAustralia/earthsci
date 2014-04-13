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

/**
 * Represents an trigger changed event on the Razer Hydra.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HydraTriggerEvent
{
	/**
	 * Value for the trigger on controller 1.
	 */
	public float trigger1;
	/**
	 * Value for the trigger on controller 2.
	 */
	public float trigger2;
	/**
	 * Delta change of controller 1's trigger.
	 */
	public float delta1;
	/**
	 * Delta change of controller 2's trigger.
	 */
	public float delta2;

	@Override
	public String toString()
	{
		return "Trigger: controller 1 = " + trigger1 + ", controller 2 = " + trigger2;
	}
}
