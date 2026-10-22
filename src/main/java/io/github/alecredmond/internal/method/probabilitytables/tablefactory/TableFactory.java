package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import java.util.List;

public interface TableFactory<T extends ProbabilityTable> {
  T buildTable(List<Node> events, List<Node> conditions);

  T copyTable(T table);
}
