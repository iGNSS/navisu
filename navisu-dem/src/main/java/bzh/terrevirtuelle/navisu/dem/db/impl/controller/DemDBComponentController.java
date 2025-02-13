/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.dem.db.impl.controller;

import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.database.relational.DatabaseServices;
import bzh.terrevirtuelle.navisu.dem.db.impl.DemDBImpl;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3Df;
import bzh.terrevirtuelle.navisu.geometry.delaunay.triangulation.Point_dt;
import bzh.terrevirtuelle.navisu.geometry.delaunay.triangulation.Triangle_dt;
import com.vividsolutions.jts.geom.Geometry;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import org.postgis.PGgeometry;

/**
 *
 * @author serge
 */
public class DemDBComponentController {

    private static DemDBComponentController INSTANCE = null;
    DemDBImpl component;
    protected static final Logger LOGGER = Logger.getLogger(DemDBComponentController.class.getName());
    DatabaseServices databaseServices;
    GuiAgentServices guiAgentServices;
    protected WorldWindow wwd;
    boolean first = true;
    List<Point3DGeo> points;
    private Connection connection;
    private String dataFileName;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private List<Point3Df> points3df;
    private List<Point3DGeo> points3d;
    protected RenderableLayer layer;
    protected static final String GROUP = "Bathymetry data";
    double longitude;
    double latitude;
    NumberFormat nf4 = new DecimalFormat("0.0000");
    NumberFormat nf1 = new DecimalFormat("0.0");
    int i = 0;
    double MIN_DEPTH = -99.0;
    double distA;
    double distB;
    double distC;
    double distMin;
    Point_dt pMin;

    double maxElevation = -20.0;
    final double THRESHOLD = 0.0015;
    double tmp;
    Geometry concaveHull;
    protected Charset charset = Charset.forName("UTF-8");

    private DemDBComponentController(DemDBImpl component,
            DatabaseServices databaseServices,
            GuiAgentServices guiAgentServices,
            double limit, RenderableLayer layer) {
        this.component = component;
        this.databaseServices = databaseServices;
        this.guiAgentServices = guiAgentServices;

        this.layer = layer;
        wwd = GeoWorldWindViewImpl.getWW();

        wwd.addPositionListener((PositionEvent event) -> {
            Position pos = event.getPosition();
            try {
                if (pos != null && connection != null && !connection.isClosed() && pos.getAltitude() < 20.0) {
                    //  points = bathymetryDBImpl.retrieve(pos.getLatitude().getDegrees(), pos.getLongitude().getDegrees());
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        });
    }

    public static DemDBComponentController getInstance(DemDBImpl component,
            DatabaseServices databaseServices, GuiAgentServices guiAgentServices,
            double limit, RenderableLayer layer) {
        if (INSTANCE == null) {
            INSTANCE = new DemDBComponentController(component,
                    databaseServices, guiAgentServices,
                    limit, layer);
        }
        return INSTANCE;
    }

    public Connection connect(String dbName, String hostName, String protocol, String port,
            String driverName, String userName, String passwd,
            String dataFileName) {
        this.dataFileName = dataFileName;
        this.connection = databaseServices.connect(dbName, hostName, protocol, port, driverName, userName, passwd);
        System.out.println(dbName + " " + hostName + " " + protocol + " " + port + " " + driverName + " " + userName + " " + passwd);
        String sql = "INSERT INTO " + "bathy" + " (coord, elevation) "
                + "VALUES ( ST_SetSRID(ST_MakePoint(?, ?), 4326), ?);";
        try {
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }
        //  create(dataFileName);
        createIndex();
        return connection;
    }

    public Connection connect(String dbName, String hostName, String protocol, String port,
            String driverName, String userName, String passwd) {
        System.out.println(dbName + " " + hostName + " " + protocol + " " + port + " " + driverName + " " + userName + " " + passwd);
        this.connection = databaseServices.connect(dbName, hostName, protocol, port, driverName, userName, passwd);
        return connection;
    }

    public void create(String filename) {
        String sql = "INSERT INTO " + "bathy" + " (coord, elevation) "
                + "VALUES ( ST_SetSRID(ST_MakePoint(?, ?), 4326), ?);";
        try {
            preparedStatement = connection.prepareStatement(sql);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }
        guiAgentServices.getJobsManager().newJob("create", (progressHandle) -> {
            String query = "DROP TABLE IF EXISTS  bathy; "
                    + "CREATE TABLE bathy("
                    + "gid SERIAL PRIMARY KEY,"
                    + "coord GEOMETRY(POINT, 4326), "
                    + "elevation FLOAT); ";
            try {
                statement = connection.createStatement();
                statement.executeUpdate(query);
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }

            points3df = readFromFile(filename);
            insert(points3df);
            // createIndex();
        });
    }

    public List<Point3Df> readFromFile(String filename) {
        List<Point3Df> tmp = new ArrayList<>();
        try {
            tmp = Files.lines(new File(filename).toPath())
                    .map(line -> line.trim())
                    .map(line -> line.split(" "))
                    .map(tab -> new Point3Df(Float.parseFloat(tab[0]),
                    Float.parseFloat(tab[1]),
                    Float.parseFloat(tab[2])))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }
        return tmp;
    }

    public final void insert(List<Point3Df> points) {
        points.stream().forEach((p) -> {
            try {
                preparedStatement.setDouble(1, p.getLon());
                preparedStatement.setDouble(2, p.getLat());
                preparedStatement.setDouble(3, p.getElevation());
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        });
    }

    public final void createIndex() {
        guiAgentServices.getJobsManager().newJob("createIndex", (progressHandle) -> {
            try {
                connection.createStatement().executeUpdate("CREATE INDEX bathyindex ON bathy USING GIST (coord)");
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        });
    }

    public List<Point3DGeo> retrieveAll() {
        List<Point3DGeo> tmp1 = new ArrayList<>();
        //  guiAgentServices.getJobsManager().newJob("retrieveAll", (progressHandle) -> {
        PGgeometry geom;
        double depth;
        try {
            ResultSet r = connection.createStatement().executeQuery("SELECT  * FROM elevation");
            while (r.next()) {
                geom = (PGgeometry) r.getObject(2);
                depth = r.getFloat(3);
                if (depth >= MIN_DEPTH) {
                    Point3DGeo pt = new Point3DGeo(geom.getGeometry().getFirstPoint().getX(),
                            geom.getGeometry().getFirstPoint().getY(),
                            depth);
                    tmp1.add(pt);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }
        // });
        return tmp1;
    }

    public List<Point3DGeo> retrieveAll(Connection connect) {
        List<Point3DGeo> tmp1 = new ArrayList<>();
        //  guiAgentServices.getJobsManager().newJob("retrieveAll", (progressHandle) -> {
        PGgeometry geom;
        double depth;
        try {
            ResultSet r = connect.createStatement().executeQuery("SELECT  * FROM elevation");
            while (r.next()) {
                geom = (PGgeometry) r.getObject(2);
                depth = r.getFloat(3);
                if (depth >= MIN_DEPTH) {
                    Point3DGeo pt = new Point3DGeo(geom.getGeometry().getFirstPoint().getX(),
                            geom.getGeometry().getFirstPoint().getY(),
                            depth);
                    tmp1.add(pt);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }
        // });
        return tmp1;
    }

    public List<Point3DGeo> retrieveIn(String table, double latMin, double lonMin, double latMax, double lonMax) {
        List<Point3DGeo> tmp1 = new ArrayList<>();
        PGgeometry geom;
        double depth;
        ResultSet r;
        if (connection != null) {
            try {
                r = connection.createStatement().executeQuery(
                        "SELECT * "
                        + "FROM " + table + " "
                        + "WHERE coord @ ST_MakeEnvelope ("
                        + latMin + ", " + lonMin + ", "
                        + latMax + ", " + lonMax
                        + ", 4326) ");
                while (r.next()) {
                    geom = (PGgeometry) r.getObject(2);
                    depth = r.getFloat(3);
                   // System.out.println("depth : " + depth);
                    if (depth >= MIN_DEPTH) {
                        Point3DGeo pt = new Point3DGeo(geom.getGeometry().getFirstPoint().getX(),
                                geom.getGeometry().getFirstPoint().getY(),
                                depth);
                        tmp1.add(pt);
                    }
                }

            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        } else {
            alert();
        }
        return tmp1;
    }

    public List<Point3DGeo> retrieveIn(Connection connection, String table, double latMin, double lonMin, double latMax, double lonMax) {

        Connection tmp0 = this.connection;
        this.connection = connection;
        List<Point3DGeo> tmp1 = retrieveIn(table, latMin, lonMin, latMax, lonMax);
        this.connection = tmp0;
        return tmp1;
    }

    public final List<Point3DGeo> retrieveAround(double lat, double lon, double limit) {
        PGgeometry geom;
        ResultSet r0;
        ResultSet r1;
        points3d.clear();
        layer.removeAllRenderables();
        try {
            r0 = connection.createStatement().executeQuery(
                    "SELECT coord,elevation "
                    + "FROM bathy "
                    + "ORDER BY coord <-> ST_SetSRID("
                    + "ST_MakePoint(" + Double.toString(lon) + ", " + Double.toString(lat) + "), 4326) "
                    + "LIMIT " + limit);
            while (r0.next()) {
                geom = (PGgeometry) r0.getObject(1);
                longitude = geom.getGeometry().getFirstPoint().getX();
                latitude = geom.getGeometry().getFirstPoint().getY();
                r1 = connection.createStatement().executeQuery(
                        "SELECT ST_DISTANCE("
                        + "ST_SetSRID(ST_MakePoint(" + longitude
                        + ", " + latitude + "), 4326)::geography,"
                        + "ST_SetSRID(ST_MakePoint(" + Double.toString(lon)
                        + ", " + Double.toString(lat) + "), 4326)::geography"
                        + ");");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }

        return points3d;
    }

    /**
     * @param orgData a simple grid of point3D, with z =0.0
     * @param nbLat nb of lines
     * @param nbLon nb of columns
     * @param triangles Delaunay tiangulation with elevation value
     * @return the initial grid whith elevation value
     *
     */
    public Point3DGeo[][] mergeData(Point3DGeo[][] orgData, List<Triangle_dt> triangles) {
        /*
        matrice.length     //  2
        matrice[0].length  //  4
        matrice[1].length  //  7
         */
        int nbLat = orgData[0].length;
        int nbLon = orgData[1].length;
        Point3DGeo[][] tmp = new Point3DGeo[nbLat][nbLon];
        for (int k = 0; k < nbLat; k++) {
            System.arraycopy(orgData[k], 0, tmp[k], 0, nbLon);
        }
        for (int k = 0; k < nbLat - 1; k++) {
            for (int l = 0; l < nbLon - 1; l++) {
                //Select one point
                Point3DGeo p = tmp[k][l];
                Point_dt pp = new Point_dt(p.getLatitude(), p.getLongitude(), p.getElevation());
                for (Triangle_dt tt : triangles) {
                    // Research  the nearest point of this triangle
                    if (tt.contains(pp)) {
                        distA = tt.A.distance(pp);
                        distB = tt.B.distance(pp);
                        distC = tt.C.distance(pp);
                        distMin = distA;
                        pMin = tt.A;
                        if (distMin > distB) {
                            distMin = distB;
                            pMin = tt.B;
                        }
                        if (distMin > distC) {
                            distMin = distC;
                            pMin = tt.C;
                        }
                        tmp[k][l].setElevation(pMin.z);
                    }
                }
            }
        }
        return tmp;
    }

    /**
     * @param orgData a simple grid of point3D, with z =0.0
     * @param nbLat nb of lines
     * @param nbLon nb of columns
     * @param triangles Delaunay tiangulation with elevation value
     * @return the initial grid whith elevation value
     *
     */
    public Point3DGeo[][] mergeData(Point3DGeo[][] orgData, List<Triangle_dt> triangles, double depth) {
        int nbLat = orgData[0].length;
        int nbLon = orgData[1].length;
        Point3DGeo[][] tmp = new Point3DGeo[nbLat][nbLon];
        for (int k = 0; k < nbLat; k++) {
            System.arraycopy(orgData[k], 0, tmp[k], 0, nbLon);
        }
        for (int k = 0; k < nbLat - 1; k++) {
            for (int l = 0; l < nbLon - 1; l++) {
                //Select one point
                Point3DGeo p = tmp[k][l];
                Point_dt pp = new Point_dt(p.getLatitude(), p.getLongitude(), p.getElevation());
                for (Triangle_dt tt : triangles) {
                    // Research  the nearest point of this triangle
                    if (tt.contains(pp)) {
                        distA = tt.A.distance(pp);
                        distB = tt.B.distance(pp);
                        distC = tt.C.distance(pp);
                        distMin = distA;
                        pMin = tt.A;
                        if (distMin > distB) {
                            distMin = distB;
                            pMin = tt.B;
                        }
                        if (distMin > distC) {
                            distMin = distC;
                            pMin = tt.C;
                        }
                        tmp[k][l].setElevation(depth);
                    }
                }
            }
        }
        return tmp;
    }

    public Connection getConnection() {
        return connection;
    }

    public void writePointList(List<Point3DGeo> points, Path pathname, boolean latLon) {

        List<String> lines = new ArrayList<>();
        if (points != null) {
            if (latLon == true) {
                points.forEach((p) -> {
                    lines.add(p.getLatitude() + " " + p.getLongitude() + " " + p.getElevation());
                });
            } else {
                points.forEach((p) -> {
                    lines.add(p.getLongitude() + " " + p.getLatitude() + " " + p.getElevation());
                });
            }
            try {
                Files.write(pathname, lines, charset, StandardOpenOption.CREATE);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
        }
    }

    private void alert() {
        if (first == true) {
            Platform.runLater(() -> {
                first = false;
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Database warning");
                alert.setHeaderText("Database not connected");
                Text s = new Text("    Verify PostGis NaVisuDB connection \n");
                s.setWrappingWidth(650);
                alert.getDialogPane().setContent(s);
                alert.show();
            });
        }
    }
}
