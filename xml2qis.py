import sys
import inspect
from qiskit import QuantumCircuit
from qiskit.circuit import Measure
from qiskit.circuit.gate import Gate
from qiskit.quantum_info import Statevector
import numpy as np
from pathlib import Path
import xml.etree.ElementTree as xml2qis

gateNames = { "h": "Hadamard", "cx": "Cnot", "x": "X", "cz": "Cz"}
gateIds = { "H": "h", "Cnot": "cx", "X": "x"}
reusable_ns = "urn:qis:reusable:1_0"
namespaces = { "g": "urn:qis:gate:1_0",
				"r": "urn:qis:reusable:1_0",
				"c": "urn:qis:circuit:1_0" }

class Map:
	
	def __init__(self, xml_map):
		self.input = xml_map.attrib.get('input')
		self.qubit = xml_map.attrib.get("qubit")
		
class Operation:
	
	def __init__(self, xml_operation):
		self.maps = []
		self.gate = ""
		for part in xml_operation:
			if part.tag == "{urn:qis:circuit:1_0}Map":
				self.maps.append(Map(part))
			else:  # should be only gateref for now
				if part[0].text != "P":  # Ignore strange probability gates for now 
					self.gate = part[0].text
					
	def getMaps(self):
		return self.maps
		
class Step:
	
	def __init__(self, xml_step):
		self.operations = []	

		for operation in xml_step:
			self.operations.append(Operation(operation))
			
	def getOperations(self):
		return self.operations
			
if __name__ == "__main__":
	
	steps = []
	instructions = [] 
	bits = []
	
	if len(sys.argv) > 1:
		tree = xml2qis.parse(sys.argv[1])
		root = tree.getroot()
		for child in root:
			for content in child:
				if content.tag == "{urn:qis:circuit:1_0}Step":
					step = Step(content)
					steps.append(step)
		for step in steps:			
			for operation in step.operations:
				qubitParms = "("
				if operation.gate != "":
					cnt = 0
					for map in operation.maps:
						if cnt == 0:
							qubitParms += map.qubit
						else:
							qubitParms += ", " + map.qubit
						cnt += 1
					qubitParms += ")\n"		
					gateId = gateIds.get(operation.gate)
					instruction = "circuit." + gateId+qubitParms
					instructions.append(instruction)
					maps = operation.getMaps()
					for map in maps:
						if not map.qubit in bits:
							bits.append(map.qubit)
		bits.sort()
		bitsNum = str(len(bits))
		program = "circuit = QuantumCircuit(" + bitsNum + "," + bitsNum + ")\n"
		for instruction in instructions:
			program = program + instruction
		program = program + "circuit.measure(" + str(bits) + "," + str(bits) + ")"
		print(program)
	else:
		print('Missing XML file')