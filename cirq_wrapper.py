import cirq
import sys
from pathlib import Path
import xml.etree.ElementTree as cirqxml

root = cirqxml.Element("QIS")
namespaces = { "g": "urn:qis:gate:1_0",
				"r": "urn:qis:reusable:1_0",
				"c": "urn:qis:circuit:1_0"
			}

class GateRef:

	def __init__(self, operation):
		print("gateref")
			
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
	#print(dir(circuit))
	#print(circuit.all_operations)
	#print(len(circuit.all_operations))
	#print(circuit.all_qubits)
	# 3. Iterate directly over the circuit to get the moments.
	# The circuit object itself is an iterable of its moments.
	print("Iterating over the circuit to get moments:")
	for i, moment in enumerate(circuit):
		#print(f"--- Moment {i} ---")
		#print(dir(moment))
		#print("operations ", len(moment.operations))
		#print(moment.operations)
		print("first moment operation")
		for operation in moment.operations:
			print(operation.gate," ",operation.qubits)
			if operation._has_unitary_():
				print(operation._unitary_())
		# A moment is a collection of operations that can happen in parallel.
		for op in moment:
			#print(dir(op))
			print(f"  {op}")
				
if __name__ == "__main__":
	if len(sys.argv) > 1:
		path = Path(sys.argv[1])
		scriptPath = str(path.parent)
		sys.path.append(scriptPath) 
		print('sys.path',sys.path)
		module = __import__(path.stem)
		introspect(module)