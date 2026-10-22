package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NetworkTableFactory implements TableFactory<NetworkTable> {
  private final RootNodeTableFactory rootNodeTableBuilder = new RootNodeTableFactory();
  private final ConditionalTableFactory conditionalTableBuilder = new ConditionalTableFactory();

  @Override
  public NetworkTable buildTable(List<Node> events, List<Node> conditions) {
    if (conditions.isEmpty()) return buildMarginalTable(events);
    return buildConditionalTable(events, conditions);
  }

  @Override
  public NetworkTable copyTable(NetworkTable table) {
    return switch (table) {
      case ConditionalTable ct -> conditionalTableBuilder.copyTable(ct);
      case RootNodeTable rt -> rootNodeTableBuilder.copyTable(rt);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    };
  }

  public RootNodeTable buildMarginalTable(List<Node> events) {
    return rootNodeTableBuilder.buildTable(events, new ArrayList<>());
  }

  public ConditionalTable buildConditionalTable(List<Node> events, List<Node> conditions) {
    return conditionalTableBuilder.buildTable(events, conditions);
  }
}
