package io.github.alecredmond.internal.application.probabilitytables.base;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class ProbabilityTableData {
  protected final Map<Serializable, NodeState> nodeStateIDMap;
  protected final Map<Serializable, Node> nodeIDMap;
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @EqualsAndHashCode.Exclude protected Serializable tableName;

  protected ProbabilityTableData(TableFactoryData tableFactoryData) {
    this.nodeStateIDMap = tableFactoryData.getNodeStateIDMap();
    this.nodeIDMap = tableFactoryData.getNodeIDMap();
    this.vector = tableFactoryData.getVector();
    this.nodes = tableFactoryData.getNodes();
    this.events = tableFactoryData.getEvents();
    this.conditions = tableFactoryData.getConditions();
    this.tableName = tableFactoryData.getTableName();
  }

  public double[] getProbabilities() {
    return vector.getProbabilities();
  }
}
