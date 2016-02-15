/*
 * Copyright (c) 2016. Daniel Kressner
 *
 * This file to under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kressnerd.jetpdf2csv;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Flight {
    private static Properties iata2icao;
    private String flightDate = "";
    private String tailNumber = "";
    private String acType = "";
    private String pIC = "";
    private String origin = "";
    private String destination = "";
    private String departTime = "";
    private String arrivalTime = "";
    private String flightTimeHours = "0";
    private String flightTimeMinutes = "0";
    private String landings = "1";
    private String remark = "";
    private boolean isFlight = false;

    public Flight() {

    }

    public static void iata2icaoInit() {
        iata2icao = new Properties();
        try {
            iata2icao.load(new FileInputStream("iata2icao.txt"));
            // crew.load(new FileInputStream("crew.txt"));
        } catch (FileNotFoundException fnf) {
            System.out.print("Missing iata2icao.txt file.");
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    public static int flightTimeInMinutes(String flightTime) {
        return Integer.parseInt(flightTime.substring(0, 2)) * 60 + Integer.parseInt(flightTime.substring(3, 5));

    }

    public static String flightTimeInHoursMinutes(int flightTimeMinutes) {
        int hours = (int) flightTimeMinutes / 60;
        int minutes = flightTimeMinutes % 60;
        String minutesOut;
        if (minutes >= 10) {
            minutesOut = Integer.toString(minutes);
        } else {
            minutesOut = "0" + Integer.toString(minutes);
        }
        return Integer.toString(hours) + ":" + minutesOut;
    }

    public static String calcFlightTime(String start, String landing) {
        int startHour = Integer.parseInt(start.substring(0, 2));
        int startMinutes = Integer.parseInt(start.substring(3, 5));
        int landingHour = Integer.parseInt(landing.substring(0, 2));
        int landingMinutes = Integer.parseInt(landing.substring(3, 5));
        int flightHours = 0;
        int flightMintues = 0;
        int flightTime = 0;

        if (startHour <= landingHour) {
            flightTime = (landingHour * 60 + landingMinutes) - (startHour * 60 + startMinutes);
        } else {
            flightTime = ((landingHour + 24) * 60 + landingMinutes) - (startHour * 60 + startMinutes);
        }
        return flightTimeInHoursMinutes(flightTime);
    }

    public void setDepartDate(String departDate) {
        this.flightDate = departDate;
    }

    public void setDepartTime(String departTime) {
        this.departTime = departTime;
    }

    public void setFlightDepartDateTime(Date departDate) {
        SimpleDateFormat dfOutDate = new SimpleDateFormat("dd.MM.yyyy");
        this.flightDate = dfOutDate.format(departDate);

        SimpleDateFormat dfOutTime = new SimpleDateFormat("HH:mm");
        this.departTime = dfOutTime.format(departDate);
    }

    public void setFlightArrivalTime(Date arrivalTime) {
        SimpleDateFormat dfOutTime = new SimpleDateFormat("HH:mm");
        this.arrivalTime = dfOutTime.format(arrivalTime);
    }

    public void setFlightArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setFlightTime(String flightTime) {
        this.flightTimeHours = flightTime.substring(0, 2);
        this.flightTimeMinutes = flightTime.substring(3, 5);
    }

    public void setIsFlight(boolean isFlight) {
        this.isFlight = isFlight;
    }

    public boolean isSim() {
        if (this.remark.contains("SIM")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFlight() {
        return this.isFlight;
    }

    public void setOrigin(String or) {
        String originICAO = iata2icao.getProperty(or);
        if (originICAO != null) {
            this.origin = originICAO;
        } else {
            this.origin = or;
        }
    }

    public void setDestination(String dest) {
        String destinationICAO = iata2icao.getProperty(dest);
        if (destinationICAO != null) {
            this.destination = destinationICAO;
        } else {
            this.destination = dest;
        }
    }

    public void setFlightLeg(String leg) {
        String originIATA = leg.substring(8, 11);
        String destinationIATA = leg.substring(12, 15);
        String originICAO = iata2icao.getProperty(originIATA);
        String destinationICAO = iata2icao.getProperty(destinationIATA);

        if (originICAO != null) {
            this.origin = originICAO;
        } else {
            this.origin = originIATA;
        }
        if (destinationICAO != null) {
            this.destination = destinationICAO;
        } else {
            this.destination = destinationIATA;
        }
    }

    public void setACType(String acType) {
        if (acType.substring(0, 3).equals("100")) {
            this.acType = "F100";
            this.tailNumber = "OELVA";
        } else if (acType.substring(0, 3).equals("F70")) {
            this.acType = "F70";
            this.tailNumber = "OELFG";
        } else {
            this.acType = acType;
        }
    }

    public void setTailNumber(String tailNumber) {
        this.tailNumber = tailNumber;
    }

    public void setPIC(String pIC) {
        this.pIC = pIC;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean checkFlightOperation(String fo) {
        if (fo.equals(flightTimeHours + ":" + flightTimeMinutes)) {
            return true;
        } else {
            return false;
        }
    }

    public int getFlightTimeInMinutes() {
        return Integer.parseInt(flightTimeHours) * 60 + Integer.parseInt(flightTimeMinutes);
    }

    public String toString() {
        return "" + ";" +
                flightDate + ";" +
                "\"" + tailNumber + "\";" +
                "\"" + acType + "\";" +
                "\"" + pIC + "\";"
                + "\"\";" +
                "\"" + origin + "\";" +
                "\"" + destination + "\";" +
                departTime + ";" +
                arrivalTime + ";" +
                flightTimeHours + ";" +
                flightTimeMinutes + ";" +
                landings +
                ";\"ATPL\";" +
                "\"" + remark +
                "\";\"\";\"\";\"ATPL\";" +
                "\"" + departTime +
                "\";\"" + arrivalTime + "\";\"\";\"\";";
    }

}
