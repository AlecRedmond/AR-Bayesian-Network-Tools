package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.tableentry.CptEntry;
import io.github.alecredmond.export.probabilitytables.tableentry.CptRow;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.ProbabilityIteratorTemplate;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardCartesianIterator;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StateUpdateBase;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class CptRowGenerator extends StateUpdateBase<Node, NodeState>
    implements ProbabilityIteratorTemplate {
  private final Node event;
  private final Set<Node> conditions;
  private final double[] probabilities;
  private final StandardCartesianIterator iterator;

  public CptRowGenerator(NetworkTable networkTable) {
    super(networkTable.getVector(), NodeState[]::new);
    this.event = networkTable.getNetworkNode();
    this.conditions = networkTable.getConditions();
    this.probabilities = networkTable.getProbabilities();
    this.iterator = StandardCartesianIterator.create(this, networkTable.getVector());
  }

  public CptRow[] buildRows() {
    List<CptRow> rows = new ArrayList<>();
    final int eventIdx = states.length - 1;
    iterator.iterateOuter(
        rowStartIdx -> {
          Set<NodeState> condStates = getConditionStates(eventIdx);
          List<CptEntry> rowEntries = new ArrayList<>();
          iterator.iterateInner(
              idx ->
                  rowEntries.add(
                      new CptEntry(
                          condStates,
                          states[eventIdx],
                          Arrays.copyOf(states, states.length),
                          idx,
                          probabilities[idx])));
          rows.add(new CptRow(condStates, Collections.unmodifiableList(rowEntries), rowStartIdx));
        });
    return rows.toArray(CptRow[]::new);
  }

  private Set<NodeState> getConditionStates(int conditionLength) {
    return Collections.unmodifiableSet(
        new LinkedHashSet<>(Arrays.asList(Arrays.copyOf(states, conditionLength))));
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return n -> n.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return event::equals;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return conditions::contains;
  }

  @Override
  public Function<Node, boolean[]> updateEvidenceArrays() {
    return n -> new boolean[0];
  }
}
