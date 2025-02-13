/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.stl.charts.impl.controller.bathy.dem;

import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.bathymetry.db.BathymetryDBServices;
import bzh.terrevirtuelle.navisu.bathymetry.view.DisplayBathymetryServices;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polygon;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author serge
 */
public class BathyDemStlController {

    protected static final Logger LOGGER = Logger.getLogger(BathyDemStlController.class.getName());
    protected BathymetryDBServices bathymetryDBServices;
    protected DisplayBathymetryServices displayBathymetryServices;
    protected GuiAgentServices guiAgentServices;
    protected Polygon polyEnvelope;
    protected List<? extends Position> positions;
    protected Path pathname;
    protected boolean latLon;
    protected WorldWindow wwd = GeoWorldWindViewImpl.getWW();
    protected RenderableLayer layer;
    protected double maxElevation = 0.0;

    public BathyDemStlController(BathymetryDBServices bathymetryDBServices,
            DisplayBathymetryServices displayBathymetryServices,
            GuiAgentServices guiAgentServices, RenderableLayer layer,
            Polygon polyEnvelope) {
        this.bathymetryDBServices = bathymetryDBServices;
        this.displayBathymetryServices = displayBathymetryServices;
        this.guiAgentServices = guiAgentServices;
        this.layer = layer;
        this.polyEnvelope = polyEnvelope;

        Iterable<? extends LatLon> bounds = polyEnvelope.getOuterBoundary();
        List<LatLon> listLatLon = new ArrayList<>();
        for (LatLon s : bounds) {
            listLatLon.add(s);
        }

        double latMin = listLatLon.get(0).getLatitude().getDegrees();
        double lonMin = listLatLon.get(0).getLongitude().getDegrees();
        double latMax = listLatLon.get(2).getLatitude().getDegrees();
        double lonMax = listLatLon.get(2).getLongitude().getDegrees();
        List<Point3DGeo> points = bathymetryDBServices.retrieveIn("bathy", latMin, lonMin, latMax, lonMax);

        points.stream().filter((p) -> (maxElevation < p.getElevation())).forEachOrdered((p) -> {
            maxElevation = p.getElevation();
        });

    }

    public void writeDem(List<? extends Position> positions, Path pathname, boolean latLon) {
        double latMin = positions.get(0).getLatitude().getDegrees();
        double lonMin = positions.get(0).getLongitude().getDegrees();
        double latMax = positions.get(2).getLatitude().getDegrees();
        double lonMax = positions.get(2).getLongitude().getDegrees();

        List<Point3DGeo> points = bathymetryDBServices.retrieveIn("bathy", latMin, lonMin, latMax, lonMax);

    }

    public double getMaxElevation() {
        return maxElevation;
    }

}
