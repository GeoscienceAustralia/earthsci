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
package au.gov.ga.earthsci.common.persistence;

import java.util.Arrays;

@Exportable
public class ExportableWithDoubleArray
{
	@Persistent
	private ExportableWithCollection[][] collectionArray;

	public ExportableWithCollection[][] getCollectionArray()
	{
		return collectionArray;
	}

	public void setCollectionArray(ExportableWithCollection[][] collectionArray)
	{
		this.collectionArray = collectionArray;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithDoubleArray ewda = (ExportableWithDoubleArray) obj;
		if (ewda.collectionArray.length != collectionArray.length)
		{
			return false;
		}
		for (int i = 0; i < collectionArray.length; i++)
		{
			if (!Arrays.equals(collectionArray[i], ewda.collectionArray[i]))
			{
				return false;
			}
		}
		return true;
	}
}
