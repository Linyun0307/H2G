package h2g;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class BarFlatUISkin extends BarGenerator {
    private Color LightGreenishBlue = new Color(0x55efc4);
    private Color FadedPoster = new Color(0x81ecec);
    private Color GreenDarnerTail = new Color(0x74b9ff);
    private Color ShyMoment = new Color(0xa29bfe);
    private Color CityLight = new Color(0xdfe6e9);
    private boolean isBarFilled = true;
    private boolean hasBarFrame = false;
    private double frameSize = 0.01;
    private Color[] palette = new Color[]{LightGreenishBlue, FadedPoster, GreenDarnerTail, ShyMoment, CityLight};

    public BarFlatUISkin(int[] barSize, double[] scale) {
        super(barSize, scale);
        baseIMG = new SigDraw(barSize[WIDTH] * 2, barSize[HEIGHT], true);
        baseIMG.setYscale(scale[MIN], scale[MAX]);
        baseIMG.setPenRadius(frameSize);
    }

    @Override
    public BufferedImage getBarChart(int frame, double val) {
        double halfWidth = barSize[WIDTH] / 2.0;
        baseIMG.setPenColor(palette[(new Random()).nextInt(5)]);
        if (isBarFilled) {
            baseIMG.filledRectangle(halfWidth, val / 2, halfWidth, val / 2);
            baseIMG.setPenColor(palette[(new Random()).nextInt(5)]);
            baseIMG.enableTransparent(0.6f);
            baseIMG.filledRectangle(halfWidth * 2, val / 2, halfWidth * 1.3, val / 4);
        }
        return baseIMG.getBuffImg();
    }

    @Override
    public BufferedImage getBarChart(int frame, double val1, double val2) {
        return null;
    }

    @Override
    public void loadConfig(String filename) throws Exception {
    }

    @Override
    public void loadConfig() throws Exception {
    }

    public static void main(String[] args) {
        //todo: Change the test program in to a real skin that can be used.
        for (int i = 1; i <= 5; i++) {
            BarFlatUISkin b = new BarFlatUISkin(new int[]{100, 1000}, new double[]{0, 10000});
            SigDraw s = new SigDraw(b.getBarChart(0, 10000));
            s.save("Overlay_test" + i + ".jpg");
        }
    }
}
