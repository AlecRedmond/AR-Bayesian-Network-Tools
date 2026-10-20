package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.application.probabilitytables.ConditionalTableData;
import io.github.alecredmond.internal.method.probabilitytables.ConditionalTableImpl;
import java.util.List;

public class ConditionalTableBuilder extends BaseTableBuilder
    implements TableBuilder<ConditionalTable> {
  @Override
  public ConditionalTable buildTable(List<Node> events, List<Node> conditions) {
    if (events.size() != 1) {
      throw new TableBuilderException(
          "Attempted to build a Conditional table with more than 1 event!");
    }
    return buildTable(events, conditions, ConditionalTableData::new, ConditionalTableImpl::new);
  }

  @Override
  public ConditionalTable copyTable(ConditionalTable table) {
    return copyTable(table, this::buildTable);
  }
}
