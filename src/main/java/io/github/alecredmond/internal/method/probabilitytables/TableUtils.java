package io.github.alecredmond.internal.method.probabilitytables;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.io.Serializable;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableUtils {

  private TableUtils() {}

  public static void marginalizeJointTable(ProbabilityTable table) {
    double[] probabilities = table.getProbabilities();
    double tableSum = Arrays.stream(probabilities).sum();
    double ratio = tableSum == 0.0 ? 0.0 : 1 / tableSum;
    for (int i = 0; i < probabilities.length; i++) {
      probabilities[i] = ratio * probabilities[i];
    }
  }

  public static Set<Node> getCommonNodes(ProbabilityTable tableA, ProbabilityTable tableB) {
    return NodeUtils.getOverlap(tableA.getNodes(), tableB.getNodes());
  }

  public static Map<NodeState, Double> buildMarginalProbMap(SingleEventTableData tableData) {
    List<NodeState> states = tableData.getEventNode().getStates();
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
    sb.append(CollectionToString.apply(eventIds));
    if (!conditionIds.isEmpty()) {
      sb.append("|");
      sb.append(CollectionToString.apply(conditionIds));
    }
    return sb.append(")").toString();
  }

  public static String buildObservedTableName(
      Node measured, Map<Node, NodeObservation> nodeObservationMap) {
    return "P(" + measured.getId() + observationStrings(nodeObservationMap) + ")";
  }

  private static String observationStrings(Map<Node, NodeObservation> nodeObservationMap) {
    StringBuilder sb = new StringBuilder();
    for (NodeObservation observation : nodeObservationMap.values()) {
      if (observation.status().equals(UNOBSERVED)) continue;
      if (sb.isEmpty()) sb.append("|");
      else sb.append(", ");
      sb.append(observation);
    }
    return sb.toString();
  }
}
