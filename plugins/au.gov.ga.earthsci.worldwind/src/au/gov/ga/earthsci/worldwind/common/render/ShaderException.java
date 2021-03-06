/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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

/**
 * Exception thrown by {@link Shader} creation/compilation.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShaderException extends Exception
{
	public ShaderException()
	{
		super();
	}

	public ShaderException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ShaderException(String message)
	{
		super(message);
	}

	public ShaderException(Throwable cause)
	{
		super(cause);
	}
}
