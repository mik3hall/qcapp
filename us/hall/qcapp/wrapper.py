import sys
import inspect
from qiskit import QuantumCircuit
from qiskit.circuit import Measure
from qiskit.circuit.gate import Gate
from qiskit.quantum_info import Statevector
import numpy as np
from pathlib import Path
import xml.etree.ElementTree as qisxml 

gate_names = { "h": "Hadamard", "cx": "Cnot", "x": "X"}
gate_ids = { "h": "H", "cx": "Cnot"}
reusable_ns = "urn:qis:reusable:1_0"
root = qisxml.Element("QIS")
namespaces = { "g": "urn:qis:gate:1_0",
				"r": "urn:qis:reusable:1_0",
				"c": "urn:qis:circuit:1_0"
			}

for prefix, uri in namespaces.items():
	qisxml.register_namespace(prefix, uri)

class GateRef:

	def __init__(self, operation):
		self.operation = operation
		
	def toXML(self):
		gate = qisxml.SubElement(root,f"{{{namespaces['g']}}}Gate")
		id = qisxml.SubElement(gate,f"{{{namespaces['r']}}}Identification")
		id.text = gate_ids.get(self.operation.name)
		name = qisxml.SubElement(gate,f"{{{namespaces['g']}}}Name")
		name.text = gate_names[self.operation.name]
		transformation = qisxml.SubElement(gate,f"{{{namespaces['r']}}}Transformation")
		transformation.set("size", str(self.operation.num_qubits))
		m = self.operation.to_matrix().tolist()
		for row in range(len(m)):
			for col in range(len(m[row])):
				if m[row][col] != 0:
					cell = qisxml.SubElement(transformation,"Cell")
					cell.set("row",str(row+1))
					cell.set("col",str(col+1))
					cell.set("real",str(m[row][col].real))
					cell.set("complex",str(m[row][col].imag)+"j")

class Step:
		
	def __init__(self, circuit_xml, instruction):
		self.circuit_xml = circuit_xml
		self.instruction = instruction
		
	def toXML(self):
		step = qisxml.SubElement(self.circuit_xml, f"{{{namespaces['c']}}}Step")
		qubits = self.instruction.qubits
		operation = self.instruction.operation
		operation_xml = qisxml.SubElement(step, f"{{{namespaces['c']}}}Operation")
		for i in range(len(self.instruction.qubits)):
			map_xml = qisxml.SubElement(operation_xml, f"{{{namespaces['c']}}}Map")
			map_xml.set("input", str(i+1))
			map_xml.set("qubit", str(qubits[i]._index))
			#map_xml.set
		gateref_xml = qisxml.SubElement(operation_xml, f"{{{namespaces['c']}}}GateRef")
		id_xml = qisxml.SubElement(gateref_xml,f"{{{namespaces['r']}}}Identification")
		id_xml.text = gate_ids.get(operation.name)
						
def introspect(module):
	qcapp = qisxml.Element("qcapp")
	root.append(qcapp)
	languages = qisxml.SubElement(qcapp, "languages")
	java = qisxml.SubElement(languages, "java")
	python = qisxml.SubElement(languages, "python")
	qisdir = dir(module)
	circuitVarName = ""

	for q in qisdir:
		if isinstance(getattr(module,q), QuantumCircuit):
			circuitVarName = q
			break
			
	circuit = getattr(module,circuitVarName)
	gateRefs = []
	circuit_size = 0
	
	for instruction in circuit.data:
		operation = instruction.operation
		if isinstance(operation, Gate):
			GateRef(operation).toXML()	

	circuit_xml = qisxml.SubElement(root,f"{{{namespaces['c']}}}Circuit")
			
	for instruction in circuit.data:
		operation = instruction.operation
		if isinstance(operation, Gate) and not isinstance(operation, Measure):
			circuit_size += 1
			Step(circuit_xml, instruction).toXML()
		
#	for instruction in circuit.data:
#			step = qisxml.SubElement(circuit_xml, f"{{{namespaces['c']}}}Step")
#			qubits = instruction.qubits
#			clbits = instruction.clbits
#			qnum = len(qubits)
#			operation = instruction.operation
#			print(f"Operation: {operation.name}, Qubits: {qubits}, Clbits: {clbits}")
			#print("instruction ", dir(instruction))
			#print("operation ", vars(instruction.operation))
#			if not isinstance(operation, Measure):
#				circuit_size += 1
##				operation_xml = qisxml.SubElement(step, f"{{{namespaces['c']}}}Operation")
#				#print(instruction.operation.num_qubits)
				#print(dir(qubits))
#				for i in range(len(instruction.qubits)):
#					map_xml = qisxml.SubElement(operation_xml, f"{{{namespaces['c']}}}Map")
#					map_xml.set("input", str(i+1))
#					map_xml.set("qubit", str(qubits[i]._index))
#					print(dir(instruction.qubits[i]))
#					print(instruction.qubits[i]._index)
#					print(instruction.qubits[i]._register)
					#map_xml.set
#				gateref_xml = qisxml.SubElement(operation_xml, f"{{{namespaces['c']}}}GateRef")
#				id_xml = qisxml.SubElement(gateref_xml,f"{{{namespaces['r']}}}Identification")
#				id_xml.text = gate_ids.get(operation.name)
#			else:
#				circuit_xml.remove(step)

		#print(dir(instruction.qubits[0]))
		#print(dir(instruction.qubits[0]._register))
		# '_name', '_repr', '_size', 'bit_type', 'index', 'instances_counter', 'name', 'prefix', 'size']
		#print(instruction.qubits[0]._register.bit_type)
		#print(instruction.qubits[0]._register.index)
		#print(instruction.qubits[0]._register.name)
		#print(instruction.qubits[0]._register.prefix)
		#print(instruction.qubits[0]._register.size)
		#clbits = instruction.clbits
		#print("definition", operation.definition)
		#print("label", operation.label)
		#print("class", operation.base_class)
		#print("params", operation.params)
		#print("name", operation.name)
		#print("")
	circuit_xml.set("size", str(circuit_size))
	
if __name__ == "__main__":
	if len(sys.argv) > 1:
		path = Path(sys.argv[1])
		scriptPath = str(path.parent)
		sys.path.append(scriptPath) 
		print('sys.path',sys.path)
		module = __import__(path.stem)
		introspect(module)
		tree = qisxml.ElementTree(root)
		qisxml.indent(tree, space="\t", level=0)
		if len(sys.argv) > 2:
			print(sys.argv[2])
			tree.write(sys.argv[2], encoding="utf-8", xml_declaration=True)
		else:
			tree.write("qiskit.xml", encoding="utf-8", xml_declaration=True)
	else:
		print("no script passed")

