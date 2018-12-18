package h2g;

public class RulerDrawingTutor {
    double[] yValue;
    double rulerStep;
    int maxRulerGrade;
    double nFactor;  // ={1,2,5}
    public void selectNfactor() {
        nFactor = 1;
        if((yValue[1]-yValue[0])/(rulerStep)<=maxRulerGrade) return;
        nFactor = 2;
        if((yValue[1]-yValue[0])/(2*rulerStep)<=maxRulerGrade) return;
        nFactor = 5;
        if((yValue[1]-yValue[0])/(5*rulerStep)<=maxRulerGrade) return;
            
         rulerStep*=10;
        //rulerStep*=2;
        selectNfactor();
    }
    //public RulerDrawingTutor(double[] yValue, int maxRulerGrade) {
    public RulerDrawingTutor(CanvaStyle canvaStyle, HistogramData histogramData) {
        this.maxRulerGrade = canvaStyle.maxRulerGrade;
        this.yValue = histogramData.yValue.clone();
        rulerStep = (yValue[1]-yValue[0])/maxRulerGrade;
        rulerStep = Math.pow(10, Math.floor( Math.log10(rulerStep)) );
        selectNfactor();
    }
    public void setYmaxValue(double yMaxValue) {
        yValue[1] = yMaxValue;
        selectNfactor();
    }
    public double getRulerStep() {
        return nFactor*rulerStep;
    }
    public int getRulerGrade() {
        return (int)( (yValue[1]-yValue[0])/(nFactor*rulerStep) );
    }
}