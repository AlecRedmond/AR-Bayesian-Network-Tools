package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.probabilitytables.ObservedTableData;
import io.github.alecredmond.internal.method.probabilitytables.ObservedTableImpl;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;

public class ObservedTableFactory extends BaseTableFactory<ObservedTable> {

  public ObservedTable buildTable(Node node) {
    return buildTable(List.of(node), new ArrayList<>());
  }

  @Override
  public ObservedTable copyTable(ObservedTable table) {
    ObservedTable copied = super.copyTable(table);
    if (!(copied instanceof ObservedTableImpl copiedImpl)) {
      throw new TableBuilderException(
          "ObservedTable %s did not have the correct implementation %s"
              .formatted(table, ObservedTableImpl.class));
    }
    copiedImpl.setTableName(SerializationUtils.clone(table.getTableName()));
    copiedImpl.setObservations(Map.copyOf(table.getObservations()));
    return copiedImpl;
  }

  @Override
  protected void performInstanceValidation(List<Node> events, List<Node> conditions) {
    if (events.size() != 1) {
      throw new TableBuilderException(
          "An observed table can only have 1 node, but found node(s): %s!"
              .formatted(CollectionToString.apply(events)));
    }
    if (!conditions.isEmpty()) {
      throw new TableBuilderException(
          "Attempted to build an observed table with conditional nodes: %s!"
              .formatted(CollectionToString.apply(conditions)));
    }
  }

  @Override
  protected ObservedTable buildTableFromFactoryData(TableFactoryData data) {
    return new ObservedTableImpl(new ObservedTableData(data));
  }
}
