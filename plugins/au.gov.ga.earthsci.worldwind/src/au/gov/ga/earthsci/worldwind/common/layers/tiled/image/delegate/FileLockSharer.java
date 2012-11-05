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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class which allows sharing of a fileLock (object on which blocks are
 * synchronized before reading from and writing to the cache). This is useful as
 * some layers may share the same imagery but do different post processing on
 * the textures.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileLockSharer
{
	private static Map<String, Object> locks = new HashMap<String, Object>();

	/**
	 * Get an object on which to synchronize for reading/writing to the cache
	 * location identified by dataCacheName.
	 * 
	 * @param dataCacheName
	 *            Cache location
	 * @return Object on which to synchronize
	 */
	public static Object getLock(String dataCacheName)
	{
		if (!locks.containsKey(dataCacheName))
		{
			locks.put(dataCacheName, new Object());
		}
		return locks.get(dataCacheName);
	}
}
