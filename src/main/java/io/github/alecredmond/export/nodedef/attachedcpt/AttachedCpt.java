package io.github.alecredmond.export.nodedef.attachedcpt;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.nodedef.NodeDefinition;
import java.util.List;

public interface AttachedCpt {
  NodeDefinition[] getOrderedNodeDefs();

  NodeDefinition getEventNodeDef();

  double[] getCptArray();

  List<ProbabilityConstraint> supplyConstraints();
}
