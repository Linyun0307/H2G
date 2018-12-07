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
    public BarGenerator(int[] barSize, double[] scale) {
        this.barSize = barSize;
        this.scale = scale;
    };
    public abstract BufferedImage getBarChart(int frame, double val);
    public abstract BufferedImage getBarChart(int frame, double val1, double val2);
    public abstract void loadConfig(String filename) throws Exception;
    public abstract void loadConfig() throws Exception;
}