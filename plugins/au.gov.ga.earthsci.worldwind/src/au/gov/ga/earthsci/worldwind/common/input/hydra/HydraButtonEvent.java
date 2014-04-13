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
 * Button changed event for the Razer Hydra.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HydraButtonEvent
{
	public static final int BUTTON1 = 6;
	public static final int BUTTON2 = 7;
	public static final int BUTTON3 = 4;
	public static final int BUTTON4 = 5;
	public static final int START = 1;
	public static final int STICK = 9;
	public static final int BACK = 8;
	
	/**
	 * Button id.
	 */
	public int button;
	/**
	 * Controller (1 or 2).
	 */
	public int controller;
	/**
	 * Is the button down or up.
	 */
	public boolean down;

	public HydraButtonEvent(int button, int controller, boolean down)
	{
		this.button = button;
		this.controller = controller;
		this.down = down;
	}

	@Override
	public String toString()
	{
		return "Controller " + controller + ", button " + button + (down ? " down" : " up");
	}
}
