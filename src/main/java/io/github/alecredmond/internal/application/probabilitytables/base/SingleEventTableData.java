package io.github.alecredmond.internal.application.probabilitytables.base;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class SingleEventTableData extends ProbabilityTableData {
  protected final Node eventNode;

  protected SingleEventTableData(TableFactoryData tableFactoryData) {
    super(tableFactoryData);
    this.eventNode = tableFactoryData.getEventNode();
  }
}
