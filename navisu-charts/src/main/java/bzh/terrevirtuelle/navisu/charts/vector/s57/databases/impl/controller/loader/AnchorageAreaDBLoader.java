/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader;

import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.Geo;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.AnchorageArea;
import bzh.terrevirtuelle.navisu.topology.TopologyServices;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author serge
 */
public class AnchorageAreaDBLoader
        extends ResultSetDBLoader {

    public AnchorageAreaDBLoader(TopologyServices topologyServices,
            Connection connection) {
        super(connection, "ACHARE");
    }

    @Override
    public List<? extends Geo> retrieveObjectsIn(double latMin, double lonMin, double latMax, double lonMax) {
        objects = new ArrayList<>();
        String geom = "";

        resultSet = retrieveResultSetIn(latMin, lonMin, latMax, lonMax);
        AnchorageArea object;
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    object = new AnchorageArea();
                    geom = resultSet.getString("geom");
                    if (geom != null) {
                        object.setGeom(geom);
                        object.setCategoryOfAnchorage(resultSet.getString("catach"));
                        object.setObjectName(resultSet.getString("objnam"));
                        object.setRestriction(resultSet.getString("restrn"));
                        object.setId(resultSet.getInt("rcid"));
                        object.getLabels().put("ACHARE", "AnchorageArea");
                        object.getLabels().put("catach", resultSet.getString("catach"));
                        object.getLabels().put("objnam", resultSet.getString("objnam"));
                        object.getLabels().put("restrn", resultSet.getString("restrn"));
                        objects.add(object);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(AnchorageAreaDBLoader.class.getName()).log(Level.SEVERE, ex.toString(), ex);
            }
        }
        return objects;
    }
}
