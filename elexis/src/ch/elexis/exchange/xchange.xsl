<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xChange="http://informatics.sgam.ch/xChange"
>
<xsl:variable name="senderID" select="/xChange:xChange/attribute::origin"/>
<xsl:template match="/">
    
  <html>
  <body>
    <h1>Elexis-KG-Auszug</h1>
    <h2>Datum :<xsl:value-of select="/xChange:xChange/attribute::timestamp"/></h2>
     <xsl:apply-templates select="/xChange:xChange"/>
    <xsl:for-each select="xChange:xChange/xChange:contacts/xChange:contact">
        <xsl:if test="@id != $senderID">
            **
            <xsl:value-of select="@lastname"/>
            <xsl:text>  </xsl:text>
            <xsl:value-of select="@firstname"/><br/>
        </xsl:if>
    </xsl:for-each>
  </body>
  </html>
</xsl:template>

<!--  xsl:template match="//xChange:contact[@id=string(/xChange:xChange/attribute::origin)]">
<b><xsl:value-of select="@lastname"/></b>
</xsl:template -->

<xsl:template match="/xChange:xChange">
    
    <xsl:for-each select="//xChange:contact[@id=$senderID]">
    <h3>Absender:  <xsl:value-of select="@lastname"/><xsl:text> </xsl:text><xsl:value-of select="@firstname"/></h3>
    </xsl:for-each>
</xsl:template>

</xsl:stylesheet>