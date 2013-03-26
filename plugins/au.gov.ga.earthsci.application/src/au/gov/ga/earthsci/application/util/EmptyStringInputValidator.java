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
package au.gov.ga.earthsci.application.util;

import org.eclipse.jface.dialogs.IInputValidator;

import au.gov.ga.earthsci.common.util.Util;


/**
 * An {@link IInputValidator} that validates the input text is not null or
 * blank.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class EmptyStringInputValidator implements IInputValidator
{

	private String message;

	public EmptyStringInputValidator(String message)
	{
		this.message = message;
	}

	@Override
	public String isValid(String newText)
	{
		if (Util.isEmpty(newText))
		{
			return message;
		}
		return null;
	}

}
