package h2g;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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

    /**
     * The color black.
     */
    public static final Color BLACK = Color.BLACK;

    /**
     * The color blue.
     */
    public static final Color BLUE = Color.BLUE;

    /**
     * The color cyan.
     */
    public static final Color CYAN = Color.CYAN;

    /**
     * The color dark gray.
     */
    public static final Color DARK_GRAY = Color.DARK_GRAY;

    /**
     * The color gray.
     */
    public static final Color GRAY = Color.GRAY;

    /**
     * The color green.
     */
    public static final Color GREEN = Color.GREEN;

    /**
     * The color light gray.
     */
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;

    /**
     * The color magenta.
     */
    public static final Color MAGENTA = Color.MAGENTA;

    /**
     * The color orange.
     */
    public static final Color ORANGE = Color.ORANGE;

    /**
     * The color pink.
     */
    public static final Color PINK = Color.PINK;

    /**
     * The color red.
     */
    public static final Color RED = Color.RED;

    /**
     * The color white.
     */
    public static final Color WHITE = Color.WHITE;

    /**
     * The color yellow.
     */
    public static final Color YELLOW = Color.YELLOW;

    /**
     * Shade of blue used in <em>Introduction to Programming in Java</em>.
     * It is Pantone 300U. The RGB values are approximately (9, 90, 166).
     */
    public static final Color BOOK_BLUE = new Color(9, 90, 166);

    /**
     * Shade of light blue used in <em>Introduction to Programming in Java</em>.
     * The RGB values are approximately (103, 198, 243).
     */
    public static final Color BOOK_LIGHT_BLUE = new Color(103, 198, 243);

    /**
     * Shade of red used in <em>Algorithms, 4th edition</em>.
     * It is Pantone 1805U. The RGB values are approximately (150, 35, 31).
     */
    public static final Color BOOK_RED = new Color(150, 35, 31);

    /**
     * Shade of orange used in Princeton University's identity.
     * It is PMS 158. The RGB values are approximately (245, 128, 37).
     */
    public static final Color PRINCETON_ORANGE = new Color(245, 128, 37);

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
        } catch (Exception e) {
        }
        offscreenImage = image;
        offscreen = offscreenImage.createGraphics();
        setCanvasSize(image.getWidth(), image.getHeight());
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
        if (!(halfWidth >= 0)) throw new IllegalArgumentException("half width must be nonnegative");
        if (!(halfHeight >= 0)) throw new IllegalArgumentException("half height must be nonnegative");
        int xs = (int) Math.round( scaleX(x) );
        int ys = (int) Math.round( scaleY(y) );
        int ws = (int) Math.round( factorX(2 * halfWidth) );
        int hs = (int) Math.round( factorY(2 * halfHeight) );
        return offscreenImage.getSubimage(xs, ys, ws, hs);
    }
    /**
     * @param factor  The coefficient of image AREA which we would like to show.
     *                Attention, the factor is double while we show only INT scalar.
     * @param quality The image quality, which is formed by "Method", and can be
     *                AUTOMATIC, SPEED, BALANCED, QUALITY, ULTRA_QUALITY.
     * @return The image rendered.
     */
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

    /**
     * Sets the canvas (drawing area) to be 512-by-512 pixels.
     * This also erases the current drawing and resets the coordinate system,
     * pen radius, pen color, and font back to their default values.
     * Ordinarly, this method is called once, at the very beginning
     * of a program.
     */

    /**
     * Sets the canvas (drawing area) to be <em>width</em>-by-<em>height</em> pixels.
     * This also erases the current drawing and resets the coordinate system,
     * pen radius, pen color, and font back to their default values.
     * Ordinarly, this method is called once, at the very beginning
     * of a program.
     *
     * @param canvasWidth  the width as a number of pixels
     * @param canvasHeight the height as a number of pixels
     * @throws IllegalArgumentException unless both {@code canvasWidth} and
     *                                  {@code canvasHeight} are positive
     */
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
    /***************************************************************************
     *  User and screen coordinate systems.
     ***************************************************************************/

    /**
     * Sets the <em>x</em>-scale to be the default (between 0.0 and 1.0).
     */
    public void setXscale() {
        setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
    }

    /**
     * Sets the <em>y</em>-scale to be the default (between 0.0 and 1.0).
     */
    public void setYscale() {
        setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
    }

    /**
     * Sets the <em>x</em>-scale and <em>y</em>-scale to be the default
     * (between 0.0 and 1.0).
     */
    public void setScale() {
        setXscale();
        setYscale();
    }

    /**
     * Sets the <em>x</em>-scale to the specified range.
     *
     * @param min the minimum value of the <em>x</em>-scale
     * @param max the maximum value of the <em>x</em>-scale
     * @throws IllegalArgumentException if {@code (max == min)}
     */
    public void setXscale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
    }

    /**
     * Sets the <em>y</em>-scale to the specified range.
     *
     * @param min the minimum value of the <em>y</em>-scale
     * @param max the maximum value of the <em>y</em>-scale
     * @throws IllegalArgumentException if {@code (max == min)}
     */
    public void setYscale(double min, double max) {
        double size = max - min;
        if (size == 0.0) throw new IllegalArgumentException("the min and max are the same");
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
    }

    /**
     * Sets both the <em>x</em>-scale and <em>y</em>-scale to the (same) specified range.
     *
     * @param min the minimum value of the <em>x</em>- and <em>y</em>-scales
     * @param max the maximum value of the <em>x</em>- and <em>y</em>-scales
     * @throws IllegalArgumentException if {@code (max == min)}
     */
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


    /**
     * Clears the screen to the default color (white).
     */
    public void clear() {
        clear(DEFAULT_CLEAR_COLOR);
    }

    /**
     * Clears the screen to the specified color.
     *
     * @param color the color to make the background
     */
    public void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
    }

    /**
     * Returns the current pen radius.
     *
     * @return the current value of the pen radius
     */
    public double getPenRadius() {
        return penRadius;
    }

    /**
     * Sets the pen size to the default size (0.002).
     * The pen is circular, so that lines have rounded ends, and when you set the
     * pen radius and draw a point, you get a circle of the specified radius.
     * The pen radius is not affected by coordinate scaling.
     */
    public void setPenRadius() {
        setPenRadius(DEFAULT_PEN_RADIUS);
    }

    /**
     * Sets the radius of the pen to the specified size.
     * The pen is circular, so that lines have rounded ends, and when you set the
     * pen radius and draw a point, you get a circle of the specified radius.
     * The pen radius is not affected by coordinate scaling.
     *
     * @param radius the radius of the pen
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public void setPenRadius(double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = radius;
        float scaledPenRadius = (float) (radius * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Returns the current pen color.
     *
     * @return the current pen color
     */
    public Color getPenColor() {
        return penColor;
    }

    /**
     * Set the pen color to the default color (black).
     */
    public void setPenColor() {
        setPenColor(DEFAULT_PEN_COLOR);
    }

    /**
     * Sets the pen color to the specified color.
     * <p>
     * The predefined pen colors are
     * {@code StdDraw.BLACK}, {@code StdDraw.BLUE}, {@code StdDraw.CYAN},
     * {@code StdDraw.DARK_GRAY}, {@code StdDraw.GRAY}, {@code StdDraw.GREEN},
     * {@code StdDraw.LIGHT_GRAY}, {@code StdDraw.MAGENTA}, {@code StdDraw.ORANGE},
     * {@code StdDraw.PINK}, {@code StdDraw.RED}, {@code StdDraw.WHITE}, and
     * {@code StdDraw.YELLOW}.
     *
     * @param color the color to make the pen
     */
    public void setPenColor(Color color) {
        if (color == null) throw new IllegalArgumentException();
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Sets the pen color to the specified RGB color.
     *
     * @param red   the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue  the amount of blue (between 0 and 255)
     * @throws IllegalArgumentException if {@code red}, {@code green},
     *                                  or {@code blue} is outside its prescribed range
     */
    public void setPenColor(int red, int green, int blue) {
        if (red < 0 || red >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue < 0 || blue >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        setPenColor(new Color(red, green, blue));
    }

    /**
     * Returns the current font.
     *
     * @return the current font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font to the default font (sans serif, 16 point).
     */
    public void setFont() {
        setFont(DEFAULT_FONT);
    }

    /**
     * Sets the font to the specified value.
     *
     * @param font the font
     */
    public void setFont(Font font) {
        if (font == null) throw new IllegalArgumentException();
        this.font = font;
    }


    /***************************************************************************
     *  Drawing geometric shapes.
     ***************************************************************************/

    /**
     * Draws a line segment between (<em>x</em><sub>0</sub>, <em>y</em><sub>0</sub>) and
     * (<em>x</em><sub>1</sub>, <em>y</em><sub>1</sub>).
     *
     * @param x0 the <em>x</em>-coordinate of one endpoint
     * @param y0 the <em>y</em>-coordinate of one endpoint
     * @param x1 the <em>x</em>-coordinate of the other endpoint
     * @param y1 the <em>y</em>-coordinate of the other endpoint
     */
    public void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
    }

    /**
     * Draws one pixel at (<em>x</em>, <em>y</em>).
     * This method is private because pixels depend on the display.
     * To achieve the same effect, set the pen radius to 0 and call {@code point()}.
     *
     * @param x the <em>x</em>-coordinate of the pixel
     * @param y the <em>y</em>-coordinate of the pixel
     */
    public void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Draws a point centered at (<em>x</em>, <em>y</em>).
     * The point is a filled circle whose radius is equal to the pen radius.
     * To draw a single-pixel point, first set the pen radius to 0.
     *
     * @param x the <em>x</em>-coordinate of the point
     * @param y the <em>y</em>-coordinate of the point
     */
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

    /**
     * Draws a circle of the specified radius, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x      the <em>x</em>-coordinate of the center of the circle
     * @param y      the <em>y</em>-coordinate of the center of the circle
     * @param radius the radius of the circle
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public void circle(double x, double y, double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }

    /**
     * Draws a filled circle of the specified radius, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x      the <em>x</em>-coordinate of the center of the circle
     * @param y      the <em>y</em>-coordinate of the center of the circle
     * @param radius the radius of the circle
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public void filledCircle(double x, double y, double radius) {
        if (!(radius >= 0)) throw new IllegalArgumentException("radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * radius);
        double hs = factorY(2 * radius);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    /**
     * Draws an ellipse with the specified semimajor and semiminor axes,
     * centered at (<em>x</em>, <em>y</em>).
     *
     * @param x             the <em>x</em>-coordinate of the center of the ellipse
     * @param y             the <em>y</em>-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either {@code semiMajorAxis}
     *                                  or {@code semiMinorAxis} is negative
     */
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

    /**
     * Draws an ellipse with the specified semimajor and semiminor axes,
     * centered at (<em>x</em>, <em>y</em>).
     *
     * @param x             the <em>x</em>-coordinate of the center of the ellipse
     * @param y             the <em>y</em>-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either {@code semiMajorAxis}
     *                                  or {@code semiMinorAxis} is negative
     */
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


    /**
     * Draws a circular arc of the specified radius,
     * centered at (<em>x</em>, <em>y</em>), from angle1 to angle2 (in degrees).
     *
     * @param x      the <em>x</em>-coordinate of the center of the circle
     * @param y      the <em>y</em>-coordinate of the center of the circle
     * @param radius the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *               you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if {@code radius} is negative
     */
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

    /**
     * Draws a square of side length 2r, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the square
     * @param y          the <em>y</em>-coordinate of the center of the square
     * @param halfLength one half the length of any side of the square
     * @throws IllegalArgumentException if {@code halfLength} is negative
     */
    public void square(double x, double y, double halfLength) {
        if (!(halfLength >= 0)) throw new IllegalArgumentException("half length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfLength);
        double hs = factorY(2 * halfLength);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }

    /**
     * Draws a filled square of the specified size, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the square
     * @param y          the <em>y</em>-coordinate of the center of the square
     * @param halfLength one half the length of any side of the square
     * @throws IllegalArgumentException if {@code halfLength} is negative
     */
    public void filledSquare(double x, double y, double halfLength) {
        if (!(halfLength >= 0)) throw new IllegalArgumentException("half length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfLength);
        double hs = factorY(2 * halfLength);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
    }


    /**
     * Draws a rectangle of the specified size, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the rectangle
     * @param y          the <em>y</em>-coordinate of the center of the rectangle
     * @param halfWidth  one half the width of the rectangle
     * @param halfHeight one half the height of the rectangle
     * @throws IllegalArgumentException if either {@code halfWidth} or {@code halfHeight} is negative
     */
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

    /**
     * Draws a filled rectangle of the specified size, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x          the <em>x</em>-coordinate of the center of the rectangle
     * @param y          the <em>y</em>-coordinate of the center of the rectangle
     * @param halfWidth  one half the width of the rectangle
     * @param halfHeight one half the height of the rectangle
     * @throws IllegalArgumentException if either {@code halfWidth} or {@code halfHeight} is negative
     */
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


    /**
     * Draws a polygon with the vertices
     * (<em>x</em><sub>0</sub>, <em>y</em><sub>0</sub>),
     * (<em>x</em><sub>1</sub>, <em>y</em><sub>1</sub>), ...,
     * (<em>x</em><sub><em>n</em>�C1</sub>, <em>y</em><sub><em>n</em>�C1</sub>).
     *
     * @param x an array of all the <em>x</em>-coordinates of the polygon
     * @param y an array of all the <em>y</em>-coordinates of the polygon
     * @throws IllegalArgumentException unless {@code x[]} and {@code y[]}
     *                                  are of the same length
     */
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

    /**
     * Draws a polygon with the vertices
     * (<em>x</em><sub>0</sub>, <em>y</em><sub>0</sub>),
     * (<em>x</em><sub>1</sub>, <em>y</em><sub>1</sub>), ...,
     * (<em>x</em><sub><em>n</em>�C1</sub>, <em>y</em><sub><em>n</em>�C1</sub>).
     *
     * @param x an array of all the <em>x</em>-coordinates of the polygon
     * @param y an array of all the <em>y</em>-coordinates of the polygon
     * @throws IllegalArgumentException unless {@code x[]} and {@code y[]}
     *                                  are of the same length
     */
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

    /***************************************************************************
     * [Summer 2016] Should we update to use ImageIO instead of ImageIcon()?
     *               Seems to have some issues loading images on some systems
     *               and slows things down on other systems.
     *               especially if you don't call ImageIO.setUseCache(false)
     *               One advantage is that it returns a BufferedImage.
     ***************************************************************************/
    @Deprecated
    private static BufferedImage getImage(String filename) {
        if (filename == null) throw new IllegalArgumentException();

        // from a file or URL
        try {
            URL url = new URL(filename);
            BufferedImage image = ImageIO.read(url);
            return image;
        } catch (IOException e) {
            // ignore
        }

        // in case file is inside a .jar (classpath relative to StdDraw)
        try {
            URL url = StdDraw.class.getResource(filename);
            BufferedImage image = ImageIO.read(url);
            return image;
        } catch (IOException e) {
            // ignore
        }

        // in case file is inside a .jar (classpath relative to root of jar)
        try {
            URL url = StdDraw.class.getResource("/" + filename);
            BufferedImage image = ImageIO.read(url);
            return image;
        } catch (IOException e) {
            // ignore
        }
        throw new IllegalArgumentException("image " + filename + " not found");
    }

    /**
     * Draws the specified image centered at (<em>x</em>, <em>y</em>).
     * The supported image formats are JPEG, PNG, and GIF.
     * As an optimization, the picture is cached, so there is no performance
     * penalty for redrawing the same image multiple times (e.g., in an animation).
     * However, if you change the picture file after drawing it, subsequent
     * calls will draw the original picture.
     *
     * @param filename the name of the image/picture, e.g., "ball.gif"
     * @throws IllegalArgumentException if the image filename is invalid
     */
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

    /**
     * Draws the specified image centered at (<em>x</em>, <em>y</em>),
     * rotated given number of degrees.
     * The supported image formats are JPEG, PNG, and GIF.
     *
     * @param x       the center <em>x</em>-coordinate of the image
     * @param y       the center <em>y</em>-coordinate of the image
     * @param image   is the BufferedImage
     * @param degrees is the number of degrees to rotate counterclockwise
     * @throws IllegalArgumentException if the image filename is invalid
     */
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

    /**
     * Draws the specified image centered at (<em>x</em>, <em>y</em>),
     * rescaled to the specified bounding box.
     * The supported image formats are JPEG, PNG, and GIF.
     *
     * @param x            the center <em>x</em>-coordinate of the image
     * @param y            the center <em>y</em>-coordinate of the image
     * @param image        is the BufferedImage.
     * @param scaledWidth  the width of the scaled image (in screen coordinates)
     * @param scaledHeight the height of the scaled image (in screen coordinates)
     * @throws IllegalArgumentException if either {@code scaledWidth}
     *                                  or {@code scaledHeight} is negative
     * @throws IllegalArgumentException if the image filename is invalid
     */
    public void picture(double x, double y, BufferedImage image, double scaledWidth, double scaledHeight) {
        //Image image = getImage(filename);
        picture(x, y, image, scaledWidth, scaledHeight, 0);
        /*if (scaledWidth  < 0) throw new IllegalArgumentException("width  is negative: " + scaledWidth);
        if (scaledHeight < 0) throw new IllegalArgumentException("height is negative: " + scaledHeight);
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(scaledWidth);
        double hs = factorY(scaledHeight);
        //if (ws < 0 || hs < 0) throw new IllegalArgumentException("image " + filename + " is corrupt");
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else {
            offscreen.drawImage(image, (int) Math.round(xs - ws/2.0),
                                       (int) Math.round(ys - hs/2.0),
                                       (int) Math.round(ws),
                                       (int) Math.round(hs), null);
        }*/
    }


    /**
     * Draws the specified image centered at (<em>x</em>, <em>y</em>), rotated
     * given number of degrees, and rescaled to the specified bounding box.
     * The supported image formats are JPEG, PNG, and GIF.
     *
     * @param x            the center <em>x</em>-coordinate of the image
     * @param y            the center <em>y</em>-coordinate of the image
     * @param image        is the BufferedImage
     * @param scaledWidth  the width of the scaled image (in screen coordinates)
     * @param scaledHeight the height of the scaled image (in screen coordinates)
     * @param degrees      is the number of degrees to rotate counterclockwise
     * @throws IllegalArgumentException if either {@code scaledWidth}
     *                                  or {@code scaledHeight} is negative
     * @throws IllegalArgumentException if the image filename is invalid
     */
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

    /***************************************************************************
     *  Drawing text.
     ***************************************************************************/

    /**
     * Write the given text string in the current font, centered at (<em>x</em>, <em>y</em>).
     *
     * @param x    the center <em>x</em>-coordinate of the text
     * @param y    the center <em>y</em>-coordinate of the text
     * @param text the text to write
     */
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

    /**
     * Write the given text string in the current font, centered at (<em>x</em>, <em>y</em>) and
     * rotated by the specified number of degrees.
     *
     * @param x       the center <em>x</em>-coordinate of the text
     * @param y       the center <em>y</em>-coordinate of the text
     * @param text    the text to write
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public void text(double x, double y, String text, double degrees) {
        if (text == null) throw new IllegalArgumentException();
        double xs = scaleX(x);
        double ys = scaleY(y);
        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, text);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);
    }


    /**
     * Write the given text string in the current font, left-aligned at (<em>x</em>, <em>y</em>).
     *
     * @param x    the <em>x</em>-coordinate of the text
     * @param y    the <em>y</em>-coordinate of the text
     * @param text the text
     */
    public void textLeft(double x, double y, String text) {
        if (text == null) throw new IllegalArgumentException();
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(text, (float) xs, (float) (ys + hs));
    }

    /**
     * Write the given text string in the current font, right-aligned at (<em>x</em>, <em>y</em>).
     *
     * @param x    the <em>x</em>-coordinate of the text
     * @param y    the <em>y</em>-coordinate of the text
     * @param text the text to write
     */
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


    /**
     * Pause for t milliseconds. This method is intended to support computer animations.
     *
     * @param t number of milliseconds
     */
    public void pause(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            System.out.println("Error sleeping");
        }
    }

    /***************************************************************************
     *  Save drawing to a file.
     ***************************************************************************/

    /**
     * Saves the drawing to using the specified filename.
     * The supported image formats are JPEG and PNG;
     * the filename suffix must be {@code .jpg} or {@code .png}.
     *
     * @param filename the name of the file with one of the required suffixes
     */
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

    /**
     * Test client.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        
    }

}
