package h2g;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.Thread;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Graphics2D;

public class ThreadManager {
    public static CanvaStyle canvaStyle;
    public static HistogramData histogramData;
    public static RulerDrawingTutor rulerDrawingTutor;
    public static BarDrawingHelper barDrawingHelper;
    public static FrameCreator frameCreator;
    public static BarDrawingTutor barDrawingTutor;
    public static Timer timer = null;
    public static ConcurrentLinkedQueue<BufferedImage> buffer = new ConcurrentLinkedQueue<>();
    public static LegendDrawer legendDrawer;

    public static BarDrawingTutor[] bDTbuffer;
    public static void bufferBarDrawingTutor() {
        bDTbuffer = new BarDrawingTutor[barDrawingHelper.getTotalFrame()];
        for(int x=0;x<barDrawingHelper.getTotalFrame();++x) {
            if(canvaStyle.isStackedBar && barDrawingHelper instanceof StackedBarDrawingHelper) {
                bDTbuffer[x] = ((StackedBarDrawingHelper) barDrawingHelper).getStackedTutor(x);
            }
            else bDTbuffer[x] = barDrawingHelper.getTutor(x);
        }
    }
    public static BarDrawingTutor fetchBarDrawingTutor(int currentFrame) {
        BarDrawingTutor rel = bDTbuffer[currentFrame];
        bDTbuffer[currentFrame] = null;
        return rel;
    }

    public static void refreshRuler(double maxValue) {
        maxValue += Math.abs(maxValue*canvaStyle.expandRatio);
        histogramData.yValue[1] = maxValue;
        rulerDrawingTutor.setYmaxValue(histogramData.yValue[1]);
        histogramData.rulerGrade = rulerDrawingTutor.getRulerGrade();
        histogramData.rulerStep = rulerDrawingTutor.getRulerStep();
    }
    public static void main(String[] args) throws Exception {
        DataLoader dataLoader = new DataLoader();
        double[][] rawData = dataLoader.loadRawData();
        canvaStyle = new CanvaStyle();
        canvaStyle.loadConfig();
        histogramData = new HistogramData();
        histogramData.loadConfig();
        histogramData.keys = dataLoader.loadKeys();
        rulerDrawingTutor = new RulerDrawingTutor(canvaStyle, histogramData);
        
        if(canvaStyle.isStackedBar) barDrawingHelper = new StackedBarDrawingHelper(canvaStyle, histogramData, rawData);
        else barDrawingHelper = new BarDrawingHelper(canvaStyle, rawData);

        legendDrawer = new LegendDrawer(canvaStyle, histogramData);
        canvaStyle.legendImg = legendDrawer.getLegend();
        long startTime = System.currentTimeMillis();
        long endTime;
        double averageFPS;
        
        bufferBarDrawingTutor();

        if(!canvaStyle.enableDynamicRuler) {
            refreshRuler( bDTbuffer[bDTbuffer.length-1].getMaxValue() );
        }

        for(int currentFrame=0;currentFrame<barDrawingHelper.getTotalFrame();++currentFrame) {
            if(currentFrame+1>=canvaStyle.FPD && timer==null) {
                timer = new Timer();
                timer.schedule(new ImagePlayer(buffer, canvaStyle.bgSize), 0, 1000/canvaStyle.FPS);
            }
            barDrawingTutor = fetchBarDrawingTutor(currentFrame);
            if(canvaStyle.enableDynamicRuler) {
                refreshRuler(barDrawingTutor.getMaxValue());
            }
            frameCreator = new FrameCreator(barDrawingTutor, canvaStyle, histogramData);
            //f.bg.save(x+".jpg");
            //bf[x] = f.bg.getBuffImg();
            buffer.offer( frameCreator.bg.getBuffImg() );
             
            if(currentFrame%canvaStyle.FPD==0) {
                endTime = System.currentTimeMillis();
                averageFPS = canvaStyle.FPD/((endTime-startTime)/1000.0);
                System.out.printf("Frame:%d Average FPS: %.3f\n",currentFrame,averageFPS);
                startTime = System.currentTimeMillis();
            }
        }
        
        
        
        /*
        CanvaStyle c = new CanvaStyle();
        c.loadConfig();
        HistogramData d = new HistogramData();
        d.keys = new String[]{"father", "father", "son"};
        d.values = new double[]{0.0, 0.0, 0.0};
        
        
        d.rulerStep = 100;
        d.rulerGrade = 10;
        d.header = "Nothing left";
        d.footer = "Nothing right";
        FrameCreator fc = new FrameCreator(c, d);
        fc.bg.save("test.jpg");
        */
    }
}
class ImagePlayer extends TimerTask {
    private static BufferedImage onscreenImage;
    private static Graphics2D onscreen;
    private static JFrame frame;
    private static ConcurrentLinkedQueue<BufferedImage> buffer;
    public ImagePlayer(ConcurrentLinkedQueue<BufferedImage> buffer, int[] bgSize) {
        ImagePlayer.buffer = buffer;

        frame = new JFrame();
        onscreenImage  = new BufferedImage(bgSize[0], bgSize[1], BufferedImage.TYPE_INT_ARGB);
        onscreen  = onscreenImage.createGraphics();

        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);

        draw.addMouseListener(null);
        draw.addMouseMotionListener(null);
        frame.addKeyListener(null);    // JLabel cannot get keyboard focus

        frame.setContentPane(draw);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Standard Draw");
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    @Override
    public void run() {
        if(buffer.isEmpty()) {
            // onscreen.dispose();
            // this.cancel();
            return;
        }
        show(buffer.poll());
    }
    public static void show(BufferedImage offscreenImage) {
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }
}