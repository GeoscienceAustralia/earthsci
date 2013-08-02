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
package au.gov.ga.earthsci.application.console;

/**
 * An interface for objects that can retrieve and update preferences
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ILoggingPreferences
{

	String QUALIFIER_ID = "au.gov.ga.earthsci.logging"; //$NON-NLS-1$
	String LOG_LEVEL = "au.gov.ga.earthsci.logging.preferences.level"; //$NON-NLS-1$

	String TRACE_LEVEL = "trace"; //$NON-NLS-1$
	String DEBUG_LEVEL = "debug"; //$NON-NLS-1$
	String INFO_LEVEL = "info"; //$NON-NLS-1$
	String WARN_LEVEL = "warn"; //$NON-NLS-1$
	String ERROR_LEVEL = "error"; //$NON-NLS-1$
	String FATAL_LEVEL = "fatal"; //$NON-NLS-1$

	String[] LOG_LEVELS = { TRACE_LEVEL, DEBUG_LEVEL, INFO_LEVEL, WARN_LEVEL, ERROR_LEVEL, FATAL_LEVEL };

	/**
	 * @return The currently set log level (using the slf4j levels)
	 *         <ol>
	 *         <li>trace
	 *         <li>debug
	 *         <li>info
	 *         <li>warn
	 *         <li>error
	 *         <li>fatal
	 *         </ol>
	 */
	int getLogLevel();

	/**
	 * @return The currently set log level, as a String
	 *         <ul>
	 *         <li>{@link #TRACE_LEVEL}
	 *         <li>{@link #DEBUG_LEVEL}
	 *         <li>{@link #INFO_LEVEL}
	 *         <li>{@link #WARN_LEVEL}
	 *         <li>{@link #ERROR_LEVEL}
	 *         <li>{@link #FATAL_LEVEL}
	 *         </ul>
	 */
	String getLogLevelStr();

	/**
	 * Set the logging level:
	 * <ol>
	 * <li>trace
	 * <li>debug
	 * <li>info
	 * <li>warn
	 * <li>error
	 * <li>fatal
	 * </ol>
	 * 
	 * @param level
	 *            The level to set
	 */
	void setLogLevel(int level);

	/**
	 * Set the log level as a String
	 * <ul>
	 * <li>{@link #TRACE_LEVEL}
	 * <li>{@link #DEBUG_LEVEL}
	 * <li>{@link #INFO_LEVEL}
	 * <li>{@link #WARN_LEVEL}
	 * <li>{@link #ERROR_LEVEL}
	 * <li>{@link #FATAL_LEVEL}
	 * </ul>
	 * 
	 * @param level
	 */
	void setLogLevel(String level);
}
