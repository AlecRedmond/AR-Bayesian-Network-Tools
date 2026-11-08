package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import java.util.Set;
import lombok.Data;

@Data
public class TableFactoryData {
  protected ProbabilityVector vector;
  protected Set<Node> nodes;
  protected Set<Node> events;
  protected Set<Node> conditions;
  protected String tableName;
  protected Node eventNode;
}
