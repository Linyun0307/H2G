package h2g;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

class CanvaStyle {
    // Background
    int[] bgSize = {1000, 1000};
    Color bgColor = Color.WHITE;
    double bgTransparency = 1;

    // Coordinate
    int[] coordSize = {800, 800};
    int xProject = bgSize[0] / 2;
    int yProject = bgSize[1] / 2;
    boolean rotated = true;
    double expandRatio = 0.01;
    double coordTrantransparency = 0.8;

    // Bar
    String[] barPattern = {"Bar1", ""};
    double[] barWidthRatio = {0.5, -1};
    String[] barSkin = {"Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1", "Basic", "Basic1"};
    int FPD = 420;
    int FPS = 60;
    double maxVelocity = 0.1;
    double maxTrantransparency = 1;
    double minTrantransparency = 0.2;
    boolean isStackedBar = false;
    int maxRulerGrade = 10;
    boolean enableDynamicRuler = true;
    String sortMethod = "BubbleSort"; // Availble method: BubbleSort, SelectionSort
    boolean extendScaleLine = true;
    BufferedImage legendImg;

    // Layout
    int rulerXoffset = 20;
    int keysYoffset = 50;
    int headerXoffset = 0;
    int headerYoffset = 50;
    int footerXoffset = 0;
    int footerYoffset = 50;

    // Legend
    int[] iconSize = new int[]{50, 50};
    double[] iconScale = new double[]{0, 50};
    Font legendFont = new Font("Microsoft YaHei Light", Font.PLAIN, 50);
    Color legendColor = Color.BLACK;
    int legendHeight = 60;
    double LegendX = 500, LegendY = 50;
    int legendColumnNum = 6;
    int legendRowNum = 3;
    double legendScaleFactor = 0.22;

    // Fonts
    Font rulerFont = new Font("consolas", Font.PLAIN, 12);
    Font keysFont = new Font("consolas", Font.PLAIN, 12);
    Font headerFont = new Font("calibri", Font.PLAIN, 20);
    Font footerFont = new Font("consolas", Font.BOLD, 16);

    // Switch
/*  boolean isBarFilled = true;
    boolean hasBarFrame = true;*/
    boolean hasBorder = true;
    boolean hasRuler = true;
    boolean hasRightRuler = true;
    boolean hasHeader = true;
    boolean hasFooter = true;


    // Color
/*    Color barFillColor = Color.BLACK;
    Color barFrameColor = Color.BLACK;*/
    Color borderColor = Color.BLACK;
    Color rulerColor = Color.BLACK;
    //Color rulerMarkColor = Color.BLACK;
    Color keysColor = Color.BLACK;
    Color headerColor = Color.BLACK;
    Color footerColor = Color.BLACK;
    ConfigLoader loader;

    /**
     * Load Config from json
     *
     * @param path the json file path
     */
    public void loadConfig(String path) throws Exception {
        loader = new ConfigLoader(path);
        DataLoader dataLoader = new DataLoader();

        // Background,bg
        bgSize = loader.setIntegerArray(bgSize, "bg.size");
        bgColor = loader.setColor(bgColor, "bg.Color");
        bgTransparency = loader.setDouble(bgTransparency, "bg.transparency");

        //Coordinate,coord
        coordSize = loader.setIntegerArray(coordSize, "coord.size");
        rotated = loader.setBool(rotated, "coord.rotated");
        expandRatio = loader.setDouble(expandRatio, "coord.expandRatio");
        coordTrantransparency = loader.setDouble(coordTrantransparency, "coord.transparency");
        borderColor = loader.setColor(borderColor, "coord.borderColor");
        xProject = bgSize[0] / 2;
        yProject = bgSize[1] / 2;

        // Header
        headerXoffset = loader.setInt(headerXoffset, "header.offset.0");
        headerYoffset = loader.setInt(headerYoffset, "header.offset.1");
        headerFont = loader.setFont(headerFont, "header.font");
        headerColor = loader.setColor(headerColor, "header.color");

        // Footer
        footerXoffset = loader.setInt(footerXoffset, "footer.offset.0");
        footerYoffset = loader.setInt(footerYoffset, "footer.offset.1");
        footerFont = loader.setFont(footerFont, "footer.font");
        footerColor = loader.setColor(footerColor, "footer.color");

        // Ruler
        rulerXoffset = loader.setInt(rulerXoffset, "ruler.offset");
        rulerFont = loader.setFont(rulerFont, "ruler.font");
        rulerColor = loader.setColor(rulerColor, "ruler.color");
        maxRulerGrade = loader.setInt(maxRulerGrade, "ruler.maxRulerGrade");
        enableDynamicRuler = loader.setBool(enableDynamicRuler, "ruler.enableDynamicRuler");
        extendScaleLine = loader.setBool(extendScaleLine, "ruler.extendScaleLine");
        LegendX = loader.setDouble(LegendX, "legend.x");
        LegendY = loader.setDouble(LegendY, "legend.y");

        //Key
        keysYoffset = loader.setInt(keysYoffset, "key.offset");
        keysFont = loader.setFont(keysFont, "key.font");
        keysColor = loader.setColor(keysColor, "key.color");
        /*there should be a markColor, but it is not in key nor in the program*/

        // Legend
        iconSize = loader.setIntegerArray(iconSize, "legend.icons");
        iconScale[1] = iconSize[1];
        legendFont = loader.setFont(legendFont, "legend.font");
        legendHeight = loader.setInt(legendHeight, "legend.height");
        legendColor = loader.setColor(legendColor, "legend.color");
        legendScaleFactor = loader.setDouble(legendScaleFactor, "legend.factor");
        legendColumnNum = loader.setInt(legendColumnNum, "legend.column");
        legendRowNum = loader.setInt(legendRowNum, "legend.row");

        // interpolator
        FPS = loader.setInt(FPS, "interpolator.FPS");
        FPD = loader.setInt(FPD, "interpolator.FPD");
        maxVelocity = loader.setDouble(maxVelocity, "interpolator.maxVelocity");
        sortMethod = loader.setStr(sortMethod, "interpolator.sortMethod");

        //bar
        barPattern = loader.setStringArray(barPattern, "bar.barPattern");
        barWidthRatio = loader.setDoubleArray(barWidthRatio, "bar.barWidthRatio");
        barSkin = dataLoader.loadSkins();
        
        //Basic_Config
        isStackedBar = loader.setBool(isStackedBar, "isStackedBar");
        hasRightRuler = loader.setBool(hasRightRuler, "hasRightRuler");
        hasBorder = loader.setBool(hasBorder, "hasBorder");
        hasHeader = loader.setBool(hasHeader, "hasHeader");
        hasFooter = loader.setBool(hasFooter, "hasFooter");
    }

    public void loadConfig() throws Exception {
        loadConfig("Data.json");
    }
}