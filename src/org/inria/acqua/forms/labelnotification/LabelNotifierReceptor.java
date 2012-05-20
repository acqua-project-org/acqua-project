
package org.inria.acqua.forms.labelnotification;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JLabel;

/**
 * @author mjost
 */
public class LabelNotifierReceptor extends Thread{
    public static final int SERIOUSNESS_ERROR = 12;
    public static final int SERIOUSNESS_ATTENTION = 8;
    public static final int SERIOUSNESS_NOTIFICATION = 5;
    public static final String NO_MESSAGE = "<no message>";

    private JLabel reportLabel;
    private Font standardFont;
    private ArrayList<Notification> notifications;

    public LabelNotifierReceptor(JLabel label){
        if (label==null){
            System.err.println("ERROR: Null jlabel given as parameter.");
        }
        notifications = new ArrayList<Notification>(); 
        reportLabel = label;
        label.setVisible(true);
        standardFont = label.getFont();
        label.setText(NO_MESSAGE);
        this.start();
    }

    @Override
    public void run(){
        try {
            while(true){
                removeOldNotifications();
                Notification notification = getMostPrioritaryNotification();
                if (notification!=null){
                    //if (notification.getPriority()>=SERIOUSNESS_ATTENTION){
                    //    reportLabel.setFont(standardFont.deriveFont(Font.BOLD));
                    //    reportLabel.setForeground(Color.red);
                    //}else{
                        reportLabel.setFont(standardFont);
                        reportLabel.setForeground(Color.black);
                    //}
                    this.reportLabel.setText(notification.getMessage());
                }else{
                    reportLabel.setText(NO_MESSAGE);
                }
                Thread.sleep(200);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void removeOldNotifications(){
        ArrayList<Notification> old = new ArrayList<Notification>();
        for(Notification n:notifications){
            if (now() > n.getEndTimeMS()){
                old.add(n);
            }
        }
        notifications.removeAll(old);
    }

    private synchronized Notification getMostPrioritaryNotification(){
        int pri_max = -1; 
        Notification not_max = null;
        for(Notification n:notifications){
            if (n.getPriority() >= pri_max){
                pri_max = n.getPriority();
                not_max = n;
            }
        }
        return not_max;
    }

    public synchronized void showErrorInGUI(String text, int duration_seconds){
        this.notifications.add(new Notification(text, duration_seconds, duration_seconds));
    }

   private long now(){
        Date d = Calendar.getInstance().getTime();
        return d.getTime();
   }    
}


class Notification{
    private String message;
    private int priority;
    private long endTimeMS;

    public Notification(String message, int priority, int duration_seconds){
        this.message = message;
        this.priority = priority;
        Date d = Calendar.getInstance().getTime();
        this.endTimeMS = d.getTime() + duration_seconds*1000;
    }

    public String getMessage() {
        return message;
    }

    public int getPriority() {
        return priority;
    }

    public long getEndTimeMS() {
        return endTimeMS;
    }

    public String toString(){
        return message;
    }
}