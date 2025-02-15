class StlPreWriterController
!!!134658.java!!!	StlPreWriterController(inout outPathname : Path, in title : String, in tilesCount : int, in index : int, inout positions : List<? extends Position>, in tileSideX : double, in tileSideY : double, in spaceX : double, in spaceY : double, in bottom : double, in magnification : double, in offset : double)
        super(outPathname,
                tilesCount, index,
                positions,
                tileSideX, tileSideY,
                spaceX, spaceY,
                bottom,
                magnification,
                offset);
        this.title = title;
!!!134786.java!!!	compute() : void
        writeInitOutFile();
        //  writeRef(outPathname.toString(), positions, tileSideX, tileSideY);// repere XYZ
        writePositionOrientation();
!!!134914.java!!!	writeInitOutFile() : void
        String txt;
        lines = new ArrayList<>();
        txt = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
                + "<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.0//EN\" "
                + "\"http://www.web3d.org/specifications/x3d-3.0.dtd\">\n"
                + "<X3D profile='Immersive' version='3.0'  "
                + "xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance'"
                + " xsd:noNamespaceSchemaLocation =' "
                + "http://www.web3d.org/specifications/x3d-3.0.xsd '> \n"
                + "<head>\n"
                + "<meta name='Title' content='" + title + "'/> \n"
                + "<meta name='TilesCount' content='" + tilesCount + "'/>\n"
                + "<meta name='Index' content='" + index + "'/>\n"
                + "<meta name='Author' content='" + System.getProperty("user.name") + "'/>\n"
                + "<meta name='Created' content='" + new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss").format(new Date()) + "'/>\n"
                + "<meta name='Generator' content='NaVisu'/>\n"
                + "<meta name='Site' content='http://www.navisu.org/'/>\n"
                + "<meta name='Github' content='https://github.com/terre-virtuelle/navisu'/>\n"
                + "<meta name='Pos0' content='Lat=" + positions.get(0).getLatitude()
                + " Lon=" + positions.get(0).getLongitude() + "'/>\n"
                + "<meta name='Pos1' content='Lat=" + positions.get(1).getLatitude()
                + " Lon=" + positions.get(1).getLongitude() + "'/>\n"
                + "<meta name='Pos2' content='Lat=" + positions.get(2).getLatitude()
                + " Lon=" + positions.get(2).getLongitude() + "'/>\n"
                + "<meta name='Pos3' content='Lat=" + positions.get(3).getLatitude()
                + " Lon=" + positions.get(3).getLongitude() + "'/>\n"
                + "</head>\n"
                + "<Scene>\n";
        lines.add(txt);
        try {
            Files.write(outPathname,
                    lines,
                    charset,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(StlPreWriterController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
!!!135042.java!!!	writeRef(in outFilename : String, inout positions : List<? extends Position>, in tileSideX : double, in tileSideY : double) : void
        RefWriter l = new RefWriter(positions, tileSideX, tileSideY);
        write(l.compute());
!!!135170.java!!!	writePositionOrientation() : void
        lines = new ArrayList<>();
        String txt = "<Transform rotation='0 1 0 1.57058' "
                + "translation='200.0 0.0 200.0' "
                + " scale='1.000900 1.000900 1.000900'> \n"
                + "<Viewpoint  position='-100.0 500.0 100'  "
                + "orientation='1 0 0 -1.57'  "
                + "fieldOfView='.5'/>\n"
                + "<Transform rotation='0 1 0 -3.14'>\n";

        lines.add(txt);
        try {
            Files.write(outPathname, lines, charset, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(StlPreWriterController.class.getName()).log(Level.SEVERE, ex.toString(), ex);
        }
