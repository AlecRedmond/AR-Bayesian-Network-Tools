package io.github.alecredmond.internal.application.probabilitytables.base;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Slf4j
public abstract class ProbabilityTableData extends ProbabilityVector {
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @EqualsAndHashCode.Exclude protected String tableName;

  protected ProbabilityTableData(TableFactoryData data) {
    super(
        data.getVector().getOrderedNodes(),
        data.getVector().getStateArrays(),
        data.getVector().getNumberOfStates(),
        data.getVector().getStrideLengths(),
        data.getVector().getProbabilities(),
        data.getVector().getNodeIndexMap(),
        data.getVector().getRank());
    this.nodes = data.getNodes();
    this.events = data.getEvents();
    this.conditions = data.getConditions();
    this.tableName = data.getTableName();
  }
}
