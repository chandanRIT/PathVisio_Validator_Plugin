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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SaxonTransformer {

	private static String svrl;
	private static File schemaFile, inputFile;
	private static final TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl();
	static Transformer transformer1;
	private static boolean produceSvrl = false;

	/**
	 * An ArrayList to store (String) message of diagnostic-reference found.
	 */
	public final ArrayList<String> diagnosticReference = new ArrayList<String>();

	/**
	 * An ArrayList to store (String) message of failed assertion found.
	 */
	private final ArrayList<String> failedAssertions = new ArrayList<String>();

	/**
	 * An ArrayList to store (String) message of successful report found.
	 */
	private final ArrayList<String> successfulReports = new ArrayList<String>();

	public SaxonTransformer() throws TransformerConfigurationException {

		System.setProperty("javax.xml.transform.TransformerFactory",
				"net.sf.saxon.TransformerFactoryImpl");
		SaxonTransformer.transformer1 = factory
				.newTransformer(new StreamSource(getUrlToIso().toString()));

	}

	public static void setProduceSvrl(boolean produceSvrl) {
		SaxonTransformer.produceSvrl = produceSvrl;
	}

	public static boolean getProduceSvrl() {
		return produceSvrl;
	}

	public static void setschemaFile(File schemaFile) {
		SaxonTransformer.schemaFile = schemaFile;
	}

	public static File getschemaFile() {
		return schemaFile;
	}

	public static void setInputFile(File inputFile) {
		SaxonTransformer.inputFile = inputFile;
	}

	public static File getInputFile() {

		return inputFile;

	}

	public InputStream[] getFiles() {

		// String isoName = "/XSLs/iso_svrl_for_xslt2.xsl";

		InputStream[] in = {
				getClass().getResourceAsStream("/resources/mimschema.sch"),
				getClass().getResourceAsStream("/resources/example.mimml") };
		return in;
	}

	public URL getUrlToIso() {

		return getClass().getResource("/iso_svrl_for_xslt2.xsl");
		// above line for ECLIPSE build

		// below line for ANT JAR BUILD
		// return getClass().getResource("/XSLs/iso_svrl_for_xslt2.xsl");
	}

	public void produceSvrlAndThenParse() throws Exception {

		// String schemaSystemId = new File(args[0]).toURL().toExternalForm();
		// String inputFileSystemId = new
		// File(args[1]).toURL().toExternalForm();

		/*
		 * System.out.println(getUrlToIso());
		 * System.out.println("XSLT Version = " +
		 * Version.getXSLVersionString());
		 * System.out.println("Product Version = " +
		 * Version.getProductVersion());
		 */

		failedAssertions.clear();
		successfulReports.clear();
		diagnosticReference.clear();

		// InputStream[] in=getFiles();

		Source schemaSource = new StreamSource(schemaFile);
		Source inputSource = new StreamSource(inputFile);

		// transformer = factory.newTransformer(new
		// StreamSource(getUrlToIso().toString()));
		// File r1,r2;
		StringWriter sw1 = new StringWriter();
		Result result1 = new StreamResult(sw1);
		// Result result2 = new
		// StreamResult(r2=File.createTempFile("SVRL_OUTPUT", null));
		// r2.deleteOnExit();

		transformer1.transform(schemaSource, result1);
		System.out.println("xsl cretaed");

		Transformer transformer2 = factory.newTransformer(new StreamSource(
				new StringReader(sw1.toString())));
		StringWriter sw2 = new StringWriter();
		Result result2 = new StreamResult(sw2);

		transformer2.transform(inputSource, result2);
		// to produce the svrl output in a file in the user's temp directory
		File svrlFile = new File(System.getProperty("user.home"),
				"svrlOutput.svrl");

		if (getProduceSvrl()) {
			transformer2.transform(inputSource, new StreamResult(svrlFile));
			// produceSvrl=false;
		}
		svrlFile.deleteOnExit();

		System.out.println("svrl cretaed");

		// System.out.println(sw.toString());
		svrl = sw2.toString();
		svrl = removeXMLheader(svrl);
		// Logger.log.debug(svrl);
		parseSVRL();
		// printMessages();

	}

	private String removeXMLheader(String svrl) {

		int firstLineEnd = svrl.indexOf("\n");
		if (svrl.startsWith("<?xml ") || svrl.startsWith("<?xml ", 1)
				|| svrl.startsWith("<?xml ", 2) // Handle Unicode BOM
				|| svrl.startsWith("<?xml ", 3)) {
			return svrl.substring(firstLineEnd + 1);
		} else
			return svrl;
	}

	private void parseSVRL() throws IOException, SAXException,
			ParserConfigurationException {

		SVRLHandler handler = new SVRLHandler(this.failedAssertions,
				this.successfulReports, this.diagnosticReference);
		// Print the every source file name and validation result to console
		// use SVRLHandler class to parse the svrl content
		// System.out.println(this.svrl);
		InputSource is = new InputSource(
				new StringReader(SaxonTransformer.svrl));
		is.setEncoding("UTF-16");
		SAXParserFactory.newInstance().newSAXParser().parse(is, handler);

	}

	private void printMessages() {

		Iterator<String> tempIterator = diagnosticReference.iterator();
		while (tempIterator.hasNext()) {
			// Logger.log.debug(tempIterator.next());
			System.out.println(tempIterator.next());
		}

	}

}