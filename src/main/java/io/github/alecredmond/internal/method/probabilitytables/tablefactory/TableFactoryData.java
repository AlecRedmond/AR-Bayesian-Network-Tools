package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;

@Data
public class TableFactoryData {
  protected Map<Serializable, NodeState> nodeStateIDMap;
  protected Map<Serializable, Node> nodeIDMap;
  protected ProbabilityVector vector;
  protected Set<Node> nodes;
  protected Set<Node> events;
  protected Set<Node> conditions;
  protected Serializable tableName;
  protected Node eventNode;
}
