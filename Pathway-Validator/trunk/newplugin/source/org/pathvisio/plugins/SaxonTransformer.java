package org.pathvisio.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
//import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Version;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class SaxonTransformer {

	  private String svrl;
	  private File schemaFile,inputFile;
	  private final TransformerFactory factory;
	/**
	   * An ArrayList to store (String) message of diagnostic-reference found.
	   */
	  public final ArrayList<String> diagnosticReference =  new ArrayList<String>(); 
	  
	  /**
	   * An ArrayList to store (String) message of failed assertion found.
	   */
	  private final ArrayList<String> failedAssertions =  new ArrayList<String>(); 

	  /**
	   * An ArrayList to store (String) message of successful report found.
	   */
	private final ArrayList<String> successfulReports = new ArrayList<String>();

	
    public void setschemaFile(File schemaFile) {
			this.schemaFile = schemaFile;
	}

	public File getschemaFile() {
			return schemaFile;
	}

	public void setInputFile(File inputFile) {
			this.inputFile = inputFile;
	}


	public File getInputFile() {
	
		return inputFile;
		
	}

	
	public InputStream[] getFiles(){
	
		//String isoName = "/XSLs/iso_svrl_for_xslt2.xsl";
	       
		InputStream[] in ={ getClass().getResourceAsStream("/resources/mimschema.sch"),
							getClass().getResourceAsStream("/resources/example.mimml")};
        return in;
	}
	
	public URL getUrlToIso(){
		
		return getClass().getResource("/iso_svrl_for_xslt2.xsl");
		//above line for ECLIPSE build
		
		//below line for ANT JAR BUILD
		//return getClass().getResource("/XSLs/iso_svrl_for_xslt2.xsl");
	}
	
	public SaxonTransformer(){
		
		System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
    	 
		factory = new net.sf.saxon.TransformerFactoryImpl();
        
	}
	
    public  void produceSvrlAndThenParse() throws Exception{   
    	
    	//String schemaSystemId = new File(args[0]).toURL().toExternalForm();
        //String inputFileSystemId = new File(args[1]).toURL().toExternalForm();
    	System.out.println(getUrlToIso());
    	System.out.println("XSLT Version = " + Version.getXSLVersionString());
    	System.out.println("Product Version = " + Version.getProductVersion());
    	
    	failedAssertions.clear();successfulReports.clear();diagnosticReference.clear();
    	
        //InputStream[] in=getFiles();
        
        Source schemaSource =  new StreamSource(schemaFile);
        Source inputSource = new StreamSource(inputFile);
        
        
        Transformer transformer = factory.newTransformer(new StreamSource(getUrlToIso().toString()));
        //File r1,r2;
        StringWriter sw1= new StringWriter();
        Result result1 = new StreamResult(sw1);
        //Result result2 = new StreamResult(r2=File.createTempFile("SVRL_OUTPUT", null));
        //r2.deleteOnExit();
       
        transformer.transform(schemaSource, result1);
        System.out.println("xsl cretaed");
        
        transformer = factory.newTransformer(new StreamSource(new StringReader(sw1.toString())));
        StringWriter sw2=new StringWriter();
        Result result2= new StreamResult(sw2);
        transformer.transform(inputSource, result2);
        System.out.println("svrl cretaed");
        
        //System.out.println(sw.toString());
        svrl=sw2.toString();
        svrl=removeXMLheader(svrl);
        //Logger.log.debug(svrl);
        parseSVRL();
        printMessages();
        
        
    }
    
    private String removeXMLheader(String svrl) {
        
        int firstLineEnd = svrl.indexOf("\n");
        if (svrl.startsWith("<?xml ") 
                    || svrl.startsWith("<?xml ", 1)   
                    || svrl.startsWith("<?xml ", 2)  // Handle Unicode BOM
                    || svrl.startsWith("<?xml ", 3)) {
            return svrl.substring(firstLineEnd + 1);
        } else
            return svrl;
      }

    private void parseSVRL() throws IOException, SAXException, ParserConfigurationException {
        
    	SVRLHandler handler = new SVRLHandler(this.failedAssertions, this.successfulReports, this.diagnosticReference);  
        //Print the every source file name and validation result to console
        //use SVRLHandler class to parse the svrl content
        //System.out.println(this.svrl);
        InputSource is = new InputSource(new StringReader(this.svrl));
        is.setEncoding("UTF-16"); 
        SAXParserFactory.newInstance().newSAXParser().parse(is, handler);
                
      }
    
    private void printMessages(){
    
    	Iterator<String> tempIterator = diagnosticReference.iterator();
    	while (tempIterator.hasNext()) {
    	//Logger.log.debug(tempIterator.next());
    	System.out.println(tempIterator.next());
    	}
        
    	
    }

}