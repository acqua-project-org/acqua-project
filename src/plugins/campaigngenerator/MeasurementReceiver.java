package plugins.campaigngenerator;

import misc.Timestamp;

public interface MeasurementReceiver {
    public void insertMeasurement(int campaignID, int id, Timestamp[][] pings) throws Exception;
}
