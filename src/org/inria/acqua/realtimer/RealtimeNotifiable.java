package org.inria.acqua.realtimer;

/**
 * @author mjost
 */
public interface RealtimeNotifiable {
    public boolean executeAndCheckIfStops() throws Exception;
}
