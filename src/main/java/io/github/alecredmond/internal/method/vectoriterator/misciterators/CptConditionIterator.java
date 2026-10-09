package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.export.probabilitytables.cptentry.CptRow;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CptConditionIterator extends StateUpdateBase<Node, NodeState>
    implements ProbabilityIteratorTemplate {
  private final Node eventNode;
  private final Set<Node> conditionNodes;
  private final StandardCartesianIterator iterator;
  private final Map<Node, NodeState> lockedPositionMap;
  private final double[] probabilities;

  public CptConditionIterator(NetworkTable networkTable) {
    super(networkTable.getVector(), NodeState[]::new);
    this.eventNode = networkTable.getNetworkNode();
    this.conditionNodes = networkTable.getConditions();
    this.lockedPositionMap = new HashMap<>();
      this.iterator = StandardCartesianIterator.create(this, networkTable.getVector());
    this.probabilities = networkTable.getProbabilities();
  }

  public void iterateConditions(Consumer<CptRow> rowConsumer, Collection<NodeState> lockedStates) {
    lockNodesAndReset(lockedStates);
    int eventIndexInStateArray = conditionNodes.size();
    iterator.iterateOuter(
        rowStartIndex ->
            consumeConditionRow(
                rowConsumer,
                eventIndexInStateArray,
                probabilities,
                buildRowConditions(eventIndexInStateArray),
                rowStartIndex));
  }

  private void lockNodesAndReset(Collection<NodeState> lockedStates) {
    lockedPositionMap.clear();
    lockedStates.forEach(state -> lockedPositionMap.put(state.getNode(), state));
    iterator.reset();
  }

  private void consumeConditionRow(
      Consumer<CptRow> rowConsumer,
      int eventIndexInStateArray,
      double[] probabilities,
      Set<NodeState> rowConditions,
      int rowStartIndex) {
    CptRow row = new CptRow(rowConditions, new ArrayList<>(), rowStartIndex);
    iterator.iterateInner(
        index ->
            row.rowEntries()
                .add(
                    new CptEntry(
                        rowConditions,
                        states[eventIndexInStateArray],
                        probabilities[index],
                        index)));
    rowConsumer.accept(row);
  }

  private Set<NodeState> buildRowConditions(int indexOfEvent) {
    return new LinkedHashSet<>(Arrays.asList(states).subList(0, indexOfEvent));
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        lockedPositionMap.containsKey(node)
            ? lockedPositionMap.get(node)
            : node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> node.equals(eventNode) || lockedPositionMap.containsKey(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> conditionNodes.contains(node) || lockedPositionMap.containsKey(node);
  }

  public Function<Node, boolean[]> updateEvidenceArrays() {
    return node -> null;
  }

}
