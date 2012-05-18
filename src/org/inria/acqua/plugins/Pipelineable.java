package org.inria.acqua.plugins;

import java.util.ArrayList;

import org.inria.acqua.exceptions.UnsupportedCommandException;

public interface Pipelineable {
    /* Data controlled pipeline assumed (data available trigger events). */    
    public void addAsSink(Pipelineable p);
    public void insertFlowElement(FlowElement fe, String signature) throws Exception;
    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException;

}
