package h2g;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.lang.Thread;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Graphics2D;

public class ThreadManager {
    
    public static void main(String[] args) {
        CanvaStyle c = new CanvaStyle();
        HistogramData d = new HistogramData();
        double[][] rawData = new double[4][];
        rawData[0] = new double[]{4,5,9,21,30,70,110,400,400,800,2000,9000};
        rawData[1] = new double[]{3,6,10,23,50,100,200,300,400,1000,3000,8000};
        rawData[2] = new double[]{2,7,11,22,40,90,300,350,500,1200,4000,7000};
        rawData[3] = new double[]{1,8,12,24,60,80,100,120,600,700,5000,6000};
        d.yValue[0] = 0;
        d.yValue[1] = 1.0;
        d.visiblePattern = 1;
        c.FPD = 180;
        RulerDrawingTutor r = new RulerDrawingTutor(d.yValue, 20);
        BarDrawingTutor initB = new BarDrawingTutor(c,rawData,0.03); // For initialization
        FrameCreator f;
        Timer timer = null;
        long startTime = System.currentTimeMillis();
        //BufferedImage[] bf = new BufferedImage[initB.getTotalFrame()];
        ConcurrentLinkedQueue<BufferedImage> buffer = new ConcurrentLinkedQueue<>();
        for(int x=0;x<initB.getTotalFrame();++x) {
            if(x>200 && timer==null) {
                timer = new Timer();
                timer.schedule(new ImagePlayer(buffer, c.bgSize), 0, 1000/c.FPS);
            }
            BarDrawingTutor b = new BarDrawingTutor(x);
            d.yValue[1] = b.getMaxValue()*1.01;
            r.setYmaxValue(d.yValue[1]);
            d.rulerGrade = r.getRulerGrade();
            d.rulerStep = r.getRulerStep();
            f = new FrameCreator(b, c, d);
            //f.bg.save(x+".jpg");
            //bf[x] = f.bg.getBuffImg();
            buffer.offer(f.bg.getBuffImg());
            long endTime = System.currentTimeMillis();
            System.out.printf("Frame:%d Average FPS: %.3f\n",x,x/((endTime-startTime)/1000.0));
            //System.out.println("Frame"+x+" has been created!");
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
class MultipleBuffer {
    //public bufferA
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
            onscreen.dispose();
            this.cancel();
            return;
        }
        show(buffer.poll());
    }
    public static void show(BufferedImage offscreenImage) {
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }
}