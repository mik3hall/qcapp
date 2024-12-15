#!/bin/bash
#set -x  # Enable tracing

python3 -m venv /Users/mjh/Documents/physics/quantumjava/qiskit_venv

source /Users/mjh/Documents/physics/quantumjava/qiskit_venv/bin/activate

pip install qiskit

pip install qiskit-ibm-runtime

pip install 'qiskit[visualization]'

pip install qiskit-aer

python3 "/Users/mjh/Documents/physics/quantumjava/qiskit1.py"

