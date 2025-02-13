/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader;

import static bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.MnsysDBLoader.LOGGER;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.Buoyage;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.view.constants.BUOYAGE;
import bzh.terrevirtuelle.navisu.topology.TopologyServices;
import bzh.terrevirtuelle.navisu.util.Pair;
import gov.nasa.worldwind.geom.LatLon;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author serge
 */
public class BuoyageDBLoader
        extends ResultSetDBLoader {

    protected final String BUOYAGE_PATH = "bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo";
    protected Buoyage buoyage;
    protected Class claz;
    protected TopologyServices topologyServices;
    protected Map<Pair<Double, Double>, String> topMarkMap;
    protected String marsys;

    public BuoyageDBLoader(TopologyServices topologyServices,
            Connection connection, String acronym,
            Map<Pair<Double, Double>, String> topMarkMap,
            String marsys) {
        super(connection, acronym);
        this.topologyServices = topologyServices;
        this.connection = connection;
        this.acronym = acronym;
        this.topMarkMap = topMarkMap;
        this.marsys = marsys;
        String className = BUOYAGE.ATT.get(acronym);
        try {
            claz = Class.forName(BUOYAGE_PATH + "." + className);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(BuoyageDBLoader.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
        String urlDB = null;
        try {
            urlDB = connection.getMetaData().getURL();
        } catch (SQLException ex) {
            Logger.getLogger(BuoyageDBLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] urlTab = urlDB.split("/");
        dbName = urlTab[urlTab.length - 1];
        //  System.out.println("dbName : " + dbName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Buoyage> retrieveObjectsIn(double latMin, double lonMin, double latMax, double lonMax) {
        List<Buoyage> buoyages = new ArrayList<>();
        String geom;
        resultSet = retrieveResultSetIn(latMin, lonMin, latMax, lonMax);
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    geom = resultSet.getString("geom");
                    if ((geom.contains("MULTIPOINT") || geom.contains("POINT")) && !geom.contains("EMPTY")) {
                        try {
                            buoyage = (Buoyage) claz.newInstance();
                        } catch (InstantiationException | IllegalAccessException ex) {
                            Logger.getLogger(BuoyageDBLoader.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                        }
                        buoyage.setGeom(geom);
                        LatLon latLon = topologyServices.wktMultiPointToWwjLatLon(geom);
                        double lat = latLon.getLatitude().getDegrees();
                        double lon = latLon.getLongitude().getDegrees();
                        buoyage.setLatitude(lat);
                        buoyage.setLongitude(lon);

                        buoyage.setMarsys(marsys);
                        String topMark;
                        if (topMarkMap != null) {
                            topMark = topMarkMap.get(new Pair(lat, lon));
                            if (topMark == null) {
                                topMark = "0";
                            }
                        } else {
                            topMark = "0";
                        }
                        buoyage.setTopMark(topMark);

                        String tmp = resultSet.getString("objnam");
                        String name = "";
                        if (tmp != null) {
                            name = tmp;
                        }
                        buoyage.setObjectName(name);

                        tmp = resultSet.getString(4);
                        String shp = "0";
                        if (tmp != null) {
                            shp = tmp;
                        }
                        buoyage.setShape(shp);
                        tmp = resultSet.getString("colour");
                        String col = "0";
                        if (tmp != null) {
                            col = tmp;
                        }
                        buoyage.setColour(col);
                        String colPat = "0";
                        try {
                            tmp = resultSet.getString("colpat");
                        } catch (SQLException ex) {

                        }
                        if (tmp != null) {
                            colPat = tmp;
                        }
                        if (dbName.equals(SPECIAL_DB_NAME)) {
                            buoyage.setColourPattern("0");
                        } else {
                            buoyage.setColourPattern(colPat);
                        }
                        String cat = "0";
                        try {
                            cat = resultSet.getString(7);
                            if (cat == null) {
                                cat = "0";
                            }
                        } catch (SQLException ex) {

                        }
                        buoyage.setCategoryOfMark(cat);

                        if (acronym.equals("DAYMAR")) {
                            String natcon = "0";
                            try {
                                natcon = resultSet.getString("natcon");
                                if (natcon == null) {
                                    natcon = "0";
                                }
                            } catch (SQLException ex) {

                            }
                            buoyage.setNatureOfConstruction(natcon);
                        }
                        buoyages.add(buoyage);
                    }
                }

            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        }
        return buoyages;
    }
}
