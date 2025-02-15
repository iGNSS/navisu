class StlComponentImpl
!!!128258.java!!!	openChart(in fileName : String) : void
        guiAgentServices.getJobsManager().newJob("", (progressHandle) -> {
            handleOpenFile(progressHandle, fileName);
        });
!!!128386.java!!!	componentInitiated() : void

        s57StlChartComponentController = new StlChartController(geodesyServices);
        s57StlComponentController = new StlComponentController(this,
                guiAgentServices, // pour afficher le widget
                layerTreeServices, // pour indiquer dans l'arbre à gauche ou est la couche
                layersManagerServices, // pour afficher la couche
                instrumentDriverManagerServices, // pour envoyer un signal sonore en fin de génération
                geodesyServices,
                bathymetryDBServices,
                displayBathymetryServices,
                s57StlChartComponentController, // la composant de génération x3D
                GROUP, NAME, // pour se positionner dans l'arborescence des couches
                wwd);                            // le lien avec WordlWind
!!!128770.java!!!	canOpen(in category : String) : boolean
        boolean canOpen = false;
        if (!category.equals(NAME)) {
        } else {
            canOpen = true;
        }
        return canOpen;
!!!128898.java!!!	on(inout  : String...files) : void
        String[] tab = files;
        openChart(tab[0]);
!!!129026.java!!!	openFile(in category : String, in file : String) : InstrumentDriver
        if (file != null) {
            openChart(file);
        }
        return this;
!!!129154.java!!!	handleOpenFile(inout pHandle : ProgressHandle, in fileName : String) : void

        try {
            if (first == true) {
                first = false;
            }
            new File("data/shp").mkdir();
            new File("data/shp/shp_" + i).mkdir();
            new File("data/shp/shp_" + i + "/soundg").mkdir();

            LOGGER.log(Level.INFO, "Opening {0} ...", fileName);

            Path inputFile = Paths.get(fileName);
            Map<String, String> environment = new HashMap<>(System.getenv());
            String options
                    = "\"RECODE_BY_DSSI=ON, "
                    + "ENCODING=UTF8, "
                    + "UPDATES=APPLY, "
                    + "RETURN_PRIMITIVES=ON, "
                    + "RETURN_LINKAGES=ON, "
                    + "LNAM_REFS=ON, "
                    + "SPLIT_MULTIPOINT=ON, "
                    + "ADD_SOUNDG_DEPTH=ON\" \n";
            environment.put("OGR_S57_OPTIONS", options);
            options = System.getProperty("user.dir") + "/gdal/data";
            environment.put("GDAL_DATA", options);

            String cmd = null;

            if (OS.isWindows()) {
                cmd = "gdal/win/ogr2ogr";
            } else if (OS.isLinux()) {
                cmd = "/usr/bin/ogr2ogr";
            } else if (OS.isMac()) {
                cmd = "gdal/osx/ogr2ogr";
            } else {
                System.out.println("OS not found");
            }
            try {
                Path tmp = Paths.get(inputFile.toString());
                Proc.BUILDER.create()
                        .setCmd(cmd)
                        .addArg("-skipfailures ").addArg("-overwrite ")
                        .addArg("data/shp/shp_" + i)// + "/out.shp ")
                        .addArg(tmp.toString())
                        .setOut(System.out)
                        .setErr(System.err)
                        .exec(environment);
                inputFile = tmp;
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }

            cmd = cmd + " -nlt POINT25D";
            try {
                Path tmp = Paths.get(inputFile.toString());
                Proc.BUILDER.create()
                        .setCmd(cmd)
                        .addArg("-skipfailures ").addArg("-append ")
                        .addArg("data/shp/shp_" + i + "/soundg/SOUNDG.shp")
                        .addArg(tmp.toString())
                        .addArg("SOUNDG")
                        .setOut(System.out)
                        .setErr(System.err)
                        .exec(environment);
                inputFile = tmp;
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, null, e);
            }
            s57StlChartComponentController.init("data/shp/shp_" + i++, fileName);

            layers = s57StlChartComponentController.getLayers();
            geoLayerList = geoViewServices.getLayerManager().getGroup(GROUP);
            groupNames.clear();
            geoLayerList.stream().forEach((l) -> {
                groupNames.add(l.getName());
            });

            layers.stream().filter((l) -> (!groupNames.contains(l.getName()))).map((l) -> GeoLayer.factory.newWorldWindGeoLayer(l)).map((gl) -> {
                geoViewServices.getLayerManager().insertGeoLayer(GROUP, gl);
                return gl;
            }).forEach((gl) -> {
                layerTreeServices.addGeoLayer(GROUP, gl);
            });

        } catch (Exception e) {
            System.out.println("handleOpenFile e " + e);
        }
!!!129282.java!!!	showGUI(inout polygon : KMLSurfacePolygonImpl) : void
        s57StlComponentController.showGUI(polygon);
!!!129410.java!!!	getDriver() : InstrumentDriver
        return this;
