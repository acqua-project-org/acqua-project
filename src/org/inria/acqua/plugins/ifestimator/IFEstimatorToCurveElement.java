package org.inria.acqua.plugins.ifestimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.inria.acqua.layers.painter.CurveElement;
import org.inria.acqua.misc.Landmark;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;


public class IFEstimatorToCurveElement {
    public static final String NAME_IFE = "ife";
    public static final String NAME_RTT = "rtt";
    public static final String NAME_SHIFT = "shift";
    public static final String NAME_RTT_LANDMARK = "rttl-";
    public static final String NAME_TIME = "time";

    public static HashMap<String, CurveElement> initializeCurveElements() throws Exception{

        HashMap<String, CurveElement> curves = new HashMap<String, CurveElement>();

        String [] toolTipTextCaption1 = {"Impact Factor: ", "Confidence Interval: ", "Unreachables: "};
        CurveElement ce1 = new CurveElement(
                    IFEstimatorToCurveElement.NAME_IFE,
                    CurveElement.BASICx2_PLUS_BOX_3ARG,
                    "Impact Factor",
                    toolTipTextCaption1
                    );
        ce1.setForcedMinimum(0.0f);
        ce1.setForcedMaximum(1.0f);
        ce1.setBaseReferenceToPaint(0.0f);
        curves.put(IFEstimatorToCurveElement.NAME_IFE, ce1);

        String [] toolTipTextCaption2 = {"Time: "};
        CurveElement ce2 = new CurveElement(
                    IFEstimatorToCurveElement.NAME_TIME,
                    CurveElement.TIME_1ARG,
                    "Time",
                    toolTipTextCaption2);
        curves.put(IFEstimatorToCurveElement.NAME_TIME,ce2);

        String [] toolTipTextCaption3 = {"RTT: ", "STD: "};
        CurveElement ce3 = new CurveElement(
                    IFEstimatorToCurveElement.NAME_RTT,
                    CurveElement.BASIC_PLUS_BOX_2ARG,
                    "Round Trip Time [ms]",
                    toolTipTextCaption3);
        ce3.setForcedMinimum(0.0f);
        //ce3.setForcedMaximum(2000.0f);
        ce3.setBaseReferenceToPaint(0.0f);
        curves.put(IFEstimatorToCurveElement.NAME_RTT, ce3);

        String [] toolTipTextCaption4 = {"RTT Shift: ","STD: ","MIN: ","MAX: "};
        CurveElement ce4 = new CurveElement(
                    IFEstimatorToCurveElement.NAME_SHIFT,
                    CurveElement.BASIC_PLUS_BOX_PLUS_EXTREMES_4ARG,
                    "Variation of RTT on abnormal landmarks [ms]",
                    toolTipTextCaption4);
        //ce4.setForcedMinimum(-2000.0f);
        //ce4.setForcedMaximum(+2000.0f);
        ce4.setBaseReferenceToPaint(0.0f);
        curves.put(IFEstimatorToCurveElement.NAME_SHIFT,ce4);

        return curves;
    }

    private static void addNewLandmarkCurveElement(Landmark l, HashMap<String, CurveElement> curves) throws Exception{
        String [] toolTipTextCaption5 = {l.toString() + "RTT [ms]: "};
        CurveElement ce5 = new CurveElement(
                    IFEstimatorToCurveElement.NAME_RTT_LANDMARK+l.toString(),
                    CurveElement.BASIC_1ARG,
                    "RTT on " + l.getDescriptiveName(),
                    toolTipTextCaption5);
        ce5.setForcedMinimum(0.0f);
        //ce5.setForcedMaximum(2000.0f);
        ce5.setBaseReferenceToPaint(0.0f);
        curves.put(IFEstimatorToCurveElement.NAME_RTT_LANDMARK+l.toString(), ce5);
    }

    public static void addFlowElementToCurveElement(FlowElement fe, HashMap<String, CurveElement> ces) throws Exception{
        CurveElement ce;
        /* IFE */
        ce = ces.get(NAME_IFE);
        ce.addElement(0,fe.get(PipDefs.FE_IFE_THIS_CAMPAIGN));
        ce.addElement(1,fe.get(PipDefs.FE_IFE_CONFIDENCE_INTERVAL_THIS_CAMPAIGN));
        ce.addElement(2,fe.get(PipDefs.FE_AVG_UNREACHABLES));

        /* TIME */
        ce = ces.get(NAME_TIME);
        ce.addElement(0,fe.get(PipDefs.FE_CAMPAIGN_TIMESTAMP));
        /* RTT */
        ce = ces.get(NAME_RTT);
        ce.addElement(0,fe.get(PipDefs.FE_AVG_RTT_THIS_CAMPAIGN));
        ce.addElement(1,fe.get(PipDefs.FE_STD_RTT_THIS_CAMPAIGN));
        /* SHIFT */
        ce = ces.get(NAME_SHIFT);
        ce.addElement(0,fe.get(PipDefs.FE_AVG_SHIFT_THIS_CAMPAIGN));
        ce.addElement(1,fe.get(PipDefs.FE_STD_SHIFT_THIS_CAMPAIGN));
        ce.addElement(2,fe.get(PipDefs.FE_MIN_SHIFT_THIS_CAMPAIGN));
        ce.addElement(3,fe.get(PipDefs.FE_MAX_SHIFT_THIS_CAMPAIGN));
        /* RTTL */
        HashMap <Landmark, Float> landmrtt = (HashMap<Landmark, Float>) fe.get(PipDefs.FE_LANDMARK_RTT_LIST);
        Set<Landmark> landmset = landmrtt.keySet();
        for(Landmark l: landmset){
            ce = ces.get(NAME_RTT_LANDMARK+l);
            if (ce==null){
                addNewLandmarkCurveElement(l, ces);
                ce = ces.get(NAME_RTT_LANDMARK+l);
            }
            ce.addElement(0,landmrtt.get(l));
        }

    }

}
