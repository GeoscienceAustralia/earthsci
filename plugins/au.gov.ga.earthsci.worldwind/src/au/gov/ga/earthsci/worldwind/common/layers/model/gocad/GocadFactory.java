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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * Factory for creating {@link Object}s from GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadFactory
{
	private static final String COMMENT_REGEX = "\\s*#.*";

	public static boolean isGocadFileSuffix(String suffix)
	{
		return suffix.equalsIgnoreCase("ts") || suffix.equalsIgnoreCase("gp") || suffix.equalsIgnoreCase("vo")
				|| suffix.equalsIgnoreCase("pl") || suffix.equalsIgnoreCase("grs") || suffix.equalsIgnoreCase("sg");
	}

	/**
	 * Enumeration of different GOCAD file types.
	 */
	public enum GocadType
	{
		PLine(GocadPLineReader.HEADER_REGEX, GocadPLineReader.END_REGEX, GocadPLineReader.class),
		Voxet(GocadVoxetReader.HEADER_REGEX, GocadVoxetReader.END_REGEX, GocadVoxetReader.class),
		TSurf(GocadTSurfReader.HEADER_REGEX, GocadTSurfReader.END_REGEX, GocadTSurfReader.class),
		SGrid(GocadSGridReader.HEADER_REGEX, GocadSGridReader.END_REGEX, GocadSGridReader.class),
		GSurf(GocadGSurfReader.HEADER_REGEX, GocadGSurfReader.END_REGEX, GocadGSurfReader.class),
		Group(GocadGroupReader.HEADER_REGEX, GocadGroupReader.END_REGEX, GocadGroupReader.class),
		VSet(GocadVSetReader.HEADER_REGEX, GocadVSetReader.END_REGEX, GocadVSetReader.class);

		/**
		 * Regular expression used for matching the first line of the GOCAD object to this type.
		 */
		public final String headerRegex;
		
		/**
		 * Regular expression used for matching the end of the GOCAD object of this type.
		 */
		public final String endRegex;
		
		/**
		 * {@link GocadReader} implementation used for reading this type.
		 */
		public final Class<? extends GocadReader<?>> readerClass;

		private GocadType(String headerRegex, String endRegex, Class<? extends GocadReader<?>> readerClass)
		{
			this.headerRegex = headerRegex;
			this.endRegex = endRegex;
			this.readerClass = readerClass;
		}

		/**
		 * @return An instance of a {@link GocadReader} for reading a file of
		 *         this type.
		 */
		public GocadReader<?> instanciateReader()
		{
			try
			{
				return readerClass.newInstance();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	public static List<FastShape> read(File file, GocadReaderParameters parameters)
	{
		try
		{
			return read(new FileReader(file), file.toURI().toURL(), parameters);
		}
		catch (MalformedURLException e)
		{
			//won't ever happen
			return null;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static List<FastShape> read(InputStream is, URL context, GocadReaderParameters parameters)
	{
		return read(new InputStreamReader(is), context, parameters);
	}

	/**
	 * Read a GOCAD source to a {@link Object}.
	 * 
	 * @param reader
	 *            Reader to read from
	 * @return A list of {@link Object}s containing the geometry from the
	 *         GOCAD file
	 */
	public static List<FastShape> read(Reader reader, URL context, GocadReaderParameters parameters)
	{
		List<FastShape> shapes = new ArrayList<FastShape>();

		try
		{
			BufferedReader br = new BufferedReader(reader);
			while (true)
			{
				String line = br.readLine();
				if (line == null)
				{
					if (shapes.size() == 0)
					{
						throw new IllegalArgumentException("No GOCAD objects found");
					}
					//file is finished, so break out of loop
					break;
				}

				//check if the line matches any of the GOCAD object header regexes
				GocadType type = determineGocadType(line);
				if (type == null)
				{
					//if this line doesn't, try the next line
					continue;
				}

				Object object = readFromGocadObject(type, parameters, br, context);
				if (object instanceof FastShape)
				{
					shapes.add((FastShape) object);
				}
				else if (object instanceof GocadReaderParameters)
				{
					parameters = (GocadReaderParameters) object;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return shapes;
	}

	/**
	 * Determine the {@link GocadType} from the header line in the file.
	 * 
	 * @param line
	 *            First line in the GOCAD file
	 * @return {@link GocadType} matched for the header, or null if none found.
	 */
	protected static GocadType determineGocadType(String line)
	{
		for (GocadType type : GocadType.values())
		{
			if (line.matches(type.headerRegex))
			{
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Reads a GOCAD object of the defined type from the buffered reader provided. 
	 * <p/>
	 * The provided buffered reader will be advanced to the last line of the GOCAD object
	 * on successful return from this method. 
	 * 
	 * @param type The type of GOCAD object to read
	 * @param parameters Global reader parameters to use
	 * @param br The buffered reader to read the GOCAD object from
	 * @param context The URL of the file being read
	 * 
	 * @return A {@link Object} that represents the read GOCAD object
	 * 
	 * @throws IOException
	 */
	private static Object readFromGocadObject(GocadType type, GocadReaderParameters parameters, BufferedReader br, URL context) throws IOException
	{
		GocadReader<?> gocadReader = type.instanciateReader();
		gocadReader.begin(parameters);
		while (true)
		{
			String line = br.readLine();
			if (line == null)
			{
				throw new IllegalArgumentException("GOCAD file ended unexpectedly");
			}
			if (line.matches(COMMENT_REGEX))
			{
				//don't pass comment lines to the reader
				continue;
			}
			if (line.matches(type.endRegex))
			{
				//object has ended, break out of the loop to parse the next object (if any)
				break;
			}
			gocadReader.addLine(line);
		}
		return gocadReader.end(context);
	}
	
}
