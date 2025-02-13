/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.visualization.view;

import bzh.terrevirtuelle.navisu.app.guiagent.geoview.GeoViewServices;
import bzh.terrevirtuelle.navisu.domain.geometry.FaceGeo;
import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import bzh.terrevirtuelle.navisu.domain.geometry.SolidGeo;
import bzh.terrevirtuelle.navisu.domain.raster.RasterInfo;
import bzh.terrevirtuelle.navisu.geometry.delaunay.triangulation.Triangle_dt;
import bzh.terrevirtuelle.navisu.geometry.objects3D.GridBox3D;
import bzh.terrevirtuelle.navisu.visualization.view.impl.controller.JfxViewer;
import com.vividsolutions.jts.geom.Geometry;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.util.List;
import java.util.Map;
import org.capcaval.c3.component.ComponentService;

/**
 * @date 15 aout 2017
 * @author Serge Morvan
 */
public interface DisplayServices
        extends ComponentService {

    void displayPoints3D(List<Point3DGeo> points, RenderableLayer layer);

    void displayPoints3DAsPath(List<Point3DGeo> points, RenderableLayer layer);

    void displayPoints3DAsPolygon(List<Point3DGeo> points, double height, RenderableLayer layer, Material material);

    void displayFaceGeoAsPolygon(FaceGeo face, double height, RenderableLayer layer, Material material);

    void displaySolidGeoAsPolygon(SolidGeo solid, double height, RenderableLayer layer, Material material);

    void displayBuildingGeoAsPolygon(SolidGeo solid, double height, RenderableLayer layer, Material material);

    void displayPositionsAsPath(List<Position> points, RenderableLayer layer, Material material);

    void displayPoints3DAsPath(List<Point3DGeo> points, double height, RenderableLayer layer, Material material);

    void displayPoints3DAsTriangles(List<Point3DGeo> points, RenderableLayer layer, Material material);

    void displayPaths(List<Path> paths, double elevation, RenderableLayer layer, Material material);

    void displayPath(Path path, double elevation, RenderableLayer layer, Material material);

    void displayPaths(List<Path> paths, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayPolygonsFromPaths(List<Path> paths, RenderableLayer layer, Material material);

    void displayPaths(GridBox3D gridBox3D, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayGridBox3D(GridBox3D gridBox3D, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayPaths(List<Path> points, RenderableLayer layer, Material material, double verticalExaggeration, double verticalOffset);

    void displayPolygons(List<Polygon> poly, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayPolygons(List<Polygon> poly, RenderableLayer layer, Material material);

    void displayPolygons(List<Polygon> poly, RenderableLayer layer, ShapeAttributes attr);

    void displayPolygon(Polygon poly, RenderableLayer layer, Material material);

    void displayPolygon(Polygon poly, RenderableLayer layer, ShapeAttributes attr);

    void displayPolygonsFromPaths(List<Path> paths, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayPolygons(List<Polygon> points, RenderableLayer layer, Material material, double verticalExaggeration, double verticalOffset);

    void displayGrid(List<List<Point3DGeo>> latLons, RenderableLayer layer, Material material);

    void displayGrid(Point3DGeo[][] latLons, RenderableLayer layer, Material material, double verticalExaggeration);

    List<Path> displayGridAsTriangles(Point3DGeo[][] latLons, RenderableLayer layer, Material material, double verticalExaggeration);

    List<List<Path>> displayGridAsTriangles(List<Point3DGeo[][]> latLons, RenderableLayer layer, double verticalExaggeration);

    void displayGrid(GridBox3D gridBox3D, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayGridAsPolygon(Point3DGeo[][] latLons, RenderableLayer layer, Material material, double verticalExaggeration);

    void displayPlane(double minLat, double minLon, double maxLat, double maxLon, double height,
            Material material, RenderableLayer l);

    void displayTriangle(Triangle_dt t, double height, double verticalExaggeration,
            Material material, RenderableLayer l);

    void displayDelaunay(List<Triangle_dt> triangles,
            double height, double verticalExaggeration,
            Material material, RenderableLayer l);

    void displayRasterInfo(RasterInfo rasterInfo, GeoViewServices geoViewServices, String GROUP);

    void displayConcaveHull(Geometry concaveHull,
            double height, double verticalExaggeration,
            Material material, RenderableLayer l);

    Path createPath(List<Position> pathPositions, Material material);

    List<Path> createPaths(Point3DGeo[][] latLons, double verticalExaggeration);

    List<List<Path>> createPaths(List<Point3DGeo[][]> latLons, double verticalExaggeration);

    List<List<Path>> createPaths(GridBox3D box3D, double verticalExaggeration);

    List<Polygon> createPolygons(List<Path> paths);

    Map<Double, Material> createCLUT(String fileName);

    void exportWKML(String outputFilename, List<Path> paths);

    void exportWKML(List<Path> paths);

    void exportKML(String outputFilename, List<Path> paths);

    void exportKML(List<Path> paths);

    void exportWKMLPolygons(String outputFilename, List<Polygon> polygons);

    void exportWKMLPolygons(List<Polygon> polygons);

    String mergeKML(String inputFilename, String outputFilename);

    void exportASC(String outputFilename, Point3DGeo[][] pts);

    Point3DGeo[][] importASC(String outputFilename);

    JfxViewer getJfxViewer();

}
