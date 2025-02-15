/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package bzh.terrevirtuelle.navisu.shapefiles.impl.controller.loader;

import bzh.terrevirtuelle.navisu.core.util.shapefile.RandomShapeAttributes;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * Converts Shapefile geometry into World Wind renderable objects. Shapefile
 * geometries are mapped to World Wind objects as follows: <table>
 * <tr><th>Shapefile Geometry</th><th>World Wind Object</th></tr> <tr><td>Point</td><td>{@link
 * gov.nasa.worldwind.render.WWIcon}</td></tr> <tr><td>MultiPoint</td><td>List
 * of {@link
 * gov.nasa.worldwind.render.WWIcon}</td></tr>
 * <tr><td>Polyline</td><td>{@link gov.nasa.worldwind.render.SurfacePolylines}</td></tr>
 * <tr><td>Polygon</td><td>{@link gov.nasa.worldwind.render.SurfacePolygons}</td></tr>
 * </table>
 * <p/>
 * Shapefiles do not contain a standard definition for color and other visual
 * attributes. Though some Shapefiles contain color information in each record's
 * key-value attributes, ShapefileLoader does not attempt to interpret that
 * information. Instead, the World Wind renderable objects created by
 * ShapefileLoader are assigned a random color. Callers can replace or extend
 * this behavior by defining a subclass of ShapefileLoader and overriding the
 * following methods: <ul>
 * <li>{@link #createPointAttributes(gov.nasa.worldwind.formats.shapefile.ShapefileRecord)}</li>
 * <li>{@link #createPolylineAttributes(gov.nasa.worldwind.formats.shapefile.ShapefileRecord)}</li> <li>{@link
 * #createPolygonAttributes(gov.nasa.worldwind.formats.shapefile.ShapefileRecord)}</li></ul>.
 *
 * @author dcollins
 * @version $Id: ShapefileLoader.java 1532 2013-08-06 23:54:50Z dcollins $
 */
public class ShapefileLoader {

    protected static final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();
    protected Shapefile shp = null;
    /**
     * Indicates the maximum number of polygons to place in a layer before
     * creating an additional layer.
     */
    protected int numPolygonsPerLayer = 5000;
    protected Set<Map.Entry<String, Object>> entries;
    protected List<ShapefileRecord> records;

    /**
     * Constructs a ShapefileLoader, but otherwise does nothing.
     */
    public ShapefileLoader() {
    }

    public Layer createLayerFromSource(Object source) {
        if (WWUtil.isEmpty(source)) {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer layer = null;
        try {
            shp = new Shapefile(source);
            layer = this.createLayerFromShapefile(shp);
        } finally {
            WWIO.closeStream(shp, source.toString());
        }

        return layer;
    }

    public List<Layer> createLayersFromSource(Object source) {
        if (WWUtil.isEmpty(source)) {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            shp = new Shapefile(source);
            return this.createLayersFromShapefile(shp);
        } finally {
            WWIO.closeStream(shp, source.toString());
        }
    }

    public Layer createLayerFromShapefile(Shapefile shp) {
        if (shp == null) {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer layer = null;
        if (Shapefile.isPointType(shp.getShapeType())) {
            layer = new RenderableLayer();
            this.addRenderablesForPoints(shp, (RenderableLayer) layer);
        } else if (Shapefile.isMultiPointType(shp.getShapeType())) {
            layer = new RenderableLayer();
            this.addRenderablesForMultiPoints(shp, (RenderableLayer) layer);
        } else if (Shapefile.isPolylineType(shp.getShapeType())) {
            layer = new RenderableLayer();
            this.addRenderablesForPolylines(shp, (RenderableLayer) layer);
        } else if (Shapefile.isPolygonType(shp.getShapeType())) {
            List<Layer> layers = new ArrayList<>();
            this.addRenderablesForPolygons(shp, layers);
            layer = layers.get(0);
        } else {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedShapeType", shp.getShapeType()));
        }

        return layer;
    }

    public List<Layer> createLayersFromShapefile(Shapefile shp) {
        if (shp == null) {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<Layer> layers = new ArrayList<>();

        if (Shapefile.isPointType(shp.getShapeType())) {
            Layer layer = new RenderableLayer();
            this.addRenderablesForPoints(shp, (RenderableLayer) layer);
            // System.out.println("isPointType");
            layers.add(layer);
        } else if (Shapefile.isMultiPointType(shp.getShapeType())) {
            Layer layer = new RenderableLayer();
            this.addRenderablesForMultiPoints(shp, (RenderableLayer) layer);
            //   System.out.println("isMultiPointType");
            layers.add(layer);
        } else if (Shapefile.isPolylineType(shp.getShapeType())) {
            Layer layer = new RenderableLayer();
            this.addRenderablesForPolylines(shp, (RenderableLayer) layer);
            layers.add(layer);
        } else if (Shapefile.isPolygonType(shp.getShapeType())) {
            //   System.out.println("isPolygonType");
            this.addRenderablesForPolygons(shp, layers);
        } else {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedShapeType", shp.getShapeType()));
        }

        return layers;
    }

    /**
     * Indicates the maximum number of polygon renderables to place in a single
     * layer. If this limit is exceeded an additional layer is added.
     *
     * @return the maximum number of polygons to place in a layer.
     */
    public int getNumPolygonsPerLayer() {
        return this.numPolygonsPerLayer;
    }

    /**
     * Specifies the maximum number of polygon renderables to place in a single
     * layer. If this limit is exceeded an additional layer is added.
     *
     * @param numPolygonsPerLayer the maximum number of polygons to place in a
     * layer.
     *
     * @throws IllegalArgumentException if the number is less than 1.
     */
    public void setNumPolygonsPerLayer(int numPolygonsPerLayer) {
        if (numPolygonsPerLayer < 1) {
            String message = Logging.getMessage("generic.InvalidSize", numPolygonsPerLayer);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numPolygonsPerLayer = numPolygonsPerLayer;
    }
    //**************************************************************//
    //********************  Geometry Conversion  *******************//
    //**************************************************************//

    protected void addRenderablesForPoints(Shapefile shp, RenderableLayer layer) {
        //  PointPlacemarkAttributes attrs = this.createPointAttributes(null);

        while (shp.hasNext()) {
            ShapefileRecord record = shp.nextRecord();
            PointPlacemarkAttributes attrs = this.createPointAttributes(record);
            if (!Shapefile.isPointType(record.getShapeType())) {
                continue;
            }

            double[] point = ((ShapefileRecordPoint) record).getPoint();
            layer.addRenderable(this.createPoint(record, point[1], point[0], attrs));
        }
    }

    protected void addRenderablesForMultiPoints(Shapefile shp, RenderableLayer layer) {
        // PointPlacemarkAttributes attrs = this.createPointAttributes(null);

        while (shp.hasNext()) {
            ShapefileRecord record = shp.nextRecord();
            //   System.out.println("record " + record.getAttributes().getEntries());

            //   System.out.println("");
            PointPlacemarkAttributes attrs = this.createPointAttributes(record);
            if (!Shapefile.isMultiPointType(record.getShapeType())) {
                continue;
            }

            Iterable<double[]> iterable = ((ShapefileRecordMultiPoint) record).getPoints(0);

            for (double[] point : iterable) {
                layer.addRenderable(this.createPoint(record, point[1], point[0], attrs));
            }
        }
    }

    protected List<ShapefileRecord> addRenderablesForPolylines(Shapefile shp, RenderableLayer layer) {
        records = new ArrayList<>();
        while (shp.hasNext()) {
            ShapefileRecord record = shp.nextRecord();
            if (!Shapefile.isPolylineType(record.getShapeType())) {
                continue;
            }
            records.add(record);
            ShapeAttributes attrs = this.createPolylineAttributes(record);
            layer.addRenderable(this.createPolyline(record, attrs));

        }
        return records;
    }

    /**
     * Creates renderables for all the polygons in the shapefile. Polygon is the
     * only shape that potentially returns a more than one layer, which it does
     * when the polygons per layer limit is exceeded.
     *
     * @param shp the shapefile to read
     * @param layers a list in which to place the layers created. May not be
     * null.
     */
    protected void addRenderablesForPolygons(Shapefile shp, List<Layer> layers) {
        RenderableLayer layer = new RenderableLayer();
        layers.add(layer);

        int recordNumber = 0;
        while (shp.hasNext()) {
            try {
                ShapefileRecord record = shp.nextRecord();
                //   System.out.println("record " + record);
                recordNumber = record.getRecordNumber();

                if (!Shapefile.isPolygonType(record.getShapeType())) {
                    continue;
                }

                ShapeAttributes attrs = this.createPolygonAttributes(record);
                this.createPolygon(record, attrs, layer);

                if (layer.getNumRenderables() > this.numPolygonsPerLayer) {
                    layer = new RenderableLayer();
                    layer.setEnabled(false);
                    layers.add(layer);
                }
            } catch (Exception e) {
                // Logging.logger().warning(Logging.getMessage("SHP.ExceptionAttemptingToConvertShapefileRecord",
                //        recordNumber, e));
                // continue with the remaining records
            }
        }
    }

    //**************************************************************//
    //********************  Primitive Geometry Construction  *******//
    //**************************************************************//
    @SuppressWarnings({"UnusedDeclaration"})
    protected Renderable createPoint(ShapefileRecord record, double latDegrees, double lonDegrees,
            PointPlacemarkAttributes attrs) {
        PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(latDegrees, lonDegrees, 0));
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(attrs);

        String txt = "";
        entries = record.getAttributes().getEntries();

        for (Map.Entry<String, Object> m : entries) {
            txt += String.format("%1$-1s", m.getKey() + " : " + m.getValue() + "\n");
        }
        placemark.setValue(AVKey.DISPLAY_NAME, txt);
        return placemark;
    }

    protected Renderable createPolyline(ShapefileRecord record, ShapeAttributes attrs) {
        SurfacePolylines shape = new SurfacePolylines(
                Sector.fromDegrees(((ShapefileRecordPolyline) record).getBoundingRectangle()),
                record.getCompoundPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected Renderable createPolyline(Shapefile shp, ShapeAttributes attrs) {

        SurfacePolylines shape = new SurfacePolylines(Sector.fromDegrees(shp.getBoundingRectangle()),
                shp.getPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected void createPolygon(ShapefileRecord record, ShapeAttributes attrs, RenderableLayer layer) {
        Double height = this.getHeight(record);

        if (height != null) // create extruded polygons
        {
            ExtrudedPolygon ep = new ExtrudedPolygon(height);
            ep.setAttributes(attrs);
            layer.addRenderable(ep);

            for (int i = 0; i < record.getNumberOfParts(); i++) {
                VecBuffer buffer = record.getCompoundPointBuffer().subBuffer(i);
                if (WWMath.computeWindingOrderOfLocations(buffer.getLocations()).equals(AVKey.CLOCKWISE)) {
                    if (!ep.getOuterBoundary().iterator().hasNext()) // has no outer boundary yet
                    {
                        ep.setOuterBoundary(buffer.getLocations());
                    } else {
                        ep = new ExtrudedPolygon();
                        ep.setAttributes(attrs);
                        ep.setOuterBoundary(record.getCompoundPointBuffer().getLocations());
                        layer.addRenderable(ep);
                    }
                } else {
                    ep.addInnerBoundary(buffer.getLocations());
                }
            }
        } else // create surface polygons
        {
            SurfacePolygons shape = new SurfacePolygons(
                    Sector.fromDegrees(((ShapefileRecordPolygon) record).getBoundingRectangle()),
                    record.getCompoundPointBuffer());
            shape.setAttributes(attrs);
            shape.setWindingRule(AVKey.CLOCKWISE);
            shape.setPolygonRingGroups(new int[]{0});
            shape.setPolygonRingGroups(new int[]{0});
            layer.addRenderable(shape);
        }
    }

    /**
     * Get the height of a record.
     *
     * @param record shape for which to find height
     *
     * @return the height of the shape, or {@code null} if there is no height.
     */
    protected Double getHeight(ShapefileRecord record) {
        return ShapefileUtils.extractHeightAttribute(record);
    }

    //**************************************************************//
    //********************  Attribute Construction  ****************//
    //**************************************************************//
    @SuppressWarnings({"UnusedDeclaration"})
    protected PointPlacemarkAttributes createPointAttributes(ShapefileRecord record) {
        //  System.out.println("createPointAttributes " + record.getAttributes().getEntries());

        return randomAttrs.nextPointAttributes();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected ShapeAttributes createPolylineAttributes(ShapefileRecord record) {
        if (record != null) {
            //  System.out.println("createPolylineAttributes " + record.getAttributes().getEntries());
        }

        return randomAttrs.nextPolylineAttributes();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected ShapeAttributes createPolygonAttributes(ShapefileRecord record) {
        //  System.out.println(record.getAttributes().getEntries());
        return randomAttrs.nextPolygonAttributes();

    }

    public List<ShapefileRecord> getRecords() {
        return records;
    }

}
