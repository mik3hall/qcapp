package us.hall.qcapp.parts;

import java.util.ArrayList;

public class Step {
	ArrayList<Operation> operations = new ArrayList();
	
	public ArrayList<Operation> getOperations() {
		return operations;
	}
	
	public void addOperation(Operation operation) {
		operations.add(operation);
	}
}