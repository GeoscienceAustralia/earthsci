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
package au.gov.ga.earthsci.ant;

/**
 * Represents a Java VM argument, used by the {@link JavaArgs} task.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JavaArg
{
	private String argument;
	private String os;
	private String arch;

	public String getArgument()
	{
		return argument;
	}

	public void setArgument(String argument)
	{
		this.argument = argument;
	}

	public String getOs()
	{
		return os;
	}

	public void setOs(String os)
	{
		this.os = os;
	}

	public String getArch()
	{
		return arch;
	}

	public void setArch(String arch)
	{
		this.arch = arch;
	}
}
