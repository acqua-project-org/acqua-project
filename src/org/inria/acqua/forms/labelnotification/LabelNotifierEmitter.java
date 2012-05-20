package org.inria.acqua.forms.labelnotification;

import java.util.ArrayList;

/**
 * @author mjost
 */
public class LabelNotifierEmitter {
    private ArrayList<LabelNotifierReceptor> receptors;

    public LabelNotifierEmitter(){
        receptors = new ArrayList<LabelNotifierReceptor>();
    }

    public void addLabelNotifierReceptor(LabelNotifierReceptor e){
        if (e!=null){
            receptors.add(e);
        }
    }

    public ArrayList<LabelNotifierReceptor> getReceptors(){
        return receptors; 
    }

    public void showMessageInGUI(String text, int duration_seconds){
        for(LabelNotifierReceptor r:receptors){
            r.showErrorInGUI(text, duration_seconds);
        }
    }

}
