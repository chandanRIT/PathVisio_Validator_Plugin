/*
 
Open Source Initiative OSI - The MIT License:Licensing
[OSI Approved License]

The MIT License

Copyright (c) Rick Jelliffe, Topologi Pty. Ltd, Allette Systems 

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package myplugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Stores the results of the schematron
 * 
 * @author Christophe lauret
 * @author Willy Ekasalim
 * 
 * @version 14 February 2007
 */
public final class SchematronResult {

  /**
   * Store the source file name or systemID
   */
  private String systemID;
  
  /**
   * Store the SVRL content
   */
  private String svrl;

  /**
   * An ArrayList to store (String) message of failed assertion found.
   */
  private final ArrayList<String> failedAssertions =  new ArrayList(); 

  /**
   * An ArrayList to store (String) message of successful report found.
   */
  private final ArrayList<String> successfulReports = new ArrayList();

  /**
   * Constructor of SchematronResult that accept the source file name (or systemID)
   * 
   * @param systemID The system ID of the XML for which this result instance is built.
   */
  public SchematronResult(String systemID) {
    this.systemID = systemID;
  }

  /**
   * @return this object's systemID.
   */
  public String getSystemID() {
    return this.systemID;
  }

  /**
   * @return <code>true</code> if there's no failed assertion;
   *         <code>false</code> if there is failed assertion
   */
  public boolean isValid() {
    return this.failedAssertions.size() == 0;
  }

  /**
   * Setter for SVRL content/file, also parse the SVRL content to extract the failed assertion and the succesfull report
   * 
   * @param svrl The corresponding generated by SVRL.
   * 
   * @throws BuildException Should an error occur whilst parsing.
   */
  public void setSVRL(String svrl) throws BuildException {
    this.svrl = removeXMLheader(svrl);
    try {
      parseSVRL();
    } catch (Exception ex) {
        // if there is a parse error then it is probably a different metastylesheet
        this.svrl=svrl;
        //was throw new BuildException("Error on parsing SVRL content: " + ex.getMessage());
        
    }
  }

  /**
   * @return SVRL content as String representation. 
   */
  public String getSVRLAsString() {
    return this.svrl;
  }

  /**
   * Print the failed assertion message only. This method is used only when failOnError is set to true.
   * @param task SchematronTask object for message logging
   */
  public void printFailedMessage(SchematronTask task) {
    if (this.failedAssertions.size() > 0) {
      task.log(("Source file: " + removePath(this.systemID)));
      for (int i = 0; i < this.failedAssertions.size(); i++) {
        task.log((String)this.failedAssertions.get(i));
      }
    }
  }
  

  /**
   * Print the failed assertion message only. This method is used for Pageseeder Schematron Validation error output.
   * 12/11/2007 Xin Chen
   */
  public String getFailedMessage() {
        String erroutput = "";
    if (this.failedAssertions.size() > 0) {
      for (int i = 0; i < this.failedAssertions.size(); i++) {
        erroutput += ((String)this.failedAssertions.get(i));
      }
    }
    return erroutput;
  }
  
  /**
   * Print both failed assertion and successfull report message to the console.
   * @param task SchematronTask object for message logging
   */
  public void printAllMessage(SchematronTask task) {

    if (this.failedAssertions.size() > 0 || this.successfulReports.size() > 0) {

      task.log(("Source file: " + removePath(this.systemID))); 
     
      
      // TRYING TO ACCESS STRING s AS A STRING RESULTS IN ERROR

      for (String s: this.failedAssertions) {
         try {   
         String theAssertion = s ; 
       task.log((String) theAssertion );
          } catch (Exception ex) {
 
          }
      }
 
      for (int i = 0; i < this.successfulReports.size(); i++) {
        task.log((String)this.successfulReports.get(i));
      }
    }
 
  }

// private helper ---------------------------------------------------------------------------------
  
  /**
   * Parse the SVRL content to extract any failed or success message.
   * The message will be stored in failedAssertions and successfulReports by SVRL handler.
   */
  private void parseSVRL() throws IOException, SAXException, ParserConfigurationException {
    SVRLHandler handler = new SVRLHandler(this.failedAssertions, this.successfulReports);  
    //Print the every source file name and validation result to console
    //use SVRLHandler class to parse the svrl content
    //System.out.println(this.svrl);
    InputSource is = new InputSource(new StringReader(this.svrl));
    is.setEncoding("UTF-16"); 
    SAXParserFactory.newInstance().newSAXParser().parse(is, handler);
            
  }
  
  /**
   * Given an XML content, remove the XML header (first line if starts with <?xml)
   * 
   * @param svrl The XML content of the SVRL including the XML declaration.
   * 
   * @return the SVRL content without the XML header(first line)
   */
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

  /**
   * Given full path to the path and return file name only
   * 
   * @param filename
   *          full path to a file
   * @return name of the file only without any of the path
   */
  private String removePath(String filename) {
    String[] splitted = filename.split("/");
    return splitted[splitted.length - 1];
  }

}
