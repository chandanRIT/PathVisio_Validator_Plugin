package bot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

//import org.pathvisio.plugins.SaxonTransformer;
import org.pathvisio.util.FileUtils;
import org.pathvisio.wikipathways.WikiPathwaysCache;


public class UseWPC {

	static void updateWPC(File wpcPath){
		WikiPathwaysCache wpc;
		try {

			wpc = new WikiPathwaysCache(new File(wpcPath.getAbsolutePath()));
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
		System.out.println("Usage: arg0: schemaFile path, arg1:cache Directory path, arg2: output-report file full path");
		File schemaFile=new File(args[0]);
		//File  currentPathwayFile=null;
		File cacheDirectory= new File(args[1]); 
		File outputFile=new File(args[2]) ;
		FileWriter fw=null;
		try {
			fw=new FileWriter(outputFile);
			fw.write("");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			System.out.println("exception with the filewriter to report file");
			e2.printStackTrace();
		}

		SaxonTransformer saxTfr=null;

		updateWPC(cacheDirectory);

		try {
			saxTfr= new SaxonTransformer(SAXParserFactory.newInstance().newSAXParser());

		} catch (Exception e1) {
			System.out.println("TransformerConfigurationException occured");	
			e1.printStackTrace();
			//return;

		}
		System.out.println("after cretaing the saxon transformer"); 

		//SaxonTransformer.setInputFile(currentPathwayFile);
		saxTfr.setschemaFile(schemaFile);

		List<File> files = FileUtils.getFiles(cacheDirectory, "gpml", true);	

		for(File f:files){
			//currentPathwayFile=f;
			saxTfr.setInputFile(f);

			try {
				saxTfr.produceSvrlAndThenParse();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				fw.append("\n"+"\n"+"----------------------new File: "+f.getName()+" below------------------"+"\n\n");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for(String diagRef : saxTfr.getHandler().getDiagnosticReference()){
				//System.out.println(tempIterator.next());
				try {
					fw.append(diagRef+"\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
