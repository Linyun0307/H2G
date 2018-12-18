package h2g;

import java.util.HashMap;
import java.awt.image.BufferedImage;

class StackedBarDrawingTutor extends BarDrawingTutor {
    //private static Interpolator interpolator;
    public double[][] stackedBarData;
    public StackedBarDrawingTutor(int currentFrame, Bar[] sortedBar, double maxValue, double[][] stackedBarData) {
        super( currentFrame, sortedBar, maxValue);
        this.stackedBarData = stackedBarData;
    }
    public BufferedImage getStackedBarImg(int[] barSize, double[] yValue, String text) {
        String skin = canvaStyle.barSkin[ getBarID() ];
        BarGenerator barSkin = DynamicLoader.get(skin, barSize, yValue, canvaStyle.rotated);
        return barSkin.getBarChart(currentFrame, text, stackedBarData[ getBarID() ] );
    }
}
class StackedBarDrawingHelper extends BarDrawingHelper{
    private static Interpolator[] interpolators;
    public static double[][] converToGlobalData(double[][] rawData, double yValueMin) {
        double[][] globalData = new double[rawData.length][];
        for(int i=0;i<rawData.length;++i) {
            double sum = 0;
            for(int x=0;x<rawData[i].length;++x) {sum+=rawData[i][x];}
            globalData[i] = new double[]{yValueMin, sum};
        }
        return globalData;
    }
    public StackedBarDrawingHelper(CanvaStyle canvaStyle, HistogramData histogramData, double[][] rawData) {
        super(canvaStyle, converToGlobalData(rawData, histogramData.yValue[0] ));
        interpolators = new Interpolator[rawData[0].length];
        for(int i=0;i<rawData[0].length;++i) {
            double[][] convertedData = new double[rawData.length][];
            for(int x=0;x<rawData.length;++x) {
                convertedData[x] = new double[]{0, rawData[x][i]};
            }
            interpolators[i] = new Interpolator(canvaStyle, convertedData);
        }
    }
    public StackedBarDrawingTutor getStackedTutor(int currentFrame) {
        this.currentFrame = currentFrame;
        Bar[] bar = interpolator.bar[currentFrame];
        Bar[] sortedBar = getSortedBar(new int[]{BarLocation.LAYER_BOTTOM, BarLocation.LAYER_MID, BarLocation.LAYER_TOP}, bar);

        double[][] stackedBarData = new double[bar.length][interpolators.length];
        for(int x=0;x<interpolators.length;++x) {
            Bar[] bars = interpolators[x].bar[currentFrame];
            for(int y=0;y<bar.length;++y) {
                stackedBarData[bars[y].id][x] = bars[y].val;
            }
        }
        return new StackedBarDrawingTutor(currentFrame, sortedBar, maxValue, stackedBarData);
    }
}