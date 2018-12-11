package h2g;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
// import java.util.Random;
import java.lang.Thread;
public class BarDrawingTutor {
    private static Interpolator i;
    private static HashMap<BarLocation, Double> transparency;
    private static double[] barWidth;
    private Bar[] bar;
    private int arrayHead = 0, index = -1;
    private double maxValue;
    public int currentFrame;
    private void putFilteredBar(int layer, Bar[] b) {
        for(int x=0;x<b.length;++x) {
            if(b[x].bL.layer == layer) bar[arrayHead++] = b[x];
        }
    }
    public BarDrawingTutor(CanvaStyle c, double[][] rawData, double maxVelocity) {
        Interpolator.barPattern = c.barPattern;
        Interpolator.barWidthRatio = c.barWidthRatio;
        Interpolator.FPD = c.FPD;
        Interpolator.rawData = rawData;
        BarSwaper.maxVelocity = maxVelocity;
        i = new Interpolator();
        transparency = Interpolator.bLD.transparency;
        barWidth = Interpolator.validWidth;
    }
    public BarDrawingTutor(int currentFrame) {
        this.currentFrame = currentFrame;
        Bar[] b = Interpolator.bar[currentFrame];
        bar = new Bar[b.length];
        maxValue = b[0].val;
        putFilteredBar(BarLocation.LAYER_BOTTOM, b);
        putFilteredBar(BarLocation.LAYER_MID, b);
        putFilteredBar(BarLocation.LAYER_TOP, b);
    }
    public boolean hasNext() {
        return index+1<arrayHead;
    }
    public void next() {
        index++;
    }
    public double getLocation() {
        return bar[index].bL.location;
    }
    public double getTransparency() {
        if(transparency.containsKey(bar[index].bL)) {
            return transparency.get(bar[index].bL);
        }
        else return 0;
    }
    public int getBarID() {
        return bar[index].id;
    }
    public double getValue() {
        return bar[index].val;
    }
    public double getDeltaValue() {
        return bar[index].dVal;
    }
    public int getTotalFrame() {
        return Interpolator.endFrame;
    }
    public double getPatternGap() {
        double[] barWidth = Interpolator.barWidthRatio;
        return barWidth[barWidth.length-1];
    }
    public double getBarWidth() {
        return barWidth[getBarID()%barWidth.length];
    }
    public void seek(int index) {
        this.index = index;
    }
    public double getMaxValue() {
        return maxValue;
    }
    public void seek() {
        seek(-1);
    }
}
class Interpolator {
    public static boolean swapping = true;
    public static int FPD = 60; // Frames per data
    public static double[][] rawData; // rawdata[bar][data]
    public static Bar[][] bar; // data[frame][bar]
    public static Bar[] curBar;
    public static BarLocation[] curBL;
    public static BarLayoutDesigner bLD;
    public static int currentFrame, endFrame, barNum, dataNum;
    public static String[] barPattern = {"Default",""};
    public static double[] barWidthRatio = {0.5,-1};
    public static double[] validWidth;
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
        double avgWidth, allocatedSpace = 0;
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
        validWidth = new double[nonEmptyNum];
        for(x=0,i=0;x<patternNum;++x) {
            if(barPattern[x].length()!=0) {
                validWidth[i] = barWidthRatio[x];
                availablePos[i++] = allocatedSpace + barWidthRatio[x]/2;
            }
            allocatedSpace += barWidthRatio[x];
        }
        for(x=0;x<barNum;++x) {
            curBL[x] = new BarLocation(x, x/nonEmptyNum + availablePos[x%nonEmptyNum]);
        }
        xScale[1] = Math.ceil(x*1.0/nonEmptyNum);
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
        int barNum = bar.length;
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
/*class BarLayoutDesigner {
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
    private double getTransparency(double progress) {
        if(progress<0.5) return progress*2;
        //else return 1-(progress-0.5)*2;
        else return 2-2*progress;
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
            transparency.put(bS.a, getTransparency(bS.progress));
            transparency.put(bS.b, getTransparency(bS.progress));
            if(swaper.endFrame<currentFrame || bS.progress==1) { // Free Useless Swaper
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
}*/
class BarLayoutDesigner {
    ArrayList<BarSwaper> activeSwaper = new ArrayList<>();
    HashMap< Integer ,LinkedList<int[]> > waitingQueue = new HashMap<>();
    HashMap<BarLocation, Double> transparency = new HashMap<>();
    BarLocation[] bar;
    int currentFrame = 0;
    BarSwaper[] flag;
    public BarLayoutDesigner(BarLocation[] bar) {
        this.bar = bar.clone();
        flag = new BarSwaper[bar.length];
        for(int x=0;x<bar.length;++x) {
            waitingQueue.put(bar[x].id, new LinkedList<int[]>() );
        }
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
            //waitingQueue.add(new int[]{id1,id2,startFrame});
            int[] form = new int[]{id1,id2,startFrame};
            waitingQueue.get(id1).offer(form);
            waitingQueue.get(id2).offer(form);
        } else if(flag[i1]!=null || flag[i2]!=null) {
            int endFrame = 0;
            if(flag[i1]!=null) endFrame=flag[i1].endFrame+1;
            if(flag[i2]!=null && flag[i2].endFrame+1>endFrame) {
                endFrame = flag[i2].endFrame+1;
            }
            //waitingQueue.add(new int[]{id1,id2,endFrame});
            int[] form = new int[]{id1,id2,endFrame};
            waitingQueue.get(id1).offer(form);
            waitingQueue.get(id2).offer(form);
        }
        else {
            BarSwapStatus bS = new BarSwapStatus( bar[i1], bar[i2] );
            flag[i1] = new BarSwaper(bS, startFrame);
            flag[i2] = flag[i1];
            activeSwaper.add(flag[i1]);
        }
    }
    private void checkWaitingQueue() {
        /*for(int x=0;x<waitingQueue.size();++x) {
            int[] form = waitingQueue.get(x);
            if(form[2]<=currentFrame) {
                waitingQueue.remove(form);
                swapBars(currentFrame, form[0], form[1]);
            }
        }*/
        for(int x=0;x<bar.length;++x) {
            LinkedList<int[]> list = waitingQueue.get(bar[x].id);
            int[] form = list.peek();
            if(form == null) continue;
            LinkedList<int[]> a = waitingQueue.get(form[0]);
            LinkedList<int[]> b = waitingQueue.get(form[1]);
            if(a.peek()!=b.peek()) continue;
            if(form[2]>currentFrame) continue;
            int i1 = searchBar(form[0]);
            int i2 = searchBar(form[1]);
            if(flag[i1]!=null || flag[i2]!=null) continue;
            a.poll();
            b.poll();
            swapBars(currentFrame, form[0], form[1]);
        }
    }
    private void applyExchange(BarLocation b) {
        int index = searchBar(b.id);
        bar[index] = b;
        flag[index] = null;
    }
    private double getTransparency(double progress) {
        if(progress<0.5) return progress*2;
        //else return 1-(progress-0.5)*2;
        else return 2-2*progress;
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
            transparency.put(bS.a, getTransparency(bS.progress));
            transparency.put(bS.b, getTransparency(bS.progress));
            if(swaper.endFrame<currentFrame || bS.progress==1) { // Free Useless Swaper
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
    public static double maxVelocity = 0.02; // Move scaled pixel per frame
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
        endFrame = startFrame + (int) Math.floor(dFrame);
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