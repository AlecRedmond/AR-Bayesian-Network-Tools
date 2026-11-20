package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import static java.util.Collections.unmodifiableSet;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class BaseTableFactory<T extends ProbabilityTable> implements TableFactory<T> {
  private final ProbabilityVectorFactory vectorFactory = new ProbabilityVectorFactory();

  protected final ProbabilityVector copyProbabilityVector(ProbabilityVector vector) {
    return vectorFactory.build(Arrays.stream(vector.getOrderedNodes()).toList());
  }

  public T copyTable(T table) {
    List<Node> events = new ArrayList<>(table.getEvents());
    List<Node> conditions = new ArrayList<>(table.getConditions());
    T copied = buildTable(events, conditions);
    double[] oldProbabilities = table.getProbabilities();
    double[] newProbabilities = copied.getProbabilities();
    System.arraycopy(oldProbabilities, 0, newProbabilities, 0, oldProbabilities.length);
    return copied;
  }

  public final T buildTable(List<Node> events, List<Node> conditions) {
    performInstanceValidation(events, conditions);
    return buildTableFromFactoryData(buildData(events, conditions));
  }

  protected abstract void performInstanceValidation(List<Node> events, List<Node> conditions);

  protected final TableFactoryData buildData(List<Node> events, List<Node> conditions) {
    TableFactoryData data = new TableFactoryData();
    data.setEvents(unmodifiableSet(new LinkedHashSet<>(events)));
    data.setConditions(unmodifiableSet(new LinkedHashSet<>(conditions)));
    data.setNodes(joinEventsAndConditions(events, conditions));
    data.setVector(buildProbabilityVector(data.getNodes()));
    data.setTableName(buildTableName(data));
    data.setEventNode(buildEventNode(data));
    return data;
  }

  protected abstract T buildTableFromFactoryData(TableFactoryData data);

  private static Set<Node> joinEventsAndConditions(List<Node> events, List<Node> conditions) {
    Set<Node> nodes =
        Stream.concat(conditions.stream(), events.stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return unmodifiableSet(nodes);
  }

  protected final ProbabilityVector buildProbabilityVector(Collection<Node> nodes) {
    return vectorFactory.build(new ArrayList<>(nodes));
  }

  protected static String buildTableName(TableFactoryData tableFactoryData) {
    Set<Node> events = tableFactoryData.getEvents();
    Set<Node> conditions = tableFactoryData.getConditions();
    return TableUtils.buildTableName(events, conditions);
  }

  protected static Node buildEventNode(TableFactoryData data) {
    Set<Node> events = data.getEvents();
    return events.size() == 1 ? events.iterator().next() : null;
  }
}
