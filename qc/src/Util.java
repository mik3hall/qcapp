package us.hall.qcapp;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.redfx.strange.gate.*;
import org.redfx.strange.BlockGate;
import org.redfx.strange.Complex;
import org.redfx.strange.Gate;
import org.redfx.strange.Program;
import org.redfx.strange.QuantumExecutionEnvironment;
import org.redfx.strange.Result;
import org.redfx.strange.local.SimpleQuantumExecutionEnvironment;
import org.redfx.strangefx.render.Renderer;
import org.redfx.strangefx.ui.GateSymbol;
import org.redfx.strangefx.ui.QubitBoard;
import org.redfx.strangefx.ui.QubitFlow;

public class Util {
	
	public static void show(Parent p) {
		show(p, "");
	}
	
	private static void show(Parent p, String indent) {
		for (Node n : p.getChildrenUnmodifiable()) {
			if (n instanceof Parent) {
				if (n instanceof QubitBoard) {
					System.out.println(indent + "QubitBoard(Group) " + n.getBoundsInParent());
				}
				else if (n instanceof QubitFlow) {
					System.out.println(indent + "QubitFlow(Region) " + " width " + ((Region)n).getWidth() + " " + n.getBoundsInParent());
				}
				else if (n instanceof BorderPane || n instanceof StackPane) {
					System.out.println(indent + n.getClass().getSimpleName() + " width " + ((Region)n).getWidth() + " "+ n.getBoundsInParent());
				}
				else {
					if (n instanceof Region) {
						System.out.println(indent + n.getClass().getSimpleName() + " width " + ((Region)n).getWidth() + " " + n.getBoundsInParent());
					}
					else {
						System.out.println(indent + n.getClass().getSimpleName() + " " + n + " " + n.getBoundsInParent());
					}
				}
				show((Parent)n, indent+"   ");
			}
			else {
				System.out.println(n.getClass().getSimpleName() + " " + n + " " + n.getBoundsInParent());
			}
		}
	}
	
	public static void debug(Program program) {
/*
		for (Node n : qb.getChildren()) {
			if (n instanceof Parent) {
				if (n instanceof QubitFlow) {
					Region r = (Region)n;
					r.setStyle("-fx-border-color: red");
					r.setBorder(new Border(new BorderStroke(Color.BLACK, 
            				BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
					System.out.println("QubitFlow(Region) " + n.getBoundsInParent());
				}
*/
/*
				else if (n instanceof BorderPane || n instanceof StackPane) {
					System.out.println(indent + n.getClass().getSimpleName() + " " + n.getBoundsInParent());
				}
				else {
					System.out.println(indent + n.getClass().getSimpleName() + " " + n + " " + n.getBoundsInParent());
				}
				show((Parent)n, indent+"   ");
*/
//			}
/*
			else {
				System.out.println(n.getClass().getSimpleName() + " " + n + " " + n.getBoundsInParent());
			}
*/
//		}
	}
/*	
	public static QubitBoard debug(Parent p, String indent) {
		for (Node n : p.getChildrenUnmodifiable()) {
			if (n instanceof MeasurementUI) {
				if (n instanceof QubitBoard) {
					System.out.println(indent + "QubitBoard(Group) " + n.getBoundsInParent());
				}
				else if (n instanceof QubitFlow) {
					System.out.println(indent + "QubitFlow(Region) " + " width " + ((Region)n).getWidth() + " " + n.getBoundsInParent());
				}
				else if (n instanceof BorderPane || n instanceof StackPane) {
					System.out.println(indent + n.getClass().getSimpleName() + " width " + ((Region)n).getWidth() + " "+ n.getBoundsInParent());
				}
				else {
					if (n instanceof Region) {
						System.out.println(indent + n.getClass().getSimpleName() + " width " + ((Region)n).getWidth() + " " + n.getBoundsInParent());
					}
					else {
						System.out.println(indent + n.getClass().getSimpleName() + " " + n + " " + n.getBoundsInParent());
					}
				}
				show((Parent)n, indent+"   ");
			}
			else {
				System.out.println(n.getClass().getSimpleName() + " " + n + " " + n.getBoundsInParent());
			}
		}	
	}
*/	
	public static void show(Object o) {
		System.out.println(o);
	}
	
	public static void renderProgram(QubitBoard qb) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
            	qb.getStylesheets().add(getClass()
            		.getResource("/styles.css")
            		.toExternalForm());
				Controller controller = QuantumJava.getController();
				ScrollPane sp = controller.getDisplayPane();
				//sp.getChildren().clear();
				BackgroundFill backgroundFill =
					new BackgroundFill(
							Color.WHITESMOKE,
							new CornerRadii(0),
							new Insets(0)
							);
				Background background = new Background(backgroundFill);
				sp.setBackground(background);
				sp.setContent(qb);
			}
		});	
	}
	
	public static void renderProgram(Program p, String title) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				QubitBoard qb = Renderer.getRenderGroup(p);
				Controller controller = QuantumJava.getController();
				qb.getStylesheets().add(getClass()
            		.getResource("/styles.css")
            		.toExternalForm());
				ScrollPane sp = controller.getDisplayPane();
				BackgroundFill backgroundFill =
					new BackgroundFill(
							Color.WHITESMOKE,
							new CornerRadii(0),
							new Insets(0)
							);
				Background background = new Background(backgroundFill);
				sp.setBackground(background);
				sp.setContent(qb);
			}
		});
	}
	
	public static void addProbabilities(Program p, int count) {
		Controller controller = QuantumJava.getController();
		BorderPane bp = controller.getRenderPane();
		showProbabilities(p, count, bp);
	}
	
	public static void showProbabilities(Program p, int count) {
		Controller controller = QuantumJava.getController();
		BorderPane bp = controller.getRenderPane();
		bp.getChildren().clear();		
	}
	
    private static void showProbabilities(Program p, int count, BorderPane bp) {
        QuantumExecutionEnvironment simulator = new SimpleQuantumExecutionEnvironment();
        int nq = p.getNumberQubits();
        int[] counter = new int[1 << nq];
        for (int i = 0; i < count; i++) {
            Result result = simulator.runProgram(p);
            int prob = result.getMeasuredProbability();
            counter[prob]++;
        }
        Platform.runLater(() -> renderMeasuredProbabilities(counter, bp));
    }
    
    public static void renderMeasuredProbabilities(int[] results) {
		Controller controller = QuantumJava.getController();
		BorderPane bp = controller.getRenderPane();
		bp.getChildren().clear();	
		renderMeasuredProbabilities(results, bp);   	
    } 
	
	private static void renderMeasuredProbabilities(int[] results, BorderPane bp) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Integer> barChart = new BarChart(xAxis, yAxis);
        barChart.setData(getChartData(results));
        barChart.setTitle("Measured probability distribution");
        bp.getChildren().add(barChart);
    }

    private static ObservableList<XYChart.Series<String, Integer>> getChartData(int[] results) {
        ObservableList<XYChart.Series<String, Integer>> answer = FXCollections.observableArrayList();
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("occurrences");
        answer.add(series);
        for (int i = 0; i < results.length; i++) {
            series.getData().add(new XYChart.Data<>(getFixedBinaryString(i, (int) (Math.log(results.length) / Math.log(2))), results[i]));
        }
        return answer;
    }

    private static String getFixedBinaryString(int i, int w) {
        StringBuffer buff = new StringBuffer(Integer.toBinaryString(i));
        while (buff.length() < w) {
            buff.insert(0, "0");
        }
        return buff.toString();
    }   
}