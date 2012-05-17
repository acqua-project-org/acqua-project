package plugins;

import exceptions.UnsupportedCommandException;
import java.util.ArrayList;

public interface Pipelineable {
    /* Data controlled pipeline assumed (data available trigger events). */    
    public void addAsSink(Pipelineable p);
    public void insertFlowElement(FlowElement fe, String signature) throws Exception;
    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException;

}
