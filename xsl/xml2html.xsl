<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/REC-html40">
<xsl:output method="html"/>
	<xsl:template match ="fragenkatalog">
		<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
			<title>Fragenkatalog</title>
		</head>
		<body>
			<h1>Fragenkatalog: <xsl:value-of select="@name" /></h1>
			<h4>Anzahl Fragen: <xsl:value-of select="@fragenanzahl" /></h4>
			<xsl:apply-templates/>
		</body>
		</html>
	</xsl:template>
	
	<xsl:template match="fragenblock">	
		<hr />
		<h3><xsl:value-of select="frage" /></h3>
		<p>Timeout: <xsl:value-of select="@timeout" /></p>
		<ul>
			<xsl:for-each select="antwort">
				<li>
					<xsl:value-of select="." />
					<xsl:if test="@richtig = 'true'">
						<span> (richtig)</span>
					</xsl:if>					
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>
</xsl:stylesheet>