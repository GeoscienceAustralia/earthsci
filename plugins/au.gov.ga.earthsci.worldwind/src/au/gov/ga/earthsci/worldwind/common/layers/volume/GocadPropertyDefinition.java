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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

/**
 * A simple mutable representation of a GOCAD property definition
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GocadPropertyDefinition
{
	private String name;
	private int id;
	private String unit;
	private float noDataValue;
	private int bytes = 4;
	private String type = "IEEE";
	private boolean cellCentred;
	private String format = "RAW";
	private int offset;
	private String file;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUnit()
	{
		return unit;
	}

	public void setUnit(String unit)
	{
		this.unit = unit;
	}

	public float getNoDataValue()
	{
		return noDataValue;
	}

	public void setNoDataValue(float noDataValue)
	{
		this.noDataValue = noDataValue;
	}

	public int getBytes()
	{
		return bytes;
	}

	public void setBytes(int eSize)
	{
		this.bytes = eSize;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String eType)
	{
		this.type = eType;
	}

	public boolean isCellCentred()
	{
		return cellCentred;
	}

	public void setCellCentred(boolean cellCentred)
	{
		this.cellCentred = cellCentred;
	}

	public String getFormat()
	{
		return format;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public String getFile()
	{
		return file;
	}

	public void setFile(String file)
	{
		this.file = file;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return id;
	}
}
