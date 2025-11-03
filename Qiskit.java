package us.hall.strange;

import us.hall.qcapp.Util;
import us.hall.qcapp.XMLDocument;

import org.redfx.strange.Program;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.Qubit;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
import org.redfx.strange.gate.Hadamard;
import org.redfx.strange.gate.Cnot;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;
import org.redfx.strangefx.render.Renderer;

import javafx.application.Platform;

public class Qiskit {

	private static final int dim = 2;

	public static void main (String[] args) {
        
		Program program = new Program(dim);
	Step step0 = new Step(new Hadamard(0));
	Step step1 = new Step(new Cnot(0,1));
	program.addSteps(step0,step1);
	QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
	Result result = qee.runProgram(program);
		Util.renderProgram(program, "Strange");
	}
}