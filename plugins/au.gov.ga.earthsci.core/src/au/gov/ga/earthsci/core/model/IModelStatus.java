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
package au.gov.ga.earthsci.core.model;

/**
 * A simple status interface that captures information about the status of model
 * objects. Includes a state, description and optional exception.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelStatus
{
	public static enum Level
	{
		ERROR,
		WARNING,
		OK
	}
	
	/**
	 * @return the level of this status
	 */
	Level getLevel();
	
	
	/**
	 * @return a (localised) message for this status
	 */
	String getMessage();
	
	/**
	 * @return any (optional) throwables associated with this status
	 */
	Throwable getThrowable();
	
	/**
	 * @return If this status has level OK
	 */
	boolean isOk();
	
	/**
	 * @return If this status has level ERROR
	 */
	boolean isError();
	
}
