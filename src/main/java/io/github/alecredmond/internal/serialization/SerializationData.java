package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class SerializationData {
  private final Map<Serializable, Node> nodeIdMap;
  private final Map<Serializable, NodeState> nodeStateIdMap;
  private final Set<Node> nodeSet;
  private final Set<ProbabilityConstraint> constraints;

  public SerializationData() {
    this.nodeIdMap = new HashMap<>();
    this.nodeStateIdMap = new HashMap<>();
    this.nodeSet = new LinkedHashSet<>();
    this.constraints = new LinkedHashSet<>();
  }
}
