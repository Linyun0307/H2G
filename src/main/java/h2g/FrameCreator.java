package h2g;

import java.awt.Color;
import java.awt.Font;

class FrameCreator {
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

    public FrameCreator(CanvaStyle c, HistogramData d) {
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
        xScale[MIN] = -barSpace;
        xScale[MAX] = barNum * 1.0;

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

    private void plotBars() {
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
        if (rulerStep < 0) decimalDigits = (int) (-Math.log10(rulerStep)) + 1;
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

    private void plotKeys() {
        Font font = c.keysFont; // TO BE Customized
        bg.setFont(font);
        bg.setPenColor(c.keyColor);
        final double y = cBorder[DOWN] - c.keysYoffset;
        for (int i = 0; i < barNum; i++) {
            if (d.keys[i].length() > 0) {
                bg.text(cp.getX(i + barWidth / 2), y, d.keys[i]);
            }
        }
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

    public static void main(String[] args) throws Exception {
        CanvaStyle c = new CanvaStyle();
        c.loadConfig();
        HistogramData d = new HistogramData();
        d.keys = new String[]{"father", "father", "son"};
        d.values = new double[]{0.0, 0.0, 0.0};
        d.yValue[0] = 0;
        d.yValue[1] = 1000.0;
        d.rulerStep = 100;
        d.rulerGrade = 10;
        d.header = "Nothing left";
        d.footer = "Nothing right";
        FrameCreator fc = new FrameCreator(c, d);
        fc.bg.save("test.jpg");
    }
}