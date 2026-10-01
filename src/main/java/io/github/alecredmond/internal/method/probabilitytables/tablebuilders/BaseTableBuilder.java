package io.github.alecredmond.internal.method.probabilitytables.tablebuilders;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class BaseTableBuilder {
  private final ProbabilityVectorFactory vectorFactory = new ProbabilityVectorFactory();

  protected <T extends ProbabilityTable, S extends ProbabilityTableData> T buildTable(
      List<Node> events,
      List<Node> conditions,
      Function<TableBuilderData, S> tableDataConstructor,
      Function<S, T> tableConstructor) {
    TableBuilderData data = buildData(events, conditions);
    S tableData = tableDataConstructor.apply(data);
    return tableConstructor.apply(tableData);
  }

  public TableBuilderData buildData(List<Node> events, List<Node> conditions) {
    TableBuilderData data = new TableBuilderData();
    data.setEvents(Collections.unmodifiableSet(new LinkedHashSet<>(events)));
    data.setConditions(Collections.unmodifiableSet(new LinkedHashSet<>(conditions)));
    data.setNodes(Collections.unmodifiableSet(joinEventsAndConditions(events, conditions)));
    data.setNodeStateIDMap(Collections.unmodifiableMap(buildNodeStateIDMap(data.getNodes())));
    data.setNodeIDMap(Collections.unmodifiableMap(buildNodeIDMap(data.getNodes())));
    data.setVector(buildProbabilityVector(data.getNodes()));
    data.setTableName(buildTableName(data));
    data.setEventNode(buildEventNode(data));
    return data;
  }

  private static Set<Node> joinEventsAndConditions(List<Node> events, List<Node> conditions) {
    return Stream.concat(conditions.stream(), events.stream())
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static Map<Serializable, NodeState> buildNodeStateIDMap(Collection<Node> nodes) {
    return nodes.stream()
        .map(Node::getStates)
        .flatMap(Collection::stream)
        .map(ns -> Map.entry(ns.getId(), ns))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<Serializable, Node> buildNodeIDMap(Collection<Node> nodes) {
    return nodes.stream()
        .map(n -> Map.entry(n.getId(), n))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  protected ProbabilityVector buildProbabilityVector(Collection<Node> nodes) {
    return vectorFactory.build(new ArrayList<>(nodes));
  }

  protected String buildTableName(TableBuilderData tableBuilderData) {
    Set<Node> events = tableBuilderData.getEvents();
    Set<Node> conditions = tableBuilderData.getConditions();
    return TableUtils.buildTableName(
        NodeUtils.getNodeIds(events), NodeUtils.getNodeIds(conditions));
  }

  protected Node buildEventNode(TableBuilderData data) {
    if (data.getEvents().size() != 1) return null;
    return data.getEvents().stream().findFirst().orElseThrow();
  }

  protected <T extends ProbabilityTable> T copyTable(
      T table, BiFunction<List<Node>, List<Node>, T> copyBuilder) {
    return copyTable(table, copyBuilder, (t, c) -> {});
  }

  protected <T extends ProbabilityTable> T copyTable(
      T table,
      BiFunction<List<Node>, List<Node>, T> copyBuilder,
      BiConsumer<T, T> additionalCopyLogic) {
    List<Node> events = new ArrayList<>(table.getEvents());
    List<Node> conditions = new ArrayList<>(table.getConditions());
    T copied = copyBuilder.apply(events, conditions);
    double[] oldProbs = table.getProbabilities();
    double[] newProbs = copied.getProbabilities();
    System.arraycopy(oldProbs, 0, newProbs, 0, oldProbs.length);
    additionalCopyLogic.accept(table, copied);
    return copied;
  }
}
