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
package au.gov.ga.earthsci.core.persistence;

import java.util.Arrays;

@Exportable
public class ExportableWithArray
{
	@Persistant
	private double[] array;

	public double[] getArray()
	{
		return array;
	}

	public void setArray(double[] array)
	{
		this.array = array;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithArray ewa = (ExportableWithArray) obj;
		return Arrays.equals(ewa.array, array);
	}
}
