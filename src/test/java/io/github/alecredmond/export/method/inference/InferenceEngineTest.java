package io.github.alecredmond.export.method.inference;

import static io.github.alecredmond.TestConfigs.*;
import static io.github.alecredmond.export.method.network.NetworkScenario.*;
import static io.github.alecredmond.export.method.network.NetworkScenario.RAIN_NETWORK;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.sampler.MonteCarloSampler;
import io.github.alecredmond.export.sampler.SampleCollection;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.method.inference.InferenceEngineImpl;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.solver.BayesSolverImpl;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Slf4j
class InferenceEngineTest {

  @Nested
  class MethodTests {
    BayesianNetwork net;
    InferenceEngine test;

    @BeforeEach
    void init() {
      net = RAIN_NETWORK.getSupplier().get();
      test = net.buildInferenceEngine();
    }

    @Test
    void observeNetwork_shouldUpdateProbabilities() {
      assertEquals(0.2, test.getObservedTableById("RAIN").getProbabilityById("RAIN:TRUE"), 1E-6);
      test.setObservedById("WET_GRASS:TRUE");
      double pRainGivenWet = test.getObservedTableById("RAIN").getProbabilityById("RAIN:TRUE");
      assertTrue(pRainGivenWet > 0.2);
      // Exact value P(R|W) = P(W|R)P(R)/P(W)
      // P(W|R) = P(W|R,S)P(S|R) + P(W|R,~S)P(~S|R) = 0.99*0.01 + 0.9*0.99 = 0.9009
      // P(W|~R) = P(W|~R,S)P(S|~R) + P(W|~R,~S)P(~S|~R) = 0.9*0.4 + 0.0*0.6 = 0.36
      // P(W) = P(W|R)P(R) + P(W|~R)P(~R) = 0.9009*0.2 + 0.36*0.8 = 0.18018 + 0.288 = 0.46818
      // P(R|W) = (0.9009 * 0.2) / 0.46818 = 0.18018 / 0.46818 = 0.38485
      assertEquals(0.384852, pRainGivenWet, 1E-6);
    }

    @Test
    void observeNetwork_withNonExistentState_shouldThrowException() {
      assertThrows(Exception.class, () -> test.setObservedById(List.of("ZOMBIE:TRUE")));
    }

    @Test
    void observeNetwork_withEmptyList_shouldBeSameAsObserveMarginals() {
      double pRainMarginal = test.getObservedTableById("RAIN").getProbabilityById("RAIN:TRUE");

      test.setObserved(List.of());
      double pRainObservedEmpty = test.getObservedTableById("RAIN").getProbabilityById("RAIN:TRUE");

      assertEquals(pRainMarginal, pRainObservedEmpty);
    }

    @Test
    void getNetworkTable_shouldReturnTable() {
      net.solveNetwork();
      ProbabilityTable rainTable = net.getNetworkTable("RAIN");
      assertNotNull(rainTable);
      assertEquals(1, rainTable.getNodes().size());

      ProbabilityTable grassTable = net.getNetworkTable("WET_GRASS");
      assertNotNull(grassTable);
      assertEquals(3, grassTable.getNodes().size());
    }

    @Test
    void getNetworkTable_nonExistentNode_shouldReturnNull() {
      net.solveNetwork();
      assertNull(net.getNetworkTable("ZOMBIE"));
    }

    @Test
    void getNetworkTable_beforeSolve_shouldImplicitlySolve() {
      assertDoesNotThrow(
          () -> {
            ProbabilityTable rainTable = net.getNetworkTable("RAIN");
            assertNotNull(rainTable);
          });
    }

    @Test
    void getObservedTable_shouldReturnTable() {
      test.setObservedById(List.of("WET_GRASS:TRUE"));
      ObservedTable rainTable = test.getObservedTableById("RAIN");
      assertNotNull(rainTable);
      assertEquals(0.384852, rainTable.getProbabilityById("RAIN:TRUE"), 1E-6);
    }

    @Test
    void getObservedTable_nonExistentNode_shouldReturnNull() {
      test.setObservedById(List.of("WET_GRASS:TRUE"));
      assertNull(test.getObservedTableById("ZOMBIE"));
    }

    @Test
    void observeProbability_singleEvent_shouldReturnCorrectProb() {
      test.resetObservations();
      double pRainTrue = test.getPosteriorProbabilityById(List.of("RAIN:TRUE"));
      assertEquals(0.2, pRainTrue, 1E-9);
    }

    @Test
    void observeProbability_jointEvent_shouldReturnCorrectProb() {
      test.resetObservations();
      // P(RAIN:TRUE, SPRINKLER:FALSE) = P(S:F | R:T) * P(R:T)
      // P(R:T, S:F) = 0.99 * 0.2 = 0.198
      double pJoint = test.getPosteriorProbabilityById(List.of("RAIN:TRUE", "SPRINKLER:FALSE"));
      assertEquals(0.198, pJoint, 1E-9);
    }

    @Test
    void observeProbability_emptyEvent_shouldReturnOne() {
      double pEmpty = test.getPosteriorProbabilityById(List.of());
      assertEquals(1.0, pEmpty, 1E-9);
    }
  }

  @Nested
  class ScenarioTests {
    InferenceEngine test;

    @Test
    void testSolves_RainSprinkler() {
      BayesianNetwork net = RAIN_NETWORK.get().solveNetwork();
      test = net.buildInferenceEngine();
      if (PRINT_TABLES) net.printNetwork();
      test.setObservedById(List.of("WET_GRASS:TRUE"));
      if (PRINT_TABLES) test.printObserved();
      test.resetObservations();

      String testState = "RAIN:TRUE";
      String includedNode = "RAIN";

      generateSamples(test, includedNode, testState);

      test.setObservedById(List.of("WET_GRASS:TRUE"));
      System.out.println("\n--- Now testing P(RAIN:TRUE | WET_GRASS:TRUE) ---");
      generateSamples(test, includedNode, testState);
    }

    private void generateSamples(InferenceEngine engine, String includedNode, String testState) {

      double observedProb = engine.getPosteriorProbabilityById(List.of(testState));
      double expected = observedProb * NUMBER_OF_SAMPLES;
      double expectedDelta = Math.sqrt(NUMBER_OF_SAMPLES) * ALLOWED_STDEV;
      long lowerBound = Math.max(0, (long) (expected - expectedDelta));
      long upperBound = (long) (expected + expectedDelta);

      SampleCollection sampleCollection =
          MonteCarloSampler.create(engine.getNetwork())
              .setObserved(engine.getCurrentObservations())
              .generateSamples(NUMBER_OF_SAMPLES);
      sampleCollection.setDisplayedNodesById(List.of(includedNode));
      int count = sampleCollection.countSamplesIncludingStateIds(List.of(testState));

      System.out.printf(
          "Test State: %s%nExpected: %.2f (%.0f samples)%nAllowed Range: [%d, %d]%nActual Sample Count: %d%n",
          testState, observedProb, expected, lowerBound, upperBound, count);

      assertTrue(
          count >= lowerBound && count <= upperBound,
          String.format(
              "Sample count %d for %s is outside expected range [%d, %d]",
              count, testState, lowerBound, upperBound));
    }

    @Test
    void testNetworkAH_NonLocalConstraints() {
      BayesianNetwork net = AH_NETWORK.get();
      test = net.buildInferenceEngine();

      if (PRINT_TABLES) {
        net.printNetwork();
        test.printObserved();
      }

      String testState = "B+";
      String includedNode = "B";
      generateSamples(test, includedNode, testState);
    }

    @Test
    void testFantasyGraph_ComplexNetwork() {
      if (!SOLVE_LONG_TESTS) return;
      BayesianNetwork net = FANTASY_GRAPH.get();
      BayesSolverImpl solver = new BayesSolverImpl(net);
      solver.solve();
      Clique[] cliques = solver.getJta().getData().getCliques();
      System.out.println("SOLVER CLIQUES:");
      for (Clique clique : cliques) {
        System.out.println(NodeUtils.formatNodesToString(clique.getNodes()));
      }
      assertDoesNotThrow(() -> test = net.buildInferenceEngine());
      cliques = ((InferenceEngineImpl) test).getJunctionTree().getData().getCliques();
      System.out.println("INFERENCE CLIQUES:");
      for (Clique clique : cliques) {
        System.out.println(NodeUtils.formatNodesToString(clique.getNodes()));
      }

      if (PRINT_TABLES) test.printObserved();
      test.setObservedById(List.of("VOTE:CPK"));
      if (PRINT_TABLES) test.printObserved();
      test.setObservedById(List.of("VOTE:UNF"));
      if (PRINT_TABLES) test.printObserved();
      test.setObservedById(List.of("RACE:ANK", "AGE:YOUNG_ADULT"));
      if (PRINT_TABLES) {
        test.printObserved();
        net.printNetwork();
      }

      String testState = "VOTE:CPK";
      String includedNode = "VOTE";

      generateSamples(test, includedNode, testState);
    }
  }
}
