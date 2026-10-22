package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RootNodeTableData extends SingleEventTableData {
  public RootNodeTableData(TableFactoryData tableFactoryData) {
    super(tableFactoryData);
  }

  public Node getNetworkNode() {
    return eventNode;
  }
}
