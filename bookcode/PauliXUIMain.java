package us.hall.qcapp;

import org.redfx.strange.*;
import org.redfx.strange.gate.*;
import org.redfx.strange.local.*;
import org.redfx.strangefx.render.Renderer;
import us.hall.qcapp.Util;

public class PauliXUIMain {

    public static void main(String[] args) {
        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
        Program program = new Program(1);
        Step step = new Step();
        step.addGate(new X(0));
        program.addStep(step);
        Result result = simulator.runProgram(program);
        Qubit[] qubits = result.getQubits();
        Qubit zero = qubits[0];
        int value = zero.measure();
        System.out.println("Value = "+value);
        Util.renderProgram(program, "paulixui");
    }  

}
