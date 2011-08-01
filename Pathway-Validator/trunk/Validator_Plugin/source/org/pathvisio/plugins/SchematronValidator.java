package org.pathvisio.plugins;

import gov.nih.nci.lmp.mimGpml.MIMFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.pathvisio.Engine;
import org.pathvisio.model.ConverterException;
import org.pathvisio.model.GpmlFormat;
import org.pathvisio.model.Pathway;
import org.xml.sax.SAXException;

class SchematronValidator {
	
	private ValidatorPlugin vPlugin;
	
	SchematronValidator(ValidatorPlugin vPlugin){
		this.vPlugin = vPlugin;
	}
	
	void exportAndValidate(final SaxonTransformer tempSaxTrnfr,
			final MIMFormat mimf, String schemaFileType, Pathway pwObject, File exportedPwFile,File schemaFile) 
	throws ConverterException,IOException,ParserConfigurationException,
	TransformerException,SAXException{
		
			//if(!schemaFileType.equalsIgnoreCase("groovy")){
				System.out.println("b4 export called: "+schemaFileType);
				//if(!doExport){
				
				if(schemaFileType.equalsIgnoreCase("gpml")){
					GpmlFormat.writeToXml (pwObject, exportedPwFile, true);
					System.out.println("gpml export called");
				}
				else {
					mimf.doExport(exportedPwFile, pwObject);
					System.out.println("mimVis export called");
				}

				//}
				//doExport=false;
				SaxonTransformer.setschemaFile(schemaFile);
				//System.out.println("after mimf export and b4 execute");
				tempSaxTrnfr.produceSvrlAndThenParse();
				System.out.println("after  produce and parsing SVRL");
				printSchematron(vPlugin.eng, vPlugin.graphIdsList, vPlugin.ignoredErrorTypesList, vPlugin.ignoredElements
						,vPlugin.ignoredSingleError,vPlugin.prevSelect );
			//}
			//else runGroovy(grvyObject);
	}
	
	/**
	 * this is used to parse the input Schematron file to derive its name, its default phase 
	 * and set them to corresponding fields in the plugin, and reset the phase combo-box 
	 */
	 void parseSchemaAndSetValues(SAXParser saxParser, Transformer tfr1, File schemaFile,
			JFrame pvFrame, JTextField schemaTitleTag, JComboBox phaseBox) throws SAXException,IOException{

		SchemaHandler mySHandler=new SchemaHandler();
		
		//try {
			saxParser.parse(schemaFile, mySHandler);
		/*} catch (SAXException e) {
			JOptionPane.showMessageDialog(pvFrame,"problem with the Schematron Ruleset",
					"Validator Plugin",JOptionPane.ERROR_MESSAGE);
			resetUI();
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(pvFrame,"problem while accessing Schematron Ruleset",
					"Validator Plugin",JOptionPane.ERROR_MESSAGE);
			resetUI();
			e.printStackTrace();
		}*/
		
		vPlugin.schemaString=mySHandler.getTheTitle();
		VPUtility.cutSchemaTitleString(vPlugin.schemaString,schemaTitleTag);
		//schemaTitleTag.setCaretPosition(0);
		//System.out.println("Schema Title - "+mySHandler.getTheTitle());
		
		String dp = mySHandler.getDefaultPhase();
		tfr1.setParameter("phase",dp);
		//System.out.println("Default Phase - "+dp);
		
		ValidatorPlugin.schemaFileType=mySHandler.getType();
		//System.out.println("Schema Type = "+mySHandler.getType());
		
		//setting phases in the phase-box 
		VPUtility.resetPhaseBox(phaseBox);
		ArrayList<String> phasesList=mySHandler.getPhases();
		Iterator<String> tempIterator= phasesList.iterator();
		while(tempIterator.hasNext()){
			phaseBox.addItem("Phase: "+tempIterator.next());
			//System.out.println(tempIterator.next());
		}
		
		// to determine the index of the phaseBox based on value of default Phase(dp) 
		ValidatorPlugin.changeOfSchema=true;
		int phaseIndex;
		if( (phaseIndex=phasesList.indexOf(dp))!=-1 ){
			phaseBox.setSelectedIndex(phaseIndex+1);
	   	}
		else {
			phaseBox.setSelectedIndex(0);
		}
		ValidatorPlugin.changeOfSchema=false;	
		
	}

	 void printSchematron(Engine eng, ArrayList<String> graphIdsList, ArrayList<String> ignoredErrorTypesList,
			 ArrayList<String> ignoredElements,ArrayList<String> ignoredSingleError, int prevSelect){
			ValidatorPlugin.prevHighlight=true;
			String tempSt,combined,tempsubSt;
			ValidatorPlugin.pth=eng.getActivePathway();
	        Iterator<String> tempIterator = (ValidatorPlugin.saxTfr.diagnosticReference).iterator();
	        int i=0,j=0,k=0,eCount=0,wCount=0;
	        ImageIcon EWIcon=vPlugin.EIcon; 
	        graphIdsList.clear();
	        
	        //reset
	        vPlugin.clearTableRows();
	        eng.getActiveVPathway().resetHighlight();//unhighlight all nodes
	        
	        while (tempIterator.hasNext()) {
	         	tempSt=tempIterator.next();
	         	combined=tempSt;
	         	String[] splitString=tempSt.split("@@");
	         	tempSt=splitString[1];
	         	tempsubSt=splitString[0];
	         	
	         	if(ignoredErrorTypesList.contains(tempSt)||
	         			ignoredElements.contains(tempsubSt)|| ignoredSingleError.contains(combined)) 
	         		continue;
	         	
	         	if(tempSt.startsWith("warning")){ 
	         		EWIcon=vPlugin.WIcon; wCount++;
	         	}
	         	else { 
	         		EWIcon=vPlugin.EIcon;eCount++;
	         	}
	         	
	         	if(prevSelect==0){
	         		vPlugin.mytbm.addRow(new Object[]{EWIcon,++i +".) "+tempSt});
	         		//System.out.println("prevsel 0");
	         	}
	         	else if(prevSelect==1 && tempSt.startsWith("error")){
	         		//System.out.println("prevsel 1");
	         		vPlugin.mytbm.addRow(new Object[]{EWIcon,++j +".) "+tempSt});
	         	}
	         	else if(prevSelect==2 && tempSt.startsWith("warning")){
	         		//System.out.println("prevsel 2");
	         		vPlugin.mytbm.addRow(new Object[]{EWIcon,++k +".) "+tempSt});
	         	}
	         	else{
	         		//System.out.println("not passed"); 
	         		//make tempSt null , so that only the corresponding nodes are highlighted, when selecting the drop down (E / W / E&W)
	         		tempSt=null;
	         	}
	         	
	         	if(tempSt!=null){
	         		graphIdsList.add(tempsubSt);
	         		vPlugin.highlightNode(tempsubSt,vPlugin.col2);
	         	}
	        }
	        
	        vPlugin.eLabel.setText("Errors:"+eCount); 
	        vPlugin.wLabel.setText("Warnings:"+wCount);
	     	
	        //refreshing the pathway , so that all the nodes highlighted appear highlighted
	        eng.getActiveVPathway().redraw();
	        
	        if( (prevSelect==0 && i!=0) || (prevSelect==1 && j!=0) || (prevSelect==2 && k!=0) ){ 
	        	vPlugin.allIgnored=false;// this boolean required for disabling/enabling the right mouse click menuitems
	        }
	        else{ 
	        	switch(prevSelect){
	        	case 0:
	        		vPlugin.mytbm.addRow(new Object[]{"","No Errors and Warnings"});
	        		break;
	        	case 1:	
	        		vPlugin.mytbm.addRow(new Object[]{vPlugin.EIcon,"No Errors"});
	        		break;
	        	case 2:	
	        		vPlugin.mytbm.addRow(new Object[]{vPlugin.WIcon,"No Warnings"});
	        		break;
	        	}
	        	vPlugin.allIgnored=true;
	    		vPlugin.jtb.setEnabled(false);
	        }
	    }

	 
}
