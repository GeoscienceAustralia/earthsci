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

@Exportable
public class ExportableWithNull
{
	@Persistent
	private String string = null;

	public String getString()
	{
		return string;
	}

	public void setString(String string)
	{
		this.string = string;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithNull ewn = (ExportableWithNull) obj;
		if (ewn.string == string)
		{
			return true;
		}
		return ewn.string.equals(string);
	}
}
