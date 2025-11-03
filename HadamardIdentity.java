import org.redfx.strange.*;
import org.redfx.strange.gate.*;
import org.redfx.strange.local.*;
import org.redfx.strangefx.render.*;
import us.hall.qcapp.Util;

public class HadamardIdentity {

    public static void main(String[] args) {
        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
        Program program = new Program(2);
        Step step1a = new Step();
        step1a.addGate(new Hadamard(0));
        program.addStep(step1a);
        Step step1b = new Step();
        step1b.addGate(new Hadamard(0));
        program.addStep(step1b);
        Step step2a = new Step();
        step2a.addGate(new X(1));
        program.addStep(step2a);
        Step step2b = new Step();
        step2b.addGate(new Hadamard(1));
        program.addStep(step2b);
        Step step2c = new Step();
        step2c.addGate(new Hadamard(1));
        program.addStep(step2c);
        Result result = simulator.runProgram(program);
        Qubit[] qubits = result.getQubits();
        Qubit q0 = qubits[0];
        Qubit q1 = qubits[1];
        int v0 = q0.measure();
        int v1 = q1.measure();
        System.out.println("Trial 1:");
        System.out.println("Qubit q0 = " + v0);
        System.out.println("Qubit q1 = " + v1);
        result = simulator.runProgram(program);
        qubits = result.getQubits();
        q0 = qubits[0];
        q1 = qubits[1];
        v0 = q0.measure();
        v1 = q1.measure();
        System.out.println("Trial 2:");
        System.out.println("Qubit q0 = " + v0);
        System.out.println("Qubit q1 = " + v1);
        result = simulator.runProgram(program);
        qubits = result.getQubits();
        q0 = qubits[0];
        q1 = qubits[1];
        v0 = q0.measure();
        v1 = q1.measure();
        System.out.println("Trial 2:");
        System.out.println("Qubit q0 = " + v0);
        System.out.println("Qubit q1 = " + v1);
        Util.renderProgram(program, "Hadamard Identity");
    }  

}
