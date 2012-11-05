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
package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Ignore;

/**
 * An abstract base class for unit test classes exercising subclasses 
 * of the {@link GocadReader}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Ignore
public abstract class AbstractGocadReaderTest<T>
{

	public T readFile(GocadReader<T> reader, GocadReaderParameters params, URL file) throws Exception
	{
		reader.begin(params);
		
		BufferedReader sr = new BufferedReader(new InputStreamReader(file.openStream()));
		
		String s;
		while ((s = sr.readLine()) != null)
		{
			reader.addLine(s);
		}
		
		return reader.end(file);
	}
	
}
