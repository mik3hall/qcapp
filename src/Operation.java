package us.hall.qcapp.parts;

import java.util.ArrayList;
	
public class Operation {
	ArrayList<Map> maps = new ArrayList();
	String gateRef;
	GateDefinition gate;
	
	public void addMap(Map map) {
		maps.add(map);
	}
	
	public ArrayList<Map> getMaps() {
		return maps;
	}
	
	public String getGateRef() {
		return gateRef;
	}
	
	public void setGateRef(String name) {
		this.gateRef = name;
	} 
	
	public GateDefinition getGate() {
		return gate;
	}
	
	public void setGate(GateDefinition gate) {
		this.gate = gate;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Operation:\n");
		for (Map map : maps ) {
			sb.append(map.toString());
		}
		sb.append(gate);
		return sb.toString();
	}
}