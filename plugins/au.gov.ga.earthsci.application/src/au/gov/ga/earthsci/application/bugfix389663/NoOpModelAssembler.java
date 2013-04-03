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
package au.gov.ga.earthsci.application.bugfix389663;

import org.eclipse.e4.ui.internal.workbench.ModelAssembler;

/**
 * {@link ModelAssembler} subclass that does nothing.
 * 
 * Workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=389663,
 * until 4.3M7 is released.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NoOpModelAssembler extends ModelAssembler
{
	@Override
	public void processModel()
	{
	}
}
