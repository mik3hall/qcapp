package us.hall.qcapp.parts;
	
public class Map {
	private int input, qubit;
	
	public Map(int qubit, int input) {
		this.input = input;
		this.qubit = qubit;
	}
	
	public int getInput() { return input; }
	public int getQubit() { return qubit; }
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Map: ");
		sb.append("Input: ").append(input);
		sb.append(" Qubit: ").append(qubit);
		return sb.toString();
	}
}
