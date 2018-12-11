package h2g;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

class FrameCreator {
    BarDrawingTutor b;
    CanvaStyle c;
    HistogramData d;
    SigDraw bg, coord;
    CoordProjecter cp;

    double[] xScale = new double[2]; // MIN, MAX
    double[] yValue; // MIN, MAX
    int[] bgSize;
    int[] coordSize;
    int[] cBorder = new int[4];
    double barWidth, barSpace;
    int barNum;
    double rulerStep;
    int rulerGrade;

    public FrameCreator(BarDrawingTutor b, CanvaStyle c, HistogramData d) {
        this.b = b;
        this.c = c;
        this.d = d;
        init();
        draw();
    }

    private void init() {
        yValue = d.yValue;
        bgSize = c.bgSize;
        coordSize = c.coordSize;
        barSpace = c.blankRatio;
        barWidth = 1.0 - barSpace;
        barNum = d.keys.length;
        rulerStep = d.rulerStep;
        rulerGrade = d.rulerGrade;
        xScale[MIN] = -b.getPatternGap();
        xScale[MAX] = d.visiblePattern;

        cBorder[LEFT] = c.xProject - coordSize[WIDTH] / 2;
        cBorder[RIGHT] = c.xProject + coordSize[WIDTH] / 2;
        cBorder[UP] = c.yProject + coordSize[HEIGHT] / 2;
        cBorder[DOWN] = c.yProject - coordSize[HEIGHT] / 2;

        bg = new SigDraw(bgSize[WIDTH], bgSize[HEIGHT], true);
        coord = new SigDraw(coordSize[WIDTH], coordSize[HEIGHT], false);
        coord.setXscale(xScale[MIN], xScale[MAX]);
        coord.setYscale(yValue[MIN], yValue[MAX]);
        bg.clear(c.bgColor);
        cp = new CoordProjecter(bg, coord, c.xProject, c.yProject);
    }

    public void draw() {
        plotBars();
        plotRuler();
        plotKeys();
        if (c.hasBorder)
            plotBorder();
        if (c.hasHeader)
            plotHeader();
        if (c.hasFooter)
            plotFooter();
    }
    private void plotKeys() {
        
        Font font = c.keysFont; // TO BE Customized
        bg.setFont(font);
        bg.setPenColor(c.keyColor);
        final double y = cBorder[DOWN] - c.keysYoffset;
        b.seek();
        while(b.hasNext()) {
            b.next();
            bg.text(cp.getX(b.getLocation()), y, d.keys[b.getBarID()]);
        }
    }
    private void plotBars() {
        b.seek();
        while(b.hasNext()) {
            b.next();
            String skin = c.barSkin[ b.getBarID() ];
            int[] barSize = new int[]{(int)(coord.factorX(b.getBarWidth())),coord.height};
            //BarGenerator barSkin = DynamicLoader.getGenerator(skin, barSize, yValue);
            //barSkin.setScale(yValue);
            BarGenerator barSkin = new BarBasicSkin(barSize, yValue);
            double x = b.getLocation();
            double y = (yValue[MIN] + yValue[MAX])/2;
            BufferedImage barImg = barSkin.getBarChart(b.currentFrame, b.getValue(), 0);
            coord.picture(x, y, barImg);
        }
        bg.picture(c.xProject, c.yProject, coord.getBuffImg());
        /*
        double[] a = d.values;
        int n = a.length;
        setHistogramScale(n);
        if (f.isBarFilled) {
            StdDraw.setPenColor(f.barFillColor);
            for (int i = 0; i < n; i++) {
                StdDraw.filledRectangle(i, a[i] / 2, 0.25, a[i] / 2);
                // (x, y, halfWidth, halfHeight)
            }
        }
        if (f.hasBarFrame) {
            StdDraw.setPenColor(f.barFrameColor);
            for (int i = 0; i < n; i++) {
                StdDraw.rectangle(i, a[i] / 2, 0.25, a[i] / 2);
                // (x, y, halfWidth, halfHeight)
            }
        }*/
    }

    private void plotRuler() {
        Font font = c.rulerFont; // TO BE Customized
        bg.setFont(font);
        bg.setPenColor(c.rulerColor);
        final int x0 = cBorder[LEFT] - 5, x1 = cBorder[LEFT] + 5;
        final int x2 = cBorder[RIGHT] - 5, x3 = cBorder[RIGHT] + 5;
        double[] rawY = new double[rulerGrade + 1];
        String[] markL = new String[rulerGrade + 1];
        String[] markR = new String[rulerGrade + 1];

        for (int i = 0; i <= rulerGrade; i++) {
            rawY[i] = yValue[MIN] + i * rulerStep;
            double y = cp.getY(rawY[i]);
            if (c.hasRuler) bg.line(x0, y, x1, y);
            if (c.hasRightRuler) bg.line(x2, y, x3, y);
        }
        int len = 0;
        if (c.hasRuler) len = formatNumber(markL, rawY);
        if (c.hasRightRuler) len = formatNumber(markR, rawY);
        final int xl = cBorder[LEFT] - c.rulerXoffset;
        final int xr = cBorder[RIGHT] + c.rulerXoffset;
        for (int i = 0; i <= rulerGrade; i++) {
            double y = cp.getY(yValue[MIN] + i * rulerStep);
            if (c.hasRuler) bg.text(xl, y, String.format("%" + len + "s", markL[i]));
            if (c.hasRightRuler) bg.text(xr, y, String.format("%-" + len + "s", markR[i]));
        }
    }

    private int formatNumber(String[] mark, double[] y) { // TO BE Customized
        int decimalDigits = 0;
        String format = "%";
        if (rulerStep < 1) decimalDigits = (int)(Math.ceil(-Math.log10(rulerStep)));
        else decimalDigits = 0;
        if (rulerStep > 100000) format += ".5g";
        else format += "." + decimalDigits + "f";
        int n = 0;
        for (int x = 0; x < y.length; ++x) {
            mark[x] = String.format(format, y[x]);
            if (n < mark[x].length())
                n = mark[x].length();
        }
        return n;
    }

    

    private void plotBorder() {
        bg.setPenColor(c.borderColor);
        bg.rectangle(c.xProject, c.yProject, coordSize[WIDTH] / 2, coordSize[HEIGHT] / 2);
    }


    private void plotHeader() {
        Font font = c.headerFont; // TO BE Customized
        bg.setFont(font);
        double x = c.headerXoffset + (cBorder[LEFT] + cBorder[RIGHT]) / 2;
        double y = c.headerYoffset + cBorder[UP];
        bg.setPenColor(c.headerColor);
        bg.text(x, y, d.header);
    }

    private void plotFooter() {
        Font font = c.footerFont; // TO BE Customized
        bg.setFont(font);
        double x = c.footerXoffset + (cBorder[LEFT] + cBorder[RIGHT]) / 2;
        double y = -c.footerYoffset + cBorder[DOWN];
        bg.setPenColor(c.footerColor);
        bg.text(x, y, d.footer);
    }

    private final static int UP = 0;
    private final static int DOWN = 1;
    private final static int LEFT = 2;
    private final static int RIGHT = 3;
    private final static int MIN = 0;
    private final static int MAX = 1;
    private final static int WIDTH = 0;
    private final static int HEIGHT = 1;
}