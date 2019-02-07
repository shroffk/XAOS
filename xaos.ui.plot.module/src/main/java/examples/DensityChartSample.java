/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package examples;

import chart.DensityChartFX;
import chart.DensityChartFX.Data;
import chart.DensityChartFX.DefaultData;
import chart.NumberAxis;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;
import plugins.CoordinatesLabel;
import plugins.CoordinatesLines;
import plugins.Pan;
import plugins.PropertyMenu;
import plugins.Zoom;
import plugins.CursorTool;
import plugins.KeyPan;


public class DensityChartSample extends Application {
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    Integer NB_POINTS = 101;
    
    @Override
    public void start(Stage stage) {
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });


        stage.setTitle("Density Chart Sample");
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setAnimated(false); 
        xAxis.setAutoRangePadding(0.0);        
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(false);
        yAxis.setAutoRangePadding(0.0);
        final DensityChartFX<Number,Number> chart = new DensityChartFX<Number, Number>(xAxis, yAxis);
        chart.setTitle("Test data");
        chart.setAnimated(false);
        chart.setSmooth(true);
        chart.setProjectionLinesVisible(true);
        
        chart.getChartPlugins().addAll(new PropertyMenu(), new Zoom(), new KeyPan(), new Pan(), new CursorTool(), new CoordinatesLines(), new CoordinatesLabel());

        
        // readImage() creates an instance of DensityChart.Data   
        chart.setData(createImage(100.0,10.55,5,3.27));
        //chart.setData(createData());
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.RIGHT);


        BorderPane borderPane = new BorderPane(chart);
                 
        chart.setFocusTraversable(true);              
        Scene scene = new Scene(borderPane, 800, 600);

        stage.setScene(scene);
        stage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    public Data<Number,Number> createImage(double meanx, double rmsx, double meany, double rmsy){        
        double[] xValues = new double[NB_POINTS];
        double[] yValues = new double[NB_POINTS];
        double[][] zValues = new double[xValues.length][yValues.length]; 
        
        for (int xIndex = 0; xIndex < xValues.length; xIndex++) {
            xValues[xIndex] = (meanx - 10.0*rmsx) + xIndex/(xValues.length-1.0)*20.0*rmsx;  
            yValues[xIndex] = (meany - 10.0*rmsy) + xIndex/(yValues.length-1.0)*20.0*rmsy; 
        }
        
        double minVal = 10.0;
                
        
        for (int yIndex = 0; yIndex < yValues.length; yIndex++) {
            for (int xIndex = 0; xIndex < xValues.length; xIndex++) {
                zValues[xIndex][yIndex] = 100*Math.exp(-(xValues[xIndex]-meanx)*(xValues[xIndex]-meanx)/(2*rmsx*rmsx))*Math.exp(-(yValues[yIndex]-meany)*(yValues[yIndex]-meany)/(2*rmsy*rmsy)) + 0.05*RANDOM.nextGaussian();  
                minVal = Math.min(zValues[xIndex][yIndex],minVal);
            }
        }   
                
        return new DefaultData<>(toNumbers(xValues), toNumbers(yValues), zValues);
    }   
    
    private static Data<Number, Number> createData(){
        double[] x = { -12, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12 };
        double[] y = x;
        double[][] z = new double[x.length][y.length];
        for (int yIndex = 0; yIndex < y.length; yIndex++) {
            for (int xIndex = 0; xIndex < x.length; xIndex++) {
                z[xIndex][yIndex] = Math.sin(y[yIndex] * x[xIndex]);
            }
        }

        return new DefaultData<>(toNumbers(x), toNumbers(y), z);
    }

    private static Number[] toNumbers(double[] array) {
        Number[] result = new Number[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    public static Data<Number, Number> readImage() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(DensityChartSample.class.getResourceAsStream("image.txt")));
            reader.readLine();
            String[] x = reader.readLine().split(" ");
            reader.readLine();
            String[] y = reader.readLine().split(" ");
            reader.readLine();
            String[] z = reader.readLine().split(" ");
            reader.close();

            Number[] xValues = toNumberArray(x);
            Number[] yValues = toNumberArray(y);

            double[][] zValues = new double[x.length][y.length];
            int i = 0;
            for (int yIdx = 0; yIdx < y.length; yIdx++) {
                for (int xIdx = 0; xIdx < x.length; xIdx++) {
                    zValues[xIdx][yIdx] = Double.parseDouble(z[i++]);
                }
            }
            return new DefaultData<>(xValues, yValues, zValues);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not read image file:\n" + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private static Number[] toNumberArray(String[] stringValues) {
        Number[] numberValues = new Number[stringValues.length];
        for (int i = 0; i < stringValues.length; i++) {
            numberValues[i] = Double.valueOf(stringValues[i]);
        }
        return numberValues;
    }

}