package org.inria.acqua.plugins.dummy;

import java.util.ArrayList;

import org.inria.acqua.exceptions.UnsupportedCommandException;
import org.inria.acqua.plugins.FlowElement;
import org.inria.acqua.plugins.Pipelineable;


/**
 * Academic purposes. 
 * Pipeline element: Pipe.
 * Input:
 * - doesn't matter
 * Output:
 * - the same input
 * @author mjost
 */
public class Dummy implements Pipelineable{
    private ArrayList<Pipelineable> sinks;

    public Dummy(){
        sinks = new ArrayList<Pipelineable>();
    }
    public void addAsSink(Pipelineable p) {
        sinks.add(p);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception {
        for(Pipelineable p:sinks){
            p.insertFlowElement(fe, signature);
        }
    }

    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException {
        throw new UnsupportedCommandException("No command.");
    }

}
