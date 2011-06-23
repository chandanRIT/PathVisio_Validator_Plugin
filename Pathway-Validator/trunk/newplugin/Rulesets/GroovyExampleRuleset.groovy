import java.util.ArrayList;

import org.pathvisio.model.ObjectType;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement;


public class RuleSetExample2  {
	
    public ArrayList<Object> main(Pathway e) { // This method must be present in a groovy file (containing other rules), which is to be used with the ValidatorPlugin 
        
    	ArrayList<Object> results= new ArrayList<Object>(); //this holds the final results from all the rules (containing only the failed results)
        
    	String[] r1=ruleTitle(e);
        String[] r2=ruleOrganism(e);
        String[] r3=ruleAuthor(e);
        String[] r4=ruleReferences(e);
       
        ArrayList<String[]> r5=ruleTextLabel(e);
        ArrayList<String[]> r6=ruleUnattachedLines(e);
        ArrayList<String[]> r7=ruleDataBaseAnnotation(e);
        
        //only the failed "result" from all the rules in the groovy file are added to the final "results" variable
        if(r1!=null)
        	results.add(r1);
        if(r2!=null)
        	results.add(r2);
        if(r3!=null)
        	results.add(r3);
        if(r4!=null)
        	results.add(r4);
        if(r5!=null)
        	results.add(r5);
        if(r6!=null)
        	results.add(r6);
        if(r7!=null)
        	results.add(r7);
        	
    	return results;
    }
    public String[] ruleTitle(Pathway pw) { //checks for the "Title" attribute in the "Pathway" tag
    
    	String[] result=null;
    	
		if(pw.getMappInfo().getMapInfoName()==null){
			result= new String[3];						// String Array created to hold the result from rule (may contain 0.)role  1.)Diagnostic message   2.)GraphId  )
			result[0]="warning"; 						// zeroth element in the string array must always contain the role (error/warning)
			result[1]="Diagrams should have a title.";  // first element must always contain the Diagnostic message
			//result[2]=pw.getMappInfo().getGraphId();  // the second element must always the contain the graphId
		}
		else System.out.println("title found = "+pw.getMappInfo().getMapInfoName());
    		
		return result; //return the result as null if it passes the rule (i.e nor error/warning)
    }
    
    public String[] ruleOrganism(Pathway pw) { // checks for the "Organism" attribute in the "Pathway" tag
        
    	String[] result=null;
	
    	if(pw.getMappInfo().getOrganism()==null){
    		result= new String[3];
    		result[0]="error";
    		result[1]="Diagrams should have an organism.";
		}
    	else System.out.println("organism found = "+pw.getMappInfo().getOrganism());
    		
		return result;
    }
    
    public String[] ruleAuthor(Pathway pw) { //checks for the "Author" attribute in the "Pathway" tag
        
    	String[] result=null;
	
    	if(pw.getMappInfo().getAuthor()==null){
    		result= new String[3];
    		result[0]="warning";
    		result[1]="Diagrams should have an author.";
		}
    	else System.out.println("author found = "+pw.getMappInfo().getAuthor());
    		
		return result;
    }
    
    public String[] ruleReferences(Pathway pw) { // this rule checks if a "biopax" tag is present under the "Pathway" tag 
        
    	String[] result=null;
	
    	if(pw.getBiopax()==null ){
			result= new String[3];
			result[0]="warning";
			result[1]="Diagrams should have references.";
		}
    	else System.out.println("passed the Biopax rule i.e references rule");
    		
		return result;
    }
    
    
    public ArrayList<String[]> ruleDataBaseAnnotation(Pathway pw) { //checks every "Xref" tag (under the "DataNode" tag) for non-empty "Database" and "Id" attributes 
        
    	ArrayList<String[]> totalResultForThisRule=null;
    	
    	for(PathwayElement pwe: pw.getDataObjects()){
    		
    		if(pwe.getObjectType()==ObjectType.DATANODE && 
    				( pwe.getXref().getDataSource()== null | pwe.getXref().getDataSource().equals("") |
    						pwe.getXref().getId()== null) | pwe.getXref().getId().equals("")){
    			
    			if(totalResultForThisRule==null){
    				totalResultForThisRule=new ArrayList<String[]>();
    			}
    			
    			String[] result= new String[3];
				result[0]= "error";
				result[1]= "Datanodes should include database annotations.";
				result[2]= pwe.getGraphId();
				
				totalResultForThisRule.add(result);
    		}
    		
    	}
    	
    	if(totalResultForThisRule==null) System.out.println("All the datanodes include database annotations");
    
    	return totalResultForThisRule;
    }

    
    
   public ArrayList<String[]> ruleTextLabel(Pathway pw) { //checks every "DataNode" tag for a "TextLabel" attribute  
        
    	ArrayList<String[]> totalResultForThisRule=null;
    	
    	for(PathwayElement pwe: pw.getDataObjects()){
    		
    		if(pwe.getObjectType()==ObjectType.DATANODE && 
    				( pwe.getTextLabel()==null | pwe.getTextLabel().equals("") ) ){
    			
    			if(totalResultForThisRule==null){
    				totalResultForThisRule=new ArrayList<String[]>();
    			}
    			
    			String[] result= new String[3];
				result[0]= "error";
				result[1]= "DataNodes should have a text label.";
				result[2]= pwe.getGraphId();
				
				totalResultForThisRule.add(result);
    		}
    		
    	}
    	
    	if(totalResultForThisRule==null) System.out.println("All the datanodes have textlabels");
    
    	return totalResultForThisRule;
    }

    
    public ArrayList<String[]> ruleUnattachedLines(Pathway pw) { //checks every "Line" tag for its first and last "GraphRef" attributes under the "Point" tag (which is under "Graphics" tag)  
        
    	ArrayList<String[]> totalResultForThisRule=null;
    	
    	for(PathwayElement pwe: pw.getDataObjects()){
    		
    		if(pwe.getObjectType()==ObjectType.LINE && 
    				( pwe.getStartGraphRef()==null | pwe.getEndGraphRef()==null 
    						| pwe.getEndGraphRef().equals("") | pwe.getStartGraphRef().equals("") ) ){
    			
    			if(totalResultForThisRule==null){
    				totalResultForThisRule=new ArrayList<String[]>();
    			}
    			
    			String[] result= new String[3];
				result[0]= "error";
				result[1]= "Lines should be attached at both ends.";
				result[2]= pwe.getGraphId();
				
				totalResultForThisRule.add(result);
    		}
    		
    	}
    	
    	if(totalResultForThisRule==null) System.out.println("All the lines are attached");
    
    	return totalResultForThisRule;
    }
    
    
}
