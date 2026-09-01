package io.github.alecredmond.export.nodedef;

import io.github.alecredmond.export.nodedef.cptdef.CptDef;

import java.util.List;
import java.util.Optional;

public interface DiscreteNodeDef extends NodeDef {
    List<StateDef> getStateDefs();

    Optional<CptDef> getCptDef();
}
