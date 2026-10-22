package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.application.probabilitytables.ConditionalTableData;
import io.github.alecredmond.internal.method.probabilitytables.ConditionalTableImpl;
import java.util.List;

public class ConditionalTableFactory extends BaseTableFactory<ConditionalTable> {

  @Override
  protected void performInstanceValidation(List<Node> events, List<Node> conditions) {
    if (events.size() != 1) {
      throw new TableBuilderException(
          "Attempted to build a Conditional table with != 1 events: %s".formatted(events));
    }
    if (conditions.isEmpty()) {
      throw new TableBuilderException(
          "Attempted to build a Conditional table with no conditions: %s".formatted(events));
    }
  }

  @Override
  protected ConditionalTable buildTableFromFactoryData(TableFactoryData data) {
    return new ConditionalTableImpl(new ConditionalTableData(data));
  }
}
