package io.github.alecredmond.export.inference.direct;

import static io.github.alecredmond.TestConfigs.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.Clique;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.networkscenario.scenarios.PengAndDingDiamond;
import io.github.alecredmond.networkscenario.scenarios.RainNetwork;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Slf4j
class InferenceEngineTest {

  @Nested
  class RainNetworkTests {
    static final double[] EXPECTED_POTENTIALS = {
      0.001980, //  [RAIN:TRUE,  SPRINKLER:TRUE,  WET_GRASS:TRUE]
      0.000020, //  [RAIN:TRUE,  SPRINKLER:TRUE,  WET_GRASS:FALSE]
      0.178200, //  [RAIN:TRUE,  SPRINKLER:FALSE, WET_GRASS:TRUE]
      0.019800, //  [RAIN:TRUE,  SPRINKLER:FALSE, WET_GRASS:FALSE]
      0.288000, //  [RAIN:FALSE, SPRINKLER:TRUE,  WET_GRASS:TRUE]
      0.032000, //  [RAIN:FALSE, SPRINKLER:TRUE,  WET_GRASS:FALSE]
      0.000000, //  [RAIN:FALSE, SPRINKLER:FALSE, WET_GRASS:TRUE]
      0.480000 //   [RAIN:FALSE, SPRINKLER:FALSE, WET_GRASS:FALSE]
    };
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
    void observeNetwork_wetGrassTrue_shouldUpdateProbabilities() {
      // P(W) =   0.468180
      // P(R,W) = 0.180180
      // P(R|W) = P(R,W) / P(W), BY BAYES' THEOREM
      // P(R|W) = 0.384852

      // BY STATE
      BinaryNode rain = rainNetwork.rain;
      BinaryNode wetGrass = rainNetwork.wetGrass;
      ObservedTable rainTable = test.getObservedTable(rain);
      double pRainPrior = rainTable.getProbability(rain.trueState());
      assertEquals(0.2, pRainPrior, DOUBLE_EQUALITY);
      assertNotNull(test.setObserved(wetGrass.trueState()));
      double pRainGivenWet = rainTable.getProbability(rain.trueState());
      assertEquals(0.384852, pRainGivenWet, DOUBLE_EQUALITY);

      // BY ID
      assertNotNull(test.setObserved());
      pRainPrior = rainTable.getProbability(rain.trueState());
      assertEquals(0.2, pRainPrior, DOUBLE_EQUALITY);
      assertNotNull(test.setObservedById(wetGrass.trueState().getId()));
      pRainGivenWet = rainTable.getProbability(rain.trueState());
      assertEquals(0.384852, pRainGivenWet, DOUBLE_EQUALITY);
    }

    @Test
    void eliminateStates_wetGrassFalse_shouldUpdateProbabilities() {
      // BY STATE
      BinaryNode rain = rainNetwork.rain;
      BinaryNode wetGrass = rainNetwork.wetGrass;
      ObservedTable rainTable = test.getObservedTable(rain);
      double pRainPrior = rainTable.getProbability(rain.trueState());
      assertEquals(0.2, pRainPrior, DOUBLE_EQUALITY);
      NodeState wetGrassFalse = wetGrass.getStateById(wetGrass.falseState().getId());
      assertNotNull(test.eliminateStates(wetGrassFalse));
      double pRainGivenWet = rainTable.getProbability(rain.trueState());
      assertEquals(0.384852, pRainGivenWet, DOUBLE_EQUALITY);

      // BY ID
      assertNotNull(test.resetObservations());
      pRainPrior = rainTable.getProbability(rain.trueState());
      assertEquals(0.2, pRainPrior, DOUBLE_EQUALITY);
      assertNotNull(test.eliminateStatesById(wetGrass.falseState().getId()));
      pRainGivenWet = rainTable.getProbability(rain.trueState());
      assertEquals(0.384852, pRainGivenWet, DOUBLE_EQUALITY);
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
    void getPosteriorProbability_singleEvent_shouldReturnCorrectProb() {
      NodeState rainTrue = rainNetwork.rain.trueState();
      test.resetObservations();
      double pRainTrue = test.getPosteriorProbability(rainTrue);
      assertEquals(0.2, pRainTrue, DOUBLE_EQUALITY);
    }

    @Test
    void getPosteriorProbability_jointEvent_shouldReturnCorrectProb() {
      NodeState rainTrue = rainNetwork.rain.trueState();
      NodeState sprinklerFalse = rainNetwork.sprinkler.falseState();
      test.resetObservations();
      // P(R) = 0.2
      // P(!S|R) = 0.99
      // P(R,!S) = P(R) * P(!S|R) = 0.198
      double pJoint = test.getPosteriorProbability(rainTrue, sprinklerFalse);
      assertEquals(0.198, pJoint, DOUBLE_EQUALITY);
    }

    @Test
    void getPosteriorProbability_emptyEvent_shouldReturnOne() {
      double pEmpty = test.getPosteriorProbability();
      assertEquals(1.0, pEmpty, DOUBLE_EQUALITY);
    }

    @Test
    void getPosteriorProbabilities_shouldFollowBayesRule() {

      BinaryNode rain = rainNetwork.rain;
      BinaryNode sprinkler = rainNetwork.sprinkler;
      BinaryNode wetGrass = rainNetwork.wetGrass;

      NodeState rainFalse = rain.falseState();
      NodeState sprinklerTrue = sprinkler.getState(true);
      NodeState wetGrassTrue = wetGrass.trueState();

      // P(!R,S) =   0.320
      assertEquals(0.32, test.getPosteriorProbability(rainFalse, sprinklerTrue), DOUBLE_EQUALITY);

      // P(!R,S,W) = 0.288
      assertEquals(
          0.288,
          test.getPosteriorProbability(rainFalse, sprinklerTrue, wetGrassTrue),
          DOUBLE_EQUALITY);

      // P(W|!R,S) = 0.900 // FROM CPT
      test.setObserved(rainFalse, sprinklerTrue);
      assertEquals(0.9, test.getPosteriorProbability(wetGrassTrue), DOUBLE_EQUALITY);

      // P(S,W) = 0.28998
      // P(!R|S,W) = P(!R,S,W) / P(S,W) = 0.993172
      test.setObserved(sprinklerTrue, wetGrassTrue);
      assertEquals(0.993172, test.getPosteriorProbability(rainFalse), DOUBLE_EQUALITY);

      // P(S,W|!R) = 0.288 / 0.8 = 0.36
      test.setObserved(rainFalse);
      assertEquals(
          0.36, test.getPosteriorProbability(sprinklerTrue, wetGrassTrue), DOUBLE_EQUALITY);
    }

    @Test
    void getCliques_expectOnlyOne_MatchingJointTable() {
      assertEquals(1, test.getCliques().length);
      assertEquals(0, test.getSeparators().length);
      Clique clique = test.getSmallestLeafClique();
      assertNotNull(clique);
      double[] actualPotentials = clique.getProbabilities();
      for (int i = 0; i < EXPECTED_POTENTIALS.length; i++) {
        assertEquals(EXPECTED_POTENTIALS[i], actualPotentials[i], DOUBLE_EQUALITY);
      }

      NodeState rF = rainNetwork.rain.falseState();
      NodeState sT = rainNetwork.sprinkler.trueState();
      NodeState wT = rainNetwork.wetGrass.trueState();
      assertEquals(0.8, clique.sumPotentials(rF), DOUBLE_EQUALITY);
      assertEquals(0.32, clique.sumPotentials(rF, sT), DOUBLE_EQUALITY);
      assertEquals(0.288, clique.sumPotentials(rF, sT, wT), DOUBLE_EQUALITY);
    }

    @Test
    void copyObservedTable_shouldBeIndependentOfEngine() {
      BinaryNode rain = rainNetwork.rain;
      BinaryNode wetGrass = rainNetwork.wetGrass;

      ObservedTable wetGrassTable = test.getObservedTableById(wetGrass.getId());
      ObservedTable wetGrassTableCopied = test.copyObservedTable(wetGrass);
      double[] p = wetGrassTable.getProbabilities();
      double[] pCopied = wetGrassTableCopied.getProbabilities();
      for (int i = 0; i < p.length; i++) {
        assertEquals(p[i], pCopied[i], DOUBLE_EQUALITY);
      }
      test.setObserved(rain.trueState());
      for (int i = 0; i < p.length; i++) {
        assertNotEquals(p[i], pCopied[i], DOUBLE_EQUALITY);
      }
      wetGrassTableCopied = test.copyObservedTableById(wetGrass.getId());
      pCopied = wetGrassTableCopied.getProbabilities();
      for (int i = 0; i < p.length; i++) {
        assertEquals(p[i], pCopied[i], DOUBLE_EQUALITY);
      }
    }

    @Test
    void printObserved_shouldPrintCorrectTables() {
      if (!PRINT_TABLES) return;
      BinaryNode rain = rainNetwork.rain;
      BinaryNode sprinkler = rainNetwork.sprinkler;
      BinaryNode wetGrass = rainNetwork.wetGrass;

      assertDoesNotThrow(() -> test.printObserved(rain));
      assertDoesNotThrow(() -> test.printObservedById(sprinkler.getId(), wetGrass.getId()));
    }
  }

  @Nested
  class PengDingDiamondTests {
    static final double[] EXPECTED_POTENTIALS =
        new double[] {
          //           ABCD
          0.014400, // 1111
          0.129600, // 1110
          0.081600, // 1101
          0.014400, // 1100
          0.043200, // 1011
          0.052800, // 1010
          0.044800, // 1001
          0.019200, // 1000
          0.007350, // 0111
          0.066150, // 0110
          0.116025, // 0101
          0.020475, // 0100
          0.061425, // 0011
          0.075075, // 0010
          0.177450, // 0001
          0.076050 //  0000
        };
    static PengAndDingDiamond scenario;
    static BayesianNetwork network;
    InferenceEngine testJTA;
    InferenceEngine testSTA;

    @BeforeAll
    static void staticInit() {
      scenario = new PengAndDingDiamond();
      network = scenario.buildNetwork();
    }

    @BeforeEach
    void initInference() {
      testJTA = new JunctionTreeAlgorithm(network);
      testSTA = new SingleTableAlgorithm(network);
    }

    @Test
    void testPotentialsMatch_sta() {
      assertEquals(1, testSTA.getCliques().length);
      assertEquals(0, testSTA.getSeparators().length);
      double[] actualPotentials = testSTA.getSmallestLeafClique().getProbabilities();
      final int expectedLen = EXPECTED_POTENTIALS.length;
      assertEquals(expectedLen, actualPotentials.length);
      for (int i = 0; i < expectedLen; i++) {
        assertEquals(EXPECTED_POTENTIALS[i], actualPotentials[i], DOUBLE_EQUALITY);
      }
    }

    @Test
    void testCliquesJTA_shouldBeTwo_ABC_BCD() {
      Clique[] cliques = testJTA.getCliques();
      assertEquals(2, cliques.length);
      assertEquals(1, testJTA.getSeparators().length);
      Set<Set<Node>> checkContains = new HashSet<>();
      checkContains.add(Set.of(scenario.a, scenario.b, scenario.c));
      checkContains.add(Set.of(scenario.b, scenario.c, scenario.d));
      for (Clique clique : cliques) {
        assertTrue(checkContains.remove(clique.getNodes()));
      }
    }

    @Test
    void testMarginals_shouldMatchExpected() {
      NodeState[] trueStates =
          new NodeState[] {
            scenario.a.trueState(),
            scenario.b.trueState(),
            scenario.c.trueState(),
            scenario.d.trueState()
          };

      double[] expectedMarginals = new double[] {0.4, 0.45, 0.45, 0.54625};

      for (int i = 0; i < trueStates.length; i++) {
        NodeState state = trueStates[i];
        double marginal = expectedMarginals[i];
        applyToBoth(
            (eng, s) -> assertEquals(marginal, eng.getPosteriorProbability(s), DOUBLE_EQUALITY),
            state);
      }
    }

    void applyToBoth(BiConsumer<InferenceEngine, NodeState[]> biConsumer, NodeState... states) {
      biConsumer.accept(testJTA, states);
      biConsumer.accept(testSTA, states);
    }
  }
}
