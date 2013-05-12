/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.discovery.csw;

import au.gov.ga.earthsci.discovery.IDiscoveryResult;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class CSWDiscoveryResult implements IDiscoveryResult
{
	/*
	<csw:Record>
		<dc:identifier scheme="urn:x-esri:specification:ServiceType:ArcIMS:Metadata:FileID">{1E1BA4EA-E8FB-47FD-9551-A045A550A478}</dc:identifier>
		<dc:identifier scheme="urn:x-esri:specification:ServiceType:ArcIMS:Metadata:DocID">{F58AC930-A27F-45BB-A46C-F38551AE9A65}</dc:identifier>
		<dc:title>Surface Geology of Australia WMS</dc:title>
		<dc:type scheme="urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType">liveData</dc:type>
		<dc:subject>OneGeology</dc:subject>
		<dc:subject>Australia</dc:subject>
		<dc:subject>geology</dc:subject>
		<dc:subject>bedrock</dc:subject>
		<dc:subject>surficial</dc:subject>
		<dc:subject>lithology</dc:subject>
		<dc:subject>lithostratigraphy</dc:subject>
		<dc:subject>age</dc:subject>
		<dc:subject>contact</dc:subject>
		<dc:subject>fault</dc:subject>
		<dc:subject>shear</dc:subject>
		<dct:modified>2013-01-18T11:03:53+11:00</dct:modified>
		<dct:abstract>The Surface Geology of Australia Web Map Service provides two seamless national coverages of bedrock and surficial geology, compiled for use at 1:1 million scale and 1:2.5 million scale. The data sets were released in 2012. The 1:1M data is limited so that it does not display at scales less than 1:1,500,000. The 1:2.5M data is limited so that it does not display at scales greater than 1:1,500,000. The data represents outcropping or near-outcropping bedrock units, and unconsolidated or poorly consolidated regolith material covering bedrock. Geological units are represented as polygon and line geometries, and are attributed with information regarding stratigraphic name and hierarchy, age, lithology, and primary data source. Layers are available for geological units coloured by lithostratigraphy, age, and lithology. The dataset also contains geological contacts, structural features such as faults and shears, and miscellaneous supporting lines like the boundaries of water and ice bodies. Copyright Commonwealth of Australia (Geoscience Australia) 2012.  This material is released free under the Creative Commons Attribution 3.0 Australia Licence - http://creativecommons.org/licenses/by/3.0/au/</dct:abstract>
		<dct:references scheme="urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server">http://www.ga.gov.au/gis/services/earth_science/GA_Surface_Geology_of_Australia/MapServer/WMSServer</dct:references>
		<dct:references scheme="urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Document">http://www.ga.gov.au/geoportal/csw?getxml=%7BF58AC930-A27F-45BB-A46C-F38551AE9A65%7D</dct:references>
		<ows:WGS84BoundingBox>
			<ows:LowerCorner>72.0 -55.0</ows:LowerCorner>
			<ows:UpperCorner>168.0 -9.0</ows:UpperCorner>
		</ows:WGS84BoundingBox>
		<ows:BoundingBox>
			<ows:LowerCorner>72.0 -55.0</ows:LowerCorner>
			<ows:UpperCorner>168.0 -9.0</ows:UpperCorner>
		</ows:BoundingBox>
	</csw:Record>
	 */

	public CSWDiscoveryResult()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getIndex()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
