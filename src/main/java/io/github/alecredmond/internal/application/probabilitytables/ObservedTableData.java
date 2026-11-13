package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ObservedTableData extends SingleEventTableData {
  protected Map<Node, NodeObservation> observations = new LinkedHashMap<>();

  public ObservedTableData(TableFactoryData tableFactoryData) {
    super(tableFactoryData);
  }

  public Node getMeasuredNode() {
    return eventNode;
  }
}
