package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.export.probabilitytables.cptentry.CptRow;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetDefault;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.OdometerUpdateWriteStatesToArray;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CptConditionIterator implements OdometerResetDefault, OdometerUpdateWriteStatesToArray {
  private final Node eventNode;
  private final Set<Node> conditionNodes;
  private final VectorIterator<VectorOdometer> iterator;
  private final Map<Node, NodeState> lockedPositionMap;

  public CptConditionIterator(NetworkTable networkTable) {
    this.eventNode = networkTable.getNetworkNode();
    this.conditionNodes = networkTable.getConditions();
    this.lockedPositionMap = new HashMap<>();
    this.iterator = new VectorIterator<>(networkTable.getVector(), this, VectorOdometer::new);
  }

  public void iterateConditions(Consumer<CptRow> rowConsumer, Collection<NodeState> lockedStates) {
    lockNodesAndReset(lockedStates);
    int eventIndexInStateArray = conditionNodes.size();
    iterator.iterateOuter(
        (odometer, rowStartIndex) ->
            consumeConditionRow(
                rowConsumer,
                eventIndexInStateArray,
                odometer.getProbabilities(),
                buildRowConditions(odometer, eventIndexInStateArray),
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
        (odometer, index) ->
            row.rowEntries()
                .add(
                    new CptEntry(
                        rowConditions,
                        odometer.getStates()[eventIndexInStateArray],
                        probabilities[index],
                        index)));
    rowConsumer.accept(row);
  }

  private Set<NodeState> buildRowConditions(VectorOdometer odometer, int indexOfEvent) {
    NodeState[] allStates = odometer.getStates();
    return new LinkedHashSet<>(Arrays.asList(allStates).subList(0, indexOfEvent));
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> node.equals(eventNode) || lockedPositionMap.containsKey(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> conditionNodes.contains(node) || lockedPositionMap.containsKey(node);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        lockedPositionMap.containsKey(node)
            ? lockedPositionMap.get(node)
            : node.getStates().getFirst();
  }
}
