package us.hall.qcapp.parts;

import java.util.ArrayList;

public class Circuit {
	int size = 0;
	ArrayList<Step> steps = new ArrayList<Step>();
	
	public Circuit() {}
	
	public void addStep(Step step) {
		steps.add(step);
	}
	
	public ArrayList<Step> getSteps() {
		return steps;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
}