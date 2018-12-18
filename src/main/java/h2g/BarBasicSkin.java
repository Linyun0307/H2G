package h2g;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class BarBasicSkin extends BarGenerator {
    private Color[] segColor = {Color.RED, Color.YELLOW, Color.BLUE};
    private Color frameColor = Color.GREEN;
    private boolean isBarFilled = true;
    private boolean hasBarFrame = false;
    private double frameSize = 0.01;
    private Color fontColor = Color.BLACK;
    private int textOffset = 50;
    private Font textFont = null;

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

    public BarBasicSkin(BarBasicSkinStyle barStyle, int[] barSize, double[] scale, boolean rotated) {
        super(barSize, scale, rotated);
        if (barStyle != null) {
            this.segColor = barStyle.segColor;
            this.frameColor = barStyle.frameColor;
            this.isBarFilled = barStyle.isBarFilled;
            this.hasBarFrame = barStyle.hasBarFrame;
            this.frameSize = barStyle.frameSize;
            this.textFont = barStyle.textFont;
            this.textOffset = barStyle.textOffset;
            this.fontColor = barStyle.fontColor;
        } else {
            this.textFont = new Font("Microsoft YaHei Light", 1, 15);
        }
        baseIMG = new SigDraw(barSize[WIDTH], barSize[HEIGHT], true);
        setScale(scale);
        baseIMG.setPenRadius(frameSize);
    }

    public BarBasicSkin(int[] barSize, double[] scale, boolean rotated) {
        this(null, barSize, scale, rotated);
    }

    @Override
    public BufferedImage getBarChart(int frame, String text, double... val) {
        double halfWidth = barSize[WIDTH] / 2.0;
        double halfHeight = barSize[HEIGHT] / 2.0;
        /*double baseVal = 0;
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
        }*/
        double baseVal = 0, totalVal;
        for (int x = Math.min(val.length,segColor.length)-1 ; x>=0 ; --x) {
            baseVal += val[x];
        }
        totalVal = baseVal;
        for (int x = Math.min(val.length,segColor.length)-1 ; x>=0 ; --x) {
            baseIMG.setPenColor(segColor[x]);
            if (rotated) {
                if (isBarFilled) baseIMG.filledRectangle((baseVal-scale[0])/2, halfHeight, (baseVal-scale[0])/2, halfHeight);
                else baseIMG.rectangle((baseVal-scale[0])/2, halfHeight, (baseVal-scale[0])/2, halfHeight);
            } else {
                if (isBarFilled) baseIMG.filledRectangle(halfWidth, (baseVal-scale[0])/2, halfWidth, (baseVal-scale[0])/2);
                else baseIMG.rectangle(halfWidth, (baseVal-scale[0])/2, halfWidth, (baseVal-scale[0])/2);
            }
            baseVal -= val[x];
        }
        if(text!=null) {
            baseVal = totalVal;
            baseIMG.setPenColor(fontColor);
            if(rotated) {
                baseVal += (scale[1]-scale[0])*textOffset/barSize[WIDTH];
                baseIMG.text(baseVal, halfHeight, text);
            } else {
                baseVal += (scale[1]-scale[0])*textOffset/barSize[HEIGHT];
                baseIMG.text(halfWidth, baseVal, text);
            }
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

    public static void main(String[] args) throws Exception {
        //Flat UI test
        BarBasicSkinStyle style = new BarBasicSkinStyle();
        style.loadConfig("BarFlatUI.json");
        BarBasicSkin b = new BarBasicSkin(style, new int[]{1000, 100}, new double[]{0, 8000}, true);
        System.out.println(b);
        SigDraw s = new SigDraw(b.getBarChart(0, "text", 1000, 2000, 1000, 2000, 2000), true);
        s.save("FlatUI_test.jpg");
        //Basic UI test
        style = new BarBasicSkinStyle();
        style.loadConfig("BarBasicSkin.json");
        b = new BarBasicSkin(style, new int[]{1000, 100}, new double[]{0, 3000}, true);
        System.out.println(b);
        s = new SigDraw(b.getBarChart(0, "text", 1000, 1000, 1000), true);
        s.save("BasicUI_test.jpg");
    }
}