
package org.inria.acqua.layers;

import java.awt.event.*;

import org.inria.acqua.forms.labelnotification.LabelNotifierEmitter;
import org.inria.acqua.forms.labelnotification.LabelNotifierReceptor;

/**
 * @author mjost
 */
public class EventsListener implements KeyListener {
    private LabelNotifierEmitter labelNotifierEmitter;
    private ExternalModule externalModule;

    public EventsListener(LabelNotifierReceptor labelNotifierReceptor,
            ExternalModule externalModule){
        labelNotifierEmitter = new LabelNotifierEmitter();
        labelNotifierEmitter.addLabelNotifierReceptor(labelNotifierReceptor);
        this.externalModule = externalModule;
    }

    public void keyTyped(KeyEvent e) {}


    
    public void keyPressed(KeyEvent e) {
        int paramA = 0, paramB = 0;
        if (externalModule.isRunning()==false){
            switch (e.getKeyCode()){
                case (KeyEvent.VK_SHIFT):
                    break;
                case (KeyEvent.VK_UP):
                    paramA=+1;
                    paramB=-1;
                    labelNotifierEmitter.showMessageInGUI(
                            "Zoom in", LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
                    break;
                case (KeyEvent.VK_DOWN):
                    paramA=-1;
                    paramB=+1;
                    labelNotifierEmitter.showMessageInGUI(
                            "Zoom out", LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
                    break;
                case (KeyEvent.VK_LEFT):
                    paramA=-1;
                    paramB=-1;
                    labelNotifierEmitter.showMessageInGUI(
                            "Move left", LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
                    break;
                case (KeyEvent.VK_RIGHT):
                    paramA=+1;
                    paramB=+1;
                    labelNotifierEmitter.showMessageInGUI(
                            "Move right", LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
                    break;
                case (KeyEvent.VK_PAGE_DOWN):
                    //desplazarTodo(10); break;
                    break;
                case (KeyEvent.VK_PAGE_UP):
                    //desplazarTodo(-10); break;
                    break;
            }
            try{
                externalModule.changeContentRange(paramA,paramB);
            }catch(Exception ex){
                labelNotifierEmitter.showMessageInGUI(
                            ex.getMessage(), LabelNotifierReceptor.SERIOUSNESS_NOTIFICATION);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode()==e.VK_SHIFT) {
            //shift_presionado = false;
        }
    }
}

