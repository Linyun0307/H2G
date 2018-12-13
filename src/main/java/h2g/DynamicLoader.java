package h2g;

public class DynamicLoader 
{
    public static BarBasicSkinStyle Basic1 = new BarBasicSkinStyle();
    static {
        try {
            Basic1.loadConfig();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static BarGenerator get(String skinName, int[] barSize, double[] scale, boolean rotated) {
        if("Basic".equals(skinName)) return new BarBasicSkin(barSize, scale, rotated);
        if("Basic1".equals(skinName)) return new BarBasicSkin(Basic1, barSize, scale, rotated);
        return null;
    }
}
