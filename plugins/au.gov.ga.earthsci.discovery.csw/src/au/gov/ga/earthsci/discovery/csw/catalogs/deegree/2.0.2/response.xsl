<?xml version="1.0"?>
<xsl:stylesheet version="1.1" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
xmlns:dct="http://purl.org/dc/terms/" 
xmlns:gmd="http://www.isotc211.org/2005/gmd"
xmlns:dc="http://purl.org/dc/elements/1.1/"
xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/"
>
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:template match="/">
        <GetRecordsResponse>
            <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults">
                <numberOfRecordsMatched>
                    <xsl:value-of select="@numberOfRecordsMatched"/>
                </numberOfRecordsMatched>
                <numberOfRecordsReturned>
                    <xsl:value-of select="@numberOfRecordsReturned"/>
                </numberOfRecordsReturned>
                <nextRecord>
                    <xsl:value-of select="@nextRecord"/>
                </nextRecord>
                <xsl:for-each select="/csw:GetRecordsResponse/csw:SearchResults/dc:metadata">
                    <Record>
                        <title>
                            <xsl:value-of select="dc:title"/>
                        </title>
                        <description>
                            <xsl:value-of select="dc:description"/>
                        </description>
                        <identifier>
                            <xsl:value-of select="dc:identifier"/>
                        </identifier>
                        <references>
		                	<xsl:for-each select="dct:references">
		                		<reference><xsl:copy-of select="@*" /><xsl:value-of select="."/></reference>
		                	</xsl:for-each>
						</references>
                        <boundingBox>
                            <latlon>1</latlon>
                            <lowerCorner>
                                <xsl:value-of select="dc:spatial/dcmiBox:Box/dcmiBox:westlimit"/>&#160;<xsl:value-of select="dc:spatial/dcmiBox:Box/dcmiBox:southlimit"/>
                            </lowerCorner>
                            <upperCorner>
                                <xsl:value-of select="dc:spatial/dcmiBox:Box/dcmiBox:eastlimit"/>&#160;<xsl:value-of select="dc:spatial/dcmiBox:Box/dcmiBox:northlimit"/>
                            </upperCorner>
                        </boundingBox>
                    </Record>
                </xsl:for-each>
            </xsl:for-each>
        </GetRecordsResponse>
    </xsl:template>
</xsl:stylesheet>