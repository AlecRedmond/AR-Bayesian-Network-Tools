package io.github.alecredmond.internal.method.sampler;

import static io.github.alecredmond.TestConfigs.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.method.network.NetworkScenario;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.sampler.SampleCollection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LikelihoodWeightingSamplerTest {
  static final Set<NetworkScenario> EXCLUDED_IN_LONG_TESTS = Set.of(NetworkScenario.FANTASY_GRAPH);
  static final boolean PRINT_DEBUG_OUTPUT = false;

  public static Stream<Arguments> allNetworkScenarios() {
    return Arrays.stream(NetworkScenario.values())
        .filter(scenario -> !EXCLUDED_IN_LONG_TESTS.contains(scenario) || SOLVE_LONG_TESTS)
        .map(NetworkScenario::get)
        .map(BayesianNetwork::solveNetwork)
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("allNetworkScenarios")
  void testSamplerReturnsCorrectCounts(BayesianNetwork network) {
    MonteCarloSampler sampler = network.buildSampler();
    List<ProbabilityConstraint> constraints =
        network.getNetworkData().getConstraints().stream()
            .filter(this::connectedConstraint)
            .toList();
    InferenceEngine engine = network.buildInferenceEngine();
    SampleCollection sampleCollection = sampler.generateSamples(NUMBER_OF_SAMPLES);
    if (PRINT_DEBUG_OUTPUT) {
      System.out.printf(
          "Measuring for network %s%n".formatted(network.getNetworkData().getNetworkName()));
    }
    constraints.forEach(c -> measureCount(sampleCollection, c, engine));
  }

  private boolean connectedConstraint(ProbabilityConstraint constraint) {
    if (constraint instanceof MarginalConstraint) return true;
    Set<Node> conditionNodes = constraint.getConditionNodes();
    Set<Node> eventNodes = constraint.getEventNodes();
    if (!sharedBonds(eventNodes)) return false;
    Set<Node> sharedConnected = getSharedConnected(eventNodes);
    return sharedConnected.containsAll(conditionNodes);
  }

  void measureCount(
      SampleCollection sampleCollection, ProbabilityConstraint constraint, InferenceEngine engine) {
    double probOfObserved = constraint.getProbability();
    Set<NodeState> conditions = constraint.getConditionStates();
    Set<NodeState> allStates = constraint.getAllStates();
    double error = Math.sqrt(NUMBER_OF_SAMPLES) * ALLOWED_STDEV;

    int conditionSamples = sampleCollection.countSamplesIncludingStates(conditions);
    double expectedConditionProb = engine.getPosteriorProbability(conditions);
    double expectedConditional = NUMBER_OF_SAMPLES * expectedConditionProb;
    if (PRINT_DEBUG_OUTPUT) {
      System.out.printf("Constraint = %s%n".formatted(constraint));
      System.out.printf("-- Cond  E/A = %d | %d%n", (int) expectedConditional, conditionSamples);
    }
    assertEquals(expectedConditional, conditionSamples, error);

    int counted = sampleCollection.countSamplesIncludingStates(allStates);
    double expected = probOfObserved * conditionSamples;
    if (PRINT_DEBUG_OUTPUT) {
      System.out.printf("-- Joint E/A = %d | %d %n", (int) expected, counted);
    }
    assertEquals(expected, counted, error);
  }

  private boolean sharedBonds(Set<Node> eventNodes) {
    if (eventNodes.size() == 1) return true;
    for (Node node : eventNodes) {
      boolean anyConnection =
          Stream.concat(node.getParents().stream(), node.getChildren().stream())
              .anyMatch(eventNodes::contains);
      if (anyConnection) return false;
    }
    return true;
  }

  private Set<Node> getSharedConnected(Set<Node> eventNodes) {
    return eventNodes.stream()
        .flatMap(node -> Stream.concat(node.getParents().stream(), node.getChildren().stream()))
        .collect(Collectors.toSet());
  }
}
