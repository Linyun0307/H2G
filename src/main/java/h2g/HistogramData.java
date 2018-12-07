package h2g;

public class HistogramData {
    String header = "";
    String footer = "";
    double minValue = 0.0;
    String[] keys = {};
    
    // Blank size will be 1.0-sum(ratio)
    double[] values = {};
    //double[] xScale = { 0, 1.0 }; // MIN, MAX
    double[] yValue = {0, 1.0}; // MIN, MAX
    double rulerStep = 0.0;
    int rulerGrade = 0;
}