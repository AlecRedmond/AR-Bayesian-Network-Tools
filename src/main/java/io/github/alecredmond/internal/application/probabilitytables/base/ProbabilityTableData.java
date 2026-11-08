package io.github.alecredmond.internal.application.probabilitytables.base;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public abstract class ProbabilityTableData {
  protected final ProbabilityVector vector;
  protected final Set<Node> nodes;
  protected final Set<Node> events;
  protected final Set<Node> conditions;
  @EqualsAndHashCode.Exclude protected String tableName;

  protected ProbabilityTableData(TableFactoryData tableFactoryData) {
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
