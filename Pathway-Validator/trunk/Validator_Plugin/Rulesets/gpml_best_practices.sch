<?xml version="1.0" encoding="UTF-8"?>
<!-- 

Schematron validation for GPML Best Practices

@author Augustin Luna
@version 9 June 2011
-->
<iso:schema    
  xmlns:iso="http://purl.oclc.org/dsdl/schematron"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  defaultPhase="#ALL"
  schemaVersion="0.1">
     
	<iso:ns prefix="gpml" uri="http://genmapp.org/GPML/2010a"/>
	<iso:ns prefix="bp" uri="http://www.biopax.org/release/biopax-level3.owl#"/>
	
	<iso:title>GPML Best Practices</iso:title>

	<iso:pattern name="check-title" id="check-title">
		<iso:rule context="gpml:Pathway" role="error">
			<iso:assert test="@Name">Diagrams should have a title.</iso:assert>
		</iso:rule> 
	</iso:pattern> 

	<iso:pattern name="check-author" id="check-author">
		<iso:rule context="gpml:Pathway" role="warning">
			<iso:assert test="@Author">Diagrams should have an author.</iso:assert>
		</iso:rule> 
	</iso:pattern> 

	<iso:pattern name="check-organism" id="check-organism">
		<iso:rule context="gpml:Pathway" role="warning">
			<iso:assert test="@Organism">Diagrams should have a organism.</iso:assert>
		</iso:rule> 
	</iso:pattern> 
	
	<iso:pattern name="check-pub-xref" id="check-pub-xref">
		<iso:rule context="gpml:Pathway" role="error">
			<iso:assert test="gpml:Biopax">Diagrams should have references.</iso:assert>
		</iso:rule> 
	</iso:pattern> 
	
	<iso:pattern name="check-db-xref" id="check-db-xref">
		<iso:rule context="gpml:DataNode" role="error">
			<iso:let name="id" value="@id"/>

			<iso:assert test="Xref[not(@Database='')] and Xref[not(@ID='')]" diagnostics="id">Datanodes should include database annotations.</iso:assert>
		</iso:rule> 
	</iso:pattern> 

	<iso:pattern name="check-labels" id="check-labels">
		<iso:rule context="gpml:DataNode" role="error">
			<iso:let name="id" value="@id"/>

			<iso:assert test="not(@TextLabel='')" diagnostics="id">DataNodes should have a text label.</iso:assert>
		</iso:rule> 
	</iso:pattern> 

	<iso:pattern name="check-unattached-lines" id="check-unattached-lines">
		<iso:rule context="gpml:Line" role="error">
			<iso:let name="id" value="@id"/>
			<iso:let name="first-point" value="gpml:Graphics/gpml:Point[position()=1]"/>
			<iso:let name="last-point" value="gpml:Graphics/gpml:Point[last()]"/>
			
			<iso:assert test="first-point/@GraphRef and last-point/@GraphRef" diagnostics="id">Lines should not be unattached.</iso:assert>
		</iso:rule> 
	</iso:pattern> 
	
	<iso:diagnostics>
		<iso:diagnostic id="id"><iso:value-of select="$id"/></iso:diagnostic> 				
	</iso:diagnostics> 
</iso:schema>