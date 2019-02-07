/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Demo;

import chart.LineChartFX;
import chart.NumberAxis;
import chart.data.DataReducingSeries;
import chart.LogAxis;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import plugins.CoordinatesLabel;
import plugins.CoordinatesLines;
import plugins.DataPointTooltip;
import plugins.Pan;
import plugins.Zoom;
import plugins.CursorTool;
import plugins.KeyPan;

/**
 *
 * @author reubenlindroos
 */
public class LineChartGenerator {
    private DataReducingSeries<Number, Number> series0;
    private DataReducingSeries<Number, Number> series1;
    private DataReducingSeries<Number, Number> series2;
    private LineChartFX<Number,Number> chart ;
    
    private ValueAxis xAxis = new NumberAxis();
    private ValueAxis yAxis = new NumberAxis();
    
     private static final Random RANDOM = new Random(System.currentTimeMillis());
    
     
  public LineChartFX getChart(Integer NB_OF_POINTS) {
        if (chart == null) {
        generateChart(NB_OF_POINTS);
    } 
    //
    return chart; }
    
    
    
    public void generateChart(Integer NB_OF_POINTS) {
        xAxis.setAnimated(false);
        yAxis.setAnimated(false);

        chart = new LineChartFX<Number, Number>(xAxis, yAxis);
        chart.setTitle("Test data");
        chart.setAnimated(false);

        chart.getChartPlugins().addAll(new CursorTool(), new KeyPan(), new CoordinatesLines(), 
               new Zoom(), new Pan(), new CoordinatesLabel(), new DataPointTooltip() );
        
        if (series0==null){
        series0 = new DataReducingSeries<>();
        series0.setName("Generated test data-horizontal");
        series0.setData(generateData(NB_OF_POINTS));
        
        
        series1 = new DataReducingSeries<>();
        series1.setName("Generated test data-vertical");
        series1.setData(generateData(NB_OF_POINTS));
        
        
        series2 = new DataReducingSeries<>();
        series2.setName("Generated test data-longitudinal");
        series2.setData(generateData(NB_OF_POINTS));}
        
        
        chart.getData().add(series0.getSeries());
        chart.getData().add(series1.getSeries());
        chart.getData().add(series2.getSeries());
        chart.setSeriesAsHorizontal(0);//red
        chart.setSeriesAsVertical(1);//blue
        chart.setSeriesAsLongitudinal(2);//horrible green
    }


    private static ObservableList<XYChart.Data<Number, Number>> generateData(int nbOfPoints) {
        int[] yValues = generateIntArray(0, 5, nbOfPoints);
        List<XYChart.Data<Number, Number>> data = new ArrayList<>(nbOfPoints);
        for (int i = 0; i < yValues.length; i++) {
            data.add(new XYChart.Data<Number, Number>(i, yValues[i]));
        }
        return FXCollections.observableArrayList(data);
    }
    public static int[] generateIntArray(int firstValue, int variance, int size) {
        int[] data = new int[size];
        data[0] = firstValue;
        for (int i = 1; i < data.length; i++) {
            int sign = RANDOM.nextBoolean() ? 1 : -1;
            data[i] = data[i - 1] + (int) (variance * RANDOM.nextDouble()) * sign;
        }
        return data;
    }

    public LineChartFX setYLogAxis(Integer nb_of_points) {
        yAxis = new LogAxis();
        generateChart(nb_of_points);
        return chart;
    }
      public LineChartFX setXLogAxis(Integer nb_of_points) {
        xAxis = new LogAxis();
        generateChart(nb_of_points);
        return chart;
    }
      public LineChartFX resetAxes(Integer nb_of_points) {
          xAxis = new NumberAxis();
          yAxis = new NumberAxis();
          generateChart(nb_of_points);
          return chart;
      }

}