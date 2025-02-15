/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.geometry.objects3D.obj;

import bzh.terrevirtuelle.navisu.domain.geometry.Point3DGeo;
import com.owens.oobjloader.builder.Face;
import com.owens.oobjloader.builder.VertexGeometric;
import java.util.List;
import org.capcaval.c3.component.ComponentService;

/**
 * @date @author Serge Morvan
 */
public interface ObjComponentServices
        extends ComponentService {

    List<Face> getFaces(String filename);

    List<VertexGeometric> getVerticesG(String filename);

    Point3DGeo toPoint3DGeo(VertexGeometric vertexGeometric);

    List<Point3DGeo> toPoint3DGeo(List<VertexGeometric> vertexGeometric);

}
