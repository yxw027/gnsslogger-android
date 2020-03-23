package com.example.gnssloggerbutterflying.RinexFilefragments.Constellations;

import android.location.GnssClock;
import android.location.GnssMeasurementsEvent;

import com.example.gnssloggerbutterflying.RinexFilefragments.GNSSConstants;

import static java.lang.Math.max;

public class GpsIonoFreeConstellation extends GpsConstellation {

    private GpsConstellation gpsL1Constellation = new GpsConstellation();
    private GpsL5Constellation gpsL5Constellation = new GpsL5Constellation();
//    private GpsL1Constellation gpsIonoFreeConstellation = new GpsL1Constellation();

    //    private static final String NAME = "GPS<sub><small><small>IF</small></small></sub>";
    private static final String NAME = "GPS IF";

   // private Time timeRefMsec;


    @Override
    public void updateMeasurements(GnssMeasurementsEvent event) {
        synchronized (this) {

            //timeRefMsec = new Time(System.currentTimeMillis());

            gpsL1Constellation.updateMeasurements(event);
            gpsL5Constellation.updateMeasurements(event);
            GnssClock gnssClock=event.getClock();

            observedSatellites.clear();

            for(SatelliteParameters satelliteL1 : gpsL1Constellation.getSatellites()) {

                SatelliteParameters satelliteL5 = null;

                for(SatelliteParameters satelliteParameters: gpsL5Constellation.getSatellites()){
                    if(satelliteParameters.getSatId() == satelliteL1.getSatId()) {
                        satelliteL5 = satelliteParameters;
                        break;
                    }
                }

                if(satelliteL5 != null) {
                    // Get the code based pseudoranges on the E1 and E5a frequencies
                    double pseudorangeL1    =  satelliteL1.getPseudorange();
                    double pseudorangeL5   =  satelliteL5.getPseudorange();

                    // Form the ionosphere-free (IF) combination
                    double pseudorangeIF    = (Math.pow(GNSSConstants.FL1,2) * pseudorangeL1 - Math.pow(GNSSConstants.FL5,2) * pseudorangeL5)/(Math.pow(GNSSConstants.FL1,2)- Math.pow(GNSSConstants.FL5,2));

                    SatelliteParameters newSatellite = new SatelliteParameters(new GpsTime(gnssClock),
                            satelliteL1.getSatId(),
                            new Pseudorange(pseudorangeIF, 0.0));

//                    newSatellite.setUniqueSatId("G"+satelliteL1.getSatId()+"<sub><small><small>IF</small></small></sub>");
                    newSatellite.setUniqueSatId("G"+satelliteL1.getSatId()+"_IF");

                    //todo: assign properly
                    newSatellite.setSignalStrength(
                            (satelliteL1.getSignalStrength() + satelliteL5.getSignalStrength())/2);

                    newSatellite.setConstellationType(satelliteL1.getConstellationType());

                    observedSatellites.add(newSatellite);
                }
            }

            visibleButNotUsed = max(gpsL1Constellation.getVisibleConstellationSize(), gpsL5Constellation.getVisibleConstellationSize())-observedSatellites.size();

            tRxGPS = gpsL1Constellation.gettRxGPS();
            weekNumberNanos = gpsL1Constellation.getWeekNumber();
        }
    }



    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getConstellationId() {
        synchronized (this) {
            return Constellation.CONSTELLATION_GPS_IonoFree;
        }
    }

    public static void registerClass() {
        register(
                NAME,
                GpsIonoFreeConstellation.class);
    }
}