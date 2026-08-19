package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.export.method.network.NetworkScenario.RAIN_NETWORK;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.solver.SolverAlgorithm;
import io.github.alecredmond.export.solver.SolverResults;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.method.network.NetworkScenario;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NewInferenceEngineTest {
  static final List<NetworkScenario> scenarios = provideScenarios();
  InferenceEngine test;

  private static List<NetworkScenario> provideScenarios() {
    return Arrays.stream(NetworkScenario.values())
        .filter(scenario -> !scenario.equals(NetworkScenario.FANTASY_GRAPH))
        .toList();
  }

  @Nested
  class ObservationTests {
    static final double DELTA = 1e-3;

    public static Stream<Arguments> allObservations() {
      return scenarios.stream().flatMap(ObservationTests::getDirectCptEntryAndIPFP);
    }

    private static Stream<Arguments> getDirectCptEntryAndIPFP(NetworkScenario scenario) {
      BayesianNetwork network = scenario.get();
      BayesSolver solver = BayesSolver.create(network);
      if (!solver.writeCPTsFromConstraints()) {
        solver.forceSolve(SolverAlgorithm.JUNCTION_TREE_IPFP);
        return Stream.of(Arguments.of(network, solver));
      }
      BayesianNetwork ipfp = scenario.get();
      BayesSolver ipfpSolver = BayesSolver.create(ipfp);
      ipfpSolver.forceSolve(SolverAlgorithm.JUNCTION_TREE_IPFP);
      return Stream.of(Arguments.of(network, solver), Arguments.of(ipfp, ipfpSolver));
    }

    @ParameterizedTest
    @MethodSource("allObservations")
    void measureProbability_shouldEqualConstraints(BayesianNetwork network, BayesSolver solver) {
      test = InferenceEngine.create(network);
      assertTrue(network.isSolved());
      SolverResults results = solver.getResults();
      network
          .getNetworkData()
          .getConstraints()
          .forEach(
              constraint -> {
                test.setObserved(constraint.getConditionStates());
                double expected = constraint.getProbability();
                double actual = test.getPosteriorProbability(constraint.getEventStates());
                if (results != null) {
                  assertTrue(results.getResult(constraint).lastError() < DELTA);
                }
                assertEquals(expected, actual, DELTA);
              });
    }
  }

  @Nested
  class CoherenceChecks {
    static List<ArgumentProvider> providers;

    @BeforeAll
    static void initProvider() {
      providers = new ArrayList<>();
      providers.add(
          new ArgumentProvider(
              RAIN_NETWORK.getSupplier(),
              "SPRINKLER",
              "WET_GRASS",
              "CLOUDY",
              Map.of("CLOUDY", List.of("CLOUDY:TRUE", "CLOUDY:FALSE")),
              List.of(
                  new ConstraintAdder("RAIN:TRUE", List.of("CLOUDY:TRUE"), 0.5),
                  new ConstraintAdder("RAIN:TRUE", List.of("CLOUDY:FALSE"), 0.1)),
              List.of("SPRINKLER:TRUE"),
              List.of("CLOUDY:TRUE"),
              List.of("WET_GRASS:TRUE")));
    }

    static Stream<Arguments> getArguments() {
      return providers.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    void standardInference_shouldSucceed(ArgumentProvider provider) {
      test = InferenceEngine.create(provider.supplier.get());
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.setObservedById(provider.evidenceAlwaysSucceeds));
      assertNotNull(test.getObservedTableById(provider.controlNodeId));
      assertNotNull(test.copyObservedTableById(provider.controlNodeId));
      assertEquals(1.0, test.getPosteriorProbabilityById(provider.evidenceAlwaysSucceeds));
      assertNull(test.getObservedTableById(provider.addedNodeId));
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    void removingNodes_ShouldSucceed(ArgumentProvider provider) {
      BayesianNetwork network = provider.supplier.get();
      test = InferenceEngine.create(network);
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.setObservedById(provider.evidenceAlwaysSucceeds));
      network.removeNodeByID(provider.removedNodeId);
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.setObservedById(provider.evidenceAlwaysSucceeds));
      assertEquals(1.0, test.getPosteriorProbabilityById(provider.evidenceAlwaysSucceeds));
      assertNull(test.getObservedTableById(provider.removedNodeId));
    }

    @ParameterizedTest
    @MethodSource("getArguments")
    void addingNodes_shouldSucceed(ArgumentProvider provider) {
      BayesianNetwork network = provider.supplier.get();
      test = InferenceEngine.create(network);
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.setObservedById(provider.evidenceAlwaysSucceeds));
      provider.addedNodesMap.forEach(
          (nodeId, stateIds) -> {
            network.addNewNode(nodeId, stateIds);
            network.addParents(nodeId, provider.controlNodeId);
          });
      provider.addedNodeConstraints.forEach(
          added -> network.addConstraint(added.eventId, added.conditionIds, added.prob));
      assertDoesNotThrow(() -> test.getCurrentObservations());
      assertDoesNotThrow(() -> test.setObservedById(provider.evidenceWithNewNodes));
      assertEquals(1.0, test.getPosteriorProbabilityById(provider.evidenceWithNewNodes));
      assertNotNull(test.getObservedTableById(provider.addedNodeId));
    }

    @AllArgsConstructor
    static class ConstraintAdder {
      String eventId;
      List<String> conditionIds;
      double prob;
    }

    @AllArgsConstructor
    static class ArgumentProvider {
      Supplier<BayesianNetwork> supplier;
      String controlNodeId;
      String removedNodeId;
      String addedNodeId;
      Map<String, List<String>> addedNodesMap;
      List<ConstraintAdder> addedNodeConstraints;
      List<String> evidenceAlwaysSucceeds;
      List<String> evidenceWithNewNodes;
      List<String> evidenceWithRemovedNodes;
    }
  }
}
