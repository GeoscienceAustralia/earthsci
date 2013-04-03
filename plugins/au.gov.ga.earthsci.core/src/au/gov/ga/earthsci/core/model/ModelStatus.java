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

import org.eclipse.core.runtime.IStatus;

/**
 * Default implementation of the {@link IModelStatus} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ModelStatus implements IModelStatus
{

	private final Level level;
	private final String message;
	private final Throwable throwable;

	/**
	 * Create a new {@link ModelStatus} instance representing an OK status
	 */
	public static ModelStatus ok()
	{
		return ok(null);
	}

	/**
	 * Create a new {@link ModelStatus} instance representing an OK status
	 */
	public static ModelStatus ok(String message)
	{
		return new ModelStatus(Level.OK, message, null);
	}

	/**
	 * Create a new {@link ModelStatus} instance representing an ERROR status
	 */
	public static ModelStatus error(String message, Throwable t)
	{
		return new ModelStatus(Level.ERROR, message, t);
	}

	/**
	 * Create a new {@link ModelStatus} instance from the given {@link IStatus}
	 * object.
	 */
	public static ModelStatus fromIStatus(IStatus s)
	{
		if (s == null)
		{
			return ok(null);
		}

		Level l = Level.OK;
		if (s.getSeverity() == IStatus.ERROR)
		{
			l = Level.ERROR;
		}
		else if (s.getSeverity() == IStatus.WARNING)
		{
			l = Level.WARNING;
		}

		return new ModelStatus(l, s.getMessage(), s.getException());
	}

	/**
	 * Create a new {@link ModelStatus} instance
	 * 
	 * @param level
	 *            The level of the status instance
	 * @param message
	 *            The localised message to associate with the status
	 * @param throwable
	 *            Optional throwable to attach to the status
	 */
	public ModelStatus(Level level, String message, Throwable throwable)
	{
		this.level = level == null ? Level.OK : level;
		this.message = message;
		this.throwable = throwable;
	}

	@Override
	public Level getLevel()
	{
		return level;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

	@Override
	public Throwable getThrowable()
	{
		return throwable;
	}

	@Override
	public boolean isError()
	{
		return level == Level.ERROR;
	}

	@Override
	public boolean isOk()
	{
		return level == Level.OK;
	}
}
