package org.inria.acqua.plugins;

import java.util.ArrayList;

import org.inria.acqua.exceptions.UnsupportedCommandException;

/**
 * Any element of the pipeline must implement this interface. 
 * @author mjost
 */
public interface Pipelineable {
    /* Pipeline with data-controlled mechanism is assumed (i.e. when data is available on the first element
     * of the pipeline, it basically pushes that data through the next elements. */    
    public void addAsSink(Pipelineable p); // When the pipeline element A processes the FlowElement, it pushes this FlowElement to B. Invoking A.addAsSink(B) is mandatory for this to happen. 
    public void insertFlowElement(FlowElement fe, String signature) throws Exception; // The signature tells what kind of element is pushing this data (to verify consistency of the pipeline's configuration).
    public ArrayList<Object> sendCommand(String command, ArrayList<Object> args) throws UnsupportedCommandException; // Some elements of the pipeline can receive commands (to change their configuration on the fly for instance). 

}
