package h2g;

import java.io.IOException;
import java.util.ArrayList;

public class HistogramData {
    String header = "2013-2018 FORBES CHINA RICHEST TOP 10";
    String footer = "Drawn by Tonny-Gu, Whexy-Shi, Linyun-Li";
    String[] keys = {"Ma Yun", "Ma Huateng", "Xu Jiayin", "Wang Jianlin", "He Xiangjian", "Yang Huiyan", "Wang Wei", "Li Yanhong", "Li Shufu", "Ding Lei", "Lei Jun", "Huang Zheng", "Wang Wenying", "Zhang Zhidong", "Zong Qinhou", "Li Hejun", "Wei Jianjun", "Liu Yongxing"};
    ConfigLoader loader;
    // Blank size will be 1.0-sum(ratio)
    //double[] xScale = { 0, 1.0 }; // MIN, MAX
    int visiblePattern = 17;
    double[] yValue = {-0.00001, 1.0}; // MIN, MAX
    double rulerStep = 0.0;
    int rulerGrade = 0;

    public void loadConfig(String pattern) throws Exception {
        loader = new ConfigLoader(pattern);
        DataLoader dataLoader = new DataLoader(pattern);
        header = loader.setStr(header, "header.text");
        footer = loader.setStr(footer, "footer.text");
        visiblePattern = loader.setInt(visiblePattern, "coord.visiblePattern");
        yValue = loader.setDoubleArray(yValue, "coord.yValue");
        keys = dataLoader.loadKeys();
    }

    public void loadConfig() throws Exception {
        loadConfig("Data.json");
    }
}