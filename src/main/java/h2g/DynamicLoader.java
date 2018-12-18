package h2g;

import java.awt.Color;
import java.util.HashMap;

public class DynamicLoader {
    //TODO: MODIFY THE FUNCTION AND DIFFERENT STYLES
    private static HashMap<String,BarBasicSkinStyle> pool = new HashMap<>();
    private static HashMap<String, Color> fontColor = new HashMap<>();
    private static void initFlatUI() {
        BarBasicSkinStyle configure;
        String[] flatUIname = new String[]{
            "FlatUI0","FlatUI1","FlatUI2","FlatUI3","FlatUI4",
            "FlatUI5","FlatUI6","FlatUI7","FlatUI8","FlatUI9",
            "FlatUI10","FlatUI11","FlatUI12","FlatUI13","FlatUI14",
            "FlatUI15","FlatUI16","FlatUI17","FlatUI18","FlatUI19",
            "BarFlatUI"
        };
        for(String name: flatUIname) {
            try {
                configure = new BarBasicSkinStyle();
                configure.loadConfig(name+".json");
                pool.put(name, configure);
                fontColor.put(name, configure.fontColor);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    private static void initBasicUI() {
        fontColor.put("Basic", Color.BLACK);
    }
    static {
        initFlatUI();
        initBasicUI();
    } 
    public static Color getFontColor(String skinName) {
        if (fontColor.containsKey(skinName)) return fontColor.get(skinName);
        return Color.BLACK;
    }
    public static BarGenerator get(String skinName, int[] barSize, double[] scale, boolean rotated) {
        if (pool.containsKey(skinName)) {
            return new BarBasicSkin(pool.get(skinName), barSize, scale, rotated);
        }
        if ("Basic".equals(skinName)) return new BarBasicSkin(barSize, scale, rotated);
        return null;
    }
}
