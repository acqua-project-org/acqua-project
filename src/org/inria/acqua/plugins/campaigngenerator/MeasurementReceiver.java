package org.inria.acqua.plugins.campaigngenerator;

import org.inria.acqua.misc.Timestamp;

public interface MeasurementReceiver {
    public void insertMeasurement(int campaignID, int id, Timestamp[][] pings) throws Exception;
}
