package org.inria.acqua.plugins.ifdumper;


import java.util.ArrayList;
import java.util.Locale;

import org.inria.acqua.exceptions.PipelineException;
import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.mjmisc.Misc;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.PipDefs;
import org.inria.acqua.plugins.Pipelineable;


public class IFDumper implements Pipelineable{
    private String ifeSummaryFilename;
    private ArrayList<Pipelineable> sinks;
    private long counter = 0;

    public IFDumper(String filename){
        this.ifeSummaryFilename = filename;
        sinks = new ArrayList<Pipelineable> ();
        try {
            Misc.deleteFile(ifeSummaryFilename);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        Float ife;
        Float rtt;
        Float shi;
        Float unr;
        
        if (PipDefs.SIGN_IFECALC.equals(signature)){
            ife = (Float) fe.get(PipDefs.FE_IFE_THIS_CAMPAIGN);
            unr = (Float) fe.get(PipDefs.FE_AVG_UNREACHABLES);
            rtt = (Float) fe.get(PipDefs.FE_AVG_RTT_THIS_CAMPAIGN);
            shi = (Float) fe.get(PipDefs.FE_AVG_SHIFT_THIS_CAMPAIGN);


            String str = String.format(Locale.ENGLISH, "%d\t%f\t%f\t%f\t%f\n", counter++, ife, unr, rtt, shi);

            Misc.appendToFile(ifeSummaryFilename, str);
            

        }else{
            throw new PipelineException("Signature not supported: '" + signature + "'.");
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
}

