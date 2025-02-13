/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.bathymetry.db.impl;

import bzh.terrevirtuelle.navisu.app.drivers.databasedriver.DatabaseDriver;
import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.app.guiagent.geoview.GeoViewServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layers.LayersManagerServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layertree.LayerTreeServices;
import bzh.terrevirtuelle.navisu.bathymetry.controller.eventsProducer.BathymetryEventProducerServices;
import bzh.terrevirtuelle.navisu.bathymetry.db.BathymetryDB;
import bzh.terrevirtuelle.navisu.bathymetry.db.BathymetryDBServices;
import bzh.terrevirtuelle.navisu.bathymetry.db.impl.controller.BathymetryDBController;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.database.relational.DatabaseServices;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3Df;
import bzh.terrevirtuelle.navisu.domain.geometry.SolidGeo;
import bzh.terrevirtuelle.navisu.geometry.delaunay.triangulation.Triangle_dt;
import bzh.terrevirtuelle.navisu.topology.TopologyServices;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.capcaval.c3.component.ComponentState;
import org.capcaval.c3.component.annotation.UsedService;

/**
 * @date 13 mars 2015
 * @author Serge Morvan
 */
public class BathymetryDBImpl
        implements BathymetryDB, BathymetryDBServices, DatabaseDriver, ComponentState {

    @UsedService
    GuiAgentServices guiAgentServices;
    @UsedService
    GeoViewServices geoViewServices;
    @UsedService
    LayersManagerServices layersManagerServices;
    @UsedService
    LayerTreeServices layerTreeServices;
    @UsedService
    DatabaseServices databaseServices;
    @UsedService
    BathymetryEventProducerServices bathymetryEventProducerServices;
    @UsedService
    TopologyServices topologyServices;

    protected static final Logger LOGGER = Logger.getLogger(BathymetryDBImpl.class.getName());
    protected final String DB_NAME = "BathyShomDB";
    protected final String LAYER_NAME = "BathyShom";
    protected final double LIMIT = 100.0;
    protected String dataFileName;
    protected Connection connection;
    protected PreparedStatement preparedStatement;
    protected Statement statement;
    protected List<Point3DGeo> points3d;
    protected WorldWindow wwd;
    protected RenderableLayer layer;
    protected static final String GROUP = "Bathymetry data";
    double longitude;
    double latitude;
    NumberFormat nf4 = new DecimalFormat("0.0000");
    NumberFormat nf1 = new DecimalFormat("0.0");
    int i = 0;
    BathymetryDBController controller;

    @SuppressWarnings("unchecked")
    @Override
    public void componentInitiated() {
        points3d = new ArrayList<>();
        wwd = GeoWorldWindViewImpl.getWW();
        layer = layersManagerServices.getLayer(GROUP, LAYER_NAME);
        //   layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(layer));
        controller = BathymetryDBController.getInstance(this,
                databaseServices, guiAgentServices,
                bathymetryEventProducerServices, topologyServices,
                LIMIT, layer);

    }

    @Override
    public void componentStarted() {
    }

    @Override
    public void componentStopped() {
    }

    @Override
    public Connection connect(String dbName, String hostName, String protocol, String port,
            String driverName, String userName, String passwd,
            String dataFileName) {
        this.dataFileName = dataFileName;
        this.connection = controller.connect(dbName, "bathy", hostName, protocol, port, driverName, userName, passwd, dataFileName);
        return connection;
    }

    @Override
    public Connection connect(String dbName, String hostName, String protocol, String port,
            String driverName, String userName, String passwd) {
        this.connection = controller.connect(dbName, hostName, protocol, port, driverName, userName, passwd);
        return connection;
    }

    @Override
    public void create(String filename, String table) {
        controller.create(filename, table);
    }

    @Override
    public void insert(String filename, String table) {
        controller.insertData(table, filename);
    }

    @Override
    public void create(String filename, String table, String query) {
        controller.create(filename, table, query);
    }

    @Override
    public void insert(String filename, String table, String query) {
        controller.insert(table, filename, query);
    }

    @Override
    public void execute(String query) {
        controller.execute(query);
    }

    @Override
    public void insert(String table, List<SolidGeo> solids) {
        controller.insert(table, solids);
    }

    @Override
    public List<Point3Df> readFromFile(String filename) {
        return controller.readFromFile(filename);
    }

    @Override
    public void insert(List<Point3Df> points) {
        controller.insertData(points);
    }

    @Override
    public void createIndex(String table) {
        controller.createIndex(table);
    }

    @Override
    public void createIndex(String table, String index) {
        controller.createIndex(table, index);
    }

    @Override
    public List<Point3DGeo> retrieveAll() {
        return controller.retrieveAll();
    }

    @Override
    public List<Point3DGeo> retrieveAround(double lat, double lon) {
        return controller.retrieveAround(lat, lon, LIMIT);
    }

    @Override
    public List<Point3DGeo> retrieveAround(double lat, double lon, double limit) {
        return controller.retrieveAround(lat, lon, limit);
    }

    @Override
    public List<Point3DGeo> retrieveIn(String table, double latMin, double lonMin, double latMax, double lonMax) {
        return controller.retrieveIn(table, latMin, lonMin, latMax, lonMax);
    }

    @Override
    public List<Point3DGeo> retrieveIn(Connection connection, String table, double latMin, double lonMin, double latMax, double lonMax) {
        return controller.retrieveIn(connection, table, latMin, lonMin, latMax, lonMax);
    }

    @Override
    public List<SolidGeo> retrieveInSolid(String table, double latMin, double lonMin, double latMax, double lonMax) {
        return controller.retrieveInSolid(table, latMin, lonMin, latMax, lonMax);
    }

    @Override
    public List<SolidGeo> retrieveInSolid(Connection connection, String table, double latMin, double lonMin, double latMax, double lonMax) {
        return controller.retrieveInSolid(connection, table, latMin, lonMin, latMax, lonMax);
    }

    @Override
    public Point3DGeo[][] mergeData(Point3DGeo[][] orgData, List<Triangle_dt> triangles) {
        return controller.mergeData(orgData, triangles);
    }

    @Override
    public Point3DGeo[][] mergeData(Point3DGeo[][] orgData, List<Triangle_dt> triangles, double depth) {
        return controller.mergeData(orgData, triangles, depth);
    }

    @Override
    public void close() {
        databaseServices.close();
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean canOpen(String dbName) {
        return dbName.trim().equalsIgnoreCase(DB_NAME);
    }

    @Override
    public DatabaseDriver getDriver() {
        return this;
    }

    @Override
    public void writePointList(List<Point3DGeo> points, Path pathname, boolean latLon) {
        controller.writePointList(points, pathname, latLon);
    }

}
