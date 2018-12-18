package h2g;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.image.BufferedImage;
// import java.util.Random;

class Interpolator {
    public static int sortMethod = 0;
    public static int FPD = 60; // Frames per data
    public double[][] rawData; // rawdata[bar][data]
    public Bar[][] bar; // data[frame][bar]
    private Bar[] curBar;
    private BarLocation[] curBL;
    public BarLayoutDesigner bLD;
    public int currentFrame, endFrame, barNum, dataNum;
    public static String[] barPattern = {"Default",""};
    public static double[] barWidthRatio = {0.5,-1};
    public static double[] validWidth;
    public static double[] xScale = { 0, 1.0 };
    public Interpolator(CanvaStyle canvaStyle, double[][] rawData) {
        Interpolator.barPattern = canvaStyle.barPattern;
        Interpolator.barWidthRatio = canvaStyle.barWidthRatio;
        Interpolator.FPD = canvaStyle.FPD;
        if("BubbleSort".equals(canvaStyle.sortMethod)) sortMethod = 1;
        if("SelectionSort".equals(canvaStyle.sortMethod)) sortMethod = 2;
        this.rawData = rawData;
        BarSwaper.setMaxVelocity(canvaStyle.maxVelocity);
        BarLayoutDesigner.setTranspartency(canvaStyle.maxTrantransparency, canvaStyle.minTrantransparency);
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
        for(x=0;x<patternNum;++x) {
            if(barWidthRatio[x]<0) barWidthRatio[x] = avgWidth;
            if(barPattern[x].length()==0) nonEmptyNum--;
        }
        xScale[0] = barWidthRatio[patternNum-1];
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
    private void sortAndSwapBarByBubbleSort(Bar[] bar, int currentFrame) {
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
    private void sortAndSwapBarBySelectionSort(Bar[] bar, int currentFrame) { 
        int x,y;
        int barNum = bar.length;
        for(x=0;x<barNum-1;++x) {
            for(y=x+1;y<barNum;++y) {
                if(bar[x].val<bar[y].val) {
                    Bar tmp = null;
                    tmp = bar[x];
                    bar[x] = bar[y];
                    bar[y] = tmp;
                    bLD.swapBars(currentFrame, bar[x].id, bar[y].id);
                }
            }
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
                if(sortMethod == 1) sortAndSwapBarByBubbleSort(curBar, FPD*x + frame);
                if(sortMethod == 2) sortAndSwapBarBySelectionSort(curBar, FPD*x + frame);
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
    public static double k1, k2, b1, b2;
    public static void setTranspartency(double max, double min) {
        BarLayoutDesigner.b1 = max;
        BarLayoutDesigner.b2 = -max+2*min;
        BarLayoutDesigner.k1 = -2*(max-min);
        BarLayoutDesigner.k2 = 2*(max-min);
    }
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
        if(progress<0.5) return BarLayoutDesigner.k1*progress + BarLayoutDesigner.b1;
        else return BarLayoutDesigner.k2*progress + BarLayoutDesigner.b2;
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
    public static void setMaxVelocity(double maxVelocity) {
        BarSwaper.maxVelocity = maxVelocity;
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