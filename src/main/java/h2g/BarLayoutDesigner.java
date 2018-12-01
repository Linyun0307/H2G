package h2g;

import java.util.ArrayList;
// import java.util.Random;
import java.lang.Thread;

public class BarLayoutDesigner {
    public static void main(String[] args) {
        BarLocation[] bar = new BarLocation[20];
        for(int x=0;x<20;++x) {
            bar[x] = new BarLocation(x, x+1);
        }
        BarLayout l = new BarLayout(bar);
        for(int x=0;x<200;++x) {
            if(x%20 != (2000-x)%20) {
                l.swapBars(500*(x/20), x%20, (2000-x)%20);
            }
        }
        int[] map;
        for(int x=0;x<5000;++x) {
            map = new int[210];
            BarLocation[] bL = l.getLayout();
            for(int i=0;i<20;++i) {
                int pos = (int)Math.round(bL[i].location*10);
                map[pos] = 1;
            }
            for(int i=0;i<210;++i) {
                System.out.print((map[i]==1)?"|":" ");
            }
            System.out.println("       "+x);
            
            try {
                Thread.sleep(20);
            } catch (Exception e) {
                //TODO: handle exception
            }
            l.nextFrame();
        }
    }
}

class BarLayout {
    ArrayList<BarSwaper> activeSwaper = new ArrayList<>();
    ArrayList<int[]> waitingSwap = new ArrayList<>();
    BarLocation[] bar;
    int currentFrame = 0;
    BarSwaper[] mask;
    public BarLayout(BarLocation[] bar) {
        this.bar = bar.clone();
        mask = new BarSwaper[bar.length];
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
            waitingSwap.add(new int[]{id1,id2,startFrame});
        } else if(mask[i1]!=null || mask[i2]!=null) {
            int endFrame = 0;
            if(mask[i1]!=null) endFrame=mask[i1].endFrame+1;
            if(mask[i2]!=null && mask[i2].endFrame+1>endFrame) {
                endFrame = mask[i2].endFrame+1;
            }
            waitingSwap.add(new int[]{id1,id2,endFrame});
        }
        else {
            BarsLocation bars = new BarsLocation( bar[i1].copy(), bar[i2].copy() );
            mask[i1] = new BarSwaper(bars, startFrame);
            mask[i2] = mask[i1];
            activeSwaper.add(mask[i1]);
        }
    }
    private void checkWaitingQueue() {
        for(int x=0;x<waitingSwap.size();++x) {
            int[] swaper = waitingSwap.get(x);
            if(swaper[2]<=currentFrame) {
                waitingSwap.remove(x--);
                swapBars(currentFrame, swaper[0], swaper[1]);
            }
        }
    }
    public BarLocation[] getLayout() {
        BarLocation[] rel = new BarLocation[bar.length];
        int i = 0;
        for(int x=0;x<bar.length;++x) {
            if(mask[x]==null) {
                rel[i++] = bar[x];
            }
        }
        for(int x=0;x<activeSwaper.size();++x) {
            BarSwaper swaper = activeSwaper.get(x);
            BarsLocation bL = swaper.getCurrentLocation(currentFrame);
            rel[i++] = bL.a;
            rel[i++] = bL.b;
            if(swaper.endFrame<=currentFrame) { // Free Useless Swaper
                int i1 = searchBar(bL.a.id);
                int i2 = searchBar(bL.b.id);
                bar[i1] = bL.a;
                bar[i2] = bL.b;
                mask[i1] = null;
                mask[i2] = null;
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
    BarsLocation bars;
    public static double maxVelocity = 0.1; // Move scaled pixel per frame
    private double k,k1,k2,b,dFrame;

    private double getDisplace(int currentFrame) {
        double f = currentFrame-startFrame;
        if(currentFrame<=midFrame) return k*f*f;
        else return k1*f*f + k2*f + b;
    }
    public BarSwaper(BarsLocation bars, int startFrame) {
        if(bars.a.location>bars.b.location) {
            this.bars = new BarsLocation(bars.b.copy(), bars.a.copy());
        } else this.bars = bars.copy();

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
    public BarsLocation getCurrentLocation(int currentFrame) {
        if(startFrame<=currentFrame && currentFrame<=endFrame) {
            double displace = getDisplace(currentFrame);
            return new BarsLocation( new BarLocation(bars.a.id, bars.a.location+displace, BarLocation.LAYER_TOP),
                                     new BarLocation(bars.b.id, bars.b.location-displace, BarLocation.LAYER_BOTTOM));
        } else if(currentFrame<startFrame) {
            return bars.copy();
        } else {
            BarsLocation tmp = bars.copy();
            tmp.a.location = bars.b.location;
            tmp.b.location = bars.a.location;
            return tmp;
        }
    }
}
class BarsLocation {
    public BarLocation a, b;
    public BarsLocation() {}
    public BarsLocation(BarLocation a, BarLocation b) {
        this.a = a;
        this.b = b;
    }
    public BarsLocation copy() {
        return new BarsLocation(a.copy(), b.copy());
    }
}
class BarLocation {
    public final static int LAYER_TOP = 1;
    public final static int LAYER_MID = 0;
    public final static int LAYER_BOTTOM = -1;
    public final static int LAYER_HIDDEN = -2;
    public int id,layer; // 0:Mid 1:Top -1:Bottom -2:Hidden
    public double location;
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