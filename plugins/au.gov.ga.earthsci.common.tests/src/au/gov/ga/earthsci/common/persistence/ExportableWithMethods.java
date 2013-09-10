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
public class ExportableWithMethods
{
	@Persistent
	private int field;

	private double method;
	private float setterMethod;

	public int getField()
	{
		return field;
	}

	public void setField(int field)
	{
		this.field = field;
	}

	@Persistent
	public double getMethod()
	{
		return method;
	}

	public void setMethod(double method)
	{
		this.method = method;
	}

	@Persistent(setter = "setSetterMethodOther")
	public float getSetterMethod()
	{
		return setterMethod;
	}

	public void setSetterMethodOther(float setterMethod)
	{
		this.setterMethod = setterMethod;
	}

	@Override
	public boolean equals(Object obj)
	{
		ExportableWithMethods ewm = (ExportableWithMethods) obj;
		return ewm.getField() == getField() && ewm.getMethod() == getMethod()
				&& ewm.getSetterMethod() == getSetterMethod();
	}
}
