package us.hall.qcapp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

public class XMLPyDocument {

	private final String[] gates = new String[] {"h", "cx"};
	private final List gateList = Arrays.asList(gates);
	private final String template = "^\\s*([a-zA-Z0-9]+)\\s=\\s%s\\((.*?)\\).*$";
    private final Pattern sim_pattern = Pattern.compile(String.format(template,"AerSimulator"));
    private final Pattern circuit_pattern = Pattern.compile(String.format(template,"QuantumCircuit"));
	private String sim_label = "N/A", circ_label = "N/A";
	private String circuitNS = "urn:qis:circuit:1_0";
	private String circuitElement = "c:Circuit";
	private final String line_start = "^\\s*";
	private Pattern gatePattern;
	private Matcher matcher, circuitMatcher;
	
	public XMLPyDocument(List<String> lines) {
		System.out.println(sim_pattern.toString());
		System.out.println(circuit_pattern.toString());
		ClassLoader cl = getClass().getClassLoader();
		InputStream is = getClass().getResourceAsStream("qiskit.stg");
		StringTemplateGroup stg = 
			new StringTemplateGroup(new InputStreamReader(is),DefaultTemplateLexer.class);
		//StringTemplate xml = stg.getInstanceOf("qiskitXML");
		for (String line : lines) {
			if (line.startsWith("#")) continue;
			if (line.trim().equals("")) continue;
			//else if (line.)
			if (sim_label.equals("N/A")) {
				matcher = sim_pattern.matcher(line);
				if (matcher.find()) {                                                
					sim_label = matcher.group(1);
					String sim_parms = matcher.group(2);
					String[] parms = sim_parms.split(",");
					
				}
			}
			else if (circ_label.equals("N/A")){
				matcher = circuit_pattern.matcher(line);
				if (matcher.find()) {
					circ_label = matcher.group(1);
					String parms = matcher.group(2);
					System.out.println("Parms " + parms);
					StringBuilder gateBuilder = new StringBuilder(line_start);
					gateBuilder.append(circ_label).append("\\.");
					gateBuilder.append("([a-zA-Z]+)\\((.*?)\\).*$");
					gatePattern = Pattern.compile(gateBuilder.toString());
				}
			}
			else if (line.trim().startsWith(circ_label)) {
				circuitMatcher = gatePattern.matcher(line);
				if (circuitMatcher.find()) {
					String circuitMethod = circuitMatcher.group(1);
					if (gateList.contains(circuitMethod)) {
						System.out.println("gate " + circuitMethod);
					}
					else if (circuitMethod.equals("measure")) {
						System.out.println("measure");
					}
					else if (circuitMethod.equals("draw")) {
						System.out.println("draw");
					}
					else {
						System.out.println("Unknown circuit method " + circuitMethod);
					}
					//System.out.println("Match on gate " + circuitMatcher.group(1) + " " + circuitMatcher.group(2));
				}	
			}
			else {
				System.out.println("UNMATCHED " + line);
			}
		}
	}
	
	public static void main(String... args) {
		if (args.length == 0) {
			System.out.println("XMLPyDocument missing path parameter");
			return;
		}
		Path p = Paths.get(args[0]);
		try {
			List<String> lines = Files.readAllLines(p);
			new XMLPyDocument(lines);
		}
		catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}
}