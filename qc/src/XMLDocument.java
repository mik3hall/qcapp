package us.hall.qcapp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.redfx.strange.gate.*;
import org.redfx.strange.Complex;
import org.redfx.strange.Gate;
import org.redfx.strangefx.ui.GateSymbol;
import org.redfx.strangefx.ui.QubitBoard;
import org.redfx.strangefx.ui.QubitFlow;

import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.Node;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class XMLDocument {
/**
 * Namespaces:
 *          Namespace          Prefix
 * Instance qis:instance:1_0   i
 * Gate     qis:gate:1_0       g
 * Circuit  qis:circuit:1_0    c
 * Program  qis:program:1_0    p
 * Reusable qis:reusable:1_0   r
 *
 * – Instance: a global wrapper to bring together the
 *   other components (gates, circuits and programs);
 * – Reusable: describes a set of common elements that
 *   can be used by the all other modules;
 * – Gate: describes quantum gates and bring them
 *   together into gate libraries;
 * – Circuit: describes quantum circuits and bring them
 *   together into circuit libraries;
 * – Program: describes algorithms and bring them
 *   together into program libraries;
 */
	private final QubitBoard board;
	private final Document doc;
	private final List<String> addedGates = new ArrayList();
	
	public XMLDocument(QubitBoard board) {
		this.board = board;
		ObservableList<QubitFlow> wires = board.getWires();
		int qnum = wires.size();
		int snum = wires.get(0).getGateRow().getChildrenUnmodifiable().size();
		Document temp_doc = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
        	DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			DOMImplementation domImpl = docBuilder.getDOMImplementation();
			//temp_doc = domImpl.createDocument("urn:qis", "qis:doc", null);
			temp_doc = docBuilder.newDocument();
		}
		catch(ParserConfigurationException pcex) {
			pcex.printStackTrace();
			temp_doc = null;
		}
		doc = temp_doc;
		if (doc == null) return;	
		//Element rootElement = document.getDocumentElement();
        Element rootElement = doc.createElement("QIS");  // NS("urn:qis","qis:QIS");
        doc.appendChild(rootElement);

        Element circuit = doc.createElementNS("urn:qis:circuit:1_0","c:Circuit");
        circuit.setAttribute("size",Integer.toString(snum));
        rootElement.appendChild(circuit);
        Gate[][] gates = new Gate[snum][qnum];
		for (int s = 0; s < snum; s++) {
			Element step = doc.createElementNS("urn:qis:circuit:1_0","c:Step");
			circuit.appendChild(step);
			for (int qubit = 0; qubit < qnum; qubit++) {
				Pane p = wires.get(qubit).getGateRow();			
				Node node = p.getChildrenUnmodifiable().get(s);
				if ((node instanceof GateSymbol) && 
					!(((GateSymbol)node).getGate() instanceof Identity)) {
					Element operation = 
						doc.createElementNS("urn:qis:circuit:1_0","c:Operation");
					step.appendChild(operation);
					appendGate(circuit, operation,((GateSymbol)node).getGate());
				}
			}			
		}
	}
	
	private void appendGate(Element circuit, Element operation, Gate g) {
		List<Integer> q_idx = g.getAffectedQubitIndexes();
		Element map = doc.createElementNS("urn:qis:circuit:1_0","c:Map");
		for (int i=0; i<q_idx.size(); i++) {
			Element qmap = (Element)map.cloneNode(false);
			int q = q_idx.get(i);
			qmap.setAttribute("qubit",Integer.toString(q+1));
			if (q_idx.size() > 1) {
				if (q == g.getMainQubitIndex()) {
					qmap.setAttribute("input","1");
				}
				else {
					qmap.setAttribute("input","2");
				}
			}
			else {
				qmap.setAttribute("input","1");
			}
			operation.appendChild(qmap);
		}
		Element id = doc.createElementNS("urn:qis:reusable:1_0","r:Identification");
		id.setTextContent(g.getCaption());
		if (!addedGates.contains(g.getCaption())) {
			addedGates.add(g.getCaption());
			Element gate = doc.createElementNS("urn:qis:gate:1_0","g:Gate");
			gate.appendChild(id);
			Element name = doc.createElementNS("urn:qis:gate:1_0","g:Name");
			name.setTextContent(g.getName());
			gate.appendChild(name);
			Complex[][] matrix = g.getMatrix();
			if (matrix.length > 0) {
				appendTransformation(gate, matrix);
			}
			doc.getDocumentElement().insertBefore(gate, circuit);
		}
		Element gateRef = doc.createElementNS("urn:qis:reusable:1_0","c:GateRef");
		gateRef.appendChild(id.cloneNode(true));
		operation.appendChild(gateRef);
	}
	
	private void appendTransformation(Element gate, Complex[][] matrix) {
		int size = matrix.length >> 1;
		if (matrix.length > 0) {
			Element tran = doc.createElementNS("urn:qis:reusable:1_0","r:Transformation");
			tran.setAttribute("size",Integer.toString(size));
			for (int i=0; i<matrix.length; i++) {
				String r = Integer.toString(i+1);
				for (int j=0; j<matrix[0].length; j++) {
					String c = Integer.toString(j+1);
					if (matrix[i][j].r > 0 || matrix[i][j].i > 0) { 
						Element cell = doc.createElement("Cell");
						cell.setAttribute("row",r);
						cell.setAttribute("col",c);
						if (matrix[i][j].r > 0) {
							cell.setAttribute("r",Float.toString(matrix[i][j].r));
						}
						if (matrix[i][j].i > 0) {
							cell.setAttribute("c",Float.toString(matrix[i][j].i));
						}
						tran.appendChild(cell);
					}
				}
			}
			gate.appendChild(tran);
		}
	}	

	public String toString() {
		try {
		   DOMSource domSource = new DOMSource(doc);
		   StringWriter writer = new StringWriter();
		   StreamResult result = new StreamResult(writer);
		   TransformerFactory tf = TransformerFactory.newInstance();
		   Transformer transformer = tf.newTransformer();
		   transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		   transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		   transformer.transform(domSource, result);
		   return writer.toString();
		}
		catch(TransformerException ex) {
		   ex.printStackTrace();
		   return null;
		}
	} 	
		
	private void write() {
	}
}