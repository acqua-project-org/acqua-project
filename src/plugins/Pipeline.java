package plugins;

import exceptions.UnsupportedCommandException;
import forms.Commandable;
import java.util.ArrayList;
import java.util.HashMap;
import mjmisc.Misc;



public class Pipeline implements Commandable{
    private HashMap<String, Pipelineable> elements;
    private Pipelineable firstElement;

    public Pipeline(){
        elements = new HashMap<String, Pipelineable>();
        //new CommandLineWindow("Pipeline: " + this.toString(), this);
    }

    public void insertFlowElement(FlowElement fe, String signature) throws Exception{
        firstElement.insertFlowElement(fe, signature);
    }

    public void remove(String p){
        elements.remove(p);
    }

    public void addAsFirst(String name, Pipelineable p){
        add(name, p, true);
    }


    public void add(String name, Pipelineable p){
        add(name, p, false);
    }
    
    public void add(String name, Pipelineable p, boolean first){
        if (first == true){
            this.firstElement = p;
        }
        this.addElement(name, p);
    }

    public Pipelineable get(String name){
        return elements.get(name);
    }
    
    private void addElement(String name, Pipelineable p){
        if (elements.values().contains(p)==false){
            elements.put(name, p);
        }
    }

    public ArrayList<Object> putCommand(String command, ArrayList<Object> args) throws Exception{
        ArrayList<Object> bigret = new ArrayList<Object>();
        for(Pipelineable p: elements.values()){
            try{
                ArrayList<Object> ret = p.sendCommand(command, args);
                if (ret!=null){
                    bigret.addAll(ret);
                }
            }catch(UnsupportedCommandException e){}
        }
        return bigret;
    }

    public String sendCommand(String cmd) {
        String retStr = "";
        ArrayList<Object> retArr;
        try{
            retArr = this.putCommand(cmd, null);
            for(Object o:retArr){
                retStr = retStr + Misc.printSpecified(o) + "\n";
            }
        }catch(Exception e){
            retStr = "Exception: " + e.getMessage();
            e.printStackTrace();
        }
        return retStr;
    }

    

}
