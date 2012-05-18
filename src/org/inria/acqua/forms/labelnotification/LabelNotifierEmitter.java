package org.inria.acqua.forms.labelnotification;

import java.util.ArrayList;

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
        //System.out.println("Trying to show " + text);
        for(LabelNotifierReceptor r:receptors){
            //System.out.println("\tin " + r);
            r.showErrorInGUI(text, duration_seconds);
        }
    }

}
