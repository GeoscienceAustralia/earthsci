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
package au.gov.ga.earthsci.discovery.darwin;

import gov.nasa.worldwind.geom.Sector;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import au.gov.ga.earthsci.common.collection.ArrayListHashMap;
import au.gov.ga.earthsci.common.collection.ListMap;
import au.gov.ga.earthsci.discovery.AbstractDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;

/**
 * {@link IDiscoveryResult} implementation for DARWIN.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscoveryResult extends AbstractDiscoveryResult<DarwinDiscovery>
{
	private final String title;
	private final String description;
	private final Sector bounds;
	private final URL metadataUrl;
	private final List<DarwinDiscoveryResultURL> urls = new ArrayList<DarwinDiscoveryResultURL>();
	private final ListMap<String, DarwinDiscoveryResultURL> urlMap =
			new ArrayListHashMap<String, DarwinDiscoveryResultURL>();

	public DarwinDiscoveryResult(DarwinDiscovery discovery, int index, JSONObject json)
	{
		super(discovery, index);

		title = json.optString("title"); //$NON-NLS-1$
		description = json.optString("theAbstract"); //$NON-NLS-1$

		Sector bounds = null;
		JSONObject spatial = json.optJSONObject("spatial"); //$NON-NLS-1$
		if (spatial != null)
		{
			double minLat = spatial.optDouble("southLatitude"); //$NON-NLS-1$
			double maxLat = spatial.optDouble("northLatitude"); //$NON-NLS-1$
			double minLon = spatial.optDouble("westLongitude"); //$NON-NLS-1$
			double maxLon = spatial.optDouble("eastLongitude"); //$NON-NLS-1$
			if (!(Double.isNaN(minLat) || Double.isNaN(minLon) || Double.isNaN(maxLat) || Double.isNaN(maxLon)))
			{
				bounds = Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
			}
		}
		this.bounds = bounds;

		URL metadataUrl = null;
		try
		{
			String urlString = json.optString("metadataLink"); //$NON-NLS-1$
			if (urlString != null)
			{
				metadataUrl = new URL(getDiscovery().getService().getServiceURL(), urlString);
				urls.add(new DarwinDiscoveryResultURL("Metadata", metadataUrl, "WWW:LINK-1.0-http--link")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch (MalformedURLException e)
		{
		}
		this.metadataUrl = metadataUrl;

		parseURLs(json);
	}

	private void parseURLs(JSONObject json)
	{
		JSONArray uriArray = json.optJSONArray("dcUris"); //$NON-NLS-1$
		if (uriArray != null)
		{
			for (int i = 0; i < uriArray.length(); i++)
			{
				JSONObject uriObject = uriArray.optJSONObject(i);
				if (uriObject != null)
				{
					try
					{
						String name = uriObject.getString("name"); //$NON-NLS-1$
						URL url = new URL(getDiscovery().getService().getServiceURL(), uriObject.getString("value")); //$NON-NLS-1$
						String protocol = uriObject.getString("protocol"); //$NON-NLS-1$
						DarwinDiscoveryResultURL ddrurl = new DarwinDiscoveryResultURL(name, url, protocol);
						urls.add(ddrurl);
						urlMap.putSingle(protocol, ddrurl);
					}
					catch (Exception e)
					{
						//ignore invalid records
					}
				}
			}
		}
	}

	@Override
	public Sector getSector()
	{
		return bounds;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public URL getMetadataUrl()
	{
		return metadataUrl;
	}

	public List<DarwinDiscoveryResultURL> getUrls()
	{
		return Collections.unmodifiableList(urls);
	}

	public List<DarwinDiscoveryResultURL> getUrls(String protocol)
	{
		List<DarwinDiscoveryResultURL> list = urlMap.get(protocol);
		if (list == null)
		{
			list = new ArrayList<DarwinDiscoveryResultURL>();
		}
		return Collections.unmodifiableList(list);
	}

	public DarwinDiscoveryResultURL getThumbnailUrl()
	{
		List<DarwinDiscoveryResultURL> list = urlMap.get("thumbnail"); //$NON-NLS-1$
		if (list == null || list.isEmpty())
		{
			return null;
		}
		return list.get(0);
	}

	@Override
	public String getInformationString()
	{
		return getDiscovery().getLabelProvider().getToolTip(this);
	}

	@Override
	public URL getInformationURL()
	{
		return metadataUrl;
	}

	/*
	{
	"exceptions": null,
	"numberOfRecordsMatched": "117881",
	"searchResults": [
	    {
	        "licence": "Commonweath of Australia (Geoscience Australia) ; Creative Commons Attribution 3.0 Australia Licence",
	        "sourceSystemCode": "gcat",
	        "theAbstract": "Australia magnetic map 1:2.5m",
	        "metadataLink": "/metadata-gateway/metadata/record/gcat_a05f7892-b228-7506-e044-00144fdd4fa6/Australia+magnetic+map+1%3A2.5m",
	        "spatial": {
	            "southLatitude": -44,
	            "valid": true,
	            "westLongitude": 111,
	            "eastLongitude": 156,
	            "allNull": false,
	            "closed": true,
	            "northLatitude": -9
	        },
	        "showPolygon": false,
	        "date": 189262800000,
	        "listOfKeywords": null,
	        "downloads": false,
	        "title": "Australia magnetic map 1:2.5m",
	        "thumbnail": false,
	        "dcUris": null,
	        "supportedService": false,
	        "identifier": "a05f7892-b228-7506-e044-00144fdd4fa6",
	        "key": "SR-f4009ce1b63a7b3d13cf33ff610415be"
	    },
	    {
	        "licence": "Commonweath of Australia (Geoscience Australia) ; Creative Commons Attribution 3.0 Australia Licence",
	        "sourceSystemCode": "gcat",
	        "theAbstract": "Outline map of Australia",
	        "metadataLink": "/metadata-gateway/metadata/record/gcat_a05f7892-c39d-7506-e044-00144fdd4fa6/Outline+map+of+Australia",
	        "spatial": {
	            "southLatitude": -44,
	            "valid": true,
	            "westLongitude": 111,
	            "eastLongitude": 160,
	            "allNull": false,
	            "closed": true,
	            "northLatitude": -8
	        },
	        "showPolygon": false,
	        "date": 1104498000000,
	        "listOfKeywords": null,
	        "downloads": true,
	        "title": "Outline map of Australia",
	        "thumbnail": true,
	        "dcUris": [
	            {
	                "protocol": "WWW:LINK-1.0-http--link",
	                "description": "Download  basic outline map of Australia [GIF 5KB]",
	                "name": "Download  basic outline map of Australia [GIF 5KB]",
	                "value": "http://www.ga.gov.au/servlet/BigObjFileManager?bigobjid=GA3029"
	            },
	            {
	                "protocol": "WWW:LINK-1.0-http--link",
	                "description": "Download basic outline map of Australia  [PDF 53KB]",
	                "name": "Download basic outline map of Australia  [PDF 53KB]",
	                "value": "http://www.ga.gov.au/servlet/BigObjFileManager?bigobjid=GA5564"
	            },
	            {
	                "protocol": "WWW:LINK-1.0-http--link",
	                "description": "Related product: Outline map of Australia (with state borders and capital city locations)",
	                "name": "Related product: Outline map of Australia (with state borders and capital city locations)",
	                "value": "https://www.ga.gov.au/products/servlet/controller?event=GEOCAT_DETAILS&catno=61756"
	            },
	            {
	                "protocol": "WWW:LINK-1.0-http--link",
	                "description": "Related product: Outline map of Australia (with state borders)",
	                "name": "Related product: Outline map of Australia (with state borders)",
	                "value": "https://www.ga.gov.au/products/servlet/controller?event=GEOCAT_DETAILS&catno=61755"
	            },
	            {
	                "protocol": "thumbnail",
	                "description": "null",
	                "name": "Basic outline map of Australia",
	                "value": "https://www.ga.gov.au/servlet/BigObjFileManager?bigobjid=GA14468"
	            }
	        ],
	        "supportedService": false,
	        "identifier": "a05f7892-c39d-7506-e044-00144fdd4fa6",
	        "key": "SR-3f8acd838b8bdbd54498220b722e50f4"
	    },
	    {
	        "licence": "Commonweath of Australia (Geoscience Australia) ; Creative Commons Attribution 3.0 Australia Licence",
	        "sourceSystemCode": "gcat",
	        "theAbstract": "This is a proof of concept web service displaying trial samples of historic flood mapping from satellite. Over the next 2 years this service will be developed into a nationwide portal displaying flooding across Australia as observed by satellite since 1987.\n\nThe service shows a summary of water observed by the Landsat-5 and MODIS satellites across Australia for periods between 2000 and 2012.\n\nThe first layer set displays national observed water from MODIS fvrom 2000 to 2012, as derived by Geoscience Australia using an automated flood mapping algorithm. The colouring of the display represents the frequency of observed water in a 500 x 500m grid. The higher the number, the more often water was observed by the satellites over the period. This means that floods have low values, while lakes, dams and other permanent water bodies have high values.\n\nThe three additional layer sets are study areas demonstrating the water observed in each study area by the Landsat-5 satellite, as derived by Geoscience Australia using an automated flood mapping algorithm. The study areas and the observation periods are:\n\nStudy Area 1, Condamine River system between Condamine and Chinchilla, Qld, observed between 2006 and 2011\nStudy Area 2, North-west Victorian rivers between Shepparton and Kerang, observed between 2006 and 2011\nStudy Area 3, Northern Qld rivers, near Normanton, observed between 2003 and 2011\n\nEach Study Area layer set includes a water summary displaying the frequency of observed water in 25 x 25m grids, plus individual flood extents for specific dates where flooding was observed. Similar to the national, MODIS summary, the higher the value, the more often water was observed by the satellites over the period. \n\nLimitations of the Information\n\nThe automated flood mapping algorithm can confuse cloud shadows and snow with flood water, so some areas shown as water may be incorrect. This is a proof of concept dataset and has not been validated.",
	        "metadataLink": "/metadata-gateway/metadata/record/gcat_ce08e530-ace2-4855-e044-00144fdd4fa6/Historical+Flood+Mapping+Proof+of+Concept",
	        "spatial": {
	            "southLatitude": -45,
	            "valid": true,
	            "westLongitude": 112,
	            "eastLongitude": 155,
	            "allNull": false,
	            "closed": true,
	            "northLatitude": -9
	        },
	        "showPolygon": false,
	        "date": 1325336400000,
	        "listOfKeywords": null,
	        "downloads": true,
	        "title": "Historical Flood Mapping Proof of Concept",
	        "thumbnail": false,
	        "dcUris": [
	            {
	                "protocol": "OGC:WMS-1.3.0-http-get-capabilities",
	                "description": "Link to web map service",
	                "name": "Link to web map service",
	                "value": "http://www.ga.gov.au/gisimg/services/earth_observation/NFRIP_Study_Areas/MapServer/WMSServer"
	            },
	            {
	                "protocol": "WWW:LINK-1.0-http--link",
	                "description": "Web service for delivery of Historical Flood Mapping Proof of Concept",
	                "name": "Web service for delivery of Historical Flood Mapping Proof of Concept",
	                "value": "http://www.ga.gov.au/gisimg/rest/services/earth_observation/NFRIP_Study_Areas/MapServer"
	            }
	        ],
	        "supportedService": true,
	        "identifier": "ce08e530-ace2-4855-e044-00144fdd4fa6",
	        "key": "SR-8c2a8cca24cb808d4f0e675ce87ac5ee"
	    },
	    {
			"date" : 1325336400000,
			"dcUris" : [ { "description" : "Link to 50m bathymetry web coverage service",
			    "name" : "Link to 50m bathymetry web coverage service",
			    "protocol" : "OGC:WCS-1.0.0-http-get-capabilities",
			    "value" : "http://www.ga.gov.au/gisimg/services/marine_coastal/Multibeam_50m_Bathymetry_2012/ImageServer/WCSServer"
			  },
			  { "description" : "Link to survey extents web feature service",
			    "name" : "Link to survey extents web feature service",
			    "protocol" : "OGC:WFS-1.1.0-http-get-capabilities",
			    "value" : "http://www.ga.gov.au/gisimg/services/marine_coastal/Multibeam_Survey_Extents_2013/MapServer/WFSServer"
			  },
			  { "description" : "Link to 50m bathymetry web map service",
			    "name" : "Link to 50m bathymetry web map service",
			    "protocol" : "OGC:WMS-1.3.0-http-get-capabilities",
			    "value" : "http://www.ga.gov.au/gisimg/services/marine_coastal/Multibeam_50m_Bathymetry_2012_RGB/MapServer/WMSServer"
			  },
			  { "description" : "Link to survey extents web map service",
			    "name" : "Link to survey extents web map service",
			    "protocol" : "OGC:WMS-1.3.0-http-get-capabilities",
			    "value" : "http://www.ga.gov.au/gisimg/services/marine_coastal/Multibeam_Survey_Extents_2013/MapServer/WMSServer"
			  },
			  { "description" : "Related product: Individual map tile download - SC46",
			    "name" : "Related product: Individual map tile download - SC46",
			    "protocol" : "WWW:LINK-1.0-http--link",
			    "value" : "https://www.ga.gov.au/products/servlet/controller?event=FILE_SELECTION&catno=74564"
			  },
			  { "description" : "Related product: Individual map tile download - SC47",
			    "name" : "Related product: Individual map tile download - SC47",
			    "protocol" : "WWW:LINK-1.0-http--link",
			    "value" : "https://www.ga.gov.au/products/servlet/controller?event=FILE_SELECTION&catno=73852"
			  },
			  ...
			  { "description" : "Related product: Individual map tile download - SO58",
			    "name" : "Related product: Individual map tile download - SO58",
			    "protocol" : "WWW:LINK-1.0-http--link",
			    "value" : "https://www.ga.gov.au/products/servlet/controller?event=FILE_SELECTION&catno=73950"
			  }
			],
			"downloads" : true,
			"identifier" : "bc422803-0ab4-5350-e044-00144fdd4fa6",
			"key" : "SR-a0fa6c6205b8e1eb5aefdb5023031d69",
			"licence" : "Commonwealth of Australia (Geoscience Australia) ; Creative Commons Attribution 3.0 Australia Licence",
			"listOfKeywords" : null,
			"metadataLink" : "/metadata-gateway/metadata/record/gcat_bc422803-0ab4-5350-e044-00144fdd4fa6/50m+Multibeam+Dataset+of+Australia",
			"showPolygon" : false,
			"sourceSystemCode" : "gcat",
			"spatial" : { "allNull" : false,
			  "closed" : true,
			  "eastLongitude" : 174,
			  "northLatitude" : -8,
			  "southLatitude" : -60,
			  "valid" : true,
			  "westLongitude" : 90
			},
			"supportedService" : true,
			"theAbstract" : "This tile contains all multibeam data held by Geoscience Australia on August 2012 within the specified area. The data has been gridded to 50m resolution.  \n\nSome deeper data has also been interpolated within the mapped area.\n\nThe image provided can be viewed on the free software CARIS Easyview, available from the CARIS website: www.caris.com under Free Downloads.",
			"thumbnail" : false,
			"title" : "50m Multibeam Dataset of Australia"
		}
	],
	"numberOfRecordsReturnedText": "10",
	"numberOfRecordsReturned": "10",
	"numberOfRecordsMatchedLargerText": "117881"
	}
	 */
}
