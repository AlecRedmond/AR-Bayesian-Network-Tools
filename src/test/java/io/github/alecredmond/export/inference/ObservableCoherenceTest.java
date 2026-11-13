package io.github.alecredmond.export.inference;

import static io.github.alecredmond.TestConfigs.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.direct.JunctionTreeAlgorithm;
import io.github.alecredmond.export.inference.indirect.LikelihoodWeightingSampler;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.inference.indirect.SampleCollection;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class ObservableCoherenceTest {
  private static final boolean PERFORM_SAMPLE_CHECK = false;
  private static final double SAMPLE_BAYES_RULE_CUTOFF_PROBABILITY = 1e-3;
  private static final int TABLE_MAX_ENTRIES_FOR_SAMPLING = 10;
  private static final double SAMPLE_DELTA = 2e-2;

  static Stream<Arguments> supplyCoherenceArguments() {
    return Arrays.stream(NetworkScenarioType.values())
        .map(NetworkScenarioType::get)
        .map(NetworkScenario::buildNetwork)
        .map(ObservableCoherenceTest::createArguments);
  }

  private static Arguments createArguments(BayesianNetwork network) {
    InferenceEngine engine = new JunctionTreeAlgorithm(network);
    MonteCarloSampler sampler = new LikelihoodWeightingSampler(network);
    return Arguments.of(network, engine, sampler);
  }

  @ParameterizedTest
  @MethodSource("supplyCoherenceArguments")
  void testCoherence(BayesianNetwork network, InferenceEngine engine, MonteCarloSampler sampler) {
    NetworkTable[] tables = network.getNetworkTables();
    final int bound = tables.length;
    for (int i = 0; i < bound; i++) {
      NetworkTable table = tables[i];
      List<CptEntry> cptEntries = table.getCptEntries();
      boolean processSampler = cptEntries.size() <= TABLE_MAX_ENTRIES_FOR_SAMPLING;
      log.info("TABLE {}/{}, {} entries to process", i + 1, bound, cptEntries.size());
      for (CptEntry cptEntry : cptEntries) {
        assertBayesRuleInferenceEngine(engine, cptEntry);
        if (PERFORM_SAMPLE_CHECK && processSampler) {
          assertBayesRuleSampler(sampler, cptEntry);
        }
      }
      log.info("PROCESSED TABLE {}/{}", i + 1, bound);
    }
  }

  private static void assertBayesRuleInferenceEngine(InferenceEngine engine, CptEntry cptEntry) {
    NodeState event = cptEntry.eventState();
    Set<NodeState> conditions = cptEntry.conditionStates();
    engine.resetObservations();
    double marginalE = engine.getPosteriorProbability(event);
    double marginalC = engine.getPosteriorProbability(conditions);
    double eGivenCExpected = cptEntry.probability();
    engine.setObserved(conditions);
    double eGivenCActual = engine.getPosteriorProbability(event);
    assertEquals(eGivenCExpected, eGivenCActual, DOUBLE_EQUALITY);
    double cGivenEExpected = (eGivenCExpected * marginalC) / marginalE;
    engine.setObserved(event);
    double cGivenEActual = engine.getPosteriorProbability(conditions);
    assertEquals(cGivenEExpected, cGivenEActual, DOUBLE_EQUALITY);
  }

  private static void assertBayesRuleSampler(MonteCarloSampler sampler, CptEntry cptEntry) {
    NodeState event = cptEntry.eventState();
    Set<NodeState> conditions = cptEntry.conditionStates();
    sampler.resetObservations();
    SampleCollection scMarginal = sampler.generateSamples(NUMBER_OF_SAMPLES);
    double marginalE = (double) scMarginal.countSamplesIncludingStates(event) / NUMBER_OF_SAMPLES;
    double marginalC =
        (double) scMarginal.countSamplesIncludingStates(conditions) / NUMBER_OF_SAMPLES;
    double eGivenCExpected = cptEntry.probability();
    sampler.setObserved(conditions);
    SampleCollection scObservedC = sampler.generateSamples(NUMBER_OF_SAMPLES);
    double eGivenCActual =
        (double) scObservedC.countSamplesIncludingStates(event) / NUMBER_OF_SAMPLES;

    if (eGivenCExpected < SAMPLE_BAYES_RULE_CUTOFF_PROBABILITY) return;

    assertEquals(eGivenCExpected, eGivenCActual, SAMPLE_DELTA, cptEntry.toString());
    double cGivenEExpected = (eGivenCExpected * marginalC) / marginalE;
    if (cGivenEExpected > 1) cGivenEExpected = 1.0;
    sampler.setObserved(event);
    SampleCollection scObservedE = sampler.generateSamples(NUMBER_OF_SAMPLES);
    double cGivenEActual =
        (double) scObservedE.countSamplesIncludingStates(conditions) / NUMBER_OF_SAMPLES;
    assertEquals(
        cGivenEExpected,
        cGivenEActual,
        SAMPLE_DELTA,
        cptEntry
            + "\n"
            + "P(E|C) = %.6f; P(C) = %.6f; P(E) = %.6f"
                .formatted(eGivenCExpected, marginalC, marginalE));
  }
}
