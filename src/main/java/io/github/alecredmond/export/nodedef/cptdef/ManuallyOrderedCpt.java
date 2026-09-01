package io.github.alecredmond.export.nodedef.cptdef;

import io.github.alecredmond.export.nodedef.StateDef;
import java.util.Set;
import java.util.function.Function;

public interface ManuallyOrderedCpt extends CptDef {
  Function<Set<StateDef>, Integer> getCptIndexFromStateDefs();

  Function<Integer, Set<StateDef>> getStateDefsFromCptIndex();
}
