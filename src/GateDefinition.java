package us.hall.qcapp.parts;

import java.util.ArrayList;

public class GateDefinition {
	private String id, name;
	int size = 0;
	ArrayList<Cell> cells = new ArrayList();
	
	public GateDefinition() {}
	
	public void setIdentification(String id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setCell(int row, int col, double r) {
		cells.add(new Cell(row, col, r));
	}
	
	public String getName() {
		return name;
	}
	
	public String getIdentification() {
		return id;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Gate: ").append(name);
		return sb.toString();
	}
}