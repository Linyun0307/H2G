package h2g;

public class CoordProjecter {
    int originX;
    int originY;
    SigDraw base;
    SigDraw img;

    public CoordProjecter(SigDraw base, SigDraw img, double xCentreOfImg, double yCentreOfImg) {
        this.base = base;
        this.img = img;
        originX = (int) Math.round(base.scaleX(xCentreOfImg) - img.width / 2.0);
        originY = (int) Math.round(base.scaleY(yCentreOfImg) - img.height / 2.0);
    }

    public double getX(double x) {
        return base.userX(img.scaleX(x) + originX);
    }

    public double getY(double y) {
        return base.userY(img.scaleY(y) + originY);
    }
}