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
package au.gov.ga.earthsci.worldwind.common.util;

/**
 * A helper class used to calculate grid spacings etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GridHelper
{
	private static final int DEFAULT_GRID_SIZE = 20;
	
	/** A container class that holds calculated grid properties */
	public static class GridProperties
	{
		/** The value of the first grid line */
		private double firstGridLineValue;
		
		/** The value change per grid line */
		private double valueChangePerGridLine;
		
		/** The number of decimal places the value change is accurate to */
		private int numberDecimalPlaces;

		public GridProperties(double firstGridLineValue, double valueChangePerGridLine, int numberOfDecimalPlaces)
		{
			this.firstGridLineValue = firstGridLineValue;
			this.valueChangePerGridLine = valueChangePerGridLine;
			this.numberDecimalPlaces = numberOfDecimalPlaces;
		}

		public double getFirstGridLineValue()
		{
			return firstGridLineValue;
		}

		public double getValueChangePerGridLine()
		{
			return valueChangePerGridLine;
		}
		
		public int getNumberDecimalPlaces()
		{
			return numberDecimalPlaces;
		}
		
		@Override
		public String toString()
		{
			return "Grid[Start: " + firstGridLineValue + ", Value change: " + valueChangePerGridLine + ", Number decimal places: " + numberDecimalPlaces + "]"; 
		}
	}
	
	/** A builder class to use for building grids */
	public static class GridBuilder
	{
		private int gridSize = DEFAULT_GRID_SIZE;
		private Integer numPixels;
		private Range<Double> valueRange;
		
		public GridBuilder ofSize(int maxGridSize)
		{
			this.gridSize = maxGridSize;
			return this;
		}
		
		public GridBuilder toFitIn(int numPixels)
		{
			this.numPixels = numPixels;
			return this;
		}
		
		public GridBuilder forValueRange(Range<Double> valueRange)
		{
			this.valueRange = valueRange;
			return this;
		}
		
		public GridProperties build()
		{
			if (numPixels == null || valueRange == null)
			{
				throw new IllegalStateException("Not enough information provided to build a grid. Please use the builder methods to provide required information");
			}
			
			return calculateGridProperties();
		}

		private GridProperties calculateGridProperties()
		{
			int pixelsPerGridLine = Integer.MAX_VALUE;
			double valueChangePerGridLine = Math.pow(10, (int)Math.log10(valueRange.getMaxValue())+1);
			
			double valueDelta = valueRange.getMaxValue() - valueRange.getMinValue();
			
			double[] dividers = new double[]{1, 0.5, 0.2};
			
			int numDecimalPlaces = 0;
			
			// Incrementally decrease the value change until it falls within the grid size
			while (pixelsPerGridLine > gridSize)
			{
				for (double d : dividers)
				{
					double candidateValueChange = valueChangePerGridLine * d;
					
					// Adjust for the <1 dividers
					if (d < 1 && candidateValueChange < 1)
					{
						numDecimalPlaces++;
					}
					
					pixelsPerGridLine = (int)((numPixels / valueDelta) * candidateValueChange);
					
					if (pixelsPerGridLine <= gridSize)
					{
						valueChangePerGridLine = candidateValueChange;
						break;
					}
					
					if (d < 1 && candidateValueChange < 1)
					{
						numDecimalPlaces--;
					}
				}
				if (pixelsPerGridLine <= gridSize)
				{
					break;
				}
				
				valueChangePerGridLine *= 0.1;
				if (valueChangePerGridLine < 1)
				{
					numDecimalPlaces++;
				}
			}
			
			double start = valueRange.getMinValue();
			double floor = Math.floor(start / valueChangePerGridLine);
			double firstGridLineValue = valueChangePerGridLine * floor;
			
			return new GridProperties(firstGridLineValue, valueChangePerGridLine, numDecimalPlaces);
		}
		
	}
	
	/** Use the builder methods to create grids */
	private GridHelper(){}
	
	public static GridBuilder createGrid()
	{
		return new GridBuilder();
	}
	
}
