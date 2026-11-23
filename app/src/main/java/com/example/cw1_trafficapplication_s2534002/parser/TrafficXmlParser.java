package com.example.cw1_trafficapplication_s2534002.parser;

import com.example.cw1_trafficapplication_s2534002.model.Situation;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class TrafficXmlParser {

    public static ArrayList<Situation> parse(InputStream inputStream)
            throws XmlPullParserException, IOException {

        ArrayList<Situation> situations = new ArrayList<>();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        Situation currentSituation = null;

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {

                String tagName = parser.getName();

                if (tagName.equals("situationRecord")) {

                    currentSituation = new Situation();

                    currentSituation.setId(
                            parser.getAttributeValue(null, "id")
                    );

                    currentSituation.setType(
                            parser.getAttributeValue(
                                    "http://www.w3.org/2001/XMLSchema-instance",
                                    "type"
                            )
                    );
                } else if (currentSituation != null) {

                    switch (tagName) {

                        case "overallStartTime":
                            currentSituation.setStartTime(parser.nextText());
                            break;

                        case "overallEndTime":
                            currentSituation.setEndTime(parser.nextText());
                            break;

                        case "tpegDirection":
                            currentSituation.setDirection(parser.nextText());
                            break;

                        case "latitude":
                            if (currentSituation.getLatitude() == null) {
                                currentSituation.setLatitude(
                                        Double.valueOf(parser.nextText())
                                );
                            }
                            break;

                        case "longitude":
                            if (currentSituation.getLongitude() == null) {
                                currentSituation.setLongitude(
                                        Double.valueOf(parser.nextText())
                                );
                            }
                            break;

                        case "impact":
                            // Impact contains nested XML elements, so don't use nextText()
                            break;

                        case "roadMaintenanceType":
                            currentSituation.setRoadManagementType(
                                    parser.nextText()
                            );
                            break;

                        case "abnormalTrafficType":
                            currentSituation.setAbnormalTrafficType(
                                    parser.nextText()
                            );
                            break;

                        case "roadOrCarriagewayOrLaneManagementType":
                            currentSituation.setRoadManagementType(
                                    parser.nextText()
                            );
                            break;

                        case "value":
                            currentSituation.setDescription(parser.nextText());
                            break;


                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {

                if (parser.getName().equals("situationRecord")
                        && currentSituation != null) {

                    situations.add(currentSituation);
                    currentSituation = null;
                }
            }

            eventType = parser.next();

            if (eventType == XmlPullParser.TEXT) {
                String text = parser.getText().trim();

            }
        }

        return situations;
    }
}

