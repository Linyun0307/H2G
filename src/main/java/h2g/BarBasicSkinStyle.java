package h2g;

import java.awt.Color;
import java.util.ArrayList;

public class BarBasicSkinStyle{
    public Color[] segColor = {Color.RED, Color.YELLOW, Color.BLUE};
    public Color frameColor = Color.GREEN;
    public boolean isBarFilled = true;
    public boolean hasBarFrame = false;
    public double frameSize = 0.01;
    
    public void loadConfig(String filename) throws Exception {
        ConfigLoader loader = new ConfigLoader(filename);
        ArrayList<Color> segColor = new ArrayList<>();
        for(int x=1;;++x) {
            Color _segColor = loader.getColor("segColor"+x);
            if (_segColor != null) segColor.add(_segColor);
            else break;
        }
        if(!segColor.isEmpty()) this.segColor = (Color[])(segColor.toArray(new Color[segColor.size()]));

        Color _frameColor = loader.getColor("frameColor");
        if (_frameColor != null) frameColor = _frameColor;
        isBarFilled = loader.getBool("isBarFilled");
        hasBarFrame = loader.getBool("hasBarFrame");
        double _frameSize = loader.getDouble("frameSize");
        if (_frameSize != 0) frameSize = _frameSize;
    }
    public void loadConfig() throws Exception {
        loadConfig("BarBasicSkin.json");
    }
}