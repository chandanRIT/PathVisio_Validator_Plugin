package org.pathvisio.plugins;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.codehaus.groovy.control.CompilationFailedException;
import org.pathvisio.Engine;
import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;
import org.pathvisio.view.VPathwayElement;

public class GroovyValidator {

	private Engine eng;
	private JComboBox phaseBox;
	private ArrayList<String> graphIdsList;
	//private ImageIcon EIcon,WIcon;
	private ValidatorPlugin vPlugin;
	//private ValidatorPlugin.MyTableModel mytbm;
	//private JTable jtb;
	
public GroovyValidator(ValidatorPlugin vPlugin,Engine eng,JComboBox phaseBox,ArrayList<String> graphIdsList) {
	// TODO Auto-generated constructor stub
	this.eng=eng;
	this.phaseBox=phaseBox;
	this.graphIdsList=graphIdsList;
	this.vPlugin=vPlugin;
}
	
 void sortGroovyResultsAndPrint(ArrayList<Object> tempList){
		
		Iterator<Object> tempIterator = tempList.iterator();
		int counter=0;
		String tempSt,graphId,combinedStrings;
		String[] tempArray;
		int[] ijkew={0,0,0,0,0};
		
		//clear and reset 
		vPlugin.mytbm.setRowCount(0);
		vPlugin.jtb.setEnabled(true);
		graphIdsList.clear();
		
		eng.getActiveVPathway().resetHighlight();//unhighlight all nodes
	    
		while (tempIterator.hasNext()) {
	         	
			 Object tempObject = tempIterator.next();
	         counter++;
	         
	         if( tempObject instanceof ArrayList){
	         		
	         		System.out.println("Array list detected in the result");
	         		
	         		for(String[] sa: (ArrayList<String[]>)tempObject){
	         		
	         			if(sa[0]==null) sa[0]="error"; // default role is null, if role is not set
	         			graphId=sa[2];
	         			tempSt=sa[0]+" - "+sa[1];
	         			combinedStrings=graphId+"@@"+tempSt;
	         			if(vPlugin.ignoredErrorTypesList.contains(tempSt)||
	         					vPlugin.ignoredElements.contains(graphId)|| vPlugin.ignoredSingleError.contains(combinedStrings)) 
	         				continue;
	         		
	         			printGroovy(tempSt,graphId,ijkew);
	         		}
	         		
	         	}
	         	
	         else {
	         		System.out.println("String Array detected "+counter);
	         		tempArray= (String[])tempObject;
	         		
	         		if(tempArray[0]==null) tempArray[0]="error";
	         		
	         		graphId=tempArray[2];
	         		tempSt=tempArray[0]+" - "+tempArray[1];
	         		combinedStrings=graphId+"@@"+tempSt;
	         		if(vPlugin.ignoredErrorTypesList.contains(tempSt)|| 
	         				vPlugin.ignoredElements.contains(graphId)|| vPlugin.ignoredSingleError.contains(combinedStrings) ) 
	         			continue;
	         		
	         		printGroovy(tempSt,graphId,ijkew);
	         }
	      
		 }
		
		vPlugin.eLabel.setText("Errors:"+ijkew[3]); vPlugin.wLabel.setText("Warnings:"+ijkew[4]);
		
		//refreshing the pathway , so that all the nodes highlighted appear highlighted
		//VPathway vpwTemp = eng.getActiveVPathway();
		//vpwTemp.setPctZoom(vpwTemp.getPctZoom());
		eng.getActiveVPathway().redraw();
        
        if( (ValidatorPlugin.prevSelect==0 && ijkew[0]!=0) || (ValidatorPlugin.prevSelect==1 && ijkew[1]!=0) || (ValidatorPlugin.prevSelect==2 && ijkew[2]!=0) ){ 
        	//jta.setText(sbf.toString());
        	vPlugin.allIgnored=false;
        }
        else if(ValidatorPlugin.prevSelect==0){
        	//jta.setText("<b><font size='4' face='verdana'>No Errors and Warnings</font></b>");
        	vPlugin.mytbm.addRow(new Object[]{"","No Errors and Warnings"});
        	vPlugin.allIgnored=true;
        	vPlugin.jtb.setEnabled(false);
        }
        else if(ValidatorPlugin.prevSelect==1){
        	//jta.setText("<b><font size='4' face='verdana'>No Errors</font></b>");	
        	vPlugin.mytbm.addRow(new Object[]{vPlugin.EIcon,"No Errors"});
        	vPlugin.allIgnored=true;
        	vPlugin.jtb.setEnabled(false);
        }
        else if(ValidatorPlugin.prevSelect==2){
        	//jta.setText("<b><font size='4' face='verdana'>No Warnings</font></b>");	
        	vPlugin.mytbm.addRow(new Object[]{vPlugin.WIcon,"No Warnings"});
        	vPlugin.allIgnored=true;
        	vPlugin.jtb.setEnabled(false);
        }
        
        System.out.println("-----------groovy part end-------------- ");
        
}
	
	private void printGroovy(String tempSt,String graphId,int[] ijkew){
		
		ValidatorPlugin.prevHighlight=true;
		VPathwayElement vpe=null;
		PathwayElement pe; 
		//String imageUrl=imageUrlE;
		ImageIcon EWIcon=vPlugin.EIcon;
		ValidatorPlugin.pth=eng.getActivePathway();
        //int higco=0; 
        
        if(tempSt.startsWith("warning")){ EWIcon=vPlugin.WIcon; ijkew[4]++;}else { EWIcon=vPlugin.EIcon; ijkew[3]++;}
		
		if(ValidatorPlugin.prevSelect==0){
			//System.out.println("prevsel 0");
			vPlugin.mytbm.addRow(new Object[]{EWIcon,++ijkew[0] +".) "+tempSt});
        }
		else if(ValidatorPlugin.prevSelect==1 && tempSt.startsWith("error")){
			//System.out.println("prevsel 1");
			vPlugin.mytbm.addRow(new Object[]{EWIcon,++ijkew[1] +".) "+tempSt});
		}
		else if(ValidatorPlugin.prevSelect==2 && tempSt.startsWith("warning")){
			//System.out.println("prevsel 2");
			vPlugin.mytbm.addRow(new Object[]{EWIcon,++ijkew[2] +".) "+tempSt}); 
		}
		else{
			//System.out.println("not passed"); 
			//make tempSt null , so that only the corresponding nodes are highlighted, when selecting the drop down (E / W / E&W)
			graphId=null;tempSt=null;
			
         }
        if(tempSt!=null){
        	graphIdsList.add(graphId+"");	
        }
		
        if(graphId!=null){
				
         	pe=ValidatorPlugin.pth.getElementById(graphId);
         	
         	if(pe!=null) {
         		vpe=eng.getActiveVPathway().getPathwayElementView(pe);
         		vpe.highlight(ValidatorPlugin.col2);
         		ValidatorPlugin.prevPwe=vpe;
         	}
         	else System.out.println("no available graphId @ id: "+graphId);
         }
      
	}
	
	 GroovyObject loadGroovy(File schemaFile){
		
		System.out.println("reached inside loadGroovy method");
		ArrayList<String[]> tempArray;//=new ArrayList<String[]>();
		
  	   	GroovyClassLoader loader =  new GroovyClassLoader(getClass().getClassLoader());
  	   	Class<GroovyObject> groovyClass=null;
  	   	GroovyObject groovyObject=null;
  	   
  	   	try {
  		   groovyClass = loader.parseClass(schemaFile);
  		   vPlugin.schemaString=groovyClass.getSimpleName();
  		   ValidatorPlugin.schemaTitleTag.setText("Schema Title: "
  				   +VPUtility.cutTitleString(vPlugin.schemaString,ValidatorPlugin.schemaTitleTag));
  		   ValidatorPlugin.schemaTitleTag.setCaretPosition(0);
 		   groovyObject = (GroovyObject) groovyClass.newInstance();
  	   	}
  	   	catch (Exception e1) {
  		   System.out.println("Exception @ groovy = "+e1.getMessage());
  		 JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
					"problem with the Groovy Ruleset","Validator Plugin",JOptionPane.ERROR_MESSAGE);
		   e1.printStackTrace();
  	   	}
  	   	
  	   	VPUtility.resetPhaseBox(phaseBox);
	   
	   	try{
	   		tempArray=(ArrayList<String[]>)(groovyObject.invokeMethod("phaseSupport", null));
	   	}
	   	catch(Exception e){System.out.println("phaseSupport method not present"); return groovyObject;}
	   	
	   	Iterator<String[]> tempIterator= tempArray.iterator();
	   	while(tempIterator.hasNext()){
	   		phaseBox.addItem("Phase: "+(tempIterator.next())[0]);
	   		//System.out.println(tempIterator.next());
	   	}

	   	return groovyObject;
	}
	
	 void runGroovy(GroovyObject groovyObject){
		
		System.out.println("--------------groovy---------------");
		ArrayList<Object> tempArray=new ArrayList<Object>();
		//phaseBoxSelection=2;
  	     	   
  	   	Pathway argPw= eng.getActivePathway();
  	   	
  	   	//checking every line element for graphId before sending the pathway for validation, generate graphId if graphId is not found
  	   	for(PathwayElement pwe: argPw.getDataObjects()){
  		
  	   		if( pwe.getObjectType()==ObjectType.LINE && ( pwe.getGraphId()=="" | pwe.getGraphId()==null) ){
  	   			pwe.setGeneratedGraphId();
  	   		}
  		}
  	   	
  	   	if(phaseBox.getSelectedIndex()==0){
  	   		//if(argPw!=null){
  	   		/*Object[] args = {argPw};
  	   		tempArray=(ArrayList<Object>)(groovyObject.invokeMethod("main", args));*/
  	   	
  	   		//code for running groovy script from java   
  	   		Binding binding = new Binding();
  	   		binding.setVariable("groovyObject", groovyObject);
  	   		binding.setVariable("tempArray", tempArray);
  	   		binding.setVariable("argPw",argPw );
  	   		GroovyShell shell = new GroovyShell(binding);
  
  	   		try {
  	   			//running groovy script from a file named GroovyScriptKC.kc
  	   			shell.evaluate(getClass().getResourceAsStream("/GroovyScriptKC.kc"));
  	   		} catch (CompilationFailedException e) {
  	   			System.out.println("CompilationFailedException in the groovyshell code");
  	   			JOptionPane.showMessageDialog(vPlugin.desktop.getFrame(), 
					"Validation Exception in Groovy","Validator Plugin",JOptionPane.ERROR_MESSAGE);
			
  	   			e.printStackTrace();
  	   		} 
  	   	}
  	   	
  	   	else { // this code runs only when there are phases present in the groovy rule 
  	   		
  	   		ArrayList<String[]> phaseTotal;//=new ArrayList<Object>();
  	   		phaseTotal=(ArrayList<String[]>)(groovyObject.invokeMethod("phaseSupport", null));
  	   		String methodNamesWithCommas=  ((String[])phaseTotal.get(phaseBox.getSelectedIndex()-1))[1];
  	   		String[] methodNamesArray= methodNamesWithCommas.split( ",\\s*" );
  	   		for(String methodName :methodNamesArray)
  	   			tempArray.add(groovyObject.invokeMethod(methodName.trim(), argPw));
    	}
  	   	
  	   	//remove null results from the overall result from the ruleset
  	   	while(tempArray.contains(null)){
  	   		tempArray.remove(null);
  	   	}
  	   	
  	   	ValidatorPlugin.globGroovyResult=tempArray; 	   	
  	   	sortGroovyResultsAndPrint(ValidatorPlugin.globGroovyResult);
 	}

}
