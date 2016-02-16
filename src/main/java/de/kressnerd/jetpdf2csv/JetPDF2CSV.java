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
 *
 */

package de.kressnerd.jetpdf2csv;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class JetPDF2CSV {

    private static final String tHead = "Year Month Date Dep Block-Off Arr " +
            "Block-On Blocktime AcType AcRegId PiC CP RP FO I SIM";

    public static void main(String[] args) {
        PDDocument pdDoc = null;
        boolean isFlight = false;
        Flight.iata2icaoInit();
        List<Flight> flightList = new ArrayList<Flight>();

        try {
            pdDoc = PDDocument.load(new File(args[0]));

            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(pdDoc.getNumberOfPages());
            String parsedText = pdfStripper.getText(pdDoc);

            String[] pages = parsedText.split(tHead);
            for (int i = 0; i < pages.length; i++) {
                String page = pages[i].trim();
                int year = 4711;
                try {
                    year = Integer.parseInt(page.substring(0, 4));
                    // throw away year and month from first column
                    String page_n = page.substring(7, page.length()).trim();
                    String[] rows = page_n.split(System.getProperty("line.separator"));
                    for (int j = 0; j < rows.length; j++) {
                        Flight curFlight = new Flight();
                        isFlight = false;
                        String row = rows[j];
                        String[] cols = row.split(" ");

                        // check for flight entry: length 14 and first is date.
                        if (cols.length == 14 && cols[0].matches("[0123][\\d]\\.[01][0-9]\\.")) {
                            isFlight = true;
                            curFlight.setDepartDate(cols[0] + year);
                            curFlight.setOrigin(cols[1]);
                            curFlight.setDepartTime(cols[2]);
                            curFlight.setDestination(cols[3]);
                            curFlight.setFlightArrivalTime(cols[4]);
                            curFlight.setFlightTime(cols[5]);
                            curFlight.setACType(cols[6]);
                            curFlight.setTailNumber(cols[7]);
                            curFlight.setPIC(cols[12]);
                            if (curFlight.checkFlightOperation(cols[13]) == false) {
                                System.out.println("Block time is not equal flight op time for flight: " + curFlight);
                            }


                            for (int k = 14; k < cols.length; k++) {
                                System.out.println(cols[k]);
                            }
                            curFlight.setIsFlight(true);
                            flightList.add(curFlight);
                        } else if (cols.length == 21 && cols[0].matches("[01-3][\\d]\\.[01][012]\\.")) {
                            isFlight = true;
                            curFlight.setDepartDate(cols[0] + year);
                            curFlight.setDepartTime(cols[2]);
                            curFlight.setFlightArrivalTime(cols[6]);
                            curFlight.setFlightTime(cols[20]);
                            curFlight.setRemark("SIM");
                            for (int k = 21; k < cols.length; k++) {
                                System.out.println(cols[k]);
                            }
                            curFlight.setIsFlight(true);
                            flightList.add(curFlight);
                        } else if (row.contains("Summary")) {
                            int totalFlightTimeMinutes = 0;
                            int totalSimTimeMinutes = 0;
                            int anzfl = 0;
                            for (Flight fl : flightList) {
                                if (fl.isSim()) {
                                    totalSimTimeMinutes += fl.getFlightTimeInMinutes();
                                } else {
                                    totalFlightTimeMinutes += fl.getFlightTimeInMinutes();
                                }
                                anzfl += 1;
                            }
                            System.out.println();
                            System.out.println("Summary " + cols[0]);
                            System.out.println("-------");
                            System.out.println("Number of flights: " + anzfl);
                            System.out.println("Calc. flight hours: " +
                                    Flight.flightTimeInHoursMinutes(totalFlightTimeMinutes));
                            System.out.println("PDF flight hours: " + cols[4]);
                            if (totalSimTimeMinutes > 0 && cols.length > 4) {
                                System.out.println("Calc sim hours: " +
                                        Flight.flightTimeInHoursMinutes(totalSimTimeMinutes));
                                System.out.println("PDF im hours: " + cols[5]);
                            }
                        }
                        isFlight = false;
                    }
                } catch (NumberFormatException e) {
                    // do nothing, just next page
                }
            }

            PrintWriter csvOut = new PrintWriter(args[0] + ".csv");



            csvOut.println("Lfd-Nr.;Datum;Kennzeichen;Muster;PiC;Gast;Von;Nach;Startzeit;Landezeit;Stunden;Minuten;"+
                    "Landungen;Startart;Bemerkungen;Flugbedingung;Startart;Lizenztyp;block-off;block-on;counter-on;"+
                    "counter-off");
            int i = 1;
            for (Flight tmpFl : flightList) {
                csvOut.print(i);
                csvOut.println(tmpFl);
                i++;
            }

            csvOut.close();
            System.out.println();

        } catch (FileNotFoundException fnfe) {
            System.out.println("Please provide a pdf file:");
            System.out.println("# java JetPDF2CSV _yourfile.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
