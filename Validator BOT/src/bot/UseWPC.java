package bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

//import org.pathvisio.plugins.SaxonTransformer;
import org.pathvisio.util.FileUtils;
import org.pathvisio.wikipathways.WikiPathwaysCache;


public class UseWPC {
	 
	 static void updateWPC(){
		 		WikiPathwaysCache wpc;
			try {
				
				wpc = new WikiPathwaysCache(new File("C:/Users/kayne/Desktop/WPcache"));
			 	wpc.update(null);
			}catch (Exception e) {
				System.out.println("exception at the wiki update");
				e.printStackTrace();
			}
	 
			System.out.println("the update is complete");
			return;
	 
		
	}
	
	public static void main(String[]  args){
		System.out.println("in the main of useWPC"); 
		File schemaFile=new File("C:/Users/kayne/Desktop/gpml_best_practices.sch");
		 //File  currentPathwayFile=null;
		 File outputFile=new File("C:/Users/kayne/Desktop/botOutput.txt") ;
		 FileWriter fw=null;
		 try {
			 fw=new FileWriter(outputFile);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	
		File cacheDirectory= new File("C:/Users/kayne/Desktop/WPcache"); 
		SaxonTransformer saxTfr=null;
		
		updateWPC();
		
		try {
			saxTfr= new SaxonTransformer();
			
		} catch (TransformerConfigurationException e1) {
			System.out.println("TransformerConfigurationException occured");	
			e1.printStackTrace();
			//return;
				
		}
		System.out.println("after cretaing the saxon transformer"); 

		//SaxonTransformer.setInputFile(currentPathwayFile);
		SaxonTransformer.setschemaFile(schemaFile);
	    
		List<File> files = FileUtils.getFiles(cacheDirectory, "gpml", true);	
	    
		for(File f:files){
			//currentPathwayFile=f;
			SaxonTransformer.setInputFile(f);
		    
			try {
				saxTfr.produceSvrlAndThenParse();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Iterator<String> tempIterator = (saxTfr.diagnosticReference).iterator();
			
			try {
				fw.append("\n"+"\n"+"--------------------------new File: "+f.getName()+" below------------------"+"\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			while(tempIterator.hasNext()){
				//System.out.println(tempIterator.next());
				try {
					fw.append(tempIterator.next()+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
