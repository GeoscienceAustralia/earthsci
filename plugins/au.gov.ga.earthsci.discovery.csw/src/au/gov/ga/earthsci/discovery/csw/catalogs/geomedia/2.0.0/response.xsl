<?xml version="1.0"?>
<xsl:stylesheet version="1.1" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:template match="/">
        <GetRecordsResponse>
            <xsl:for-each select="GetRecordsResponse/SearchResults">
                <numberOfRecordsMatched>
                    <xsl:value-of select="@numberOfRecordsMatched"/>
                </numberOfRecordsMatched>
                <numberOfRecordsReturned>
                    <xsl:value-of select="@numberOfRecordsReturned"/>
                </numberOfRecordsReturned>
                <nextRecord>
                    <xsl:value-of select="@nextRecord"/>
                </nextRecord>
                <xsl:for-each select="/GetRecordsResponse/SearchResults/Record">
                    <Record>
                        <title><xsl:value-of select="Title"/></title>
                        <description><xsl:value-of select="Abstract"/></description>
                        <identifier><xsl:value-of select="Title"/></identifier>
                        <references>
		                	<xsl:for-each select="dct:references">
		                		<reference><xsl:copy-of select="@*" /><xsl:value-of select="."/></reference>
		                	</xsl:for-each>
						</references>
                        <boundingBox>
                            <latlon>1</latlon>
                            <lowerCorner>
                                <xsl:value-of select="Spatial/@WestBoundLongitude"/>&#160;<xsl:value-of select="Spatial/@SouthBoundLatitude"/>
                            </lowerCorner>
                            <upperCorner>
                                <xsl:value-of select="Spatial/@EastBoundLongitude"/>&#160;<xsl:value-of select="Spatial/@NorthBoundLatitude"/>
                            </upperCorner>
                        </boundingBox>
                    </Record>
                </xsl:for-each>
            </xsl:for-each>
        </GetRecordsResponse>
    </xsl:template>
</xsl:stylesheet>