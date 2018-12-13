package h2g;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public final class SigDraw {
    public static final Color BLACK = Color.BLACK;
    public static final Color BLUE = Color.BLUE;
    public static final Color CYAN = Color.CYAN;
    public static final Color DARK_GRAY = Color.DARK_GRAY;
    public static final Color GRAY = Color.GRAY;
    public static final Color GREEN = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA = Color.MAGENTA;
    public static final Color ORANGE = Color.ORANGE;
    public static final Color PINK = Color.PINK;
    public static final Color RED = Color.RED;
    public static final Color WHITE = Color.WHITE;
    public static final Color YELLOW = Color.YELLOW;
    //public static final Color BOOK_BLUE = new Color(9, 90, 166);
    //public static final Color BOOK_LIGHT_BLUE = new Color(103, 198, 243);
    //public static final Color BOOK_RED = new Color(150, 35, 31);
    //public static final Color PRINCETON_ORANGE = new Color(245, 128, 37);
    public static final Color SUSTech_ORANGE = new Color(237, 108, 0);
    public static final Color SUSTech_DARK = new Color(0, 63, 67);
    public static final Color SUSTech_LIGHT = new Color(43, 183, 179);

    // default colors
    public static final Color DEFAULT_PEN_COLOR = BLACK;
    public static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // current pen color
    public Color penColor;

    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    public static final int DEFAULT_SIZE = 512;
    public int width = DEFAULT_SIZE;
    public int height = DEFAULT_SIZE;

    // default pen radius
    public static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    public double penRadius;

    public boolean unaltered = false;

    // boundary of drawing canvas, 0% border
    // private static final double BORDER = 0.05;
    public static final double BORDER = 0.00;
    public static final double DEFAULT_XMIN = 0.0;
    public static final double DEFAULT_XMAX = 1.0;
    public static final double DEFAULT_YMIN = 0.0;
    public static final double DEFAULT_YMAX = 1.0;
    public double xmin, ymin, xmax, ymax;

    // default font
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // current font
    public Font font;

    // double buffered graphics
    public BufferedImage offscreenImage;
    public Graphics2D offscreen;

    //Modify by Tonny (2018.11)
    public BufferedImage getBuffImg() {
        offscreen.dispose();
        return offscreenImage;
    }

    public void setBuffImg(BufferedImage image) {
        if (image == null)
            throw new IllegalArgumentException("image must not be null pointer");
        try {
            offscreen.dispose();
        } catch (Exception ignored) {
        }
        offscreenImage = image;
        offscreen = offscreenImage.createGraphics();
        setCanvasSize(image.getWidth(), image.getHeight());
    }

    public void enableTransparent(float transparent) {
        offscreen.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, transparent)); // Get image transparent
        offscreen.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT); // Get background transparent
    }

    public static BufferedImage cloneImage(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = image.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public BufferedImage getScaledImage(double factor) {
        return getScaledImage(factor, Scalr.Method.AUTOMATIC);
    }

    public BufferedImage getSubImage(double x, double y, double halfWidth, double halfHeight) {
        if (!(halfWidth >= 0)) throw new IllegalArgumentException("half width must be positive");
        if (!(halfHeight >= 0)) throw new IllegalArgumentException("half height must be positive");
        int xs = (int) Math.round(scaleX(x));
        int ys = (int) Math.round(scaleY(y));
        int ws = (int) Math.round(factorX(2 * halfWidth));
        int hs = (int) Math.round(factorY(2 * halfHeight));
        return offscreenImage.getSubimage(xs, ys, ws, hs);
    }

    public BufferedImage getScaledImage(double factor, Scalr.Method quality) {
        return Scalr.resize(
                this.getBuffImg(),
                //Scalr.Method.QUALITY,
                //Scalr.Method.ULTRA_QUALITY,
                //Scalr.Method.AUTOMATIC,
                quality,
                Scalr.Mode.FIT_EXACT,
                (int) (width * factor),
                (int) (height * factor),
                (BufferedImageOp) (null));
    }

    // singleton for callbacks: avoids generation of extra .class files
    //private static SigDraw std = new SigDraw();

    // the frame for drawing to the screen


    public void setCanvasSize(int canvasWidth, int canvasHeight) {
        if (canvasWidth <= 0 || canvasHeight <= 0)
            throw new IllegalArgumentException("width and height must be positive");
        width = canvasWidth;
        height = canvasHeight;
        setScaleUnaltered();
    }

    // init
    public SigDraw() {
        init(DEFAULT_SIZE, DEFAULT_SIZE, null);
    }

    @Deprecated
    public SigDraw(int canvasWidth, int canvasHeight) {
        init(canvasWidth, canvasHeight, null);
    }

    public SigDraw(int canvasWidth, int canvasHeight, boolean unaltered) {
        init(canvasWidth, canvasHeight, null);
        setScaleUnaltered(unaltered);
    }

    public SigDraw(BufferedImage image) {
        init(DEFAULT_SIZE, DEFAULT_SIZE, image);
    }

    public SigDraw(BufferedImage image, boolean unaltered) {
        init(DEFAULT_SIZE, DEFAULT_SIZE, image);
        setScaleUnaltered(unaltered);
    }

    private void init(int canvasWidth, int canvasHeight, BufferedImage image) {
        if (image == null) {
            setCanvasSize(canvasWidth, canvasHeight);
            offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            offscreen = offscreenImage.createGraphics();
        } else {
            setBuffImg(image);
        }
        enableAntialiasing();
        setXscale();
        setYscale();
        setPenColor();
        setPenRadius();
        setFont();
        if (image == null) clear();
    }

    public void enableAntialiasing() {
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        offscreen.addRenderingHints(hints);
    }

    public void disableAntialiasing() {
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        offscreen.addRenderingHints(hints);
    }


    public void setXscale() {
        setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
    }


    public void setYscale() {
        setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
    }


    public void setScale() {
        setXscale();
        setYscale();
    }


    public void setXscale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
    }


    public void setYscale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
    }


    public void setScale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
    }

    public void setScaleUnaltered() {
        if (unaltered) {
            setXscale(0, width);
            setYscale(0, height);
        }
    }

    public void setScaleUnaltered(boolean status) {
        unaltered = status;
        setScaleUnaltered();
    }


    // helper functions that scale from user coordinates to screen coordinates and back
    public double scaleX(double x) {
        return width * (x - xmin) / (xmax - xmin);
    }

    public double scaleY(double y) {
        return height * (ymax - y) / (ymax - ymin);
    }

    public double factorX(double w) {
        return w * width / Math.abs(xmax - xmin);
    }

    public double factorY(double h) {
        return h * height / Math.abs(ymax - ymin);
    }

    public double userX(double x) {
        return xmin + x * (xmax - xmin) / width;
    }

    public double userY(double y) {
        return ymax - y * (ymax - ymin) / height;
    }


    public void clear() {
        clear(DEFAULT_CLEAR_COLOR);
    }


    public void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
    }


    public double getPenRadius() {
        return penRadius;
    }


    public void setPenRadius() {
        setPenRadius(DEFAULT_PEN_RADIUS);
    }


    public void setPenRadius(double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = radius;
        float scaledPenRadius = (float) (radius * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }


    public Color getPenColor() {
        return penColor;
    }


    public void setPenColor() {
        setPenColor(DEFAULT_PEN_COLOR);
    }


    public void setPenColor(Color color) {
        if (color == null) throw new IllegalArgumentException();
        penColor = color;
        offscreen.setColor(penColor);
    }


    public void setPenColor(int red, int green, int blue) {
        if (red < 0 || red >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue < 0 || blue >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        setPenColor(new Color(red, green, blue));
    }


    public Font getFont() {
        return font;
    }


    public void setFont() {
        setFont(DEFAULT_FONT);
    }


    public void setFont(Font font) {
        if (font == null) throw new IllegalArgumentException();
        this.font = font;
    }


    public void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
    }


    public void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }


    public void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);

        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (scaledPenRadius <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - scaledPenRadius / 2, ys - scaledPenRadius / 2,
                scaledPenRadius, scaledPenRadius));
    }


    public void circle(double x, double y, double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void filledCircle(double x, double y, double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (!(semiMajorAxis >= 0)) throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        if (!(semiMinorAxis >= 0)) throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * semiMajorAxis);
        double hs = factorY(2 * semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (!(semiMajorAxis >= 0)) throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        if (!(semiMinorAxis >= 0)) throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * semiMajorAxis);
        double hs = factorY(2 * semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void arc(double x, double y, double radius, double angle1, double angle2) {
        if (radius < 0) throw new IllegalArgumentException("arc radius must be nonnegative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Arc2D.Double(xs - ws / 2, ys - hs / 2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
    }


    public void square(double x, double y, double halfLength) {
        if (!(halfLength >= 0)) throw new IllegalArgumentException("half length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfLength);
        double hs = factorY(2 * halfLength);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void filledSquare(double x, double y, double halfLength) {
        if (!(halfLength >= 0)) throw new IllegalArgumentException("half length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfLength);
        double hs = factorY(2 * halfLength);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void rectangle(double x, double y, double halfWidth, double halfHeight) {
        if (!(halfWidth >= 0)) throw new IllegalArgumentException("half width must be nonnegative");
        if (!(halfHeight >= 0)) throw new IllegalArgumentException("half height must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfWidth);
        double hs = factorY(2 * halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        if (!(halfWidth >= 0)) throw new IllegalArgumentException("half width must be nonnegative");
        if (!(halfHeight >= 0)) throw new IllegalArgumentException("half height must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfWidth);
        double hs = factorY(2 * halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    public void polygon(double[] x, double[] y) {
        if (x == null) throw new IllegalArgumentException("x-coordinate array is null");
        if (y == null) throw new IllegalArgumentException("y-coordinate array is null");
        int n1 = x.length;
        int n2 = y.length;
        if (n1 != n2) throw new IllegalArgumentException("arrays must be of the same length");
        int n = n1;
        if (n == 0) return;

        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < n; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.draw(path);
    }


    public void filledPolygon(double[] x, double[] y) {
        if (x == null) throw new IllegalArgumentException("x-coordinate array is null");
        if (y == null) throw new IllegalArgumentException("y-coordinate array is null");
        int n1 = x.length;
        int n2 = y.length;
        if (n1 != n2) throw new IllegalArgumentException("arrays must be of the same length");
        int n = n1;
        if (n == 0) return;

        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < n; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.fill(path);
    }


    public static BufferedImage loadImage(String filename) {
        if (filename == null) throw new IllegalArgumentException();

        // from a file or URL
        try {
            URL url = new URL(filename);
            BufferedImage image = ImageIO.read(url);
            return image;
        } catch (IOException e) {
            // ignore
        }
        throw new IllegalArgumentException("image " + filename + " not found");
    }

    public void picture(double x, double y, BufferedImage image) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth();    // can call only if image is a BufferedImage
        int hs = image.getHeight();
        //int ws = image.getWidth(null);
        //int hs = image.getHeight(null);
        //if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0), (int) Math.round(ys - hs / 2.0), null);
    }


    public void picture(double x, double y, BufferedImage image, double degrees) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = image.getWidth();    // can call only if image is a BufferedImage
        int hs = image.getHeight();
        //int ws = image.getWidth(null);
        //int hs = image.getHeight(null);
        //if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");

        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0), (int) Math.round(ys - hs / 2.0), null);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);

    }


    public void picture(double x, double y, BufferedImage image, double scaledWidth, double scaledHeight) {
        //Image image = getImage(filename);
        picture(x, y, image, scaledWidth, scaledHeight, 0);

    }


    public void picture(double x, double y, BufferedImage image, double scaledWidth, double scaledHeight, double degrees) {
        if (scaledWidth < 0) throw new IllegalArgumentException("width is negative: " + scaledWidth);
        if (scaledHeight < 0) throw new IllegalArgumentException("height is negative: " + scaledHeight);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(scaledWidth);
        double hs = factorY(scaledHeight);
        //if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);

        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
                (int) Math.round(ys - hs / 2.0),
                (int) Math.round(ws),
                (int) Math.round(hs), null);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);

    }


    public void text(double x, double y, String text) {
        if (text == null) throw new IllegalArgumentException();
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(text);
        int hs = metrics.getDescent();
        offscreen.drawString(text, (float) (xs - ws / 2.0), (float) (ys + hs));
    }


    public void text(double x, double y, String text, double degrees) {
        if (text == null) throw new IllegalArgumentException();
        double xs = scaleX(x);
        double ys = scaleY(y);
        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, text);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);
    }


    public void textLeft(double x, double y, String text) {
        if (text == null) throw new IllegalArgumentException();
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(text, (float) xs, (float) (ys + hs));
    }


    public void textRight(double x, double y, String text) {
        if (text == null) throw new IllegalArgumentException();
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(text);
        int hs = metrics.getDescent();
        offscreen.drawString(text, (float) (xs - ws), (float) (ys + hs));
    }


    public void pause(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            System.out.println("Error sleeping");
        }
    }


    public void save(String filename) {
        if (filename == null) throw new IllegalArgumentException();
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        // png files
        if ("png".equalsIgnoreCase(suffix)) {
            try {
                ImageIO.write(offscreenImage, suffix, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // need to change from ARGB to RGB for JPEG
        // reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
        else if ("jpg".equalsIgnoreCase(suffix)) {
            WritableRaster raster = offscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[]{0, 1, 2});
            DirectColorModel cm = (DirectColorModel) offscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
                    cm.getRedMask(),
                    cm.getGreenMask(),
                    cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false, null);
            try {
                ImageIO.write(rgbBuffer, suffix, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }

    public static void main(String[] args) {
        SigDraw im1 = new SigDraw(1000,1000,true);
        im1.enableTransparent(0.2f);
        Font a = new Font("Microsoft YaHei Light", Font.PLAIN, 100);
        im1.setFont(a);
        im1.setPenColor(SigDraw.BLACK);
        im1.text(500, 500, "Hello");
        new SigDraw(im1.getScaledImage(0.5, Scalr.Method.ULTRA_QUALITY)).save("AA_HR_UQ.jpg");
        new SigDraw(im1.getScaledImage(0.5, Scalr.Method.QUALITY)).save("AA_HR_Q.jpg");
        new SigDraw(im1.getScaledImage(0.5, Scalr.Method.AUTOMATIC)).save("AA_HR_AUTO.jpg");
        new SigDraw(im1.getScaledImage(0.5, Scalr.Method.SPEED)).save("AA_HR_SPD.jpg");
        new SigDraw(im1.getScaledImage(0.5, Scalr.Method.BALANCED)).save("AA_HR_BL.jpg");
    }
}
