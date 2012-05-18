package org.inria.acqua.mjmisc;

public class ProcessOutput {
    private String outputSTDOUT;
    private int returnValue;
    private String outputSTDERR;

    public ProcessOutput(String out, String err, int ret){
        this.outputSTDERR = err;
        this.outputSTDOUT = out;
        this.returnValue = ret;
    }

    public String getOutputSTDERR() {
        return outputSTDERR;
    }

    public String getOutputSTDOUT() {
        return outputSTDOUT;
    }

    public int getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString(){
        return "*** STDOUT ***\n" + outputSTDOUT + "\n" +
                "*** STDERR ***\n" + outputSTDERR + "\n" +
                "*** RETURN ***\n" + returnValue + "\n";
    }

}
