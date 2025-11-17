import cirq
import sys
from pathlib import Path
import xml.etree.ElementTree as cirqxml
import numpy as np

gate_names = { "H": "Hadamard", "CNOT": "Cnot", "X": "X"}
gate_ids = { "H": "H", "CNOT": "Cnot"}
gate_refs = []
root = cirqxml.Element("QIS")
namespaces = { "g": "urn:qis:gate:1_0",
				"r": "urn:qis:reusable:1_0",
				"c": "urn:qis:circuit:1_0"
			}

for prefix, uri in namespaces.items():
	cirqxml.register_namespace(prefix, uri)
	
class GateRef:

	def __init__(self, operation):
		self.operation = operation

	def toXML(self):
		gateName = str(self.operation.gate)
		if not gateName in gate_refs and not "MeasurementGate" in gateName:
			gate_refs.append(gateName)
			gate = cirqxml.SubElement(root,f"{{{namespaces['g']}}}Gate")
			id = cirqxml.SubElement(gate,f"{{{namespaces['r']}}}Identification")
			id.text = gate_ids.get(gateName)
			name = cirqxml.SubElement(gate,f"{{{namespaces['g']}}}Name")
			name.text = gate_names.get(gateName)
			transformation = cirqxml.SubElement(gate,f"{{{namespaces['r']}}}Transformation")
			transformation.set("size", str(len(self.operation.qubits)))
			if self.operation._has_unitary_():
				t = self.operation._unitary_()
				for row in range(t.shape[0]):  	# Iterate through rows
					for col in range(t.shape[1]): # Iterate through columns
						if t[row][col] != 0:
							cell = cirqxml.SubElement(transformation,"Cell")
							cell.set("row",str(row+1))
							cell.set("col",str(col+1))
							cell.set("real",str(t[row][col].real))
							cell.set("complex",str(t[row][col].imag)+"j")

class Step:
		
	def __init__(self, circuit_xml, operation):
		self.circuit_xml = circuit_xml
		self.operation = operation
		
	def toXML(self):
		gateName = str(self.operation.gate)
		qubits = self.operation.qubits
		if not "MeasurementGate" in gateName:
			step = cirqxml.SubElement(self.circuit_xml, f"{{{namespaces['c']}}}Step")
			operation_xml = cirqxml.SubElement(step, f"{{{namespaces['c']}}}Operation")
			for i in range(len(qubits)):
				map_xml = cirqxml.SubElement(operation_xml, f"{{{namespaces['c']}}}Map")
				map_xml.set("input", str(i+1))
				# qubit_numbers = [int(q.name.split('_')[-1]) for q in qubits]
				map_xml.set("qubit", str(qubits[i].x))
				#map_xml.set
			gateref_xml = cirqxml.SubElement(operation_xml, f"{{{namespaces['c']}}}GateRef")
			id_xml = cirqxml.SubElement(gateref_xml,f"{{{namespaces['r']}}}Identification")
			id_xml.text = gate_ids.get(str(self.operation.gate))
										
def introspect(module):
	qcapp = cirqxml.Element("qcapp")
	root.append(qcapp)
	languages = cirqxml.SubElement(qcapp, "languages")
	java = cirqxml.SubElement(languages, "java")
	python = cirqxml.SubElement(languages, "python")
	cirqdir = dir(module)
	circuitVarName = ""
	for q in cirqdir:
		if isinstance(getattr(module,q), cirq.Circuit):
			circuitVarName = q
			break
	print(circuitVarName)
	circuit = getattr(module,circuitVarName)
	gateRefs = []
	circuit_size = 0

	for i, moment in enumerate(circuit):
		for operation in moment.operations:
			#print(operation.gate," ",operation.qubits)
			GateRef(operation).toXML()

	circuit_xml = cirqxml.SubElement(root,f"{{{namespaces['c']}}}Circuit")
			
	for i, moment in enumerate(circuit):
		for operation in moment.operations:
			print("operation type ", type(operation))
			#print(dir(operation))
			#print(dir(operation.with_gate))
			#print(type(operation.with_gate()))
			print("qubits type ",operation.qubits," len ",len(operation.qubits))
			#if isinstance(operation, Gate) and not isinstance(operation, Measure):
			circuit_size += 1
			Step(circuit_xml, operation).toXML()
				
if __name__ == "__main__":
	if len(sys.argv) > 1:
		path = Path(sys.argv[1])
		scriptPath = str(path.parent)
		sys.path.append(scriptPath) 
		print('sys.path',sys.path)
		module = __import__(path.stem)
		introspect(module)
		tree = cirqxml.ElementTree(root)
		cirqxml.indent(tree, space="\t", level=0)
		if len(sys.argv) > 2:
			print(sys.argv[2])
			tree.write(sys.argv[2], encoding="utf-8", xml_declaration=True)
		else:
			tree.write("cirq.xml", encoding="utf-8", xml_declaration=True)
	else:
		print("no script passed")
