<?xml version="1.0"?>
<xsl:stylesheet version="1.1" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:csw="http://www.opengis.net/cat/csw"
xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
xmlns:wrs="http://www.opengis.net/cat/wrs"
xmlns:gml="http://www.opengis.net/gml"
>
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
  <xsl:template match="/">
      <GetRecordsResponse>
       <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults">
           <numberOfRecordsMatched><xsl:value-of select="@numberOfRecordsMatched"/></numberOfRecordsMatched>
           <numberOfRecordsReturned><xsl:value-of select="@numberOfRecordsReturned"/></numberOfRecordsReturned>
           <nextRecord><xsl:value-of select="@nextRecord"/></nextRecord>
           <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/rim:RegistryObject">
            <Record>
                <title><xsl:value-of select="rim:Name/rim:LocalizedString/@value"/></title>
                <description><xsl:value-of select="rim:Description/rim:LocalizedString/@value"/></description>
                <identifier><xsl:value-of select="@id"/></identifier>
                <references>
                	<xsl:for-each select="dct:references">
                		<reference><xsl:copy-of select="@*" /><xsl:value-of select="."/></reference>
                	</xsl:for-each>
				</references>
                <boundingBox>
                	<latlon>0</latlon>
                    <lowerCorner>
                        <xsl:value-of select="rim:Slot/wrs:ValueList/wrs:AnyValue/gml:Envelope/gml:lowerCorner"/>
                    </lowerCorner>
                    <upperCorner>
                        <xsl:value-of select="rim:Slot/wrs:ValueList/wrs:AnyValue/gml:Envelope/gml:upperCorner"/>
                    </upperCorner>
                </boundingBox>
            </Record>
           </xsl:for-each>
       </xsl:for-each>
       </GetRecordsResponse>
  </xsl:template>
</xsl:stylesheet>