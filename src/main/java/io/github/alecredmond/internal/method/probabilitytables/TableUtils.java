package io.github.alecredmond.internal.method.probabilitytables;

import static io.github.alecredmond.internal.method.node.NodeUtils.formatIDsToString;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.StateCombinationGenerator;
import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableUtils {

  private TableUtils() {}

  public static <T extends ProbabilityTableData> double getProbability(
      Collection<NodeState> states, T tableData) {
    return tableData.getProbabilities()[getIndex(states, tableData)];
  }

  public static <T extends ProbabilityTableData> int getIndex(
      Collection<NodeState> states, T tableData) {
    ProbabilityVector vector = tableData.getVector();
    int[] strideLengths = vector.getStrideLengths();
    int index = 0;
    for (NodeState state : states) {
      int stateValue = vector.getStateValueMap().getOrDefault(state, 0);
      int nodeIndex = vector.getNodeIndexMap().getOrDefault(state.getNode(), 0);
      index += strideLengths[nodeIndex] * stateValue;
    }
    return index;
  }

  public static <S extends Serializable, T extends ProbabilityTableData>
      Collection<NodeState> assertAllIdsPresent(
          Collection<S> stateIds, Set<Node> expected, T tableData) {
    return assertAllNodesPresent(convertIdsToStates(stateIds, tableData), expected);
  }

  public static Collection<NodeState> assertAllNodesPresent(
      Collection<NodeState> states, Set<Node> allNodes) {
    Map<Node, NodeState> request = NodeUtils.generateRequest(states);
    if (request.keySet().containsAll(allNodes)) return states;
    throw new ProbabilityTableRequestException(
        "request %s does not contain all nodes requested %s"
            .formatted(
                NodeUtils.formatStatesToString(states), NodeUtils.formatNodesToString(allNodes)));
  }

  public static <S extends Serializable, T extends ProbabilityTableData>
      List<NodeState> convertIdsToStates(Collection<S> ids, T tableData) {
    Map<Serializable, NodeState> idMap = tableData.getNodeStateIDMap();
    List<Serializable> missing = new ArrayList<>();
    List<NodeState> states = new ArrayList<>();
    ids.forEach(
        id ->
            Optional.ofNullable(idMap.get(id)).ifPresentOrElse(states::add, () -> missing.add(id)));
    if (missing.isEmpty()) return states;
    throw new ProbabilityTableRequestException(
        "IDs %s not found in table %s!".formatted(formatIDsToString(missing), tableData));
  }

  public static void marginalizeJointTable(ProbabilityTable table) {
    double[] probabilities = table.getProbabilities();
    double tableSum = Arrays.stream(probabilities).sum();
    double ratio = tableSum == 0.0 ? 0.0 : 1 / tableSum;
    for (int i = 0; i < probabilities.length; i++) {
      probabilities[i] = ratio * probabilities[i];
    }
  }

  public static <T extends Collection<NodeState>, R extends T> List<T> generateStateCombinations(
      Set<Node> includedNodes, Supplier<R> supplier, ProbabilityTable table) {
    if (includedNodes.isEmpty()) return new ArrayList<>();
    return new StateCombinationGenerator(table).generateCombos(includedNodes, supplier);
  }

  public static Set<Node> getCommonNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return NodeUtils.getOverlap(tableA.getNodes(), tableB.getNodes());
  }

  public static <T extends SingleEventTableData> Map<NodeState, Double> buildConditionalProbMap(
      Collection<NodeState> conditionStates, T tableData) {
    Map<NodeState, Double> map = new LinkedHashMap<>();
    List<NodeState> events = tableData.getEventNode().getNodeStates();
    double[] probabilities = tableData.getProbabilities();
    int firstIndex = getIndex(conditionStates, tableData);
    int bound = events.size();
    for (int i = 0; i < bound; i++) map.put(events.get(i), probabilities[firstIndex + i]);
    return map;
  }

  public static Map<NodeState, Double> buildMarginalProbMap(SingleEventTableData tableData) {
    List<NodeState> states = tableData.getEventNode().getNodeStates();
    double[] prob = tableData.getProbabilities();
    Map<NodeState, Double> map = new LinkedHashMap<>();
    for (int i = 0; i < prob.length; i++) {
      map.put(states.get(i), prob[i]);
    }
    return map;
  }

  public static String buildTableName(
      List<Serializable> eventIds, List<Serializable> conditionIds) {
    StringBuilder sb = new StringBuilder("P(");
    sb.append(formatIDsToString(eventIds));
    if (!conditionIds.isEmpty()) {
      sb.append("|");
      sb.append(formatIDsToString(conditionIds));
    }
    return sb.append(")").toString();
  }

  public static String buildObservedTableName(
      Node measured, Map<Node, NodeObservation> nodeObservationMap) {
    StringBuilder sb = new StringBuilder("P(");
    sb.append(measured.getId());
    if (!nodeObservationMap.isEmpty()) {
      sb.append("|");
      appendAllObservations(sb, nodeObservationMap);
    }
    return sb.append(")").toString();
  }

  private static void appendAllObservations(
      StringBuilder sb, Map<Node, NodeObservation> nodeObservationMap) {
    Iterator<NodeObservation> iterator = nodeObservationMap.values().iterator();
    while (iterator.hasNext()) {
      NodeObservation observation = iterator.next();
      switch (observation.status()) {
        case NEGATED, OBSERVED, PARTIALLY_OBSERVED -> sb.append(observation);
      }
      if (iterator.hasNext()) {
          sb.append(", ");
      }
    }
  }
}
