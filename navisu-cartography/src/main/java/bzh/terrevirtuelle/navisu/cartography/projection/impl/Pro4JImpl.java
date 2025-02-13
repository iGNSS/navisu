/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.cartography.projection.impl;

import bzh.terrevirtuelle.navisu.cartography.projection.Pro4J;
import bzh.terrevirtuelle.navisu.cartography.projection.Pro4JServices;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import bzh.terrevirtuelle.navisu.geometry.objects3D.obj.ObjComponentServices;
import java.util.ArrayList;
import java.util.List;
import org.capcaval.c3.component.ComponentState;
import org.capcaval.c3.component.annotation.UsedService;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.ProjCoordinate;

/**
 * @date @author Serge Morvan
 */
public class Pro4JImpl
        implements Pro4J, Pro4JServices, ComponentState {

    @UsedService
    ObjComponentServices objComponentServices;

    @Override
    public Point3DGeo convertLambert93ToWGS84(double lat, double lon) {
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:4326");//4326
        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:2154");//Lambert93

        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);

        // Note these are x, y so lng, lat
        ProjCoordinate srcCoord = new ProjCoordinate(lon, lat);
        ProjCoordinate dstCoord = new ProjCoordinate();

        transform.transform(srcCoord, dstCoord);
        return new Point3DGeo(dstCoord.y, dstCoord.x, 0.0);
    }

    /*
    input is xyz format (lon,lat)
    output is in LatLon format
     */
    @Override
    public String convertLambert93ToWGS84(String xyz, double xOffset, double yOffset) {
        String result = "";
        String tmp = xyz.replace("v ", "").trim();
        String[] tab = tmp.split("\\s+");
        double x = Double.valueOf(tab[0]) + xOffset;
        double y = Double.valueOf(tab[1]) + yOffset;
        double z = Double.valueOf(tab[2]);
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:4326");//4326
        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:2154");//Lambert93

        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);

        // Note these are x, y so lng, lat
        ProjCoordinate srcCoord = new ProjCoordinate(x, y);
        ProjCoordinate dstCoord = new ProjCoordinate();

        transform.transform(srcCoord, dstCoord);

        double xx = dstCoord.x;
        double yy = dstCoord.y;
        //  result = "v " + dstCoord.y + " " + dstCoord.x + " " + z;
        result = "v " + xx + " " + yy + " " + z;
        return result;
    }

    @Override
    public Point3DGeo convertLambert93ToWGS84(Point3DGeo pt) {
        return convertLambert93ToWGS84(pt.getLatitude(), pt.getLongitude());

    }

    @Override
    public List<Point3DGeo> convertLambert93ToWGS84(List<Point3DGeo> pts) {
        List<Point3DGeo> result = new ArrayList<>();
        pts.forEach((p) -> {
            result.add(convertLambert93ToWGS84(p));
        });
        return result;
    }

    @Override
    public Point3DGeo convertCoordinates(String epsgSrc, String epsgdest, Point3DGeo pt) {
        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem srcCrs = factory.createFromName(epsgSrc);
        CoordinateReferenceSystem dstCrs = factory.createFromName(epsgdest);

        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);

        // Note these are x, y so lng, lat
        ProjCoordinate srcCoord = new ProjCoordinate(pt.getLongitude(), pt.getLatitude());
        ProjCoordinate dstCoord = new ProjCoordinate();

        transform.transform(srcCoord, dstCoord);
        return new Point3DGeo(dstCoord.y, dstCoord.x, pt.getElevation());
    }

    @Override
    public String convertObjLambert93ToObjWGS84(String filename) {
        String filenameResult = null;

        return filenameResult;
    }

    @Override
    public void componentInitiated() {
    }

    @Override
    public void componentStarted() {
    }

    @Override
    public void componentStopped() {
    }

}
