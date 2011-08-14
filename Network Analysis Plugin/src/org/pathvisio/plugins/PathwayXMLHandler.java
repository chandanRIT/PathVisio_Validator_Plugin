package org.pathvisio.plugins;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class PathwayXMLHandler extends DefaultHandler {

	private StringBuilder chars = new StringBuilder();
	//private int iso_ns_counter = 0;// <iso:ns> tag counter
	private ArrayList<VPNode> vpnodes;
	
	private boolean insideLine=false;
	private VPLine vpl ;
	private int refCounter=0;

	public PathwayXMLHandler(ArrayList<VPNode> vpnodes){
		this.vpnodes=vpnodes;
	}
	
	public void startElement(String uri, String localName, String rawName,
			Attributes attributes) {

		if (rawName.equals("Line")) {
			
			vpl = new VPLine();
			vpl.graphId=attributes.getValue("GraphId");
			insideLine=true;
			refCounter=0;
		}
		
		else if (rawName.equals("DataNode")) {
			
			VPDataNode vpdn = new VPDataNode(attributes.getValue("GraphId"),attributes.getValue("Type"));
			//vpdn.graphId= attributes.getValue("GraphId");
			//vpdn.dNodeType=attributes.getValue("Type");
			vpnodes.add(vpdn);
		
		}

		else if (rawName.equals("Anchor")) { 
											
			VPAnchor vpa= new VPAnchor(attributes.getValue("Shape"),attributes.getValue("GraphId"),vpl);
			vpnodes.add(vpa);
			
		}
		
		else if(rawName.equals("Point") && insideLine){
			
			String tref = attributes.getValue("GraphRef");
			if( tref!=null && !tref.equals("") ){
				vpl.graphRef[refCounter]=tref;
				refCounter++;
			}
		}
		
		
	}

	
	public void endElement(String namespaceURL, String localName, String rawName) {

		if (rawName.equals("Line")) {
			
			if(refCounter==2)
				vpnodes.add(vpl);
			else 
				System.out.println(vpl+ " : Line doesnot have 2 grefs ");
			
			vpl=null;
			insideLine=false;
		}
		//insideLine=false;
	}

	public void characters(char[] ch, int start, int length) {
		// print svrl:text text node if the lastElement is svrl:text
		this.chars.append(ch, start, length);
	}

	private String getCharacters() {
		String retstr = this.chars.toString();
		this.chars.setLength(0);
		return retstr;
	}

	// getters and setters are below

}

