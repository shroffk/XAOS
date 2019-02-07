/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package chart;

import com.sun.javafx.charts.Legend;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;


/**
 * A thin extension of the FX {@link LineChart} supporting custom {@link XYChartPlugin} plugin implementations.
 * 
 * @author Grzegorz Kruk
 * @param <X> type of X values
 * @param <Y> type of Y values
 */
public class LineChartFX<X, Y> extends LineChart<X, Y> {
    
    //Variables used to include plugIns in teh Chart
    private final Group pluginsNodesGroup = new Group();
    private final PluginManager pluginManager = new PluginManager(this, pluginsNodesGroup);
    
    //Variables that controls the line that doens't show in the Legend or series that are not displayed at the chart at a given time
    private final List<String> noShowInLegend = new ArrayList<>();
    private final List<String> seriesDrawnInPlot = new ArrayList<>();
   
    // Variable that stored the color line setup
    private String colorStyle = new String();
   
    /**
     * Construct a new LineChart with the given axis and data.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @see javafx.scene.chart.LineChart#LineChart(Axis, Axis)
     */
    public LineChartFX(Axis<X> xAxis, Axis<Y> yAxis) {
        this(xAxis, yAxis, FXCollections.<Series<X, Y>> observableArrayList());
    }

    /**
     * Construct a new LineChart with the given axis and data.
     *
     * @param xAxis The x axis to use
     * @param yAxis The y axis to use
     * @param data The data to use, this is the actual list used so any changes to it will be reflected in the chart
     * @see javafx.scene.chart.LineChart#LineChart(Axis, Axis, ObservableList)
     */
    public LineChartFX(Axis<X> xAxis, Axis<Y> yAxis, ObservableList<Series<X, Y>> data) {
        super(xAxis, yAxis, data);
        getStylesheets().add("/styles/chart.css");                         
    }
    public LineChartFX(ObservableList<Double> data) {
        super((Axis<X>)new NumberAxis(), (Axis<Y>)new NumberAxis());
        Series<X,Y> dataSet = new Series<X,Y>();
        
        
        for (Integer step = 0; step<data.size();step++ ) {
            Data dataPoint = new Data(step,data.get(step));
            dataSet.getData().add(dataPoint);
        }
        this.getData().add(dataSet);
        getStylesheets().add("/styles/chart.css");                         
    }

    /**
     * Returns a list of plugins added to the chart.
     * 
     * @return non-null list of plugins added to the chart
     */
    public final ObservableList<XYChartPlugin> getChartPlugins() {
        return pluginManager.getPlugins();
    }
    public void addChartPlugins(ObservableList<XYChartPlugin> plugins){
         plugins.forEach(item->{
         try{
            pluginManager.getPlugins().add(item);
         }
         catch (Exception e) {
             System.out.println("Error occured whilst adding" + item.getClass().toString());
         }
         });
        
    }

    public final void setSeriesAsHorizontal(Integer index){        
                        
        colorStyle = colorStyle+"\n"+"-color"+index+": -horizontal;";
        this.lookup(".chart").setStyle(colorStyle);
               
    }
    
    public final void setSeriesAsVertical(Integer index){        
                
        colorStyle = colorStyle+"\n"+"-color"+index+": -vertical;";
        this.lookup(".chart").setStyle(colorStyle);  
               
    }
    
    public final void setSeriesAsLongitudinal(Integer index){        
        
        colorStyle = colorStyle+"\n"+"-color"+index+": -longitudinal;";
        this.lookup(".chart").setStyle(colorStyle);  
               
    }
    
    public final void setNoShowInLegend(String name){                
        noShowInLegend.add(name);
        updateLegend();
    }    
    
    public boolean isNoShowInLegend(String name){                
        return noShowInLegend.contains(name);
    }
    
    public boolean isSeriesDrawn(String name){
        return seriesDrawnInPlot.contains(name);
    }
    
    @Override
    protected void layoutPlotChildren() {
        movePluginsNodesToFront();        
        super.layoutPlotChildren();
  
    }

    private void movePluginsNodesToFront() {
        getPlotChildren().remove(pluginsNodesGroup);
        getPlotChildren().add(pluginsNodesGroup);       
    }         
    
     @Override
    protected void updateLegend()
    {
        final Legend legend = new Legend();     
        seriesDrawnInPlot.clear();
        legend.getItems().clear();
        for (final Series<X, Y> series : getData())
        {            
            if(!noShowInLegend.contains(series.getName())){
                Legend.LegendItem legenditem = new Legend.LegendItem(series.getName());                
                final CheckBox cb = new CheckBox(series.getName());                
                seriesDrawnInPlot.add(series.getName());
                cb.setUserData(series);
                cb.setSelected(true); 
                //cb.setPadding(new Insets(0,10,0,0));
                cb.setStyle("-fx-text-fill: -color"+this.getData().indexOf(series)+" ;");
                cb.addEventHandler(ActionEvent.ACTION, e ->{
                    final CheckBox box = (CheckBox) e.getSource();
                    @SuppressWarnings("unchecked")
                    final Series<Number, Number> s = (Series<Number, Number>) box.getUserData();
                    s.getNode().setVisible(box.isSelected());
                    s.getData().forEach(data ->{
                        StackPane stackPane = (StackPane) data.getNode();
                        stackPane.setVisible(box.isSelected());
                    }); 
                    if(box.isSelected()){
                        if (!seriesDrawnInPlot.contains(s.getName())){
                            seriesDrawnInPlot.add(s.getName());
                        }
                    } else {
                        seriesDrawnInPlot.remove(s.getName());
                    }
                });
                legenditem.setText("");
                legenditem.setSymbol(cb);
                legend.getItems().add(legenditem);
            }
        }
        setLegend(legend);
    }
   
}