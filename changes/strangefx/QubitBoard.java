/*-
 * #%L
 * StrangeFX
 * %%
 * Copyright (C) 2020 Johan Vos
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Johan Vos nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.redfx.strangefx.ui;

import org.redfx.strangefx.simulator.RenderModel;
import org.redfx.strange.ui.render.*;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.*;
import javafx.scene.layout.*;

import java.util.*;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.shape.Line;
import org.redfx.strange.Gate;
import org.redfx.strange.Program;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.Qubit;
import org.redfx.strange.Result;
import org.redfx.strange.Step;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;

import org.redfx.strange.Complex;
import org.redfx.strange.gate.ProbabilitiesGate;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class QubitBoard extends Group {
    
    public static final int WIRE_HEIGHT = 77; // the vertical distance between 2 wires
    private final RenderModel model;
    private ObservableList<QubitFlow> wires = FXCollections.observableArrayList();

    private final int nQubits;
    private Line[] line;

    public QubitBoard(RenderModel model) {
        this.model = model;
        this.nQubits = model.getNQubits();
        line = new Line[nQubits];
        for (int i = 0; i < nQubits; i++) {
            line[i] = new Line();
            line[i].setTranslateY(WIRE_HEIGHT * i + WIRE_HEIGHT / 2);
            line[i].getStyleClass().add("wire");
            getChildren().add(line[i]);
        }

        wires.addListener((Observable o) -> {
            model.refreshRequest().set(true);
        });

        model.stepsProperty().addListener((Observable observable) -> {
            processCircuit(model.stepsProperty().get());
            renderCircuit();
        });

        for (int i = 0; i < nQubits; i++) {
            appendQubit();
            QubitFlow q = wires.get(i);
            q.setTranslateY(i * GateSymbol.SEP);
            getChildren().add(q);
        }
        this.setOnMouseReleased(e-> {
        	Point2D where = new Point2D(e.getSceneX(),e.getSceneY());
        	System.out.println("QB mouse release at " + where);
        	boolean inAll = true, haveTarget = false;
        	QubitFlow targetFlow = null;
        	for (QubitFlow qf : wires) {
        		if (qf.contains(where)) {
					Pane p = qf.getGateRow();
					for (Node node : p.getChildrenUnmodifiable()) { 
						if (((GateSymbol)node).getGate() instanceof ProbabilitiesGate) {
							ProbabilitiesGate gate = (ProbabilitiesGate)((GateSymbol)node).getGate(); 	
							if (((Label)node).getGraphic() instanceof AnchorPane) {
								AnchorPane ap = (AnchorPane)((Label)node).getGraphic();
								Group group = (Group)ap.getChildrenUnmodifiable().get(0);
								Rectangle rect = (Rectangle)group.getChildren().get(0);
								Point2D rectAt = rect.localToScene(0,0);					
								if (where.getX() >= rectAt.getX() && where.getX() <= rectAt.getX()+rect.getWidth()) {
									int cellNum = (int)(where.getY()/(rect.getHeight()/Math.pow(nQubits,2)));
									String s = String.format("%4s", Integer.toBinaryString(cellNum-1)).replace(' ', '0');
									final Stage dialog = new Stage();
									dialog.setTitle("Probability ("+s+")");
									Complex[] ip = gate.getProbabilities();
									dialog.initModality(Modality.APPLICATION_MODAL);
									dialog.initOwner((Stage)node.getScene().getWindow());
									VBox container = new VBox(20);
									HBox dialogHbox = new HBox(20);
									double x = 20d, y=50d;
									for (int i=0; i<s.length(); i++) {
										StackPane sp = new StackPane();
										if (s.charAt(i) == '1') {
											Circle c = new Circle(x,y,18);
											c.setStrokeWidth(4d);
											c.setStroke(Color.GREEN);	
											c.setFill(Color.WHITE);
											sp.getChildren().addAll(c,new Text("1"));									 
										}
										else {
											Circle c = new Circle(x,y,18);
											c.setStrokeWidth(4d);
											c.setStroke(Color.RED);
											c.setFill(Color.WHITE);
											sp.getChildren().addAll(c,new Text("0"));
										}
										dialogHbox.getChildren().add(sp);
									}
									StackPane prob = new StackPane();
									int pr = (int)(Math.round(ip[cellNum-1].abssqr()*100));
									Circle outerCircle = new Circle(20, 20, 50);
									LinearGradient g = LinearGradient.valueOf(
    									"from 0.0% 0.0% to 0.0% 100.0% "+    // from top to bottom
    									"rgb(14, 147, 0) 0%, "+              // green at the top
										"rgb(14, 147, 0) "+pr+"%, "+  		 // green at percentage
										"rgb(148, 0, 0) "+pr+"%, "+          // red at percentage
										"rgb(148, 0, 0) 100%"                // red at the bottom
									);
									if (pr == 0) {
										outerCircle.setFill(Color.RED);
									}
									else if (pr == 100) {
										outerCircle.setFill(Color.GREEN);
									}
									else {
										outerCircle.setFill(g);
									}
									Circle innerCircle = new Circle(20, 20, 39);
									innerCircle.setFill(Color.WHITE);
									prob.getChildren().addAll(outerCircle, innerCircle, new Text(Double.toString(pr)+"%"));
									container.getChildren().addAll(dialogHbox, prob);
									Scene dialogScene = new Scene(container, 225, 165);
									dialog.setScene(dialogScene);
									dialog.show();
								}
							}
						}
					}
        		}
        	}
        });   
    }

    public void addOverlay(BoardOverlay overlay) {
        this.getChildren().add(overlay);
    }

    public ObservableList<QubitFlow> getWires() {
        return wires;
    }

    public void appendQubit() {
        QubitFlow flow = new QubitFlow(wires.size(), model);
        wires.add(flow);
    }

    public void clear() {
    	System.out.println("QubitBoard: clear");
        wires.forEach(QubitFlow::clear);
        wires.removeIf(qb -> qb.getIndex() > (nQubits - 1));
    }
    
    private void renderCircuit() {
        clear();
        for (Step step: model.getSteps()) {
            List<Gate> gates = step.getGates();
            boolean[] gotit = new boolean[nQubits];
            for (Gate gate : gates) {
                int qb = gate.getMainQubitIndex();
                gotit[qb] = true;
                QubitFlow wire = wires.get(qb);
                wire.setMinWidth(480);
                GateSymbol symbol = wire.addGate(gate);
            }
        }
    }
    
    public List<QubitFlow> getQubitFlows() {
        return this.wires;
    }
    
    private void processCircuit(ArrayList<Step> steps) {
        System.err.println("Process circuit with "+wires.size()+" qubits and "+steps.size()+" steps.");
        Program p = new Program(wires.size());
        for (Step step : steps) {
            System.err.println("Step: "+step);
            p.addStep(step);
        }
        QuantumExecutionEnvironment qee = new SimpleQuantumExecutionEnvironment();
        Consumer<Result> resultConsumer = (t) -> {
            Platform.runLater(() -> {
                Qubit[] qubits = t.getQubits();
                ObservableList<Double> endStates = model.getEndStates();
                for (int i = 0; i < wires.size(); i++) {
                    if (endStates.size() > i) {
                        endStates.set(i, qubits[i].getProbability());
                    } else {
                        endStates.add(i, qubits[i].getProbability());
                    }
                }
            });
        };
        qee.runProgram(p, resultConsumer);
    }
    
    public void redraw() {
        wires.forEach(w -> w.redraw());
    }
}
