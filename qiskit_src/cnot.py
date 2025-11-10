from qiskit import QuantumCircuit, transpile
from qiskit_aer import AerSimulator
from qiskit.visualization import plot_histogram
from qiskit.quantum_info import Statevector

def run00(simulator, circuit):
	# Add a CX (CNOT) gate on control qubit 0 and target qubit 1
	circuit.cx(0, 1)

	# Compile the circuit for the support instruction set (basis_gates)
	# and topology (coupling_map) of the backend
	compiled_circuit = transpile(circuit, simulator)
	
	# Execute the circuit on the aer simulator
	job = simulator.run(compiled_circuit, shots=1)

	# Grab results from the job
	result = job.result()
	
	return result.get_counts()

def run01(simulator, circuit):
	# Add a CX (CNOT) gate on control qubit 0 and target qubit 1
	circuit.cx(0, 1)
	circuit.x(1)
	# Compile the circuit for the support instruction set (basis_gates)
	# and topology (coupling_map) of the backend
	compiled_circuit = transpile(circuit, simulator)
	
	# Execute the circuit on the aer simulator
	job = simulator.run(compiled_circuit, shots=1)

	# Grab results from the job
	result = job.result()
	
	return result.get_counts()
		
# Use Aer's AerSimulator
simulator = AerSimulator()

# Create a Quantum Circuit acting on the q register
circuit00 = QuantumCircuit(2, 2)

# Add a CX (CNOT) gate on control qubit 0 and target qubit 1
circuit00.cx(0, 1)

# Map the quantum measurement to the classical bits
circuit00.measure([0, 1], [0, 1])

counts = run00(simulator, circuit00)
print("IN = |00>\tOUT= |"+list(counts.keys())[0]+">");

# Create a Quantum Circuit acting on the q register
circuit01 = QuantumCircuit(2, 2)

# Add a CX (CNOT) gate on control qubit 0 and target qubit 1
circuit01.cx(0, 1)

# not qubit 1 to 1
circuit01.x(1)

# Map the quantum measurement to the classical bits
circuit01.measure([0, 1], [0, 1])

counts = run01(simulator, circuit01)
print("IN = |10>\tOUT= |"+list(counts.keys())[0]+">");

# Create a Quantum Circuit acting on the q register
circuit10 = QuantumCircuit(2, 2)

# Add a CX (CNOT) gate on control qubit 0 and target qubit 1
circuit10.cx(0, 1)

# not qubit 0 to 1
circuit10.x(0)

# qubit 1 to 1 as well
circuit10.x(1)

# Map the quantum measurement to the classical bits
circuit10.measure([0, 1], [0, 1])

counts = run01(simulator, circuit10)
print("IN = |01>\tOUT= |"+list(counts.keys())[0]+">");

# Create a Quantum Circuit acting on the q register
circuit10 = QuantumCircuit(2, 2)

# Add a CX (CNOT) gate on control qubit 0 and target qubit 1
circuit10.cx(0, 1)

# not qubit 0 to 1
circuit10.x(0)

# Map the quantum measurement to the classical bits
circuit10.measure([0, 1], [0, 1])


counts = run01(simulator, circuit10)
print("IN = |11>\tOUT= |"+list(counts.keys())[0]+">");
        