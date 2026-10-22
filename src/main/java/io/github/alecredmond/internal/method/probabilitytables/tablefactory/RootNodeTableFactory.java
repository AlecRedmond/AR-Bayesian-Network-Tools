package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.internal.application.probabilitytables.RootNodeTableData;
import io.github.alecredmond.internal.method.probabilitytables.RootNodeTableImpl;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.List;

public class RootNodeTableFactory extends BaseTableFactory<RootNodeTable> {

  @Override
  protected void performInstanceValidation(List<Node> events, List<Node> conditions) {
    if (events.size() != 1) {
      throw new TableBuilderException(
          "A root node table can only have 1 node, but found node(s): %s!"
              .formatted(CollectionToString.apply(events)));
    }
    if (!conditions.isEmpty()) {
      throw new TableBuilderException(
          "Attempted to build a root node table with conditional nodes: %s!"
              .formatted(CollectionToString.apply(conditions)));
    }
  }

  @Override
  protected RootNodeTable buildTableFromFactoryData(TableFactoryData data) {
    return new RootNodeTableImpl(new RootNodeTableData(data));
  }
}
