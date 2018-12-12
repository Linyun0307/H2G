package h2g;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class BarBasicSkin extends BarGenerator {
    private Color[] segColor = {Color.RED, Color.YELLOW, Color.BLUE};
    private Color frameColor = Color.GREEN;
    private boolean isBarFilled = true;

    @Override
    public String toString() {
        return "BarBasicSkin{" +
                "segColor=" + Arrays.toString(segColor) +
                ", frameColor=" + frameColor +
                ", isBarFilled=" + isBarFilled +
                ", hasBarFrame=" + hasBarFrame +
                ", frameSize=" + frameSize +
                '}';
    }

    private boolean hasBarFrame = false;
    private double frameSize = 0.01;

    public BarBasicSkin(int[] barSize, double[] scale, boolean rotated) {
        super(barSize, scale, rotated);
        baseIMG = new SigDraw(barSize[WIDTH], barSize[HEIGHT], true);
        setScale(scale);
        baseIMG.setPenRadius(frameSize);
    }

    @Override
    public BufferedImage getBarChart(int frame, String text, double... val) {
        double halfWidth = barSize[WIDTH] / 2.0;
        double halfHeight = barSize[HEIGHT] / 2.0;
        double baseVal = 0;
        for (int x = 0; x < val.length && x < segColor.length; ++x) {
            baseIMG.setPenColor(segColor[x]);
            if (rotated) {
                if (isBarFilled) baseIMG.filledRectangle(baseVal + val[x] / 2, halfHeight, val[x] / 2, halfHeight);
                else baseIMG.rectangle(baseVal + val[x] / 2, halfHeight, val[x] / 2, halfHeight);
            } else {
                if (isBarFilled) baseIMG.filledRectangle(halfWidth, baseVal + val[x] / 2, halfWidth, val[x] / 2);
                else baseIMG.rectangle(halfWidth, baseVal + val[x] / 2, halfWidth, val[x] / 2);
            }
            baseVal += val[x];
        }

        baseIMG.setPenColor(frameColor);
        if (hasBarFrame) {
            if (rotated)
                baseIMG.rectangle((scale[MAX] + scale[MIN]) / 2, halfHeight, (scale[MAX] - scale[MIN]) / 2, halfHeight);
            else baseIMG.rectangle(halfWidth, (scale[MAX] + scale[MIN]) / 2, halfWidth, (scale[MAX] - scale[MIN]) / 2);
        }

        //return baseIMG.getSubImage(x, y, width, height);
        return baseIMG.getBuffImg();
    }

    @Override
    public void loadConfig(String filename) throws Exception {
        ConfigLoader loader = new ConfigLoader(filename);
        ArrayList<Color> segColor = new ArrayList<>();
        for (int x = 1; ; ++x) {
            Color _segColor = loader.getColor("segColor" + x);
            if (_segColor != null) segColor.add(_segColor);
            else break;
        }
        if (!segColor.isEmpty()) this.segColor = (Color[]) (segColor.toArray());

        Color _frameColor = loader.getColor("frameColor");
        if (_frameColor != null) frameColor = _frameColor;
        isBarFilled = loader.getBool("isBarFilled");
        hasBarFrame = loader.getBool("hasBarFrame");
        double _frameSize = loader.getDouble("frameSize");
        if (_frameSize != 0) frameSize = _frameSize;
    }

    @Override
    public void loadConfig() throws Exception {
        loadConfig("BarBasicSkin.json");
    }

    public static void main(String[] args) throws Exception {
        BarBasicSkin b = new BarBasicSkin(new int[]{1000, 100}, new double[]{0, 10000}, true);
        //b.loadConfig();
        System.out.println(b);
        SigDraw s = new SigDraw(b.getBarChart(0, "text", 1000, 4000, 2000), true);
        s.save("test1.jpg");
        //new SigDraw( s.getSubImage(50, 500, 20, 200) ).save("test2.jpg");
    }
}