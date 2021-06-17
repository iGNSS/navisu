/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.stl.databases.impl.controller;

import bzh.terrevirtuelle.navisu.api.progress.Job;
import bzh.terrevirtuelle.navisu.api.progress.ProgressHandle;
import bzh.terrevirtuelle.navisu.app.drivers.driver.DriverManagerServices;
import bzh.terrevirtuelle.navisu.app.drivers.instrumentdriver.InstrumentDriverManagerServices;
import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.app.guiagent.geoview.GeoViewServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layers.LayersManagerServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layertree.LayerTreeServices;
import static bzh.terrevirtuelle.navisu.app.guiagent.utilities.Translator.tr;
import bzh.terrevirtuelle.navisu.bathymetry.db.BathymetryDBServices;
import bzh.terrevirtuelle.navisu.cartography.projection.Pro4JServices;
import bzh.terrevirtuelle.navisu.cartography.projection.lambert.LambertServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.S57ChartComponentServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.controller.navigation.S57Controller;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.BuoyageDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.CoastlineDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.DepareDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.DepthContourDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.MnsysDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.TopmarDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.BuoyageView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.DepareView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.LandmarkView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.LightView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.S57ObjectView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.UnderwaterAwashRockView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.view.WrecksView;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.AnchorageAreaDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.DockAreaDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.DredgedAreaDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.LandmarkDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.LightDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.NavigationLineDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.RestrictedAreaDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.UnderwaterAwashRockDBLoader;
import bzh.terrevirtuelle.navisu.charts.vector.s57.databases.impl.controller.loader.WrecksDBLoader;
import bzh.terrevirtuelle.navisu.citygml.CityGMLServices;
import bzh.terrevirtuelle.navisu.core.util.Proc;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.database.relational.DatabaseServices;
import bzh.terrevirtuelle.navisu.domain.bathymetry.model.DEM;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.Geo;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.Buoyage;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.DepthContour;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.Landmark;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.Light;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.view.constants.BUOYAGE;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import bzh.terrevirtuelle.navisu.geometry.delaunay.DelaunayServices;
import bzh.terrevirtuelle.navisu.geometry.geodesy.GeodesyServices;
import bzh.terrevirtuelle.navisu.shapefiles.ShapefileObjectServices;
import bzh.terrevirtuelle.navisu.stl.databases.impl.StlDBComponentImpl;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.loader.bathy.BathyLoader;
import bzh.terrevirtuelle.navisu.topology.TopologyServices;
import bzh.terrevirtuelle.navisu.util.Pair;
import bzh.terrevirtuelle.navisu.visualization.view.DisplayServices;
import gov.nasa.worldwind.WorldWindow;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import bzh.terrevirtuelle.navisu.widgets.impl.Widget2DController;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.loader.dem.DemDbLoader;
import bzh.terrevirtuelle.navisu.dem.db.DemDBServices;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.UnderwaterAwashRock;
import bzh.terrevirtuelle.navisu.domain.charts.vector.s57.model.geo.Wrecks;
import bzh.terrevirtuelle.navisu.domain.geometry.SolidGeo;
import bzh.terrevirtuelle.navisu.domain.raster.RasterInfo;
import bzh.terrevirtuelle.navisu.domain.svg.SVGPath3D;
import bzh.terrevirtuelle.navisu.gdal.GdalServices;
import bzh.terrevirtuelle.navisu.geometry.jts.JTSServices;
import bzh.terrevirtuelle.navisu.geometry.objects3D.GridBox3D;
import bzh.terrevirtuelle.navisu.geometry.objects3D.obj.ObjComponentServices;
import bzh.terrevirtuelle.navisu.kml.KmlComponentServices;
import bzh.terrevirtuelle.navisu.speech.SpeakerServices;
import bzh.terrevirtuelle.navisu.stl.StlComponentServices;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.kml.BuoyageExportKML;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.kml.GridBox3DExportKML;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.BuildingsExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.BuoyageExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.DaeExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.DepareExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.GridBox3DExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.LandmarkExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.MeshExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.S57ObjectsExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.SLConsShapefileExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.UwtrocExportToSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.stl.WrecksExportSTL;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.svg.DepareExportToSVG;
import bzh.terrevirtuelle.navisu.stl.databases.impl.controller.export.wkt.DepareExportWKT;
import bzh.terrevirtuelle.navisu.stl.impl.StlComponentImpl;
import bzh.terrevirtuelle.navisu.stl.util.impl.controller.SlConsEditorController;
import bzh.terrevirtuelle.navisu.util.interval.Interval;
import bzh.terrevirtuelle.navisu.visualization.view.impl.controller.JfxViewer;

import com.google.common.collect.ImmutableMap;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.SurfaceImageLayer;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.FileHandler;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

/**
 * @author Serge Morvan
 * @date 14/02/2018 12:49
 */
public class StlDBComponentController
        extends Widget2DController
        implements Initializable {

    protected StlDBComponentImpl component;

    protected BathymetryDBServices bathymetryDBServices;
    protected CityGMLServices cityGMLServices;
    protected DatabaseServices databaseServices;
    protected DelaunayServices delaunayServices;
    protected DemDBServices demDBServices;
    protected DriverManagerServices driverManagerServices;
    protected DisplayServices displayServices;
    protected GdalServices gdalServices;
    protected GeodesyServices geodesyServices;
    protected GuiAgentServices guiAgentServices;
    protected InstrumentDriverManagerServices instrumentDriverManagerServices;
    protected JTSServices jtsServices;
    protected LambertServices lambertServices;
    protected LayersManagerServices layersManagerServices;
    protected KmlComponentServices kmlComponentServices;
    protected ObjComponentServices objComponentServices;
    protected Pro4JServices pro4JServices;
    protected ShapefileObjectServices shapefileObjectServices;
    protected StlComponentServices stlComponentServices;
    protected TopologyServices topologyServices;
    protected GeoViewServices geoViewServices;
    protected LayerTreeServices layerTreeServices;
    protected SpeakerServices speakerServices;

    protected static final Logger LOGGER = Logger.getLogger(StlDBComponentController.class.getName());
    private final String NAVISU_HOME = System.getProperty("user.home") + "/.navisu";
    private final String FXML = "stlDBController.fxml";
    protected static final String SEP = File.separator;
    protected String CONFIG_FILE_NAME = System.getProperty("user.home") + "/.navisu/config/config.properties";
    protected String CACHE_FILE_NAME = System.getProperty("user.home") + "/.navisu/caches/caches.properties";
    protected static final String ALARM_SOUND = "/data/sounds/pling.wav";
    protected static final String ALARM_SOUND_1 = "/data/sounds/openSea.wav";
    protected static final String DATA_PATH = System.getProperty("user.dir").replace("\\", "/");
    protected static final String DEFAULT_KML_PATH = "privateData" + SEP + "kml" + SEP;
    protected static final String DEFAULT_STL_PATH = "privateData" + SEP + "stl" + SEP;
    protected static final String DEFAULT_SVG_PATH = "privateData" + SEP + "svg" + SEP;
    protected static final String DEFAULT_WKT_PATH = "privateData" + SEP + "wkt" + SEP;
    protected static final String DEFAULT_ASC_PATH = "privateData" + SEP + "asc" + SEP;
    protected static final String USER_DIR = System.getProperty("user.dir");
    protected static final String IMAGE_DIR = USER_DIR + SEP + "privateData" + SEP + "tif";

    private final String HOST = "localhost";
    private final String PROTOCOL = "jdbc:postgresql://";
    private final String PORT = "5432";
    private final String DRIVER = "org.postgresql.Driver";
    private final String USER = "admin";
    private final String PASSWD = "admin";

    private final String S57_DEFAULT_DATABASE_1 = "s57NP1DB";
    private final String S57_DEFAULT_DATABASE_2 = "s57NP2DB";
    private final String S57_DEFAULT_DATABASE_3 = "s57NP3DB";
    private final String S57_DEFAULT_DATABASE_4 = "s57NP4DB";
    private final String S57_DEFAULT_DATABASE_5 = "s57NP5DB";
    private final String S57_DEFAULT_DATABASE_6 = "s57NP6DB";
    private final String S57_DEFAULT_DATABASE_7 = "BalisageMaritimeDB";

    protected Properties configProperties = new Properties();
    protected Properties cacheProperties = new Properties();

    private static final String CSS_STYLE_PATH = Paths.get(System.getProperty("user.dir") + "/css/").toUri().toString();
    protected String viewgroupstyle = "configuration.css";
    protected Map<String, String> acronyms;
    protected WorldWindow wwd = GeoWorldWindViewImpl.getWW();
    protected MeasureTool measureTool;
    protected final Set<S57Controller> s57Controllers = new HashSet<>();
    protected boolean first = true;
    protected static final String GROUP_0 = "S57 charts";
    protected static final String GROUP_1 = "Bathymetry";
    protected static final String BATHYMETRY_LAYER = "Bathymetry";
    protected static final String S57_LAYER = "S57Stl";
    protected static final String LIGHTS_LAYER = "LIGHTS";
    protected static final String SELECTED_LAYER = "TargetCmd";

    protected String LAT_MIN;
    protected String LON_MIN;
    protected String LAT_MAX;
    protected String LON_MAX;

    protected double INTERVAL_MAX = 0.000026998;//3m

    protected static final double RETRIEVE_OFFSET = 0.00025;

    protected RenderableLayer bathymetryLayer;
    protected RenderableLayer s57Layer;
    protected RenderableLayer selectLayer;
    protected RenderableLayer lightsLayer;
    protected ShapeAttributes normalAttributes;
    protected ShapeAttributes highlightAttributes;

    protected double DEFAULT_SIDE = 180.0;
    protected double DEFAULT_BASE_HEIGHT = 3.99;//4.5;//3.99;
    protected double tileSideX = DEFAULT_SIDE;
    protected double tileSideY = DEFAULT_SIDE;
    protected double tileSideZ = DEFAULT_BASE_HEIGHT;
    protected double svgSideX = DEFAULT_SIDE;
    protected double svgSideY = DEFAULT_SIDE;
    protected double DEFAULT_GRID = 30.0;
    protected double gridX = DEFAULT_GRID;
    protected double gridY = DEFAULT_GRID;
    protected double lat0 = 520;
    protected double lon0;
    protected double lat1;
    protected double lon1;
    protected double latRange;
    protected double lonRange;
    protected double latRangeMetric;
    protected double lonRangeMetric;
    protected double latScale;
    protected double lonScale;
    protected double globalScale;
    protected List<String> stlFileNames;
    protected List<String> kmlFileNames;
    protected List<Polygon> selectedPolygons = new ArrayList<>();
    protected List<Polygon> realSelectedPolygons;
    protected List<Pair<Integer, Integer>> selectedPolygonIndexList = new ArrayList<>();
    protected int tileCount = 1;
    protected double DEFAULT_EXAGGERATION = 1.0;
    protected double verticalExaggeration = DEFAULT_EXAGGERATION;
    protected double simplifyFactor;
    protected S57ObjectView s57Viewer;
    protected S57ObjectsExportToSTL s57ObjectsExport;
    protected List<? extends Geo> objects = new ArrayList<>();
    protected List<DepthContour> depthContours = new ArrayList<>();
    protected List<Buoyage> buoyages = new ArrayList<>();
    protected List<Landmark> landmarks = new ArrayList<>();
    protected List<UnderwaterAwashRock> underwaterAwashRocks = new ArrayList<>();
    protected List<Wrecks> wrecks = new ArrayList<>();
    protected Connection s57Connection;
    protected Connection bathyConnection;
    protected Connection elevationConnection;
    protected Connection highlElevationConnection;
    protected Connection buildingsConnection;
    protected DEM bathymetry;
    protected DEM elevation;
    protected double lowestElevationAlti = 0.0;
    protected double highestElevationBathy = 0.0;
    protected double maxDepth;
    protected Map<Pair<Double, Double>, String> topMarkMap = new HashMap<>();
    protected String marsys;
    protected List<String> selectedObjects = new ArrayList<>();
    protected String sep = File.separator;
    protected List<Polygon> tiles;
    protected List<Point3DGeo[][]> grids = null;
    protected SurfaceImageLayer layerTif;
    protected static final String GROUP = "GeoTiff charts";
    protected Map<Double, List<SVGPath3D>> svgMap;
    /* Common controls */
    @FXML
    public Group view;
    @FXML
    public Pane viewPane;
    @FXML
    public Button quit;
    @FXML
    public Button requestButton;
    @FXML
    public Button tilesSelectionStartButton;
    @FXML
    public Button helpButton;
    @FXML
    public ChoiceBox<String> s57DatabasesCB;
    @FXML
    public ChoiceBox<String> bathyDatabasesCB;
    @FXML
    public Button latLonButton;
    @FXML
    public Button interactiveButton;
    @FXML
    public Button selectedButton;
    @FXML
    public TextField s57DatabaseTF;
    @FXML
    public TextField bathyDatabaseTF;
    @FXML
    public RadioButton buildingsRB;
    @FXML
    public ChoiceBox<String> buildingsDatabasesCB;
    @FXML
    public TextField buildingsDatabaseTF;
    @FXML
    public TextField objectsTF;
    @FXML
    public TextField scaleDaeTF;
    @FXML
    public TextField elevationMagnificationTF;
    @FXML
    public TextField landVerticalOffsetTF;
    @FXML
    public Label latMinLabel;
    @FXML
    public Label lonMinLabel;
    @FXML
    public Label latMaxLabel;
    @FXML
    public Label lonMaxLabel;
    @FXML
    public ComboBox<String> objectsCB;
    @FXML
    public TextField outFileTF;
    @FXML
    public TextField hostnameTF;
    @FXML
    public TextField tilesCountTF;
    @FXML
    public TextField rangeLatTF;
    @FXML
    public TextField rangeLonTF;
    @FXML
    public TextField scaleTF;
    @FXML
    public TextField tileSideXTF;
    @FXML
    public TextField tileSideYTF;
    @FXML
    public TextField tileSideZTF;
    @FXML
    public TextField svgSideXTF;
    @FXML
    public TextField svgSideYTF;
    @FXML
    public ChoiceBox<String> supportCB;
    @FXML
    public TextField supportTF;
    @FXML
    public TextField gridSideXTF;
    @FXML
    public TextField gridSideYTF;
    @FXML
    public TextField encPortDBTF;
    @FXML
    public RadioButton bathyRB;
    @FXML
    public RadioButton noBathyRB;
    @FXML
    public Button validSTLButton;
    @FXML
    public ChoiceBox<String> tilesCountCB;
    @FXML
    public GridPane paneGP;
    @FXML
    public TextField elevationDatabaseTF;
    @FXML
    public ChoiceBox<String> elevationDatabasesCB;
    @FXML
    public RadioButton elevationRB;
    @FXML
    public RadioButton lowTideRB;
    @FXML
    public TextField tidalRangeTF;
    @FXML
    public RadioButton noAltiRB;
    @FXML
    public CheckBox stlPreviewCB;
    @FXML
    public CheckBox buildingsPreviewCB;
    @FXML
    public CheckBox generateStlCB;
    @FXML
    public CheckBox generateSvgCB;
    @FXML
    public CheckBox generateWktCB;
    @FXML
    public Button validSVGButton;
    @FXML
    public CheckBox depareVisuCB;
    @FXML
    public CheckBox generateKmlCB;
    @FXML
    public RadioButton mergeLitto3dRB;
    /*--------------------------------------Checkboxes ----*/
    @FXML
    public CheckBox autoBoundCB;
    @FXML
    public CheckBox allCB;
    @FXML
    public CheckBox achareCB;
    @FXML
    public CheckBox buoyageCB;
    @FXML
    public CheckBox coalneCB;
    @FXML
    public CheckBox depcntCB;
    @FXML
    public CheckBox docareCB;
    @FXML
    public CheckBox drgareCB;
    @FXML
    public CheckBox lightsCB;
    @FXML
    public CheckBox lndmrkCB;
    @FXML
    public CheckBox navlneCB;
    @FXML
    public CheckBox slconsCB;
    @FXML
    public CheckBox pontonCB;
    @FXML
    public CheckBox resareCB;
    @FXML
    public CheckBox uwtrocCB;
    @FXML
    public CheckBox wrecksCB;
    @FXML
    public Button meshStlObjectButton;
    @FXML
    public Button daeButton;
    @FXML
    public Button wktObjectButton;

    /*-----------------------------Tools------*/
    @FXML
    public Button slConsEditorButton;
    @FXML
    public Button importShapefileButton;
    @FXML
    public Button svgViewerButton;
    @FXML
    public TextField heightShapefileTF;

    int k = 0;
    int i = 0;

    int j = 0;
    String resultStlFilename = null;

    final ToggleGroup bathyGroup = new ToggleGroup();
    final ToggleGroup wsGroup = new ToggleGroup();
    final ToggleGroup altiGroup = new ToggleGroup();
    final ToggleGroup terrainGroup = new ToggleGroup();

    protected ObservableList<String> s57DbCbData = FXCollections.observableArrayList(S57_DEFAULT_DATABASE_1, S57_DEFAULT_DATABASE_2,
            S57_DEFAULT_DATABASE_3, S57_DEFAULT_DATABASE_4,
            S57_DEFAULT_DATABASE_5, S57_DEFAULT_DATABASE_6,
            S57_DEFAULT_DATABASE_7);
    protected ObservableList<String> bathyDbCbData = FXCollections.observableArrayList("BathyShomDB");
    protected ObservableList<String> elevationDbCbData = FXCollections.observableArrayList("SRTM30mDB", "NasaDemDB",
            "BrestMetropole5mDB", "BrestMetropole1mDB", "Finistere5mDB", "TestAltiDB",
            "AltiV2_2-0_75mIgnDB");
    protected Map<String, Double> elevationDbMap = new HashMap<String, Double>() {
        {
            put("SRTM30mDB", 30.0);
            put("NasaDemDB", 30.0);
            put("BrestMetropole5mDB", 5.0);
            put("BrestMetropole1mDB", 1.0);
            put("Finistere5mDB", 5.0);

        }
    };
    protected ObservableList<String> buildingsDbCbData = FXCollections.observableArrayList("BuildingsPaysBrestDB");
    protected ObservableList<String> tilesCbData = FXCollections.observableArrayList("1x1", "2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8", "9x9", "10x10");
    protected ObservableList<String> supportCbData = FXCollections.observableArrayList("With magnet", "Simple", "No support");
    protected final String HIGH_ELEVATION_DB = "Litto3D5mDB";

    protected Map<String, CheckBox> s57SelectionMap;

    protected StlGuiController stlGuiController;
    protected DaeExportToSTL daeExportToSTL;
    protected MeshExportToSTL meshExportToSTL;
    protected BuildingsExportToSTL buildingsExportToSTL;
    protected Shapefile shapefile;
    protected List<Pair<File, Double>> slConsShapefiles;
    protected double heightShapefile;
    protected final double DEFAULT_HEIGHT_SHAPEFILE = 5.0;
    protected Shape shapeSVG;
    protected List<Point3DGeo> boundList;

    protected long startTime;

    public StlDBComponentController(StlDBComponentImpl component,
            KeyCode keyCode, KeyCombination.Modifier keyCombination,
            GuiAgentServices guiAgentServices,
            LayersManagerServices layersManagerServices,
            S57ChartComponentServices s57ChartComponentServices,
            DatabaseServices databaseServices,
            DelaunayServices delaunayServices,
            DemDBServices demServices,
            DisplayServices displayServices,
            BathymetryDBServices bathymetryDBServices,
            InstrumentDriverManagerServices instrumentDriverManagerServices,
            TopologyServices topologyServices,
            ShapefileObjectServices shapefileObjectServices,
            GeodesyServices geodesyServices,
            JTSServices jtsServices,
            StlComponentServices stlComponentServices,
            KmlComponentServices kmlComponentServices,
            ObjComponentServices objComponentServices,
            Pro4JServices pro4JServices,
            GeoViewServices geoViewServices,
            LayerTreeServices layerTreeServices,
            GdalServices gdalServices,
            DriverManagerServices driverManagerServices,
            SpeakerServices speakerServices,
            CityGMLServices cityGMLServices) {
        super(keyCode, keyCombination);

        this.component = component;
        this.guiAgentServices = guiAgentServices;
        this.layersManagerServices = layersManagerServices;
        this.databaseServices = databaseServices;
        this.delaunayServices = delaunayServices;
        this.demDBServices = demServices;
        this.displayServices = displayServices;
        this.bathymetryDBServices = bathymetryDBServices;
        this.instrumentDriverManagerServices = instrumentDriverManagerServices;
        this.topologyServices = topologyServices;
        this.jtsServices = jtsServices;
        this.shapefileObjectServices = shapefileObjectServices;
        this.geodesyServices = geodesyServices;
        this.stlComponentServices = stlComponentServices;
        this.kmlComponentServices = kmlComponentServices;
        this.objComponentServices = objComponentServices;
        this.pro4JServices = pro4JServices;
        this.geoViewServices = geoViewServices;
        this.layerTreeServices = layerTreeServices;
        this.gdalServices = gdalServices;
        this.driverManagerServices = driverManagerServices;
        this.speakerServices = speakerServices;
        this.cityGMLServices = cityGMLServices;

        this.meshExportToSTL = new MeshExportToSTL(geodesyServices, guiAgentServices, jtsServices);
        this.buildingsExportToSTL = new BuildingsExportToSTL(bathymetryDBServices, geodesyServices);

        s57Layer = layersManagerServices.getLayer(GROUP_0, S57_LAYER);
        bathymetryLayer = layersManagerServices.getLayer(GROUP_0, BATHYMETRY_LAYER);
        lightsLayer = layersManagerServices.getLayer(GROUP_0, LIGHTS_LAYER);
        selectLayer = layersManagerServices.getLayer(GROUP_0, SELECTED_LAYER);

        LOGGER.setLevel(Level.INFO);
        FileHandler fh = null;
        try {
            fh = new FileHandler(NAVISU_HOME + "/logs/" + "navisu.log");
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(StlDBComponentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOGGER.addHandler(fh);

        guiAgentServices.getScene().addEventFilter(KeyEvent.KEY_RELEASED, this);
        guiAgentServices.getRoot().getChildren().add(this);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FXML));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }

        String uri = CSS_STYLE_PATH + viewgroupstyle;
        view.getStylesheets().add(uri);

        try {
            configProperties.load(new FileInputStream(CONFIG_FILE_NAME));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        }

        InputStream input = null;
        try {
            input = new FileInputStream(CACHE_FILE_NAME);
            cacheProperties.load(input);
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(StlDBComponentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
        LAT_MIN = cacheProperties.getProperty("LAT_MIN");
        LON_MIN = cacheProperties.getProperty("LON_MIN");
        LAT_MAX = cacheProperties.getProperty("LAT_MAX");
        LON_MAX = cacheProperties.getProperty("LON_MAX");
        //Mat values :  48.21N -4.61     48.42N -4.30

        kmlFileNames = new ArrayList<>();
        slConsShapefiles = new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        makeAttributes();

        // Init s57 selection Panel
        s57SelectionMap = ImmutableMap.<String, CheckBox>builder()
                .put("ACHARE", achareCB)
                .put("BUOYAGE", buoyageCB)
                .put("COALNE", coalneCB)
                .put("DEPCNT", depcntCB)
                .put("DOCARE", docareCB)
                .put("DRGARE", drgareCB)
                .put("LIGHTS", lightsCB)
                .put("LNDMRK", lndmrkCB)
                .put("NAVLNE", navlneCB)
                .put("SLCONS", slconsCB)
                .put("PONTON", pontonCB)
                .put("RESARE", resareCB)
                .put("UWTROC", uwtrocCB)
                .put("WRECKS", wrecksCB)
                .build();
        stlGuiController = new StlGuiController(this,
                geodesyServices, displayServices,
                s57SelectionMap, allCB,
                selectedObjects,
                objectsTF,
                LAT_MIN, LAT_MAX, LON_MIN, LON_MAX,
                lat0, lon0, lat1, lon1,
                latMinLabel, lonMinLabel, latMaxLabel, lonMaxLabel,
                CACHE_FILE_NAME,
                s57Layer, selectedPolygons);
        selectedObjects.clear();
        objectsTF.clear();
        stlGuiController.initS57Gui();
        allCB.setOnAction((ActionEvent event) -> {
            stlGuiController.initS57Gui("ALL");
        });

        s57DatabasesCB.setItems(s57DbCbData);
        s57DatabasesCB.getSelectionModel().select(S57_DEFAULT_DATABASE_5);
        s57DatabaseTF.setText(S57_DEFAULT_DATABASE_5);
        s57DatabasesCB.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    s57DatabaseTF.setText(s57DatabasesCB.getValue());
                    if (s57DatabasesCB.getValue().equalsIgnoreCase(S57_DEFAULT_DATABASE_7)) {
                        achareCB.setDisable(true);
                        coalneCB.setDisable(true);
                        depcntCB.setDisable(true);
                        docareCB.setDisable(true);
                        drgareCB.setDisable(true);
                        navlneCB.setDisable(true);
                        slconsCB.setDisable(true);
                        pontonCB.setDisable(true);
                        resareCB.setDisable(true);
                        uwtrocCB.setDisable(true);
                        wrecksCB.setDisable(true);
                        allCB.setDisable(true);
                    } else {
                        achareCB.setDisable(false);
                        coalneCB.setDisable(false);
                        depcntCB.setDisable(false);
                        docareCB.setDisable(false);
                        drgareCB.setDisable(false);
                        navlneCB.setDisable(false);
                        slconsCB.setDisable(false);
                        pontonCB.setDisable(false);
                        resareCB.setDisable(false);
                        uwtrocCB.setDisable(false);
                        wrecksCB.setDisable(false);
                        allCB.setDisable(false);
                    }
                });

        bathyDatabasesCB.setItems(bathyDbCbData);
        bathyDatabasesCB.getSelectionModel().select("BathyShomDB");
        bathyDatabaseTF.setText("BathyShomDB");
        bathyDatabasesCB.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> bathyDatabaseTF.setText(bathyDatabasesCB.getValue())
                );

        elevationDatabasesCB.setItems(elevationDbCbData);
        elevationDatabasesCB.getSelectionModel().select("SRTM30mDB");
        elevationDatabaseTF.setText("SRTM30mDB");
        elevationDatabasesCB.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> elevationDatabaseTF.setText(elevationDatabasesCB.getValue())
                );
        buildingsDatabasesCB.setItems(buildingsDbCbData);
        buildingsDatabasesCB.getSelectionModel().select("BuildingsPaysBrestDB");
        buildingsDatabaseTF.setText("BuildingsPaysBrestDB");
        buildingsDatabasesCB.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> buildingsDatabaseTF.setText(buildingsDatabasesCB.getValue())
                );
        tilesCountCB.setItems(tilesCbData);
        tilesCountCB.getSelectionModel().select("1x1");
        tilesCountTF.setText("1");
        tilesCountCB.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> {
                    tileCount = Integer.parseInt(tilesCountCB.getValue().split("x")[0]);
                    tilesCountTF.setText(Integer.toString(tileCount * tileCount));
                    selectedPolygons.addAll(stlGuiController.createAndDisplayTiles(s57Layer, Material.RED, 100, lat0, lon0, lat1, lon1, tileCount, tileCount));
                });
        supportCB.setItems(supportCbData);
        supportCB.getSelectionModel().select("With magnet");
        supportTF.setText("With magnet");
        supportCB.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)
                        -> supportTF.setText(supportCB.getValue())
                );
        gridSideXTF.setText(Double.toString(DEFAULT_GRID));
        gridSideXTF.setOnAction((ActionEvent event) -> {
            try {
                gridX = Double.parseDouble(gridSideXTF.getText());
                gridSideXTF.setText(Double.toString(gridX));
                gridSideYTF.setText(Double.toString(gridX));
            } catch (NumberFormatException e) {
                gridX = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
                gridY = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
            }
            /*
            if (gridX < DEFAULT_GRID) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range is too small, min = " + DEFAULT_GRID + " m");
                alert.show();
                gridX = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
                gridY = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
            }
             */
        });
        gridSideYTF.setText(Double.toString(DEFAULT_GRID));
        gridSideYTF.setOnAction((ActionEvent event) -> {
            try {
                gridY = Double.parseDouble(gridSideYTF.getText());
                gridSideYTF.setText(Double.toString(gridY));
                gridSideXTF.setText(Double.toString(gridY));
            } catch (NumberFormatException e) {
                gridY = DEFAULT_GRID;
                gridSideYTF.setText(Double.toString(gridY));
                gridX = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
            }
            /*
            if (gridY < DEFAULT_GRID) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range is too small, min = " + DEFAULT_GRID + " m");
                alert.show();
                gridX = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
                gridY = DEFAULT_GRID;
                gridSideXTF.setText(Double.toString(gridX));
            }
             */
        });
        tileSideXTF.setText(Double.toString(DEFAULT_SIDE));
        tileSideXTF.setOnAction((ActionEvent event) -> {
            try {
                tileSideX = Double.parseDouble(tileSideXTF.getText());
                tileSideXTF.setText(Double.toString(tileSideX));
                tileSideY = tileSideX;
                tileSideYTF.setText(Double.toString(tileSideX));
            } catch (NumberFormatException e) {
                tileSideX = DEFAULT_SIDE;
                tileSideXTF.setText(Double.toString(tileSideX));
                tileSideY = DEFAULT_SIDE;
                tileSideYTF.setText(Double.toString(tileSideY));
            }
        });
        tileSideYTF.setText(Double.toString(DEFAULT_SIDE));
        tileSideYTF.setOnAction((ActionEvent event) -> {

            try {
                tileSideY = Double.parseDouble(tileSideYTF.getText());
                tileSideYTF.setText(Double.toString(tileSideY));
                tileSideX = tileSideY;
                tileSideXTF.setText(Double.toString(tileSideY));
            } catch (NumberFormatException e) {
                tileSideY = DEFAULT_SIDE;
                tileSideYTF.setText(Double.toString(tileSideY));
                tileSideX = DEFAULT_SIDE;
                tileSideXTF.setText(Double.toString(tileSideX));
            }
        });
        tileSideZTF.setText(Double.toString(DEFAULT_BASE_HEIGHT));
        tileSideZTF.setOnAction((ActionEvent event) -> {
            try {
                tileSideZ = Double.parseDouble(tileSideZTF.getText());
                tileSideZTF.setText(Double.toString(tileSideZ));
            } catch (NumberFormatException e) {
                tileSideZ = DEFAULT_BASE_HEIGHT;
                tileSideZTF.setText(Double.toString(tileSideZ));
            }
        });

        svgSideXTF.setText(Double.toString(DEFAULT_SIDE));
        svgSideXTF.setOnAction((ActionEvent event) -> {
            try {
                svgSideX = Double.parseDouble(svgSideXTF.getText());
                svgSideXTF.setText(Double.toString(svgSideX));
                svgSideY = tileSideX;
                svgSideYTF.setText(Double.toString(svgSideX));
            } catch (NumberFormatException e) {
                svgSideX = DEFAULT_SIDE;
                svgSideXTF.setText(Double.toString(svgSideX));
                svgSideY = DEFAULT_SIDE;
                svgSideYTF.setText(Double.toString(svgSideY));
            }
        });
        svgSideYTF.setText(Double.toString(DEFAULT_SIDE));
        svgSideYTF.setOnAction((ActionEvent event) -> {

            try {
                svgSideY = Double.parseDouble(tileSideYTF.getText());
                svgSideYTF.setText(Double.toString(svgSideY));
                svgSideX = svgSideY;
                svgSideXTF.setText(Double.toString(svgSideY));
            } catch (NumberFormatException e) {
                svgSideY = DEFAULT_SIDE;
                svgSideYTF.setText(Double.toString(svgSideY));
                svgSideX = DEFAULT_SIDE;
                svgSideYTF.setText(Double.toString(svgSideX));
            }
        });

        elevationMagnificationTF.setText(Double.toString(DEFAULT_EXAGGERATION));
        elevationMagnificationTF.setOnAction((ActionEvent event) -> {
            try {
                verticalExaggeration = Double.valueOf(elevationMagnificationTF.getText());
            } catch (NumberFormatException e) {
                verticalExaggeration = DEFAULT_EXAGGERATION;
                elevationMagnificationTF.setText(Double.toString(verticalExaggeration));
            }
        });

        outFileTF.setText("out");

        scaleDaeTF.setText("1");

        bathyRB.setToggleGroup(bathyGroup);
        noBathyRB.setToggleGroup(bathyGroup);

        noAltiRB.setToggleGroup(terrainGroup);

        noAltiRB.setToggleGroup(altiGroup);
        elevationRB.setToggleGroup(altiGroup);
        elevationRB.setSelected(true);

        quit.setOnMouseClicked((MouseEvent event) -> {
            component.off();
        });

        helpButton.setOnMouseClicked((MouseEvent event) -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Options");
            alert.setHeaderText("Display S57 objects");
            Text s = new Text(
                    "    Choice database with scale :\n\n"
                    + "    s57NP1DB - Overview 	< 1 : 1 500 000\n"
                    + "    s57NP2DB - General 	1 : 350 000 à 1 : 1 500 000\n"
                    + "    s57NP3DB - Coastal 	1 : 90 000 à 1 : 350 000\n"
                    + "    s57NP4DB - Approach 	1 : 22 000 à 1 : 90 000\n"
                    + "    s57NP5DB - Harbour 	1 : 4 000 à 1 : 22 000\n"
                    + "    s57NP6DB - Berthing 	> 1 : 4 000\n\n"
                    + "    Selection of objects to display");
            s.setWrappingWidth(650);
            alert.getDialogPane().setContent(s);
            alert.show();

        });

        latLonButton.setOnMouseClicked((MouseEvent event) -> {
            Platform.runLater(() -> {
                stlGuiController.initSelectedZone();
            });
        });

        interactiveButton.setOnMouseClicked((MouseEvent event) -> {

            measureTool = new MeasureTool(wwd);
            MeasureToolController measureToolController = new MeasureToolController();
            measureTool.setController(measureToolController);
            measureTool.setMeasureShapeType(MeasureTool.SHAPE_SQUARE);
            measureTool.setPathType(AVKey.GREAT_CIRCLE);
            measureTool.setArmed(true);
        });

        selectedButton.setOnMouseClicked((MouseEvent event) -> {

            if (measureTool != null) {
                List<? extends Position> positions = measureTool.getPositions();
                if (!positions.isEmpty()) {
                    lat0 = positions.get(0).getLatitude().getDegrees();
                    lon0 = positions.get(0).getLongitude().getDegrees();
                    lat1 = positions.get(2).getLatitude().getDegrees();
                    lon1 = positions.get(2).getLongitude().getDegrees();

                    latMinLabel.setText(String.format("%.5f", lat0));
                    lonMinLabel.setText(String.format("%.5f", lon0));
                    latMaxLabel.setText(String.format("%.5f", lat1));
                    lonMaxLabel.setText(String.format("%.5f", lon1));
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Sélectionnez une zone d'acquisition");
                    alert.show();
                }
                measureTool.setArmed(false);
                measureTool.dispose();
                List<Polygon> selected = stlGuiController.createAndDisplayTiles(s57Layer, Material.RED, 100, lat0, lon0, lat1, lon1, tileCount, tileCount);
                selectedPolygons.addAll(selected);
            }
            tilesSelectionStartButton.setDisable(false);
            stlGuiController.saveProperties(lat0, lon0, lat1, lon1);
        });
        tilesSelectionStartButton.setOnMouseClicked((MouseEvent event) -> {
            realSelectedPolygons = stlGuiController.getSelectedPolygons(selectedPolygons);
        });

        meshStlObjectButton.setOnMouseClicked((MouseEvent event) -> {
            boundList = meshExportToSTL.loadMesh();
            wwd.redrawNow();
            displayServices.displayPoints3DAsPath(boundList, 150, s57Layer, Material.YELLOW);
        });
        wktObjectButton.setOnMouseClicked((MouseEvent event) -> {
            System.out.println("work in progress");
        });
        daeButton.setOnMouseClicked((MouseEvent event) -> {
            elevationConnection = databaseServices.connect("SRTM30mDB", HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
            this.daeExportToSTL = new DaeExportToSTL(geodesyServices, guiAgentServices, jtsServices, demDBServices, elevationConnection);
            daeExportToSTL.loadKmzAndSaveStlWgs84();
        });

        slConsEditorButton.setOnMouseClicked((MouseEvent event) -> {
            SlConsEditorController stConsEditorController = new SlConsEditorController(
                    guiAgentServices, layersManagerServices,
                    shapefileObjectServices,
                    topologyServices, kmlComponentServices,
                    selectLayer,
                    KeyCode.M, KeyCombination.CONTROL_DOWN);
            guiAgentServices.getScene().addEventFilter(KeyEvent.KEY_RELEASED, stConsEditorController);
            guiAgentServices.getRoot().getChildren().add(stConsEditorController);
            stConsEditorController.setVisible(true);
        });
        svgViewerButton.setOnMouseClicked((MouseEvent event) -> {
            // 900x600

        });

        importShapefileButton.setOnMouseClicked((MouseEvent event) -> {
            try {
                heightShapefile = Double.parseDouble(heightShapefileTF.getText());
            } catch (NumberFormatException e) {
                heightShapefile = DEFAULT_HEIGHT_SHAPEFILE;
                heightShapefileTF.setText(Double.toString(heightShapefile));
            }
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter("Shapefile files (*.shp)", "*.shp", "*.SHP");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle(tr("popup.fileChooser.open"));
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            List<File> list = fileChooser.showOpenMultipleDialog(null);
            for (File f : list) {
                slConsShapefiles.add(new Pair(f, heightShapefile));
            }
        });

        requestButton.setOnMouseClicked((MouseEvent event) -> {
            stlGuiController.showRealselected(selectedPolygons, realSelectedPolygons);
            selectedPolygonIndexList = getSelectedPolygonIndexList(realSelectedPolygons);
            s57Connection = databaseServices.connect(s57DatabaseTF.getText(),
                    "localhost", "jdbc:postgresql://", "5432", "org.postgresql.Driver", USER, PASSWD);
            if (lat0 != 0 && lon0 != 0 && lat1 != 0 && lon1 != 0) {
                stlGuiController.initTile(tileSideX, tileSideY, tileSideZ, tileCount);
                scaleTF.setText(stlGuiController.initScale(lat0, lon0, lat1, lon1));
                retrieveIn(objectsTF.getText(), lat0, lon0, lat1, lon1);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Lat and Lon fields must be filled \n"
                        + "Range latitude : -90° <= Latitude <= 90° \n"
                        + "Range longitude : -180° <= Longitude <= 180°");
                alert.show();
            }
        });

    }

    public void setLat0(double lat0) {
        this.lat0 = lat0;
    }

    public void setLon0(double lon0) {
        this.lon0 = lon0;
    }

    public void setLat1(double lat1) {
        this.lat1 = lat1;
    }

    public void setLon1(double lon1) {
        this.lon1 = lon1;
    }

    @SuppressWarnings("unchecked")
    public void retrieveIn(String object, double latMin, double lonMin, double latMax, double lonMax) {

        guiAgentServices.getJobsManager().newJob("Load S57 objects", new Job() {

            @Override
            public void run(ProgressHandle progressHandle) {
                startTime = System.currentTimeMillis();
                gridX = Double.parseDouble(gridSideXTF.getText());
                gridY = Double.parseDouble(gridSideYTF.getText());
                LOGGER.info("Start compute : ");
                if (!selectedObjects.isEmpty()) {
                    //Define TopMak for all buoyages, default is 0 : no topmark
                    TopmarDBLoader topmarDbLoader = new TopmarDBLoader(s57Connection);
                    topMarkMap = topmarDbLoader.retrieveIn(latMin, lonMin, latMax, lonMax);

                    //Define IALA system for all buoyages, default is 1
                    MnsysDBLoader mnsysDbLoader = new MnsysDBLoader(s57Connection);
                    marsys = mnsysDbLoader.retrieveIn(latMin, lonMin, latMax, lonMax);
                }
                if (generateStlCB.isSelected() && !generateSvgCB.isSelected()) {
                    //ELEVATION AND TILES
                    if ((elevationRB.isSelected() && noBathyRB.isSelected())
                            || (elevationRB.isSelected() && (selectedObjects.contains("ALL")))) {
                        grids = createElevationTab(lat0, lon0, lat1, lon1, gridX, gridY);
                    }
                    //BATHY AND TILES
                    if (bathyRB.isSelected() && noAltiRB.isSelected()) {
                        grids = createBathymetryTab(lat0, lon0, lat1, lon1);
                    }
                    if (elevationRB.isSelected() && bathyRB.isSelected()) {
                        grids = createBathymetryAndElevationTab(lat0, lon0, lat1, lon1);

                    }
                    if (elevationRB.isSelected() && (selectedObjects.contains("ALL"))) {
                        grids = createElevationAndDepare(lat0, lon0, lat1, lon1);
                    }

                    if (grids != null) {
                        verticalExaggeration = Double.valueOf(elevationMagnificationTF.getText());
                        List<GridBox3D> gridBoxes = new ArrayList<>();
                        grids.stream().map((g) -> {
                            scaleCompute(g);
                            return g;
                        }).map((g) -> new GridBox3D(g, verticalExaggeration, highestElevationBathy)).forEachOrdered((gb) -> {
                            gridBoxes.add(gb);
                        });
                        if (generateKmlCB.isSelected()) {
                            k = 0;
                            gridBoxes.forEach((gb) -> {
                                LOGGER.info("In export GridBox3D en KML");
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                String filename = DEFAULT_KML_PATH + outFileTF.getText() + "_" + i + "," + j + ".kml";
                                kmlFileNames.add(filename);
                                new GridBox3DExportKML(gb).exportWKML(filename, false);
                                LOGGER.info("Out export GridBox3D en KML");
                            });
                            k++;
                        }
                        //Support
                        k = 0;
                        gridBoxes.forEach((gb) -> {

                            i = k / tileCount + 1;
                            j = k % tileCount + 1;
                            if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";

                                String supportType = supportTF.getText();
                                if (supportType.equals("With magnet")) {
                                    LOGGER.info("In export GridBox3D in STL");
                                    new GridBox3DExportToSTL(geodesyServices, displayServices, s57Layer, gb).exportSTL(filename, latScale, lonScale, tileSideZ);
                                    LOGGER.info("Out export GridBox3D in STL");
                                    LOGGER.info("In export exportBaseSTL in STL");
                                    stlComponentServices.exportBaseSTL(filename, "data/stl/base/support_" + i + "," + j + ".stl");
                                    LOGGER.info("Out export exportBaseSTL in STL");
                                }
                                if (supportType.equals("Simple")) {
                                    tileSideZ = 0.99;
                                    LOGGER.info("In export GridBox3D in STL");
                                    new GridBox3DExportToSTL(geodesyServices, displayServices, s57Layer, gb).exportSTL(filename, latScale, lonScale, tileSideZ);
                                    LOGGER.info("Out export GridBox3D in STL");
                                    LOGGER.info("In export exportBaseSTL in STL");
                                    stlComponentServices.exportBaseSTL(filename, "data/stl/base/support_1mm.stl");
                                    LOGGER.info("Out export exportBaseSTL in STL");
                                }
                                if (supportType.equals("No support")) {
                                    tileSideZ = 0.01;
                                    LOGGER.info("In export GridBox3D in STL");
                                    new GridBox3DExportToSTL(geodesyServices, displayServices, s57Layer, gb).exportSTL(filename, latScale, lonScale, tileSideZ);
                                    LOGGER.info("Out export GridBox3D in STL");
                                    LOGGER.info("In/Out export exportBaseSTL in STL with No support");
                                }
                            }
                            k++;
                        });
                        //SlConsShapefile
                        if (!slConsShapefiles.isEmpty()) {
                            k = 0;
                            List<Pair<Shapefile, Double>> slConsShapefilesClipped = new ArrayList<>();
                            gridBoxes.forEach((gb) -> {
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    LOGGER.log(Level.INFO, "In export SlConsShapefile in STL on filename : {0}", filename);
                                    //Retrieve avec Clip
                                    //iter sur la list
                                    //  System.out.println("slConsShapefiles : " + slConsShapefiles.size());
                                    for (Pair<File, Double> p : slConsShapefiles) {
                                        double h = p.getY();
                                        String inShp = p.getX().getAbsolutePath();
                                        String outShp = p.getX().getName();
                                        outShp = outShp.replace(".shp", "_clipped_" + i + "," + j + ".shp");
                                        String out = USER_DIR + SEP + "privateData" + SEP + "shp" + SEP + outShp;
                                        String command = "ogr2ogr -t_srs EPSG:4326 -clipdst "
                                                + gb.getLonMin() + " " + gb.getLatMin() + " " + gb.getLonMax() + " " + gb.getLatMax() + " " + out + " " + inShp;
                                        try {
                                            Proc.BUILDER.create()
                                                    .setCmd(command)
                                                    .execSh();
                                        } catch (IOException | InterruptedException ex) {
                                            Logger.getLogger(StlComponentImpl.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                                        }
                                        Shapefile shp = new Shapefile(out);
                                        if (shp.getNumberOfRecords() != 0) {
                                            slConsShapefilesClipped.add(new Pair(shp, h));
                                        }
                                    }
                                    for (Pair<Shapefile, Double> p : slConsShapefilesClipped) {
                                        //  System.out.println("p.getX() : " +p.getX());
                                        new SLConsShapefileExportToSTL(geodesyServices, jtsServices, displayServices, topologyServices,
                                                p.getX(), gb, p.getY(), s57Layer)
                                                .export(filename, verticalExaggeration, latScale, lonScale, tileSideZ);//Tide
                                    }
                                    LOGGER.log(Level.INFO, "Out export SlConsShapefile in STL on filename : " + filename, filename);
                                }
                                k++;
                            });
                        }
                        //LOW TIDE
                        k = 0;
                        if (lowTideRB.isSelected()) {
                            gridBoxes.forEach((gb) -> {
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    LOGGER.log(Level.INFO, "In export DEPARE in STL on filename : {0}", filename);
                                    Shapefile shp = new DepareDBLoader(databaseServices,
                                            s57DatabaseTF.getText(), USER, PASSWD)
                                            .retrieveIn(gb.getLatMin(), gb.getLonMin(), gb.getLatMax(), gb.getLonMax());
                                    DepareExportToSTL depareExportToSTL = new DepareExportToSTL(geodesyServices, jtsServices, displayServices, topologyServices,
                                            shp, gb, highestElevationBathy, s57Layer);
                                    System.out.println("tileSideZ : " + tileSideZ);
                                    depareExportToSTL.exportGround(filename, 1E-8, 6.0, latScale, lonScale, tileSideZ);
                                    LOGGER.log(Level.INFO, "Out export DEPARE in STL on filename : {0}", filename);
                                    k++;
                                }
                            });
                        }
                        //BUOYAGE
                        k = 0;
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("BUOYAGE")) {
                            Set<String> buoyageKeySet = BUOYAGE.ATT.keySet();
                            for (Point3DGeo[][] g : grids) {
                                LOGGER.info("In export BUOYAGE en STL");
                                buoyages.clear();
                                for (String b : buoyageKeySet) {
                                    buoyages.addAll(new BuoyageDBLoader(topologyServices, s57Connection, b, topMarkMap, marsys)
                                            .retrieveObjectsIn(g[0][0].getLatitude(),
                                                    g[0][0].getLongitude(),
                                                    g[g.length - 1][g[0].length - 1].getLatitude(),
                                                    g[g.length - 1][g[0].length - 1].getLongitude()));
                                }
                                for (Buoyage b : buoyages) {
                                    DEM dem = new DemDbLoader(elevationConnection, demDBServices)
                                            .retrieveIn(b.getLatitude() - 2 * RETRIEVE_OFFSET,
                                                    b.getLongitude() - 2 * RETRIEVE_OFFSET,
                                                    b.getLatitude() + 2 * RETRIEVE_OFFSET,
                                                    b.getLongitude() + 2 * RETRIEVE_OFFSET);
                                    b.setElevation(Double.toString(dem.getMaxElevation() * verticalExaggeration));
                                }
                                new BuoyageView(s57Layer).display(buoyages, 1.0);
                                if (generateKmlCB.isSelected()) {
                                    File src = new File(System.getProperty("user.dir") + SEP + "data" + SEP + "collada" + SEP + "buoys");
                                    File dest = new File(System.getProperty("user.dir") + SEP + DEFAULT_KML_PATH + "buoys");
                                    try {
                                        FileUtils.copyDirectory(src, dest);
                                    } catch (IOException ex) {
                                        Logger.getLogger(StlDBComponentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                                    }
                                    new BuoyageExportKML(kmlFileNames.get(k)).export(buoyages, lowestElevationAlti + tileSideZ);
                                }
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    scaleCompute(g);
                                    BuoyageExportToSTL buoyageExportSTL = new BuoyageExportToSTL(geodesyServices, g, filename, latScale, lonScale);
                                    buoyageExportSTL.export(buoyages, highestElevationBathy * verticalExaggeration, tileSideZ);
                                    LOGGER.info("Out export BUOYAGE en STL");
                                }
                                k++;
                            }
                        }
                        //LNDMRK
                        k = 0;
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("LNDMRK")) {
                            landmarks.clear();
                            for (Point3DGeo[][] g : grids) {
                                LOGGER.info("In export LNDMRK en STL");
                                landmarks.clear();
                                landmarks.addAll(new LandmarkDBLoader(topologyServices, s57Connection, marsys)
                                        .retrieveObjectsIn(g[0][0].getLatitude(),
                                                g[0][0].getLongitude(),
                                                g[g.length - 1][g[0].length - 1].getLatitude(),
                                                g[g.length - 1][g[0].length - 1].getLongitude()));
                                for (Landmark l : landmarks) {
                                    DEM dem = new DemDbLoader(elevationConnection, demDBServices)
                                            .retrieveIn(l.getLatitude() - 2 * RETRIEVE_OFFSET,
                                                    l.getLongitude() - 2 * RETRIEVE_OFFSET,
                                                    l.getLatitude() + 2 * RETRIEVE_OFFSET,
                                                    l.getLongitude() + 2 * RETRIEVE_OFFSET);
                                    l.setElevation(Double.toString(dem.getMaxElevation() * verticalExaggeration));
                                }
                                new LandmarkView(s57Layer).display(landmarks);
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    scaleCompute(g);
                                    new LandmarkExportToSTL(geodesyServices, g, filename, latScale, lonScale)
                                            .export(landmarks, verticalExaggeration * highestElevationBathy, tileSideZ);
                                }
                                k++;
                                LOGGER.info("Out export LNDMRK en STL");
                            }
                        }
                        //UWTROC
                        k = 0;
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("UWTROC")) {
                            for (Point3DGeo[][] g : grids) {
                                LOGGER.info("In export UWTROC en STL");
                                underwaterAwashRocks.clear();
                                underwaterAwashRocks.addAll(new UnderwaterAwashRockDBLoader(topologyServices, s57Connection)
                                        .retrieveObjectsIn(g[0][0].getLatitude(),
                                                g[0][0].getLongitude(),
                                                g[g.length - 1][g[0].length - 1].getLatitude(),
                                                g[g.length - 1][g[0].length - 1].getLongitude()));
                                new UnderwaterAwashRockView(s57Layer).display(underwaterAwashRocks);
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    scaleCompute(g);
                                    new UwtrocExportToSTL(geodesyServices, g, filename, latScale, lonScale)
                                            .export(underwaterAwashRocks, verticalExaggeration * highestElevationBathy, tileSideZ);
                                }
                                k++;
                                LOGGER.info("Out export UWTROC en STL");
                            }
                        }
                        //WRECKS
                        k = 0;
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("WRECKS")) {
                            for (Point3DGeo[][] g : grids) {
                                LOGGER.info("In export WRECKS en STL");
                                wrecks.clear();
                                wrecks.addAll(new WrecksDBLoader(topologyServices, s57Connection)
                                        .retrieveObjectsIn(g[0][0].getLatitude(),
                                                g[0][0].getLongitude(),
                                                g[g.length - 1][g[0].length - 1].getLatitude(),
                                                g[g.length - 1][g[0].length - 1].getLongitude()));
                                new WrecksView(s57Layer).display(wrecks);
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    scaleCompute(g);
                                    new WrecksExportSTL(geodesyServices, g, filename, latScale, lonScale)
                                            .export(wrecks, verticalExaggeration * highestElevationBathy, tileSideZ);
                                }
                                k++;
                                LOGGER.info("Out export WRECKS en STL");
                            }
                        }
                        // Buildings 
                        if (buildingsRB.isSelected()) {
                            k = 0;
                            buildingsConnection = databaseServices.connect(buildingsDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
                            for (Point3DGeo[][] g : grids) {
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    LOGGER.info("In export Buildings en STL");
                                    //  String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    String filenamebuildings = DEFAULT_STL_PATH + outFileTF.getText() + "_buildings_" + i + "," + j + ".stl";
                                    try {
                                        java.nio.file.Path path = Paths.get(filenamebuildings);
                                        Files.write(path, "".getBytes(), StandardOpenOption.CREATE);
                                    } catch (IOException ex) {
                                        Logger.getLogger(GridBox3DExportToSTL.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                                    }
                                    scaleCompute(g);
                                    List<SolidGeo> solids = buildingsExportToSTL.read(buildingsConnection, g);
                                    buildingsExportToSTL.export(solids, filenamebuildings, latScale, lonScale, tileSideZ, lowestElevationAlti);
                                    if (buildingsPreviewCB.isSelected()) {
                                        Material[] materials = {Material.GREEN, Material.BLUE, Material.YELLOW, Material.PINK,
                                            Material.CYAN, Material.MAGENTA, Material.ORANGE, Material.RED};
                                        int color = 0;
                                        for (SolidGeo solid : solids) {
                                            displayServices.displayBuildingGeoAsPolygon(solid, 0.0, s57Layer, materials[color++ % 8]);
                                        }
                                    }
                                    LOGGER.info("Out export Buildings en STL");
                                }
                                k++;
                            }
                        }
                        // DAE
                        k = 0;
                        if (boundList != null && !boundList.isEmpty()) {
                            for (Point3DGeo[][] g : grids) {
                                objects.clear();
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = DEFAULT_STL_PATH + outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    scaleCompute(g);
                                    meshExportToSTL.export(g, filename, latScale, lonScale, tileSideZ, lowestElevationAlti);
                                }
                                k++;
                            }
                        }

                        if (selectedObjects.contains("ALL") || selectedObjects.contains("ACHARE")) {
                            objects = new AnchorageAreaDBLoader(topologyServices, s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("ACHARE", topologyServices, s57Layer);
                            objects.forEach((g) -> {
                                s57Viewer.display(g);
                            });

                        }
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("COALNE")) {
                            objects = new CoastlineDBLoader(s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("COALNE", topologyServices, s57Layer);
                            objects.forEach((g) -> {
                                s57Viewer.display(g);
                            });
                        }
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("DEPCNT")) {
                            objects = new DepthContourDBLoader(s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("DEPCNT", topologyServices, bathymetryLayer);
                            objects.forEach((g) -> {
                                s57Viewer.display(g);

                            });
                        }
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("DOCARE")) {
                            objects = new DockAreaDBLoader(s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("DOCARE", topologyServices, bathymetryLayer);
                            objects.forEach((g) -> {
                                s57Viewer.display(g);
                            });
                        }
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("DRGARE")) {
                            objects = new DredgedAreaDBLoader(s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("DRGARE", topologyServices, bathymetryLayer);
                            objects.forEach((g) -> {
                                s57Viewer.display(g);
                            });
                        }

                        if (selectedObjects.contains("ALL") || selectedObjects.contains("LIGHTS")) {

                            List<Light> lights = new LightDBLoader(topologyServices, s57Connection, marsys)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            new LightView(lightsLayer).display(lights);
                        }
                        if (selectedObjects.contains("ALL") || selectedObjects.contains("NAVLNE")) {
                            objects = new NavigationLineDBLoader(s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("NAVLNE", topologyServices, s57Layer);
                            objects.forEach((g) -> {
                                s57Viewer.display(g);
                            });
                        }

                        if (selectedObjects.contains("ALL") || selectedObjects.contains("RESARE")) {
                            objects = new RestrictedAreaDBLoader(s57Connection)
                                    .retrieveObjectsIn(latMin, lonMin, latMax, lonMax);
                            s57Viewer = new S57ObjectView("RESARE", topologyServices, s57Layer);
                            objects.forEach((g) -> {
                                normalAttributes.setOutlineMaterial(new Material(new Color(197, 69, 195)));
                                s57Viewer.display(g, normalAttributes, highlightAttributes);
                            });
                        }
                        k = 0;
                        if (stlPreviewCB.isSelected()) {
                            for (Point3DGeo[][] g : grids) {
                                i = k / tileCount + 1;
                                j = k % tileCount + 1;
                                if (selectedPolygonIndexList.isEmpty() || (!selectedPolygonIndexList.isEmpty() && selectedPolygonIndexList.contains(new Pair(i, j)))) {
                                    String filename = outFileTF.getText() + "_" + i + "," + j + ".stl";
                                    filename = System.getProperty("user.dir") + SEP + DEFAULT_STL_PATH + filename;
                                    try {
                                        Thread.sleep(1000);
                                        stlComponentServices.viewSTL(filename);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(StlDBComponentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                                    }
                                }
                                k++;
                            }
                        }
                        instrumentDriverManagerServices.open(DATA_PATH + ALARM_SOUND, "true", "1");
                        selectLayer.removeAllRenderables();
                        wwd.redrawNow();
                        //}
                        LOGGER.info("Out export in STL");
                    }
                }

                if (!generateStlCB.isSelected() && generateSvgCB.isSelected()) {
                    //DEPARE for Laser CN
                    LOGGER.log(Level.INFO, "In export DEPARE in STL on filename : {0}", outFileTF.getText());
                    if (depareVisuCB.isSelected()) {
                        Shapefile shp = new DepareDBLoader(databaseServices,
                                s57DatabaseTF.getText(), USER, PASSWD)
                                .retrieveIn(latMin, lonMin, latMax, lonMax);
                        new DepareView(bathymetryLayer, s57Layer, s57Layer, 10.0, 1.0, false)
                                .display(shp);
                    }
                    Shapefile shp = new DepareDBLoader(databaseServices,
                            s57DatabaseTF.getText(), USER, PASSWD)
                            .retrieveIn(latMin, lonMin, latMax, lonMax);

                    Pair<Double, Double> scales = scaleCompute(latMin, lonMin, latMax, lonMax, svgSideY, svgSideY);  //sideY
                    DepareExportToSVG depareExportToSVG = new DepareExportToSVG(geodesyServices, topologyServices,
                            shp,
                            DEFAULT_SVG_PATH, outFileTF.getText(),
                            latMin, lonMin, svgSideY, svgSideY, scales.getX(), scales.getY(),
                            s57Layer);
                    List<Polygon> polygonList = depareExportToSVG.initExport();
                    displayServices.displayPolygons(polygonList, s57Layer, Material.GREEN);
                    List<Polygon> selectedPolygonList = stlGuiController.depareSelection(polygonList, s57Layer);

                    while (!validSVGButton.isPressed()) {
                        validSVGButton.setOnMouseClicked((MouseEvent event) -> {
                            svgMap = new HashMap<>();
                            List<SVGPath3D> shapeSVGList = depareExportToSVG.createSVGPath(selectedPolygonList);
                            for (SVGPath3D svg : shapeSVGList) {
                                double height = svg.getHeight();
                                if (!svgMap.keySet().contains(height)) {
                                    List<SVGPath3D> l = new ArrayList<>();
                                    l.add(svg);
                                    svgMap.put(height, l);
                                } else {
                                    svgMap.get(height).add(svg);
                                }
                            }
                            if (selectedObjects.contains("BUOYAGE")) {
                                Set<String> buoyageKeySet = BUOYAGE.ATT.keySet();
                                LOGGER.info("In export BUOYAGE en SVG");
                                buoyages.clear();
                                for (String b : buoyageKeySet) {
                                    buoyages.addAll(new BuoyageDBLoader(topologyServices, s57Connection, b, topMarkMap, marsys)
                                            .retrieveObjectsIn(latMin, lonMin, latMax, lonMax));
                                }
                                new BuoyageView(s57Layer).display(buoyages, 1.0);
                                depareExportToSVG.addBuoyage(shapeSVGList, buoyages);
                                LOGGER.info("Out export BUOYAGE en SVG");
                            }
                            svgMap = depareExportToSVG.processOnTop(svgMap);
                            if (!svgMap.keySet().isEmpty()) {
                                if (generateSvgCB.isSelected()) {
                                    Platform.runLater(() -> {
                                        JfxViewer jfxViewer = displayServices.getJfxViewer();
                                        jfxViewer.display(svgMap);
                                    });
                                    depareExportToSVG.write(svgMap);

                                }
                                if (generateWktCB.isSelected()) {
                                    new DepareExportWKT(DEFAULT_WKT_PATH, outFileTF.getText()).write(svgMap);
                                }
                            }
                        });
                    }
                    LOGGER.log(Level.INFO, "Out export DEPARE in SVG on filename : {0}", outFileTF.getText());
                }
            }
        }
        );
    }

    /*
    Create a list of points on a regular grid.
     */
    @SuppressWarnings("unchecked")
    private List<Point3DGeo[][]> createBathymetryTab(double latMin, double lonMin, double latMax, double lonMax) {
        bathyConnection = databaseServices.connect(bathyDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
        /*
        DEM dem = new BathyLoader(bathyConnection, bathymetryDBServices)
                .retrieveIn(latMin - RETRIEVE_OFFSET,
                        lonMin - RETRIEVE_OFFSET,
                        latMax + RETRIEVE_OFFSET,
                        lonMax + RETRIEVE_OFFSET);
         */
        //TODO create DB lon lat or lat lon, for all DB
        DEM dem = new BathyLoader(bathyConnection, bathymetryDBServices)
                .retrieveIn(lonMin - RETRIEVE_OFFSET,
                        latMin - RETRIEVE_OFFSET,
                        lonMax + RETRIEVE_OFFSET,
                        latMax + RETRIEVE_OFFSET);
        if (!dem.getGrid().isEmpty()) {
            highestElevationBathy = dem.getMaxElevation();

            Point3DGeo[][] pts = delaunayServices.toGridTab(latMin, lonMin, latMax, lonMax, 100, 100, 0.0);
            Point3DGeo[][] pts1 = jtsServices.mergePointsToGrid(dem.getGrid(), pts);

            //Transformation en tableau lat decroissantes pour GeoTiff
            int gridTmpLines = pts1.length;
            int gridTmpCols = pts1[0].length;
            Point3DGeo[][] grid = new Point3DGeo[gridTmpLines][gridTmpCols];
            for (int ii = 0; ii < gridTmpLines; ii++) {
                System.arraycopy(pts1[gridTmpLines - ii - 1], 0, grid[ii], 0, gridTmpCols);
            }

            RasterInfo rasterInfo = delaunayServices.toGridTiff(grid, "demBathy");
            displayServices.displayRasterInfo(rasterInfo, geoViewServices, GROUP);

            Point3DGeo[][] gridTmp = delaunayServices.rasterToGridTab(rasterInfo);

            //Transformation en tableau lat croissantes
            gridTmpLines = gridTmp.length;
            gridTmpCols = gridTmp[0].length;
            Point3DGeo[][] grid1 = new Point3DGeo[gridTmpLines][gridTmpCols];
            for (int ii = 0; ii < gridTmpLines; ii++) {
                System.arraycopy(gridTmp[gridTmpLines - ii - 1], 0, grid1[ii], 0, gridTmpCols);
            }

            //Mise a modulo
            int lines = tileCount * (grid1.length / tileCount);
            int cols = tileCount * (grid1[0].length / tileCount);

            Point3DGeo[][] realGrid = new Point3DGeo[lines][cols];
            for (int ii = 0; ii < lines; ii++) {
                System.arraycopy(grid1[ii], 0, realGrid[ii], 0, cols);
            }
            //Update Org
            lat0 = realGrid[0][0].getLatitude();
            lon0 = realGrid[0][0].getLongitude();
            lat1 = realGrid[lines - 1][0].getLatitude();
            lon1 = realGrid[0][cols - 1].getLongitude();

            //Elevation on the support 
            for (int ii = 0; ii < lines; ii++) {
                for (int jj = 0; jj < cols; jj++) {
                    realGrid[ii][jj].setElevation(highestElevationBathy - realGrid[ii][jj].getElevation());
                }
            }
            return createGrids(realGrid, tileCount);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No data, in the DataBase");
                alert.show();
            });
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Point3DGeo[][]> createElevationTab(double latMin, double lonMin, double latMax, double lonMax, double gridX, double gridY) {
        elevationConnection = databaseServices.connect(elevationDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
        DEM dem = new DemDbLoader(elevationConnection, demDBServices)
                .retrieveIn(latMin - RETRIEVE_OFFSET,
                        lonMin - RETRIEVE_OFFSET,
                        latMax + RETRIEVE_OFFSET,
                        lonMax + RETRIEVE_OFFSET);
        double step = elevationDbMap.get(elevationDatabaseTF.getText());
        System.out.println("step : " + step);
        if (!dem.getGrid().isEmpty()) {
            lowestElevationAlti = dem.getMinElevation();
            if (lowTideRB.isSelected()) {
                lowestElevationAlti += Double.valueOf(tidalRangeTF.getText());
            }
            List<Point3DGeo> pts = dem.getGrid();

            int incY = (int) Math.floor(gridY / step);
            int incX = (int) Math.floor(gridX / step);

            int linesOrig = dem.getDimensions().getY();
            int colOrig = dem.getDimensions().getX();

            System.out.println("linesOrig : " + linesOrig + "  colOrig : " + colOrig);
            int linesF = linesOrig / incY;
            int colsF = colOrig / incX;

            //Sampling
            List<Point3DGeo> demFList = new ArrayList<>();
            Point3DGeo[][] gridF = new Point3DGeo[linesF][colsF];
            int u = 0;
            int v = 0;
            int w = 0;

            for (int i = 0; i < linesOrig; i += incY) {
                v = 0;
                for (int j = 0; j < colOrig; j += incX) {
                    if ((u < linesF) && (v < colsF)) {
                        gridF[u][v] = pts.get((i * colOrig) + j);
                        v++;
                    }
                }
                u++;
            }
            for (int i = 0; i < linesF; i++) {
                for (int j = 0; j < colsF; j++) {
                    demFList.add(gridF[i][j]);
                }
            }
            DEM demF = new DEM(demFList);
            RasterInfo rasterInfo = delaunayServices.toGridTiff(demF, "demAlti");
            displayServices.displayRasterInfo(rasterInfo, geoViewServices, GROUP);
            Point3DGeo[][] gridTmp = delaunayServices.rasterToGridTab(rasterInfo);
            int gridTmpLines = gridTmp.length;
            int gridTmpCols = gridTmp[0].length;
            System.out.println("linesF : " + gridTmpLines);
            System.out.println("colsF : " + gridTmpCols);

            //Transformation en tableau lat croissantes
            Point3DGeo[][] grid = new Point3DGeo[gridTmpLines][gridTmpCols];
            for (int ii = 0; ii < gridTmpLines; ii++) {
                System.arraycopy(gridTmp[gridTmpLines - ii - 1], 0, grid[ii], 0, gridTmpCols);
            }

            //Update for modulo
            int lines = tileCount * (grid.length / tileCount);
            int cols = tileCount * (grid[0].length / tileCount);
            Point3DGeo[][] realGrid = new Point3DGeo[lines][cols];
            for (int ii = 0; ii < lines; ii++) {
                System.arraycopy(grid[ii], 0, realGrid[ii], 0, cols);
            }

            //Update Org
            lat0 = realGrid[0][0].getLatitude();
            lon0 = realGrid[0][0].getLongitude();
            lat1 = realGrid[lines - 1][0].getLatitude();
            lon1 = realGrid[0][cols - 1].getLongitude();

            //Elevation on the support 
            for (int ii = 0; ii < lines; ii++) {
                for (int jj = 0; jj < cols; jj++) {
                    realGrid[ii][jj].setElevation(realGrid[ii][jj].getElevation() - lowestElevationAlti + highestElevationBathy);
                }
            }
            return createGrids(realGrid, tileCount);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No data, in the DataBase");
                alert.show();
            });
            return null;
        }
    }

    /*
    Create a list of points on a regular grid.
    These grids are translate vertically with the max of depth
     */
    private List<Point3DGeo[][]> createBathymetryAndElevationTab(double latMin, double lonMin, double latMax, double lonMax) {

        Point3DGeo[][] grid;
        RasterInfo rasterInfoAlti = null;
        RasterInfo rasterInfoBathy;
        Point3DGeo[][] gridBathy1 = null;
        Point3DGeo[][] gridTmpAlti = null;
        elevationConnection = databaseServices.connect(elevationDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
        DEM demAlti = new DemDbLoader(elevationConnection, demDBServices)
                .retrieveIn(latMin - RETRIEVE_OFFSET,
                        lonMin - RETRIEVE_OFFSET,
                        latMax + RETRIEVE_OFFSET,
                        lonMax + RETRIEVE_OFFSET);
        if (!demAlti.getGrid().isEmpty()) {
            lowestElevationAlti = demAlti.getMinElevation();
            rasterInfoAlti = delaunayServices.toGridTiff(demAlti, "demAlti");
            rasterInfoAlti = gdalServices.gdalInfo(rasterInfoAlti);

            displayServices.displayRasterInfo(rasterInfoAlti, geoViewServices, GROUP);
            gridTmpAlti = delaunayServices.rasterToGridTab(rasterInfoAlti);

        }
        bathyConnection = databaseServices.connect(bathyDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
        DEM demBathy = new BathyLoader(bathyConnection, bathymetryDBServices)
                .retrieveIn(latMin - 10 * RETRIEVE_OFFSET,
                        lonMin - 10 * RETRIEVE_OFFSET,
                        latMax + 10 * RETRIEVE_OFFSET,
                        lonMax + 10 * RETRIEVE_OFFSET);

        if (!demBathy.getGrid().isEmpty()) {
            highestElevationBathy = demBathy.getMaxElevation();
            Point3DGeo[][] pts = delaunayServices.toGridTab(latMin - 10 * RETRIEVE_OFFSET, lonMin - 10 * RETRIEVE_OFFSET,
                    latMax + 10 * RETRIEVE_OFFSET, lonMax + 10 * RETRIEVE_OFFSET, 100, 100, 0.0);
            Point3DGeo[][] pts1 = jtsServices.mergePointsToGrid(demBathy.getGrid(), pts);

            //Transformation en tableau lat decroissantes pour GeoTiff
            Point3DGeo[][] gridBathy0 = new Point3DGeo[pts1.length][pts1[0].length];
            for (int ii = 0; ii < pts1.length; ii++) {
                System.arraycopy(pts1[pts1.length - ii - 1], 0, gridBathy0[ii], 0, pts1[0].length);
            }
            rasterInfoBathy = delaunayServices.toGridTiff(gridBathy0, "demBathy");
            rasterInfoBathy = gdalServices.resample(rasterInfoAlti, rasterInfoBathy);
            delaunayServices.rasterToDemTiff(rasterInfoBathy);
            displayServices.displayRasterInfo(rasterInfoBathy, geoViewServices, GROUP);
            gridBathy1 = delaunayServices.rasterToGridTab(rasterInfoBathy);
        }
        Point3DGeo[][] realGrid;
        if (gridTmpAlti != null && !demBathy.getGrid().isEmpty()) {
            int lines = gridTmpAlti.length;
            int cols = gridTmpAlti[0].length;

            for (int u = 0; u < lines; u++) {
                for (int v = 0; v < cols; v++) {
                    gridTmpAlti[u][v].setElevation(highestElevationBathy + gridTmpAlti[u][v].getElevation());
                    gridBathy1[u][v].setElevation(highestElevationBathy - gridBathy1[u][v].getElevation());
                }
            }

            grid = new Point3DGeo[lines][cols];

            for (int u = 0; u < lines; u++) {
                for (int v = 0; v < cols; v++) {
                    if (gridBathy1[u][v].getElevation() < highestElevationBathy) {
                        gridTmpAlti[u][v] = gridBathy1[u][v];
                    }
                }
            }

            for (int u = 0; u < lines; u++) {
                System.arraycopy(gridTmpAlti[lines - u - 1], 0, grid[u], 0, cols);
            }

            RasterInfo rasterInfo = delaunayServices.toGridTiff(gridTmpAlti, "dem");
            displayServices.displayRasterInfo(rasterInfo, geoViewServices, GROUP);

            //Update for modulo
            lines = tileCount * (grid.length / tileCount);
            cols = tileCount * (grid[0].length / tileCount);
            realGrid = new Point3DGeo[lines][cols];
            for (int ii = 0; ii < lines; ii++) {
                System.arraycopy(grid[ii], 0, realGrid[ii], 0, cols);
            }

            //Update Org
            lat0 = realGrid[0][0].getLatitude();
            lon0 = realGrid[0][0].getLongitude();
            lat1 = realGrid[lines - 1][0].getLatitude();
            lon1 = realGrid[0][cols - 1].getLongitude();
            return createGrids(realGrid, tileCount);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No data, in the DataBase");
                alert.show();
            });
            return null;
        }
    }

    private List<Point3DGeo[][]> createElevationAndDepare(double latMin, double lonMin, double latMax, double lonMax) {

        Point3DGeo[][] grid;
        RasterInfo rasterInfoAlti;
        Point3DGeo[][] gridTmpAlti = null;
        elevationConnection = databaseServices.connect(elevationDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
        DEM demAlti = new DemDbLoader(elevationConnection, demDBServices).retrieveIn(latMin - RETRIEVE_OFFSET,
                lonMin - RETRIEVE_OFFSET, latMax + RETRIEVE_OFFSET, lonMax + RETRIEVE_OFFSET);
        if (!demAlti.getGrid().isEmpty()) {
            lowestElevationAlti = demAlti.getMinElevation();
            rasterInfoAlti = delaunayServices.toGridTiff(demAlti, "demAlti");
            rasterInfoAlti = gdalServices.gdalInfo(rasterInfoAlti);

            displayServices.displayRasterInfo(rasterInfoAlti, geoViewServices, GROUP);
            gridTmpAlti = delaunayServices.rasterToGridTab(rasterInfoAlti);

        }
        bathyConnection = databaseServices.connect(bathyDatabaseTF.getText(), HOST, PROTOCOL, PORT, DRIVER, USER, PASSWD);
        DEM demBathy = new BathyLoader(bathyConnection, bathymetryDBServices).retrieveIn(latMin - 10 * RETRIEVE_OFFSET,
                lonMin - 10 * RETRIEVE_OFFSET, latMax + 10 * RETRIEVE_OFFSET, lonMax + 10 * RETRIEVE_OFFSET);

        if (!demBathy.getGrid().isEmpty()) {
            highestElevationBathy = demBathy.getMaxElevation();

        }
        Point3DGeo[][] realGrid;
        if (gridTmpAlti != null) {
            int lines = gridTmpAlti.length;
            int cols = gridTmpAlti[0].length;
            for (int u = 0; u < lines; u++) {
                for (int v = 0; v < cols; v++) {
                    gridTmpAlti[u][v].setElevation(highestElevationBathy + gridTmpAlti[u][v].getElevation());
                }
            }

            //Hole for DEPARE volume
            grid = new Point3DGeo[lines][cols];
            for (int u = 0; u < lines; u++) {
                for (int v = 0; v < cols; v++) {
                    if (gridTmpAlti[u][v].getElevation() == highestElevationBathy) {
                        gridTmpAlti[u][v].setElevation(0.0);
                    }
                }
            }

            for (int u = 0; u < lines; u++) {
                System.arraycopy(gridTmpAlti[lines - u - 1], 0, grid[u], 0, cols);
            }
            RasterInfo rasterInfo = delaunayServices.toGridTiff(gridTmpAlti, "dem");
            displayServices.displayRasterInfo(rasterInfo, geoViewServices, GROUP);

            //Update for modulo
            lines = tileCount * (grid.length / tileCount);
            cols = tileCount * (grid[0].length / tileCount);
            realGrid = new Point3DGeo[lines][cols];
            for (int ii = 0; ii < lines; ii++) {
                System.arraycopy(grid[ii], 0, realGrid[ii], 0, cols);
            }

            //Update Org
            lat0 = realGrid[0][0].getLatitude();
            lon0 = realGrid[0][0].getLongitude();
            lat1 = realGrid[lines - 1][0].getLatitude();
            lon1 = realGrid[0][cols - 1].getLongitude();

            return createGrids(realGrid, tileCount);
        } else {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No data, in the DataBase alti");
                alert.show();
            });
            return null;
        }
    }

    private List<Point3DGeo[][]> createGrids(Point3DGeo[][] grid, int tileCount) {
        LOGGER.log(Level.INFO, "In createGrids : {0} {1}", new Object[]{grid[0][0].getLongitude(), grid[0][grid[0].length - 1].getLongitude()});
        List<Point3DGeo[][]> gridList = new ArrayList<>();
        boolean decal = true;
        if (tileCount == 1) {
            gridList.add(grid);
        } else {
            int lines = (grid.length - 1) / tileCount;
            int cols = (grid[0].length - 1) / tileCount;
            int lonOffset = 0;
            int latOffset = 0;
            k = 0;
            for (int l = 0; l < tileCount; l++) {
                for (int cc = 0; cc < tileCount; cc++) {
                    Point3DGeo[][] newGrid = new Point3DGeo[lines][cols];
                    for (int ii = 0; ii < lines; ii++) {
                        for (int jj = 0; jj < cols; jj++) {
                            newGrid[ii][jj] = new Point3DGeo(
                                    grid[ii + l * (lines - latOffset)][jj + cc * (cols - lonOffset)].getLatitude(),
                                    grid[ii + l * (lines - latOffset)][jj + cc * (cols - lonOffset)].getLongitude(),
                                    grid[ii + l * (lines - latOffset)][jj + cc * (cols - lonOffset)].getElevation()
                            );
                        }
                    }
                    if (decal == true) {
                        lonOffset++;
                        latOffset++;
                        decal = false;
                    }
                    gridList.add(newGrid);
                    k++;
                    LOGGER.log(Level.INFO, "createGrids grids : {0} {1}", new Object[]{lines, cols});
                    LOGGER.log(Level.INFO, "createGrids : {0} {1}", new Object[]{newGrid[0][0].getLongitude(), newGrid[0][cols - 1].getLongitude()});
                }
            }
        }
        LOGGER.info("Out createGrids in STL");
        return gridList;
    }

    private void scaleCompute(Point3DGeo[][] grid) {

        double realLatMin = grid[0][0].getLatitude();
        double realLonMin = grid[0][0].getLongitude();

        double realLatMax = grid[grid.length - 1][0].getLatitude();
        double realLonMax = grid[0][grid[0].length - 1].getLongitude();
        //  System.out.println("realLatMin : " + realLatMin);
        //  System.out.println("realLonMin : " + realLonMin);
        //  System.out.println("realLatMax : " + realLatMax);
        //  System.out.println("realLonMax : " + realLonMax);

        latRangeMetric = geodesyServices.getDistanceM(realLatMin, realLonMin, realLatMax, realLonMin);
        lonRangeMetric = geodesyServices.getDistanceM(realLatMin, realLonMin, realLatMin, realLonMax);

        latScale = tileSideY / latRangeMetric;
        lonScale = tileSideX / lonRangeMetric;

        //  System.out.println("latScaleChart : " + latRangeMetric / (tileSideY * tileCount));
        // System.out.println("lonScaleChart : " + lonRangeMetric / (tileSideX * tileCount));
        Platform.runLater(() -> {
            rangeLatTF.setText(Integer.toString((int) latRangeMetric));
            rangeLonTF.setText(Integer.toString((int) lonRangeMetric));
        });

    }

    private Pair<Double, Double> scaleCompute(double latMin, double lonMin, double latMax, double lonMax, double sideX, double sideY) {

        double latRangeMetri = geodesyServices.getDistanceM(latMin, lonMin, latMax, lonMin);
        double lonRangeMetri = geodesyServices.getDistanceM(latMin, lonMin, latMin, lonMax);

        double latScal = sideY / latRangeMetri;
        double lonScal = sideX / lonRangeMetri;

        return new Pair<>(latScal, lonScal);
    }

    private void makeAttributes() {
        normalAttributes = new BasicShapeAttributes();
        normalAttributes.setOutlineMaterial(Material.RED);
        normalAttributes.setOutlineOpacity(0.5);
        normalAttributes.setOutlineWidth(1);
        normalAttributes.setDrawOutline(true);
        normalAttributes.setDrawInterior(false);
        highlightAttributes = new BasicShapeAttributes(normalAttributes);
        highlightAttributes.setOutlineOpacity(1);
        highlightAttributes.setOutlineMaterial(new Material(Color.WHITE));
    }

    public Connection getConnection() {
        return s57Connection;
    }

    @SuppressWarnings("unchecked")
    private List<Pair<Integer, Integer>> getSelectedPolygonIndexList(List<Polygon> selected) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        if (selected != null) {
            for (Polygon p : selected) {
                String value = (String) p.getValue(AVKey.DISPLAY_NAME);
                value = value.replace("tile : ", "");
                String[] id = value.split(",");
                result.add(new Pair(Integer.parseInt(id[0].trim()), Integer.parseInt(id[1].trim())));
            }
        }
        return result;
    }
}
