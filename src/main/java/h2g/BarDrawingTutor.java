package h2g;

import java.util.HashMap;
import java.awt.Color;
import java.awt.image.BufferedImage;

class BarDrawingTutor {
    //private static Interpolator interpolator;
    public static HashMap<BarLocation, Double> transparency;
    public static double[] barWidth;
    public static CanvaStyle canvaStyle;
    private Bar[] bar;
    private int index = -1;
    private double maxValue;
    public int currentFrame;
    public BarDrawingTutor(int currentFrame, Bar[] sortedBar, double maxValue) {
        this.currentFrame = currentFrame;
        this.bar = sortedBar;
        this.maxValue = maxValue;
    }
    public boolean hasNext() { return index+1<bar.length; }
    public void next() { index++; }
    public double getLocation() { return bar[index].bL.location; }
    public double getTransparency() {
        if(transparency.containsKey(bar[index].bL)) {
            return transparency.get(bar[index].bL);
        }
        else return canvaStyle.maxTrantransparency;
    }
    public BufferedImage getBarImg(int[] barSize, double[] yValue, String text) {
        String skin = getBarSkin();
        BarGenerator barSkin = DynamicLoader.get(skin, barSize, yValue, canvaStyle.rotated);
        barSkin.baseIMG.enableTransparent((float)getTransparency());
        return barSkin.getBarChart(currentFrame, text, getValue());
    }
    public String getBarSkin() {return canvaStyle.barSkin[ getBarID() ];}
    public Color getTextColor() {return DynamicLoader.getFontColor(getBarSkin());}
    public int getBarID() { return bar[index].id; }
    public double getValue() { return bar[index].val; }
    public double getDeltaValue() { return bar[index].dVal; }
    public double getPatternGap() { return barWidth[barWidth.length-1]; }
    public double getBarWidth() { return barWidth[getBarID()%barWidth.length]; }
    public void seek(int index) { this.index = index; }
    public double getMaxValue() { return maxValue; }
    public void seek() { seek(-1); }
}
class BarDrawingHelper {
    public static Interpolator interpolator;
    public int currentFrame;
    public double maxValue;
    public Bar[] getSortedBar(int[] layerOrder, Bar[] bar) {
        double maxValue = -Double.MIN_VALUE;
        int index = 0;
        Bar[] tmp = new Bar[bar.length];
        for(int layer:layerOrder) {
            for(int x=0;x<bar.length;++x) {
                if(bar[x].bL.layer == layer) {
                    tmp[index++] = bar[x];
                    maxValue = (maxValue<bar[x].val)?bar[x].val:maxValue;
                }
            }
        }
        Bar[] sortedBar = new Bar[index];
        for(int x=0;x<index;++x) {
            sortedBar[x] = tmp[x];
        }
        this.maxValue = maxValue;
        return sortedBar;
    }
    public BarDrawingHelper(CanvaStyle canvaStyle, double[][] rawData) {
        BarDrawingHelper.interpolator = new Interpolator(canvaStyle, rawData);
        BarDrawingTutor.canvaStyle = canvaStyle;
        BarDrawingTutor.transparency = interpolator.bLD.transparency;
        BarDrawingTutor.barWidth = Interpolator.validWidth;
    }
    public BarDrawingTutor getTutor(int currentFrame) {
        this.currentFrame = currentFrame;
        Bar[] bar = interpolator.bar[currentFrame];
        Bar[] sortedBar = getSortedBar(new int[]{BarLocation.LAYER_TOP, BarLocation.LAYER_MID, BarLocation.LAYER_BOTTOM}, bar);
        return new BarDrawingTutor(currentFrame, sortedBar, maxValue);
    }
    public int getTotalFrame() { return interpolator.endFrame; }
}