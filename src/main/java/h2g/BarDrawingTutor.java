package h2g;

import java.util.ArrayList;
import java.util.HashMap;
// import java.util.Random;
import java.lang.Thread;
public class BarDrawingTutor {
    private final static Interpolator i = new Interpolator();
    private final static HashMap<BarLocation, Double> transparency = Interpolator.bLD.transparency;
    private Bar[] bar;
    private int arrayHead = 0, position = 0;
    public int currentFrame;
    private void putFilteredBar(int layer, Bar[] b) {
        for(int x=0;x<b.length;++x) {
            if(b[x].bL.layer == layer) bar[arrayHead++] = b[x];
        }
    }
    public BarDrawingTutor(int currentFrame) {
        this.currentFrame = currentFrame;
        Bar[] b = Interpolator.bar[currentFrame];
        bar = new Bar[b.length];
        putFilteredBar(BarLocation.LAYER_BOTTOM, b);
        putFilteredBar(BarLocation.LAYER_MID, b);
        putFilteredBar(BarLocation.LAYER_TOP, b);
    }
    public boolean hasNext() {

    }
    public void next() {

    }
    public double getLocation() {

    }
    public double getTransparency() {

    }
    public int getBarID() {

    }
    public double getValue() {
        
    }
}
class Interpolator {
    public static boolean swapping = true;
    public static int FPS = 60;
    public static int FPD = 60; // Frames per data
    public static double[][] rawData; // rawdata[bar][data]
    public static Bar[][] bar; // data[frame][bar]
    public static Bar[] curBar;
    public static BarLocation[] curBL;
    public static BarLayoutDesigner bLD;
    public static int currentFrame, endFrame, barNum, dataNum;
    public static String[] barPattern = {"Default",""};
    public static double[] barWidthRatio = {0.5,-1};
    public static double[] xScale = { 0, 1.0 };
    public Interpolator() {
        init();
        interpolateBarValue();
        interpolateBarLocation();
    }
    private void parseBarPattern() {
        curBL = new BarLocation[barNum];
        double unallocatedSpace = 1;
        double unallocatedBar = 0;
        double avgWidth;
        int patternNum = barWidthRatio.length;
        int nonEmptyNum = patternNum;
        int x,i;
        double[] availablePos;
        for(double r:barWidthRatio) {
            if(r>0) unallocatedSpace -= r;
            else unallocatedBar++;
        }
        avgWidth = unallocatedSpace/unallocatedBar;
        xScale[0] = -avgWidth;
        for(x=0;x<patternNum;++x) {
            if(barWidthRatio[x]<0) barWidthRatio[x] = avgWidth;
            if(barPattern[x].length()==0) nonEmptyNum--;
        }
        availablePos = new double[nonEmptyNum];
        for(x=0,i=0;x<patternNum;++x) {
            if(barPattern[x].length()!=0) {
                availablePos[i++] = (((x==0)?0:barWidthRatio[x-1])+barWidthRatio[x])/2;
            }
        }
        for(x=0,i=0;x<barNum;++x,++i) {
            curBL[x] = new BarLocation(x, i/nonEmptyNum + availablePos[i%nonEmptyNum]);
        }
        xScale[1] = Math.ceil(i*1.0/nonEmptyNum);
    }
    private void init() {
        dataNum = rawData[0].length-1;
        barNum = rawData.length;
        endFrame = dataNum*FPD;
        bar = new Bar[endFrame][];
        curBar = new Bar[barNum];
        parseBarPattern();
        bLD = new BarLayoutDesigner(curBL);
        for(int x=0;x<barNum;++x) {
            curBar[x] = new Bar(x, rawData[x][0], 0, null);
        }
        bar[0] = deepCopyBarArray(curBar);
    }
    private Bar[] deepCopyBarArray(Bar[] b) {
        Bar[] r = new Bar[b.length];
        for(int i=0;i<b.length;++i) {
            r[i] = b[i].copy();
        }
        return r;
    }
    private void sortAndSwapBar(Bar[] bar, int currentFrame) { // BubbleSort
        int x,y;
        for(x=0;x<barNum-1;++x) {
            boolean flag = true;
            for(y=0;y<barNum-1-x;++y) {
                if(bar[y].val<bar[y+1].val) {
                    Bar tmp = null;
                    tmp = bar[y];
                    bar[y] = bar[y+1];
                    bar[y+1] = tmp;
                    flag = false;
                    bLD.swapBars(currentFrame, bar[y].id, bar[y+1].id);
                }
            }
            if(flag) return;
        }
    }
    private void interpolateBarValue() {
        int x,y,frame;
        double[] dVal = new double[barNum];
        for(x=0;x<dataNum;++x) {
            
            for(y=0;y<barNum;++y) {
                dVal[y] = (rawData[y][x+1]-rawData[y][x])/FPD;
            }
            for(y=0;y<barNum;++y) {
                curBar[y].dVal = dVal[curBar[y].id];
                curBar[y].val = rawData[curBar[y].id][x];
            }
            bar[FPD*x] = deepCopyBarArray(curBar);
            for(frame=1;frame<FPD;++frame) {
                for(y=0;y<barNum;++y) {
                    curBar[y].val += dVal[curBar[y].id];
                }
                sortAndSwapBar(curBar, FPD*x + frame);
                bar[FPD*x + frame] = deepCopyBarArray(curBar);
            }
        }
    }
    private void interpolateBarLocation() {
        for(int frame=0;frame<endFrame;++frame) {
            BarLocation[] retBL = bLD.getLayout();
            for(int i=0;i<barNum;++i) {
                int index = BarLayoutDesigner.searchBar(retBL, bar[frame][i].id);
                bar[frame][i].bL = retBL[index];
            }
            bLD.nextFrame();
        }
    }
    public static void loadConfig(String filename) {

    }
    public static void loadConfig() {

    }
    public static void main(String[] args) {
        rawData = new double[3][];
        rawData[0] = new double[]{3,4,7,8};
        rawData[1] = new double[]{1,5,6,9};
        rawData[2] = new double[]{2,3,4,5};
        Interpolator i = new Interpolator();
        System.out.println("hello");
    }
}
class Bar {
    double val,dVal;
    int id;
    BarLocation bL;
    public Bar(int id, double val, double dVal, BarLocation bL) {
        this.id = id;
        this.val = val;
        this.dVal = dVal;
        this.bL = bL;
    }
    public Bar() { this(0,0,0,null); }
    public Bar copy() {
        if(bL == null) return new Bar(id, val, dVal, null);
        else return new Bar(id, val, dVal, bL.copy());
    }
}
class BarLayoutDesigner {
    ArrayList<BarSwaper> activeSwaper = new ArrayList<>();
    ArrayList<int[]> waitingQueue = new ArrayList<>();
    HashMap<BarLocation, Double> transparency = new HashMap<>();
    BarLocation[] bar;
    int currentFrame = 0;
    BarSwaper[] flag;
    public BarLayoutDesigner(BarLocation[] bar) {
        this.bar = bar.clone();
        flag = new BarSwaper[bar.length];
    }
    public static int searchBar(BarLocation[] b, int id) {
        for(int x=0;x<b.length;++x) {
            if(b[x].id==id) return x;
        }
        return -1;
    }
    private int searchBar(int id) {
        return searchBar(bar, id);
    }
    public void swapBars(int startFrame, int id1, int id2) {
        int i1 = searchBar(id1);
        int i2 = searchBar(id2);
        if(startFrame>currentFrame) {
            waitingQueue.add(new int[]{id1,id2,startFrame});
        } else if(flag[i1]!=null || flag[i2]!=null) {
            int endFrame = 0;
            if(flag[i1]!=null) endFrame=flag[i1].endFrame+1;
            if(flag[i2]!=null && flag[i2].endFrame+1>endFrame) {
                endFrame = flag[i2].endFrame+1;
            }
            waitingQueue.add(new int[]{id1,id2,endFrame});
        }
        else {
            BarSwapStatus bS = new BarSwapStatus( bar[i1], bar[i2] );
            flag[i1] = new BarSwaper(bS, startFrame);
            flag[i2] = flag[i1];
            activeSwaper.add(flag[i1]);
        }
    }
    private void checkWaitingQueue() {
        for(int x=0;x<waitingQueue.size();++x) {
            int[] form = waitingQueue.get(x);
            if(form[2]<=currentFrame) {
                waitingQueue.remove(form);
                swapBars(currentFrame, form[0], form[1]);
            }
        }
    }
    private void applyExchange(BarLocation b) {
        int index = searchBar(b.id);
        bar[index] = b;
        flag[index] = null;
    }
    public BarLocation[] getLayout() {
        BarLocation[] rel = new BarLocation[bar.length];
        int i = 0;
        for(int x=0;x<bar.length;++x) {
            if(flag[x]==null) {
                rel[i++] = bar[x];
            }
        }
        for(int x=0;x<activeSwaper.size();++x) {
            BarSwaper swaper = activeSwaper.get(x);
            BarSwapStatus bS = swaper.getCurrentLocation(currentFrame);
            rel[i++] = bS.a;
            rel[i++] = bS.b;
            transparency.put(bS.a, bS.progress);
            transparency.put(bS.b, bS.progress);
            if(swaper.endFrame<=currentFrame || bS.progress==1) { // Free Useless Swaper
                applyExchange(bS.a);
                applyExchange(bS.b);
                activeSwaper.remove(x--);
            }
        }
        return rel;
    }
    public void nextFrame() {
        currentFrame++;
        checkWaitingQueue();
    }
}
class BarSwaper {
    int startFrame, endFrame;
    double distance, midFrame;
    final BarSwapStatus bars;
    public static double maxVelocity = 0.15; // Move scaled pixel per frame
    private double k,k1,k2,b,dFrame;

    private double getDisplace(int currentFrame) {
        double f = currentFrame-startFrame;
        if(currentFrame<=midFrame) return k*f*f;
        else return k1*f*f + k2*f + b;
    }
    private double getProgress(int currentFrame) {
        double f = currentFrame-startFrame;
        return f/dFrame;
    }
    public BarSwaper(BarSwapStatus bars, int startFrame) {
        if(bars.a.location>bars.b.location) {
            this.bars = new BarSwapStatus(bars.b.copy(), bars.a.copy());
        } else this.bars = new BarSwapStatus(bars.a.copy(), bars.b.copy());

        this.startFrame = startFrame;
        distance = this.bars.b.location - this.bars.a.location;
        dFrame = 2*distance/maxVelocity;
        endFrame = startFrame + (int) Math.round(dFrame);
        midFrame = (startFrame+endFrame)/2.0;
        
        k1 = -maxVelocity/dFrame;
        k2 = 2*maxVelocity;
        b = -maxVelocity*dFrame/2;
        k = maxVelocity/dFrame;
    }
    public BarSwapStatus getCurrentLocation(int currentFrame) {
        if(startFrame<=currentFrame && currentFrame<=endFrame) {
            double displace = getDisplace(currentFrame);
            double progress = getProgress(currentFrame);
            return new BarSwapStatus( new BarLocation(bars.a.id, bars.a.location+displace, BarLocation.LAYER_TOP),
                                      new BarLocation(bars.b.id, bars.b.location-displace, BarLocation.LAYER_BOTTOM),
                                      progress);
        } else if(currentFrame<startFrame) {
            return bars.copy();
        } else {
            return new BarSwapStatus( new BarLocation(bars.a.id, bars.b.location, BarLocation.LAYER_TOP),
                                      new BarLocation(bars.b.id, bars.a.location, BarLocation.LAYER_BOTTOM),
                                      1);
        }
    }
}
class BarSwapStatus {
    public BarLocation a, b;
    public double progress;
    public BarSwapStatus() { this( null, null, 0 ); }
    public BarSwapStatus(BarLocation a, BarLocation b) { this( a, b, 0 ); }
    public BarSwapStatus(BarLocation a, BarLocation b, double progress) {
        this.a = a;
        this.b = b;
        this.progress = progress;
    }
    public BarSwapStatus deepCopy() {
        return new BarSwapStatus(a.copy(), b.copy());
    }
    public BarSwapStatus copy() {
        return new BarSwapStatus(a, b);
    }
}
class BarLocation {
    public final static int LAYER_TOP = 1;
    public final static int LAYER_MID = 0;
    public final static int LAYER_BOTTOM = -1;
    public final static int LAYER_HIDDEN = -2;
    public final int id,layer; // 0:Mid 1:Top -1:Bottom -2:Hidden
    public final double location;
    public BarLocation(int id, double location) {this(id, location, LAYER_MID);}
    public BarLocation(int id, double location, int layer) {
        this.id = id;
        this.location = location;
        this.layer = layer;
    }
    public BarLocation copy() {
        return new BarLocation(id, location, layer);
    }
}