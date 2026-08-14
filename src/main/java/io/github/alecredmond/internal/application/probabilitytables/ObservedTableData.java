package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.TableBuilderData;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ObservedTableData extends SingleEventTableData {
  protected Map<Node, NodeObservation> observations = new LinkedHashMap<>();

  public ObservedTableData(TableBuilderData tableBuilderData) {
    super(tableBuilderData);
  }

  public Node getMeasuredNode() {
    return eventNode;
  }
}
