package h2g;

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

        bg.enableTransparent((float)c.bgTransparency);
        coord.enableTransparent((float)c.coordTrantransparency);

        if(c.rotated) {
            coord.setYscale(xScale[MIN], xScale[MAX]);
            coord.setXscale(yValue[MIN], yValue[MAX]);
        } else {
            coord.setXscale(xScale[MIN], xScale[MAX]);
            coord.setYscale(yValue[MIN], yValue[MAX]);
        }
        
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
        if (c.legendImg != null) {
            plotLegend();
        }
    }
    private void plotLegend() {
        bg.picture(c.LegendX, c.LegendY, c.legendImg);
    }
    private void plotKeys() {
        
        Font font = c.keysFont; // TO BE Customized
        bg.setFont(font);
        bg.setPenColor(c.keysColor);
        
        
        b.seek();
        while(b.hasNext()) {
            b.next();
            double x,y;
            if(b.getLocation()>d.visiblePattern) continue;
            if(c.rotated) {
                x = cBorder[LEFT] - c.keysYoffset;
                y = cp.getY(b.getLocation());
                
            }
            else {
                y = cBorder[DOWN] - c.keysYoffset;
                x = cp.getX(b.getLocation());
            }
            bg.setPenColor(b.getTextColor());
            bg.text(x, y, d.keys[b.getBarID()]);
        }
    }
    private void plotBars() {
        b.seek();
        while(b.hasNext()) {
            b.next();
            int[] barSize;
            double x,y;
            if(c.rotated) {
                barSize = new int[]{coord.width, (int)(coord.factorY(b.getBarWidth()))};
                y = b.getLocation();
                x = (yValue[MIN] + yValue[MAX])/2;
            } else {
                barSize = new int[]{(int)(coord.factorX(b.getBarWidth())),coord.height};
                x = b.getLocation();
                y = (yValue[MIN] + yValue[MAX])/2;
            }
            BufferedImage barImg = null;
            String[] barText = new String[1];
            formatNumber(barText, new double[]{b.getValue()} );

            if(c.isStackedBar && b instanceof StackedBarDrawingTutor) {
                barImg = ((StackedBarDrawingTutor) b).getStackedBarImg(barSize, yValue, barText[0] );
            }
            else barImg = b.getBarImg(barSize, yValue, barText[0]);
            coord.picture(x, y, barImg);
        }
        bg.picture(c.xProject, c.yProject, coord.getBuffImg());
    }

    private void plotRuler() {
        Font font = c.rulerFont; // TO BE Customized
        bg.setFont(font);
        bg.setPenColor(c.rulerColor);
        if(c.rotated) {
            int y0,y1,y2,y3;
            if(c.extendScaleLine) {
                y0 = cBorder[DOWN];
                y1 = cBorder[UP] + 5;
                y2 = cBorder[DOWN] - 5;
                y3 = cBorder[UP];
            } else {
                y0 = cBorder[UP] - 5;
                y1 = cBorder[UP] + 5;
                y2 = cBorder[DOWN] - 5;
                y3 = cBorder[DOWN] + 5;
            }
            
            double[] rawX = new double[rulerGrade + 1];
            String[] markL = new String[rulerGrade + 1];
            String[] markR = new String[rulerGrade + 1];

            for (int i = 0; i <= rulerGrade; i++) {
                rawX[i] = yValue[MIN] + i * rulerStep;
                double x = cp.getX(rawX[i]);
                if (c.hasRuler) bg.line(x, y0, x, y1);
                if (c.hasRightRuler) bg.line(x, y2, x, y3);
            }
            int len = 0;
            if (c.hasRuler) len = formatNumber(markL, rawX);
            if (c.hasRightRuler) len = formatNumber(markR, rawX);
            final int yu = cBorder[UP] + c.rulerXoffset;
            final int yd = cBorder[DOWN] - c.rulerXoffset;
            for (int i = 0; i <= rulerGrade; i++) {
                double x = cp.getX(yValue[MIN] + i * rulerStep);
                if (c.hasRuler) bg.text(x, yu, String.format("%" + len + "s", markL[i]));
                if (c.hasRightRuler) bg.text(x, yd, String.format("%-" + len + "s", markR[i]));
            }
        } else {
            int x0,x1,x2,x3;
            if(c.extendScaleLine) {
                x0 = cBorder[LEFT] - 5;
                x1 = cBorder[RIGHT];
                x2 = cBorder[LEFT];
                x3 = cBorder[RIGHT] + 5;
            } else {
                x0 = cBorder[LEFT] - 5;
                x1 = cBorder[LEFT] + 5;
                x2 = cBorder[RIGHT] - 5;
                x3 = cBorder[RIGHT] + 5;
            }
            
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