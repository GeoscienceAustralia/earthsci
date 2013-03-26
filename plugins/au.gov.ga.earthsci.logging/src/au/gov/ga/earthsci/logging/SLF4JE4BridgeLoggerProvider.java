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
package au.gov.ga.earthsci.logging;

import org.eclipse.e4.core.services.log.ILoggerProvider;
import org.eclipse.e4.core.services.log.Logger;

/**
 * An {@link ILoggerProvider} that yields the {@link SLF4JE4LoggerBridge} used
 * to divert logging messages to the SLF4J framework.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class SLF4JE4BridgeLoggerProvider implements ILoggerProvider
{

	@Override
	public Logger getClassLogger(Class<?> clazz)
	{
		return new SLF4JE4LoggerBridge(clazz);
	}

}
