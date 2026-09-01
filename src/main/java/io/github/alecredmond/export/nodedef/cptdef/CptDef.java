package io.github.alecredmond.export.nodedef.cptdef;

import io.github.alecredmond.export.nodedef.NodeDef;

import java.util.List;

public interface CptDef {
    double[] getCptArray();

    List<NodeDef> getNodeDefs();
}
