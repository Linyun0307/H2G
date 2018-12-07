package h2g;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BarBasicSkin extends BarGenerator {
    private Color segColor1 = Color.BLACK;
    private Color segColor2 = Color.BLUE;
    private Color segColor3 = Color.WHITE;
    private Color frameColor = Color.GREEN;
    private boolean isBarFilled = true;

    @Override
    public String toString() {
        return "BarBasicSkin{" +
                "segColor1=" + segColor1 +
                ", segColor2=" + segColor2 +
                ", segColor3=" + segColor3 +
                ", frameColor=" + frameColor +
                ", isBarFilled=" + isBarFilled +
                ", hasBarFrame=" + hasBarFrame +
                ", frameSize=" + frameSize +
                '}';
    }

    private boolean hasBarFrame = true;
    private double frameSize = 0.01;
    public BarBasicSkin(int[] barSize, double[] scale) {
        super(barSize, scale);
        baseIMG = new SigDraw(barSize[WIDTH], barSize[HEIGHT], true);
        baseIMG.setYscale(scale[MIN], scale[MAX]);
        baseIMG.setPenRadius(frameSize);
    }
    @Override
    public BufferedImage getBarChart(int frame, double val) {
        return getBarChart(frame, val, 0);
    }
    @Override
    public BufferedImage getBarChart(int frame, double val1, double val2) {
        double halfWidth = barSize[WIDTH]/2;
        baseIMG.setPenColor(segColor1); 
        if(isBarFilled) baseIMG.filledRectangle(halfWidth, val1/2, halfWidth, val1/2);
        else baseIMG.rectangle(halfWidth, val1/2, halfWidth, val1/2);

        baseIMG.setPenColor(segColor2);
        if(isBarFilled) baseIMG.filledRectangle(halfWidth, val1 + val2/2, halfWidth, val2/2);
        else baseIMG.rectangle(halfWidth, val1 + val2/2, halfWidth, val2/2);

        double val3 = scale[HEIGHT]-val1-val2;
        baseIMG.setPenColor(segColor3);
        if(isBarFilled) baseIMG.filledRectangle(halfWidth, val1 + val2 + val3/2, halfWidth, val3/2);
        else baseIMG.rectangle(halfWidth, val1 + val2 + val3/2, halfWidth, val3/2);
        

        baseIMG.setPenColor(frameColor);
        if(hasBarFrame) baseIMG.rectangle(halfWidth, (scale[MAX] + scale[MIN])/2, halfWidth, (scale[MAX] - scale[MIN])/2);

        //return baseIMG.getSubImage(x, y, width, height);
        return baseIMG.getBuffImg();
    }
    @Override
    public void loadConfig(String filename) throws Exception {
        ConfigLoader loader = new ConfigLoader(filename);
        Color _segColor1 = loader.getColor("segColor1");
        if (_segColor1 != null) segColor1 = _segColor1;
        Color _segColor2 = loader.getColor("segColor2");
        if (_segColor2 != null) segColor2 = _segColor2;
        Color _segColor3 = loader.getColor("segColor3");
        if (_segColor3 != null) segColor3 = _segColor3;
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
        BarBasicSkin b = new BarBasicSkin(new int[]{100,1000} ,new double[]{0,10000});
        b.loadConfig();
        System.out.println(b);
        SigDraw s = new SigDraw( b.getBarChart(0, 800, 6000) ,true);
        s.save("test1.jpg");
        new SigDraw( s.getSubImage(50, 500, 20, 200) ).save("test2.jpg");
    }
}