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
package au.gov.ga.earthsci.worldwind.common.layers;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;

/**
 * Represents an object that can be bounded by a sector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Bounded
{
	/**
	 * @return This object's bounds
	 */
	Bounds getBounds();

	/**
	 * @return Does this object follow the terrain?
	 */
	boolean isFollowTerrain();

	/**
	 * Utility class which reads a sector from an Object.
	 */
	public class Reader
	{
		/**
		 * Get source's bounds. Checks if object is an instance of Bounded,
		 * otherwise checks if the object is an AVList and contains a value for
		 * AVKey.SECTOR.
		 * 
		 * @param source
		 * @return source's bounds, or null if they couldn't be determined
		 */
		public static Bounds getBounds(Object source)
		{
			if (source instanceof Bounded)
			{
				return ((Bounded) source).getBounds();
			}
			else if (source instanceof AVList)
			{
				Object o = ((AVList) source).getValue(AVKey.SECTOR);
				if (o instanceof Sector)
				{
					return Bounds.fromSector((Sector) o);
				}
			}

			return null;
		}

		/**
		 * Does the source follow the terrain? If the object is an instance of
		 * Bounded, returns {@link Bounded#isFollowTerrain()}. Otherwise returns
		 * <code>true</code>.
		 * 
		 * @param source
		 * @return True if source follows the terrain
		 */
		public static boolean isFollowTerrain(Object source)
		{
			if (source instanceof Bounded)
			{
				return ((Bounded) source).isFollowTerrain();
			}
			return true;
		}
	}
}
