package io.github.alecredmond.export.inference;

import static io.github.alecredmond.TestConfigs.DOUBLE_EQUALITY;
import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static io.github.alecredmond.export.inference.ObservationStatus.NEGATED;
import static io.github.alecredmond.export.inference.ObservationStatus.UNOBSERVED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alecredmond.export.method.network.NetworkScenario;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ObservableTest {
  private static final int LONG_TEST_LOG2_WIDTH = 8;
  private static final int SHORT_TEST_LOG2_WIDTH = 6;
  private static final double MAX_LOG_WIDTH = getMaxLogWidth();

  private static double getMaxLogWidth() {
    return Math.log(2) * (SOLVE_LONG_TESTS ? LONG_TEST_LOG2_WIDTH : SHORT_TEST_LOG2_WIDTH);
  }

  static Stream<Arguments> supplyNetworks() {
    return Arrays.stream(NetworkScenario.values())
        .map(NetworkScenario::get)
        .filter(ObservableTest::suitableTreeWidth)
        .map(BayesianNetwork::solveNetwork)
        .map(Arguments::of);
  }

  private static boolean suitableTreeWidth(BayesianNetwork network) {
    double logWidth =
        network.getNetworkData().getNodeIDsMap().values().stream()
            .map(Node::getNodeStates)
            .mapToInt(Collection::size)
            .mapToDouble(Math::log)
            .sum();

    return logWidth <= MAX_LOG_WIDTH;
  }

  private boolean observationsNegated(Map<Node, NodeObservation> obsMap) {
    return obsMap.values().stream().map(NodeObservation::status).anyMatch(NEGATED::equals);
  }

  @ParameterizedTest
  @MethodSource("supplyNetworks")
  void testSetObservations(BayesianNetwork network) {
    testCommon(network, InferenceEngine::setObserved);
  }

  @ParameterizedTest
  @MethodSource("supplyNetworks")
  void testObserveIndividualStates(BayesianNetwork network) {
    testCommon(
        network,
        (engine, obsMap) -> {
          if (observationsNegated(obsMap)) {
            engine.setObserved(obsMap);
            return;
          }
          List<NodeState> toObserve =
              obsMap.values().stream()
                  .filter(o -> !o.status().equals(UNOBSERVED))
                  .map(NodeObservation::observedStates)
                  .flatMap(Collection::stream)
                  .toList();

          engine.setObserved(toObserve);
        });
  }

  @ParameterizedTest
  @MethodSource("supplyNetworks")
  void testEliminateStates(BayesianNetwork network) {
    testCommon(
        network,
        (engine, obsMap) -> {
          List<NodeState> toEliminate =
              obsMap.values().stream()
                  .map(NodeObservation::eliminatedStates)
                  .flatMap(Collection::stream)
                  .toList();

          engine.eliminateStates(toEliminate);
        });
  }

  private void testCommon(
      BayesianNetwork network,
      BiConsumer<InferenceEngine, Map<Node, NodeObservation>> applyObservations) {
    InferenceEngine observedEngine = network.buildInferenceEngine();
    InferenceEngine unobservedEngine = network.buildInferenceEngine();
    Map<Node, NodeObservation> defaultObsMap = NodeObservation.createUnobservedMap(network);
    List<Node> nodes = new ArrayList<>(network.getNodes());
    Map<Node, List<ObservationRecord>> recordMap = new LinkedHashMap<>();
    nodes.forEach(node -> recordMap.put(node, recursiveObservationFinder(node)));
    recursivelyCheckElimination(
        nodes,
        unobservedEngine,
        observedEngine,
        defaultObsMap,
        0,
        nodes.size(),
        recordMap,
        new Combinations(),
        applyObservations);
  }

  private void recursivelyCheckElimination(
      List<Node> nodes,
      InferenceEngine unobservedEngine,
      InferenceEngine observedEngine,
      Map<Node, NodeObservation> obsMap,
      int depth,
      int nodeSize,
      Map<Node, List<ObservationRecord>> recordMap,
      Combinations combinations,
      BiConsumer<InferenceEngine, Map<Node, NodeObservation>> applyObservations) {
    if (depth == nodeSize) {
      combinations.performInternalLogic();
      performEndCheck(observedEngine, unobservedEngine, obsMap, applyObservations);
      return;
    }
    List<ObservationRecord> records = recordMap.get(nodes.get(depth));
    if (!combinations.depthReached) combinations.combos *= records.size();
    for (ObservationRecord r : records) {
      recursivelyCheckElimination(
          nodes,
          unobservedEngine,
          observedEngine,
          NodeObservation.eliminate(obsMap, r.eliminated),
          depth + 1,
          nodeSize,
          recordMap,
          combinations,
          applyObservations);
    }
  }

  private void performEndCheck(
      InferenceEngine engine,
      InferenceEngine unobservedEngine,
      Map<Node, NodeObservation> obsMap,
      BiConsumer<InferenceEngine, Map<Node, NodeObservation>> applyObservations) {
    applyObservations.accept(engine, obsMap);
    if (observationsNegated(obsMap)) {
      double totalProb =
          engine.getObservedTables().values().stream()
              .map(ObservedTable::getProbabilities)
              .flatMapToDouble(Arrays::stream)
              .sum();
      assertEquals(0.0, totalProb, DOUBLE_EQUALITY);
      engine.resetObservations();
      return;
    }
    Set<NodeState> allEliminated =
        getAllFromObservations(obsMap.values(), NodeObservation::eliminatedStates);
    if (!allEliminated.isEmpty()) {
      double jointEliminatedSum = engine.getPosteriorProbability(allEliminated);
      assertEquals(0.0, jointEliminatedSum, DOUBLE_EQUALITY);
    }

    Set<NodeState> allRemaining =
        getAllFromObservations(obsMap.values(), NodeObservation::observedStates);
    double jointSumInUnobserved = unobservedEngine.getPosteriorProbability(allRemaining);
    if (jointSumInUnobserved != 0.0) {
      double probability = engine.getPosteriorProbability(allRemaining);
      assertEquals(1.0, probability, DOUBLE_EQUALITY);
    }
    engine.resetObservations();
  }

  private Set<NodeState> getAllFromObservations(
      Collection<NodeObservation> values, Function<NodeObservation, Set<NodeState>> getStates) {
    return values.stream()
        .map(getStates)
        .flatMap(Collection::stream)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  List<ObservationRecord> recursiveObservationFinder(Node node) {
    List<NodeState> states = node.getNodeStates();
    List<ObservationRecord> remainingObs = new ArrayList<>();
    for (int kept = states.size(); kept >= 0; kept--) {
      recursivelyGenerateEliminationSets(
          states,
          new LinkedHashSet<>(states),
          new LinkedHashSet<>(),
          remainingObs,
          kept,
          0,
          states.size());
    }
    return remainingObs;
  }

  private void recursivelyGenerateEliminationSets(
      List<NodeState> states,
      LinkedHashSet<NodeState> remaining,
      LinkedHashSet<NodeState> eliminated,
      List<ObservationRecord> records,
      int kept,
      int depth,
      int statesSize) {
    if (remaining.size() == kept) {
      ObservationStatus status = determineObservationStatus(remaining.size(), eliminated.size());
      records.add(
          new ObservationRecord(
              new LinkedHashSet<>(remaining), new LinkedHashSet<>(eliminated), status));
      return;
    }

    if (depth >= statesSize) {
      return;
    }

    NodeState currentState = states.get(depth);

    recursivelyGenerateEliminationSets(
        states, remaining, eliminated, records, kept, depth + 1, statesSize);

    remaining.remove(currentState);
    eliminated.add(currentState);

    recursivelyGenerateEliminationSets(
        states, remaining, eliminated, records, kept, depth + 1, statesSize);

    eliminated.remove(currentState);
    remaining.add(currentState);
  }

  private ObservationStatus determineObservationStatus(int remainingCount, int eliminatedCount) {
    if (eliminatedCount == 0) {
      return ObservationStatus.UNOBSERVED;
    } else if (remainingCount == 0) {
      return NEGATED;
    } else if (remainingCount == 1) {
      return ObservationStatus.OBSERVED;
    } else {
      return ObservationStatus.PARTIALLY_OBSERVED;
    }
  }

  record ObservationRecord(
      Set<NodeState> remaining, Set<NodeState> eliminated, ObservationStatus status) {}

  static class Combinations {
    int completionSegs = -1;
    int combos;
    int totalCombos;
    boolean depthReached;
    double segmentSize;
    double nextPrint;

    Combinations() {
      this.combos = 1;
      this.depthReached = false;
    }

    void performInternalLogic() {
      if (depthReached) decrementAndPrint();
      else setCombosAndInit();
    }

    void decrementAndPrint() {
      combos--;
      if (combos >= nextPrint) return;
      double completionPerc = 100 * ((double) (totalCombos - combos) / totalCombos);
      System.out.printf("%2.2f%% COMPLETE, REMAINING == %d%n", completionPerc, combos);
      nextPrint -= segmentSize;
    }

    void setCombosAndInit() {
      totalCombos = combos;
      depthReached = true;
      segmentSize = (double) combos / completionSegs;
      nextPrint = completionSegs > 0 ? combos - segmentSize : segmentSize;
      if (nextPrint > 0) {
        System.out.println("NUMBER OF COMBOS TO COVER = " + combos);
      }
    }
  }
}
