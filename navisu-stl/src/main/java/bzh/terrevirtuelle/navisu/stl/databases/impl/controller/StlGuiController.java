/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.stl.databases.impl.controller;

import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.geometry.geodesy.GeodesyServices;
import bzh.terrevirtuelle.navisu.visualization.view.DisplayServices;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.SVGPath;

/**
 *
 * @author serge
 */
public class StlGuiController {

    protected StlDBComponentController stlDBComponentController;

    protected double latRange;
    protected double lonRange;
    protected double latRangeMetric;
    protected double lonRangeMetric;
    protected double latScale;
    protected double lonScale;
    protected double globalScale;

    protected String LAT_MIN;
    protected String LON_MIN;
    protected String LAT_MAX;
    protected String LON_MAX;
    protected double lat0 = 520;
    protected double lon0;
    protected double lat1;
    protected double lon1;
    protected Label latMinLabel;
    protected Label lonMinLabel;
    protected Label latMaxLabel;
    protected Label lonMaxLabel;
    protected String CACHE_FILE_NAME;
    protected Properties cacheProperties = new Properties();
    protected RenderableLayer layer;
    protected double tileSideX;
    protected double tileSideY;
    protected double tileSideZ;
    protected int tileCount = 1;

    protected GeodesyServices geodesyServices;
    protected DisplayServices displayServices;

    protected TextField objectsTF;
    protected Map<String, CheckBox> s57SelectionMap;
    protected List<String> selectedObjects;
    protected List<Polygon> selectedPolygons;
    protected List< SVGPath> shapeSVGList;
    protected List<Polygon> polygonList;
    protected CheckBox allCB;
    protected WorldWindow wwd = GeoWorldWindViewImpl.getWW();

    public StlGuiController(StlDBComponentController stlDBComponentController,
            GeodesyServices geodesyServices,
            DisplayServices displayServices,
            Map<String, CheckBox> s57SelectionMap, CheckBox allCB,
            List<String> selectedObjects,
            TextField objectsTF,
            String latMin, String lonMin, String latMax, String lonMax,
            double lat0, double lon0, double lat1, double lon1,
            Label latMinLabel, Label lonMinLabel, Label latMaxLabel, Label lonMaxLabel,
            String caheFileName, RenderableLayer layer, List<Polygon> selectedPolygons) {
        this.stlDBComponentController = stlDBComponentController;
        this.displayServices = displayServices;
        this.geodesyServices = geodesyServices;
        this.s57SelectionMap = s57SelectionMap;
        this.allCB = allCB;
        this.selectedObjects = selectedObjects;
        this.objectsTF = objectsTF;
        this.LAT_MIN = latMin;
        this.LON_MIN = lonMin;
        this.LAT_MAX = latMax;
        this.LON_MAX = lonMax;
        this.lat0 = lat0;
        this.lon0 = lon0;
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.latMinLabel = latMinLabel;
        this.lonMinLabel = lonMinLabel;
        this.latMaxLabel = latMaxLabel;
        this.lonMaxLabel = lonMaxLabel;
        this.CACHE_FILE_NAME = caheFileName;
        this.layer = layer;
        this.selectedPolygons = selectedPolygons;

    }

    public void initSelectedZone() {
        InputStream input;
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

        Dialog dialog = new Dialog<>();
        dialog.setTitle("Create Area");
        dialog.setHeaderText("Please enter selected area coordinates.");
        dialog.setResizable(false);

        Label lat0Label = new Label("Southwest point latitude : ");
        Label lon0Label = new Label("Southwest point longitude : ");
        Label lat1Label = new Label("Northeast point latitude : ");
        Label lon1Label = new Label("Northeast point longitude : ");

        TextField lat0TF = new TextField();
        TextField lat1TF = new TextField();
        TextField lon0TF = new TextField();
        TextField lon1TF = new TextField();

        //Default values
        lat0TF.setText(LAT_MIN);
        lat1TF.setText(LAT_MAX);
        lon0TF.setText(LON_MIN);
        lon1TF.setText(LON_MAX);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 35, 20, 35));
        grid.add(lat0Label, 0, 0);
        grid.add(lat0TF, 1, 0);
        grid.add(lon0Label, 0, 1);
        grid.add(lon0TF, 1, 1);

        grid.add(lat1Label, 0, 2);
        grid.add(lat1TF, 1, 2);
        grid.add(lon1Label, 0, 3);
        grid.add(lon1TF, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                lat0 = Double.valueOf(lat0TF.getText().trim());
                lon0 = Double.valueOf(lon0TF.getText().trim());
                lat1 = Double.valueOf(lat1TF.getText().trim());
                lon1 = Double.valueOf(lon1TF.getText().trim());
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range latitude : -90° <= Latitude <= 90° \n"
                        + "Range longitude : -180° <= Longitude <= 180°");
                alert.show();
            }
            if (lat0 < -90.0 || lat0 > 90.0 || lat1 < -90.0 || lat1 > 90.0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range latitude : -90° <= Latitude <= 90°");
                alert.show();
            }
            if (lon0 < -180.0 || lon0 > 180.0 || lon1 < -180.0 || lon1 > 180.0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Range longitude : -180° <= Longitude <= 180°");
                alert.show();
            }
            if (lon1 < lon0 || lat1 < lat0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Right inputs are LatMin, LonMin & LatMax, LonMax");
                alert.show();
            }

            double latRan = geodesyServices.getDistanceM(lat0, lon0, lat1, lon0);
            double lonRan = geodesyServices.getDistanceM(lat0, lon0, lat0, lon1);
            if (latRan > lonRan) {
                Position position = geodesyServices.getPosition(lat1, lon0, 90.0, latRan);
                lon1 = position.getLongitude().getDegrees();
            } else {
                Position position = geodesyServices.getPosition(lat0, lon1, 0.0, lonRan);
                lat1 = position.getLatitude().getDegrees();
            }
            event.consume();
            dialog.close();
            saveProperties(lat0, lon0, lat1, lon1);
            
            selectedPolygons.addAll(createAndDisplayTiles(layer, Material.RED, 100, lat0, lon0, lat1, lon1, tileCount, tileCount));
            stlDBComponentController.setLat0(lat0);
            stlDBComponentController.setLon0(lon0);
            stlDBComponentController.setLat1(lat1);
            stlDBComponentController.setLon1(lon1);
        });

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.showAndWait();
        selectedPolygons.addAll(createAndDisplayTiles(layer, Material.RED, 100, lat0, lon0, lat1, lon1, tileCount, tileCount));

    }

    public void saveProperties(double lat0, double lon0, double lat1, double lon1) {
        latMinLabel.setText(Double.toString(lat0));
        lonMinLabel.setText(Double.toString(lon0));
        latMaxLabel.setText(Double.toString(lat1));
        lonMaxLabel.setText(Double.toString(lon1));
        LAT_MIN = Double.toString(lat0);
        LON_MIN = Double.toString(lon0);
        LAT_MAX = Double.toString(lat1);
        LON_MAX = Double.toString(lon1);
        OutputStream output;
        Properties properties = new Properties();
        try {
            output = new FileOutputStream(CACHE_FILE_NAME);
            properties.setProperty("LAT_MIN", LAT_MIN);
            properties.setProperty("LON_MIN", LON_MIN);
            properties.setProperty("LAT_MAX", LAT_MAX);
            properties.setProperty("LON_MAX", LON_MAX);
            properties.store(output, null);
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(StlDBComponentController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
    }

    public String initScale(double latMin, double lonMin,
            double latMax, double lonMax) {
        latRangeMetric = geodesyServices.getDistanceM(latMin, lonMin, latMax, lonMin);
        lonRangeMetric = geodesyServices.getDistanceM(latMin, lonMin, latMin, lonMax);
        double scaleLat = latRangeMetric / (tileSideY / 1000.0);
        double scaleLon = lonRangeMetric / (tileSideX / 1000.0);

        globalScale = (scaleLat + scaleLon) / 2;//Arrondi pour l'affichage
        globalScale /= tileCount;
        double sc = 1;
        if (globalScale <= 1000) {
            sc = 100;
        } else if (globalScale > 1000 && globalScale <= 10000) {
            sc = 1000;
        } else if (globalScale > 10000 && globalScale <= 100000) {
            sc = 1000;
        } else if (globalScale > 100000 && globalScale <= 1000000) {
            sc = 10000;
        } else if (globalScale > 1000000 && globalScale <= 10000000) {
            sc = 100000;
        }
        globalScale /= sc;
        return "1/" + Integer.toString((int) (Math.round(globalScale) * sc));
    }

    public List<Polygon> createAndDisplayTiles(RenderableLayer layer, Material material,
            double hight, double latMin, double lonMin, double latMax, double lonMax,
            int line, int col) {
        if (layer.getNumRenderables() >= 1) {
            layer.removeAllRenderables();
        }

        latRange = latMax - latMin;
        lonRange = lonMax - lonMin;

        latRange /= line;
        lonRange /= col;

        latRange = Math.abs(latRange);
        lonRange = Math.abs(lonRange);

        double orgLat = latMin;
        double orgLon = lonMin;

        int indexLabel0;
        int indexLabel1;
        List<Polygon> tiles = new ArrayList<>();
        for (int i = 0; i < line; i++) {
            for (int j = 0; j < col; j++) {
                List<Position> positions = new ArrayList<>();
                positions.add(new Position(Angle.fromDegrees(orgLat + i * latRange), Angle.fromDegrees(orgLon + j * lonRange), hight));
                positions.add(new Position(Angle.fromDegrees(orgLat + i * latRange), Angle.fromDegrees(orgLon + (j + 1) * lonRange), hight));
                positions.add(new Position(Angle.fromDegrees(orgLat + (i + 1) * latRange), Angle.fromDegrees(orgLon + (j + 1) * lonRange), hight));
                positions.add(new Position(Angle.fromDegrees(orgLat + (i + 1) * latRange), Angle.fromDegrees(orgLon + j * lonRange), hight));
                positions.add(new Position(Angle.fromDegrees(orgLat + i * latRange), Angle.fromDegrees(orgLon + j * lonRange), hight));
                Polygon polygon = new Polygon(positions);

                BasicShapeAttributes normalAttributes = new BasicShapeAttributes();
                normalAttributes.setInteriorMaterial(material);
                normalAttributes.setOutlineOpacity(0.5);
                normalAttributes.setInteriorOpacity(0.1);
                normalAttributes.setOutlineMaterial(material);
                normalAttributes.setOutlineWidth(2);
                normalAttributes.setDrawOutline(true);
                normalAttributes.setDrawInterior(true);
                normalAttributes.setInteriorOpacity(0.02);
                normalAttributes.setEnableLighting(true);

                BasicShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
                highlightAttributes.setOutlineMaterial(material);
                highlightAttributes.setOutlineOpacity(1);
                highlightAttributes.setInteriorMaterial(material);
                highlightAttributes.setDrawInterior(true);
                highlightAttributes.setInteriorOpacity(0.2);

                indexLabel0 = i + 1;
                indexLabel1 = j + 1;

                polygon.setValue(AVKey.DISPLAY_NAME, "tile : " + indexLabel0 + "," + indexLabel1);

                polygon.setAltitudeMode(WorldWind.ABSOLUTE);
                polygon.setAttributes(normalAttributes);
                polygon.setHighlightAttributes(highlightAttributes);
                tiles.add(polygon);
            }
            layer.addRenderables(tiles);
            wwd.redrawNow();
        }
        return tiles;

    }

    public void initS57Gui() {
        s57SelectionMap.keySet().forEach((cb) -> {
            s57SelectionMap.get(cb).setOnAction((ActionEvent event) -> {
                initS57Gui(cb);
            });
        });
    }

    public void initS57Gui(String selected) {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (!selectedObjects.contains("ALL")) {
                    if (selected.contains("ALL")) {
                        selectedObjects.clear();
                        selectedObjects.add("ALL");
                        objectsTF.setText("");
                        for (String s : s57SelectionMap.keySet()) {
                            objectsTF.appendText(s + " ; ");
                            s57SelectionMap.get(s).setSelected(true);
                        }
                    } else {
                        if (selectedObjects.contains(selected)) {
                            selectedObjects.remove(selected);
                            objectsTF.setText(objectsTF.getText().replace(selected + " ; ", ""));
                        } else {
                            selectedObjects.add(selected);
                            objectsTF.appendText(selected + " ; ");
                        }
                    }
                } else {//selectedObjects.contains("ALL")
                    if (selected.contains("ALL")) {
                        selectedObjects.clear();
                        selectedObjects.remove("ALL");
                        objectsTF.setText("");
                        for (String s : s57SelectionMap.keySet()) {;
                            s57SelectionMap.get(s).setSelected(false);
                        }
                    } else {
                        selectedObjects.remove("ALL");
                        objectsTF.setText("");
                        allCB.setSelected(false);
                        for (String s : s57SelectionMap.keySet()) {
                            objectsTF.appendText(s + " ; ");
                            selectedObjects.add(s);
                        }
                        if (selectedObjects.contains(selected)) {
                            selectedObjects.remove(selected);
                            s57SelectionMap.get(selected).setSelected(false);
                            objectsTF.setText(objectsTF.getText().replace(selected + " ; ", ""));
                        } else {
                            selectedObjects.add(selected);
                            objectsTF.appendText(selected + " ; ");
                        }
                    }
                }
            }

        });

    }

    public void displayGuiGridBM(RenderableLayer layer) {

        //Position origPos = new Position(Angle.fromDegrees(48.33713333), Angle.fromDegrees(-4.63665), 100.0);//21/12/18
        Position origPos = new Position(Angle.fromDegrees(48.33748333), Angle.fromDegrees(-4.63665), 100.0);
        double x = 820;
        double y = 820;
        int xNbWest = 0;
        int xNbEast = 29;//27
        int yNbSouth = 5;
        int yNbNorth = 17;//9
        double azimuth = 5.614477142;//5.614477142;
        double azimuthS = 180 - azimuth;
        double bearing = 90 - azimuth;
        Position[][] posTab = new Position[yNbSouth + yNbNorth][xNbWest + xNbEast];
        for (int i = 0; i < yNbSouth; i++) {
            Position position = geodesyServices.getPosition(origPos, azimuthS, y * (yNbSouth - i - 1), 100.0);
            for (int j = 0; j < xNbEast; j++) {
                posTab[i][j] = geodesyServices.getPosition(position, bearing, x * j, 100.0);
            }
        }
        azimuthS = 360 - azimuth;
        for (int i = yNbSouth; i < yNbNorth + yNbSouth; i++) {
            Position position = geodesyServices.getPosition(origPos, azimuthS, y * (i - yNbSouth + 1), 100.0);
            for (int j = 0; j < xNbEast; j++) {
                posTab[i][j] = geodesyServices.getPosition(position, bearing, x * j, 100.0);
            }
        }

        Material material = new Material(Color.MAGENTA);

        ShapeAttributes normalAttributes = new BasicShapeAttributes();
        normalAttributes.setInteriorMaterial(material);
        normalAttributes.setDrawInterior(true);
        normalAttributes.setInteriorOpacity(0.01);
        normalAttributes.setOutlineMaterial(material);

        BasicShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
        highlightAttributes.setOutlineMaterial(material);
        highlightAttributes.setOutlineOpacity(1);
        highlightAttributes.setInteriorMaterial(material);
        highlightAttributes.setInteriorOpacity(0.2);
        layer.setPickEnabled(true);
        addListener();
        List<Polygon> polygons = new ArrayList<>();
        List<String> exluded = Arrays.asList(
                "55-115", "56-115", "57-115", "58-115", "59-115", "60-115", "61-115", "62-115", "63-115", "64-115", "65-115", "66-115", "67-115", "68-115", "69-115", "70-115",
                "72-115", "73-115", "74-115", "75-115", "76-115", "77-115", "78-115", "79-115", "80-115", "81-115", "82-115", "83-115", "84-115", "85-115",
                "55-114", "56-114", "57-114", "58-114", "59-114", "60-114", "61-114", "62-114", "63-114", "64-114", "65-114", "66-114", "67-114", "68-114", "69-114", "70-114",
                "73-114", "74-114", "75-114", "76-114", "77-114", "78-114", "79-114", "80-114", "81-114", "82-114", "83-114", "84-114", "85-114",
                "55-113", "56-113", "57-113", "58-113", "59-113", "60-113", "61-113", "62-113", "63-113", "64-113", "65-113", "66-113", "69-113", "80-113", "81-113", "82-113", "83-113", "84-113", "85-113",
                "55-112", "56-112", "57-112", "58-112", "59-112", "60-112", "64-112", "82-112",
                "55-111", "56-111", "57-111", "58-111", "59-111", "60-11", "82-111",
                "55-110", "56-110", "57-110", "58-110", "82-110",
                "55-109", "56-109", "57-109", "58-109", "59-109", "82-109",
                "55-108", "56-108", "57-108", "58-108", "82-108",
                "55-106", "60-106",
                "60-105", "81-105", "82-105",
                "55-104", "82-104",
                "55-103", "56-103", "74-103", "75-103", "82-103",
                "55-102", "68-102", "69-102", "70-102", "71-102", "72-102", "73-102", "74-102",
                "55-101", "66-101", "67-101", "68-101", "69-101", "70-101", "71-101", "72-101", "73-101",
                "55-100", "63-100", "65-100", "66-100", "67-100", "68-100", "69-100", "70-100", "71-100", "72-100",
                "55-99", "61-99", "62-99", "63-99", "64-99", "65-99", "66-99", "67-99", "68-99", "69-99", "70-99", "71-99",
                "55-98", "58-98", "59-98", "60-98", "61-98", "62-98", "63-98", "64-98", "65-98", "66-98", "67-98", "68-98", "69-98", "70-98", "71-98", "82-98",
                "55-97", "56-97", "57-97", "58-97", "59-97", "60-97", "61-97", "62-97", "63-97", "64-97", "65-97", "66-97", "67-97", "68-97", "69-97", "70-97", "71-97", "82-97", "85-97",
                "55-96", "56-96", "57-96", "58-96", "59-96", "60-96", "61-96", "62-96", "63-96", "64-96", "65-96", "66-96", "67-96", "68-96", "69-96", "80-96", "81-96", "82-96", "85-96",
                "55-95", "56-95", "57-95", "58-95", "59-95", "60-95", "61-95", "62-95", "63-95", "64-95", "65-95", "66-95", "67-95", "68-95", "69-95", "70-95", "73-95", "74-95", "79-95", "80-95", "81-95", "82-95", "85-95"
        );
        for (int i = 0; i < yNbSouth + yNbNorth - 1; i++) {
            for (int j = 0; j < posTab[1].length - 1; j++) {
                int valX = i + 95;
                int valY = j + 55;
                String label = valY + "-" + valX;
                if (!exluded.contains(label)) {
                    List<Position> positions = new ArrayList<>();
                    positions.add(posTab[i][j]);
                    positions.add(posTab[i][j + 1]);
                    positions.add(posTab[i + 1][j + 1]);
                    positions.add(posTab[i + 1][j]);
                    positions.add(posTab[i][j]);
                    Polygon polygon = new Polygon(positions);
                    polygon.setAltitudeMode(WorldWind.ABSOLUTE);
                    polygon.setAttributes(normalAttributes);
                    polygon.setHighlightAttributes(highlightAttributes);
                    polygon.setValue("Key", label);
                    polygon.setValue(AVKey.DISPLAY_NAME, label);
                    polygons.add(polygon);
                }
            }
        }
        layer.addRenderables(polygons);
        wwd.redrawNow();
    }

    private void addListener() {
        Material material = new Material(Color.MAGENTA);
        ShapeAttributes pickedAttributes = new BasicShapeAttributes();
        pickedAttributes.setInteriorMaterial(material);
        pickedAttributes.setDrawInterior(true);
        pickedAttributes.setInteriorOpacity(1.0);
        pickedAttributes.setOutlineMaterial(material);
        wwd.addSelectListener((SelectEvent event) -> {
            Object o = event.getTopObject();
            if (event.isLeftClick() && o != null) {
                if (o.getClass() == Polygon.class) {
                    Polygon polygon = ((Polygon) o);
                    polygon.setAttributes(pickedAttributes);
                    polygon.setHighlightAttributes(pickedAttributes);
                    wwd.redrawNow();
                }
            }
        });
    }

    public List<Polygon> depareSelection(List<Polygon> shapes, RenderableLayer l) {
        Material material = new Material(Color.MAGENTA);
        ShapeAttributes pickedAttributes = new BasicShapeAttributes();
        pickedAttributes.setInteriorMaterial(material);
        pickedAttributes.setDrawInterior(true);
        pickedAttributes.setInteriorOpacity(1.0);
        pickedAttributes.setOutlineMaterial(material);
        wwd.addSelectListener((SelectEvent event) -> {
            Stack<Polygon> unDo = new Stack<>();
            Object o = event.getTopObject();
            if (event.isLeftClick() && o != null) {
                if (o.getClass() == Polygon.class) {
                    Polygon obj = ((Polygon) o);
                    obj.setAttributes(pickedAttributes);
                    obj.setHighlightAttributes(pickedAttributes);
                    shapes.remove(obj);
                    unDo.push(obj);
                    layer.removeRenderable(obj);
                    wwd.redrawNow();
                }
            }
        });
        return shapes;
    }

    public List<Polygon> getSelectedPolygons(List<Polygon> tiles) {
        List<Polygon> selected = new ArrayList<>();
        Material material = new Material(Color.MAGENTA);
        ShapeAttributes pickedAttributes = new BasicShapeAttributes();
        pickedAttributes.setInteriorMaterial(material);
        pickedAttributes.setDrawInterior(true);
        pickedAttributes.setInteriorOpacity(1.0);
        pickedAttributes.setOutlineMaterial(material);

        ShapeAttributes unPickedAttributes = new BasicShapeAttributes();
        unPickedAttributes.setInteriorMaterial(material);
        unPickedAttributes.setDrawInterior(true);
        unPickedAttributes.setInteriorOpacity(0.1);
        unPickedAttributes.setOutlineMaterial(material);

        wwd.addSelectListener((SelectEvent event) -> {
            Object o = event.getTopObject();
            if (event.isLeftClick() && o != null) {
                if (o.getClass() == Polygon.class) {
                    Polygon polygon = ((Polygon) o);
                    if (tiles.contains(polygon)) {
                        if (selected.contains(polygon)) {
                            selected.remove(polygon);
                            polygon.setAttributes(unPickedAttributes);
                            polygon.setHighlightAttributes(unPickedAttributes);
                        } else {
                            selected.add(polygon);
                            polygon.setAttributes(pickedAttributes);
                        }
                        wwd.redrawNow();
                    }
                }
            }
        });
        return selected;
    }

    public void showRealselected(List<Polygon> polygons, List<Polygon> selected) {
        Material material = new Material(Color.GREEN);

        ShapeAttributes pickedAttributes = new BasicShapeAttributes();
        pickedAttributes.setInteriorMaterial(material);
        pickedAttributes.setDrawInterior(true);
        pickedAttributes.setInteriorOpacity(0.1);
        pickedAttributes.setOutlineMaterial(material);

        ShapeAttributes unPickedAttributes = new BasicShapeAttributes();
        unPickedAttributes.setDrawInterior(false);
        unPickedAttributes.setOutlineMaterial(material);

        for (Polygon p : polygons) {
            p.setAttributes(unPickedAttributes);
            wwd.redrawNow();
        }
        if (selected != null && !selected.isEmpty()) {
            for (Polygon p : selected) {
                p.setAttributes(pickedAttributes);
                wwd.redrawNow();
            }
        }
    }

    public void setTileSideX(double tileSideX) {
        this.tileSideX = tileSideX;
    }

    public void setTileSideY(double tileSideY) {
        this.tileSideY = tileSideY;
    }

    public void setTileSideZ(double tileSideZ) {
        this.tileSideZ = tileSideZ;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }

    public void initTile(double tileSideX, double tileSideY, double tileSideZ, int tileCount) {
        this.tileSideX = tileSideX;
        this.tileSideY = tileSideY;
        this.tileSideZ = tileSideZ;
        this.tileCount = tileCount;
    }

    public List<Polygon> getPolygonList() {
        return polygonList;
    }

}
