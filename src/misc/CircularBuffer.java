
package misc;

import java.util.ArrayList;

public class CircularBuffer <T> {
    private T[] buffer;
    private int p = 0;

    public CircularBuffer(int length){
        buffer =  (T[]) new Object[length];
        p = buffer.length - 1;
    }

    public synchronized void insert(T value){
        p = (p + 1) % buffer.length;
        buffer[p] = value;
    }

    public synchronized T get(int index){
        int i = (p - index + buffer.length) % buffer.length;
        return buffer[i];
    }

    public synchronized ArrayList<T> getNewArrayList(){
        ArrayList<T> ret = new ArrayList<T>(buffer.length);
        for(int i=0;i<buffer.length;i++){
            ret.add(i, this.get(i));
        }
        return ret;
    }
    public synchronized int getSize(){
        return buffer.length;
    }

}
