package us.hall.qcapp;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory; 
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import us.hall.qcapp.parts.*;

public class XMLParser extends DefaultHandler {
	
	
	// Currently unused. Intention is circuit libraries as mentioned in QIS-XML document
	//private final ArrayList<Circuit>program = new ArrayList<Circuit>();
	//private final ArrayList<Step> circuit = new ArrayList<Step>();
	private final HashMap<String, GateDefinition>gates = new HashMap<String, GateDefinition>();
	private final ArrayList<String> gateNames = new ArrayList<String>();
	private final ArrayList<Operation>steps = new ArrayList<Operation>();
	
	private enum ElementType {
		GateType,
		GateRefType,
		StepType,
		OperationType
	}
	boolean DEBUG = false;
	public final String CELL = "Cell";
	public final String CIRCUIT = "c:Circuit";
	public final String GATE = "g:Gate";
	public final String GATEREF = "c:GateRef";
	public final String NAME = "g:Name";
	public final String STEP = "c:Step";
	public final String OPERATION = "c:Operation";
	public final String MAP = "c:Map";
	public final String IDENTIFICATION = "r:Identification";
	public final String TRANSFORMATION = "r:Transformation";
	private boolean key	= false, string = false;
	private StringBuilder accum	= new StringBuilder();
	private String name, identification;
	private Circuit circuit;
	private int dim = 0, circuitSize = 0;
	private Step currentStep;
	private Operation currentOperation;
	private GateDefinition currentGateDef;
	private ElementType elementType;
	
	public XMLParser(File qisxml) {
		this(qisxml.toPath());
	}
	
	public XMLParser(Path qisxml) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			InputStream is = Files.newInputStream(qisxml);
		    saxParser.parse(new InputSource(is), this);
		    is.close();
		}
		catch (SAXException saxex) {
			saxex.printStackTrace(); 
		}
		catch (IOException ioex) { ioex.printStackTrace(); }
		catch (Exception ex) {	
			ex.printStackTrace();
		}
	}
	
	public int getNQbit() {
		return dim;
	}
	
	public Circuit getCircuit() {
		return circuit;
	}
	
	public ArrayList<String> getGateNames() {
		return gateNames;
	}
	
	public GateDefinition getGate(String name) {
		return gates.get(name);
	}
	
	public void startDocument() throws SAXException {
		System.out.println("startDocument");
	}
	
	public void endDocument() throws SAXException {
		System.out.println("endDocument");
	}
	
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		System.out.println("startPrefixMapping: " + prefix + " " + uri);
	}
	
	public void endPrefixMapping(String prefix) throws SAXException {
		System.out.println("endPrefixMapping");
	}

	public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
		throw new SAXException("resolveEntity: " + publicID + " , " + systemID);
	}
	
	public InputSource resolveEntity(String name,String publicID, String baseURI, String systemID) throws SAXException, IOException {
		System.out.println(name + " " + publicID + " " + baseURI + " " + systemID);
		throw new SAXException("resolveEntity");
	}
		
	public InputSource getExternalSubset(String name,String baseURI) throws SAXException, IOException {
		throw new SAXException("getExternalSubset");
	}
	
	public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes attrs) throws SAXException {
		System.out.println("XML startElement(" + namespaceURI + ") local: " + localName + " qualified: " + qualifiedName);
		switch(qualifiedName) {
			case CIRCUIT:
				circuit = new Circuit();
				for (int i = 0; i < attrs.getLength(); i++) {
					if (attrs.getQName(i).equals("size")) {
						circuit.setSize(Integer.parseInt(attrs.getValue(i)));
						//program.add(circuit);
						break;
					}
				}
				System.out.println("Circuit size not found");
				break;
			case GATE:
				elementType = ElementType.GateType;
				currentGateDef = new GateDefinition();
				break;
			case GATEREF:
				elementType = ElementType.GateRefType;
				break;
			case STEP:
				elementType = ElementType.StepType;
				dim++;			// increment program dimensions
				currentStep = new Step();
				break;
			case OPERATION:
				elementType = ElementType.OperationType;
				currentOperation = new Operation();
				break;
			case MAP:
				int qubit = -1, input = -1;
				if (attrs.getLength() > 0) {
					for (int i = 0; i < attrs.getLength(); i++) {
						if (attrs.getQName(i).equals("qubit")) {
							qubit = Integer.parseInt(attrs.getValue(i));	
						}
						else if (attrs.getQName(i).equals("input")) {
							input = Integer.parseInt(attrs.getValue(i));
						}
					}
					if (qubit != -1 && input != -1) {
						currentOperation.addMap(new Map(qubit, input));
					}
					else {
						throw new IllegalArgumentException("XMLParser: Map missing qubit or input");
					}
				}
				else {
					throw new IllegalArgumentException("XMLParser: Map missing attributes");
				}
				break;
			case CELL:
				if (attrs.getLength() > 0) {
					int row = 0, col = 0;
					double r = 0.0;
					for (int i = 0; i < attrs.getLength(); i++) {
						if (attrs.getQName(i).equals("row")) {
							row = Integer.parseInt(attrs.getValue(i));
						}
						else if (attrs.getQName(i).equals("col")) {
							col = Integer.parseInt(attrs.getValue(i));
						}
						else if (attrs.getQName(i).equals("r")) {
							r = Double.parseDouble(attrs.getValue(i));
						}
					}
					currentGateDef.setCell(row,col,r);
				}
				else {
					
				}
				break;
			case TRANSFORMATION:
				if (attrs.getLength() > 0) {
					System.out.println("\tAttributes:");
					for (int i = 0; i < attrs.getLength(); i++) {
						if (attrs.getQName(i).equals("size")) {
							currentGateDef.setSize(Integer.parseInt(attrs.getValue(i)));
						}
						System.out.println("\t\t" + attrs.getQName(i) + " = " + attrs.getValue(i));
					}	
				}
				else {
					System.out.println("ERROR: Tranformation missing size");
				}
				break;
		}

	}
	
	public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
		System.out.println("XML endElement(" + namespaceURI + ") local: " + localName + " qualified: " + qualifiedName);
		try {
			switch(qualifiedName) {
				case NAME:
					name = accum.toString().trim();
					System.out.println("Name: " + name);
					if (elementType == ElementType.GateType) {
						currentGateDef.setName(accum.toString().trim());
					}
					accum.setLength(0);
					break;
				case IDENTIFICATION:
					identification = accum.toString().trim();
					System.out.println("Identification: " + identification);
					if (elementType == ElementType.GateType) {
						currentGateDef.setIdentification(accum.toString().trim());
					}
					else if (elementType == ElementType.GateRefType) {
						currentOperation.setGate(gates.get(accum.toString().trim()));
					}
					accum.setLength(0);
					break;
				case GATE:
					gates.put(currentGateDef.getIdentification(), currentGateDef);
					gateNames.add(currentGateDef.getName());
					break;
				case STEP:
					circuit.addStep(currentStep);
					break;
				case OPERATION:
					currentStep.addOperation(currentOperation);
					break;
			}
        }
        catch (Throwable tossed) {
        	tossed.printStackTrace(); 
        }
		System.out.println("endElement(" + namespaceURI + ") local: " + localName + " qualified: " + qualifiedName);
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
    	if (DEBUG) {
    		System.out.println("Characters: \"" + new String(ch,start,length) + "\"");
    	}
    	while (ch[start] == 0) {
    		start += 1;
    		length -= 1;
    	}
    	accum.append(ch,start,length);
	}
	
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
	}
	
	public void processingInstruction(String target, String data) throws SAXException {
		System.out.println("processingInstruction: " + target + " = " + data);
	}
	
	public void skippedEntity(String name) throws SAXException {
		System.out.println("skippedEntity: " + name);
	}
}