package h2g;

import java.awt.image.BufferedImage;

public abstract class BarGenerator {
    final static int HEIGHT = 1;
    final static int WIDTH = 0;
    final static int MAX = 1;
    final static int MIN = 0;
    public SigDraw baseIMG;
    public int[] barSize;
    public double[] scale;
    public boolean rotated;
    public BarGenerator(int[] barSize, double[] scale, boolean rotated) {
        this.barSize = barSize;
        this.scale = scale;
        this.rotated = rotated;
    };
    public void setScale(double[] scale) {
        this.scale = scale;
        if(rotated) baseIMG.setXscale(scale[0], scale[1]);
        else baseIMG.setYscale(scale[0], scale[1]);
    }
    public abstract BufferedImage getBarChart(int frame ,String text, double... val);
}