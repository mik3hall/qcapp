import org.redfx.strange.Program;
import org.redfx.strange.Qubit;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
import org.redfx.strange.gate.Toffoli;
import org.redfx.strange.gate.X;

public class ToffoliTest {

    public static void main(String... args) {
    	QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
    	System.out.println("|101> -> |101>");
        Program p = new Program(3,
           new Step(new X(2),new X(0)),
           new Step(new Toffoli(2,1,0)));
        Result res = simulator.runProgram(p);
        Qubit[] qubits = res.getQubits();
        for (int i=0; i<qubits.length; i++) {
        	System.out.println(i + " " + qubits[i].measure());
        }
        p = new Program(3,
           new Step(new X(2),new X(0)),
           new Step(new Toffoli(2,1,0)));
        res = simulator.runProgram(p);
        qubits = res.getQubits();
        for (int i=0; i<qubits.length; i++) {
        	System.out.println(i + " " + qubits[i].measure());
        }
    }
    
}