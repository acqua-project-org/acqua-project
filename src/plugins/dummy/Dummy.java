package plugins.dummy;

import exceptions.UnsupportedCommandException;
import java.util.ArrayList;
import plugins.FlowElement;
import plugins.Pipelineable;

/**
 * Pipeline element: Pipe.
 * Input:
 * - doesn't matter
 * Output:
 * - the same input
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
