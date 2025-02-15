class DEPARE_Stl_ShapefileWriter
!!!140034.java!!!	DEPARE_Stl_ShapefileWriter(in filename : String, inout polyEnveloppe : Polygon)

        this.outFilename = filename;
        this.polyEnveloppe = polyEnveloppe;

        positions = polyEnveloppe.getBoundaries().get(0);
        latOrg = positions.get(0).getLatitude().getDegrees();
        lonOrg = positions.get(0).getLongitude().getDegrees();
        String wkt = WwjJTS.toPolygonWkt(positions);
        WKTReader wktReader = new WKTReader();

        if (wkt != null) {
            try {
                geometryEnveloppe = wktReader.read(wkt);
            } catch (com.vividsolutions.jts.io.ParseException ex) {
                Logger.getLogger(DEPARE_Stl_ShapefileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
!!!140162.java!!!	DEPARE_Stl_ShapefileWriter(inout polyEnveloppe : Polygon)
        this.polyEnveloppe = polyEnveloppe;
        positions = polyEnveloppe.getBoundaries().get(0);
        latOrg = positions.get(0).getLatitude().getDegrees();
        lonOrg = positions.get(0).getLongitude().getDegrees();
        String wkt = WwjJTS.toPolygonWkt(positions);
        WKTReader wktReader = new WKTReader();

        if (wkt != null) {
            try {
                geometryEnveloppe = wktReader.read(wkt);
            } catch (com.vividsolutions.jts.io.ParseException ex) {
                Logger.getLogger(DEPARE_Stl_ShapefileWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
!!!140290.java!!!	createPolygonAttributes(inout record : ShapefileRecord) : ShapeAttributes
        normalAttributes = super.createPolygonAttributes(record);
        nb++;
        val1 = 51 - val1;
        List<LatLon> pts = addRenderablesForExtrudedPolygons(record.getShapeFile(), val1, val2);
        Geometry geo = WwjJTS.filter(geometryEnveloppe, pts);
        List<Position> ptsFiltered = null;
        if (!geo.toString().contains("EMPTY") && !geo.toString().contains("MULTIPOLYGON")) {
            ptsFiltered = WwjJTS.wktPolygonToPositionList(geo.toString());
        }
        if (ptsFiltered != null) {
            if (!ptsFiltered.isEmpty() && val1 != 0) {
                txt
                        = "<Transform rotation='0 0 1 3.14'> \n"
                        + "<Transform translation ='0 -510 0'> \n"
                        + " <Shape>\n"
                        + "<Appearance>\n"
                        + " <Material diffuseColor='1.0 0.3 0'/> \n"
                        + "</Appearance>\n"
                        + "<Extrusion convex='true' \n"
                        + " crossSection='";

                for (Position p : ptsFiltered) {
                    Pair<Double, Double> xy = WwjGeodesy.getXYM(positions.get(0), p);
                    txt += xy.getX() * 1000 + " " + xy.getY() * 1000 + (",");
                }
                val1 = -val1 * 10;
                txt += "'\n "
                        + "solid='true' \n"
                        + "spine='0 0 0 0 "
                        // + val1 * 10 + " 0'/>\n"
                        // + 100 + " 0'/>\n"
                        + val1 + " 0'/>\n"
                        + "</Shape>\n"
                        + "</Transform> \n"
                        + "</Transform> \n";

                try {
                    Files.write(Paths.get(outFilename), txt.getBytes(), APPEND);
                } catch (IOException ex) {
                    Logger.getLogger(DEPARE_Stl_ShapefileWriter.class.getName()).log(Level.SEVERE, ex.toString(), ex);
                }
            }
        }
        return normalAttributes;
!!!140418.java!!!	addRenderablesForExtrudedPolygons(inout shp : Shapefile, in val1 : float, in val2 : float) : List<LatLon>
        ExtrudedPolygon ep = new ExtrudedPolygon();
        List<LatLon> pts = new ArrayList<>();
        for (int i = 0; i < record.getNumberOfParts(); i++) {
            VecBuffer buffer = record.getCompoundPointBuffer().subBuffer(i);
            if (WWMath.computeWindingOrderOfLocations(buffer.getLocations()).equals(AVKey.CLOCKWISE)) {
                if (!ep.getOuterBoundary().iterator().hasNext()) // has no outer boundary yet
                {
                    if (val1 >= 0 && val2 >= 0) {
                        ep.setOuterBoundary(buffer.getLocations());
                        Iterator<? extends LatLon> iterator = ep.getOuterBoundary().iterator();
                        while (iterator.hasNext()) {
                            pts.add(iterator.next());
                        }
                        ep.setHeight(val1);
                        //   layer.addRenderable(ep);
                    }
                } else {
                    ep.setOuterBoundary(record.getCompoundPointBuffer().getLocations());
                }
            } else {
                ep.addInnerBoundary(buffer.getLocations());
            }
        }
        return pts;
!!!140546.java!!!	compute() : String
        return txt;
