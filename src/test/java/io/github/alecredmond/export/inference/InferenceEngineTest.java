package io.github.alecredmond.export.inference;

import static io.github.alecredmond.TestConfigs.*;
import static io.github.alecredmond.networkscenario.NetworkScenarioType.values;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.direct.JunctionTreeAlgorithm;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.inference.indirect.SampleCollection;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.directinput.DirectCptSolver;
import io.github.alecredmond.export.solver.ipfp.IPFPConstraintResult;
import io.github.alecredmond.export.solver.ipfp.IPFPJunctionTreeSolver;
import io.github.alecredmond.export.solver.ipfp.IPFPResults;
import io.github.alecredmond.export.solver.ipfp.IPFPSolver;
import io.github.alecredmond.internal.method.utils.DoublePrecision;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import io.github.alecredmond.networkscenario.scenarios.AHNetwork;
import io.github.alecredmond.networkscenario.scenarios.RainNetwork;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class InferenceEngineTest {

  @Nested
  class MethodTests {
    RainNetwork rainNetwork;
    BayesianNetwork net;
    InferenceEngine test;

    @BeforeEach
    void init() {
      rainNetwork = new RainNetwork();
      net = rainNetwork.buildNetwork();
      test = net.buildInferenceEngine();
    }

    @Test
    void observeNetwork_shouldUpdateProbabilities() {
      BinaryNode rain = rainNetwork.rain;
      BinaryNode wetGrass = rainNetwork.wetGrass;

      ObservedTable rainTable = test.getObservedTable(rain);
      double pRainPrior = rainTable.getProbability(rain.trueState());
      assertEquals(0.2, pRainPrior, 1E-6);
      System.out.println(pRainPrior);
      test.setObserved(wetGrass.trueState());
      double pRainGivenWet = rainTable.getProbability(rain.trueState());
      assertTrue(pRainGivenWet > 0.2);
      System.out.println(pRainGivenWet);
      // Exact value P(R|W) = P(W|R)P(R)/P(W)
      // P(W|R) = P(W|R,S)P(S|R) + P(W|R,~S)P(~S|R) = 0.99*0.01 + 0.9*0.99 = 0.9009
      // P(W|~R) = P(W|~R,S)P(S|~R) + P(W|~R,~S)P(~S|~R) = 0.9*0.4 + 0.0*0.6 = 0.36
      // P(W) = P(W|R)P(R) + P(W|~R)P(~R) = 0.9009*0.2 + 0.36*0.8 = 0.18018 + 0.288 = 0.46818
      // P(R|W) = (0.9009 * 0.2) / 0.46818 = 0.18018 / 0.46818 = 0.38485
      assertEquals(0.384852, pRainGivenWet, 1E-6);
    }

    @Test
    void observeNetwork_withEmptyList_shouldBeSameAsObserveMarginals() {
      ObservedTable table = test.getObservedTable(rainNetwork.rain);
      double pRainMarginal = table.getProbability(rainNetwork.rain.trueState());
      test.setObserved(List.of());
      double pRainObservedEmpty = table.getProbability(rainNetwork.rain.trueState());
      assertEquals(pRainMarginal, pRainObservedEmpty, DOUBLE_EQUALITY);
    }

    @Test
    void getNetworkTable_shouldReturnTable() {
      ProbabilityTable rainTable = net.getNetworkTable(rainNetwork.rain);
      assertNotNull(rainTable);
      assertEquals(1, rainTable.getNodes().size());

      ProbabilityTable grassTable = net.getNetworkTable(rainNetwork.wetGrass);
      assertNotNull(grassTable);
      assertEquals(3, grassTable.getNodes().size());
    }

    @Test
    void getNetworkTable_nonExistentNode_shouldReturnNull() {
      assertNull(net.getNetworkTableByNodeId("CLOUDY"));
    }

    @Test
    void getObservedTable_shouldReturnTable() {
      NodeState wetGrassTrue = rainNetwork.wetGrass.trueState();
      NodeState rainTrue = rainNetwork.rain.trueState();
      test.setObserved(wetGrassTrue);
      ObservedTable rainTable = test.getObservedTableByNodeName("RAIN");
      assertNotNull(rainTable);
      assertEquals(0.384852, rainTable.getProbability(rainTrue), DOUBLE_EQUALITY);
    }

    @Test
    void getObservedTable_nonExistentNode_shouldReturnNull() {
      NodeState wetGrassTrue = rainNetwork.wetGrass.trueState();
      test.setObserved(wetGrassTrue);
      assertNull(test.getObservedTableById("CLOUDY"));
    }

    @Test
    void observeProbability_singleEvent_shouldReturnCorrectProb() {
      NodeState rainTrue = rainNetwork.rain.trueState();
      test.resetObservations();
      double pRainTrue = test.getPosteriorProbability(rainTrue);
      assertEquals(0.2, pRainTrue, DOUBLE_EQUALITY);
    }

    @Test
    void observeProbability_jointEvent_shouldReturnCorrectProb() {
      NodeState rainTrue = rainNetwork.rain.trueState();
      NodeState sprinklerFalse = rainNetwork.sprinkler.falseState();
      test.resetObservations();
      // P(RAIN:TRUE, SPRINKLER:FALSE) = P(S:F | R:T) * P(R:T)
      // P(R:T, S:F) = 0.99 * 0.2 = 0.198
      double pJoint = test.getPosteriorProbability(rainTrue, sprinklerFalse);
      assertEquals(0.198, pJoint, DOUBLE_EQUALITY);
    }

    @Test
    void observeProbability_emptyEvent_shouldReturnOne() {
      double pEmpty = test.getPosteriorProbability();
      assertEquals(1.0, pEmpty, DOUBLE_EQUALITY);
    }
  }

  @Nested
  class ScenarioTests {
    InferenceEngine test;

    @Test
    void testSolves_RainSprinkler() {
      RainNetwork rainNetwork = new RainNetwork();
      BayesianNetwork net = rainNetwork.buildNetwork();
      test = net.buildInferenceEngine();

      BinaryNode rain = rainNetwork.rain;
      NodeState rainTrue = rain.trueState();
      NodeState wetGrassTrue = rainNetwork.wetGrass.trueState();

      if (PRINT_TABLES) net.printNetwork();
      test.setObserved(wetGrassTrue);
      if (PRINT_TABLES) test.printObserved();
      test.resetObservations();

      generateSamples(test, rainTrue, Set.of());

      test.setObserved(wetGrassTrue);
      System.out.println("\n--- Now testing P(RAIN:TRUE | WET_GRASS:TRUE) ---");
      generateSamples(test, rainTrue, Set.of(wetGrassTrue));
    }

    private void generateSamples(
        InferenceEngine engine, NodeState measuredState, Set<NodeState> observedStates) {
      double observedProb = engine.getPosteriorProbability(measuredState);
      double expected = observedProb * NUMBER_OF_SAMPLES;
      double expectedDelta = Math.sqrt(NUMBER_OF_SAMPLES) * STANDARD_DEVIATIONS;
      long lowerBound = Math.max(0, (long) (expected - expectedDelta));
      long upperBound = (long) (expected + expectedDelta);

      Map<Node, NodeObservation> currentObs = engine.getCurrentObservations();
      currentObs.values().forEach(System.out::println);
      assertEquals(ObservationStatus.UNOBSERVED, currentObs.get(measuredState.getNode()).status());
      for (NodeState observed : observedStates) {
        assertEquals(ObservationStatus.OBSERVED, currentObs.get(observed.getNode()).status());
      }

      SampleCollection sampleCollection =
          MonteCarloSampler.create(engine.getNetwork())
              .setObserved(engine.getCurrentObservations())
              .generateSamples(NUMBER_OF_SAMPLES);
      sampleCollection.setDisplayedNodes(List.of(measuredState.getNode()));
      int count = sampleCollection.countSamplesIncludingStates(List.of(measuredState));

      System.out.printf(
          "Test State: %s%nExpected: %.2f (%.0f samples)%nAllowed Range: [%d, %d]%nActual Sample Count: %d%n",
          measuredState, observedProb, expected, lowerBound, upperBound, count);

      assertTrue(
          count >= lowerBound && count <= upperBound,
          String.format(
              "Sample count %d for %s is outside expected range [%d, %d]",
              count, measuredState, lowerBound, upperBound));
    }

    @Test
    void testNetworkAH_NonLocalConstraints() {
      AHNetwork ahNetwork = new AHNetwork();
      BayesianNetwork net = ahNetwork.buildNetwork();
      test = net.buildInferenceEngine();

      if (PRINT_TABLES) {
        net.printNetwork();
        test.printObserved();
      }

      NodeState testState = ahNetwork.b.trueState();
      generateSamples(test, testState, Set.of());
    }
  }

  @Nested
  class ObservationTests {
    static final double IPFP_DELTA = 1e-3;
    static final boolean DEBUG_LOG_LOSSES = false;
    static final List<NetworkScenario> scenarioTypes = provideScenarios();

    InferenceEngine test;

    public static Stream<Arguments> allObservations() {
      return scenarioTypes.stream().flatMap(ObservationTests::getDirectCptEntryAndIPFP);
    }

    private static Stream<Arguments> getDirectCptEntryAndIPFP(NetworkScenario scenario) {
      BayesianNetworkBuilder builder = scenario.getBuilder();
      BayesSolver defaultSolver = scenario.getDefaultSolver();
      if (defaultSolver instanceof DirectCptSolver directCptSolver) {
        IPFPSolver ipfpSolver = new IPFPJunctionTreeSolver();
        return Stream.of(Arguments.of(builder, directCptSolver), Arguments.of(builder, ipfpSolver));
      } else {
        return Stream.of(Arguments.of(builder, defaultSolver));
      }
    }

    private static List<NetworkScenario> provideScenarios() {
      return Arrays.stream(values()).map(NetworkScenarioType::get).toList();
    }

    @ParameterizedTest
    @MethodSource("allObservations")
    void measureProbability_shouldEqualConstraints(
        BayesianNetworkBuilder networkBuilder, BayesSolver solver) {
      BayesianNetwork network = networkBuilder.build(solver).orElseThrow();
      assertNotNull(network);
      test = new JunctionTreeAlgorithm(network);

      double delta;
      IPFPResults results;
      switch (solver) {
        case IPFPSolver ipfpSolver -> {
          delta = IPFP_DELTA;
          results = ipfpSolver.getResults();
        }
        case null, default -> {
          delta = DOUBLE_EQUALITY;
          results = null;
        }
      }

      network
          .getConstraints()
          .forEach(
              constraint -> {
                test.setObserved(constraint.getConditionStates());
                double expected = constraint.getProbability();
                double actual = test.getPosteriorProbability(constraint.getEventStates());
                assertEquals(expected, actual, delta);

                if (results == null) return;

                // TEST ERROR DECREASES OVER TIME
                IPFPConstraintResult result = results.getResult(constraint);
                double lastError = result.lastError();
                assertTrue(lastError < delta);
                Arrays.stream(result.errors())
                    .mapToObj(
                        error ->
                            error >= lastError || DoublePrecision.fuzzyEquals(error, lastError))
                    .forEach(Assertions::assertTrue);

                // LOG ERROR & LOSS OVER TIME
                if (!DEBUG_LOG_LOSSES) return;
                System.out.println(constraint);
                System.out.println(Arrays.toString(result.errors()));
                System.out.println(Arrays.toString(result.losses()));
              });
    }
  }
}
