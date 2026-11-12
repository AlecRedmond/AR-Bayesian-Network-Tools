package io.github.alecredmond.export.sampler;

import static io.github.alecredmond.TestConfigs.*;
import static io.github.alecredmond.export.node.BinaryNode.StateOrder.TRUE_FIRST;
import static io.github.alecredmond.networkscenario.NetworkScenarioType.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.export.inference.JunctionTreeAlgorithm;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.export.node.DefaultBinaryNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.observable.NodeObservation;
import io.github.alecredmond.export.observable.ObservationStatus;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SampleCollectionTest {
  static final Function<BayesianNetwork, InferenceEngine> INFERENCE = JunctionTreeAlgorithm::new;
  static final Predicate<? super NetworkScenarioType> PACKAGE_FILTER = Objects::nonNull;
  static List<SamplePackage> packages;

  static Stream<Arguments> provideSamplePackages() {
    return packages.stream().map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    packages =
        Arrays.stream(values())
            .filter(PACKAGE_FILTER)
            .map(NetworkScenarioType::get)
            .flatMap(SampleCollectionTest::buildPackages)
            .toList();
  }

  private static Stream<SamplePackage> buildPackages(NetworkScenario scenario) {
    BayesianNetwork network = scenario.buildNetwork();
    return scenario.buildObservationPackages().stream()
        .map(p -> new SamplePackage(network, NUMBER_OF_SAMPLES, p, false, INFERENCE));
  }

  @Nested
  class Utils {
    public static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getObservations(SamplePackage samplePackage) {
      Map<Node, Set<NodeState>> declaredEvidence =
          NodeUtils.generateMultiRequest(samplePackage.getObservedStates());

      Map<Node, NodeObservation> observations = samplePackage.getTest().getObservations();

      declaredEvidence.forEach(
          (node, evidence) -> {
            assertTrue(observations.containsKey(node));
            NodeObservation observation = observations.get(node);
            ObservationStatus status = observation.status();
            assertNotEquals(ObservationStatus.UNOBSERVED, status);
            assertNotEquals(ObservationStatus.NEGATED, status);
            assertEquals(evidence, observation.observedStates());
            if (status.equals(ObservationStatus.OBSERVED)) {
              assertEquals(1, evidence.size());
            } else {
              assertTrue(evidence.size() > 1);
            }
          });
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getNodes(SamplePackage samplePackage) {
      BayesianNetwork network = samplePackage.getNetwork();
      SampleCollection test = samplePackage.getTest();
      Node[] nodes = network.getNodes();
      Node[] testNodes = test.getNodes();
      IntStream.range(0, nodes.length).forEach(i -> assertEquals(nodes[i], testNodes[i]));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void isEmpty(SamplePackage samplePackage) {
      SampleCollection test = samplePackage.getTest();
      assertFalse(test.isEmpty());
    }
  }

  @Nested
  class CountSampleTests {

    static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStateIds_multipleIDs(SamplePackage samplePackage) {
      Set<Serializable> measuredStateIds =
          samplePackage.getMeasuredStates().stream()
              .map(NodeState::getId)
              .collect(Collectors.toSet());
      posteriorCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStateIds(measuredStateIds),
          e -> e.getPosteriorProbabilityById(measuredStateIds));
    }

    void posteriorCount(
        SamplePackage samplePackage,
        ToIntFunction<SampleCollection> countFunc,
        ToDoubleFunction<InferenceEngine> probabilityFunc) {
      measureCount(
          samplePackage.getNumberOfSamples(),
          samplePackage.getEngine(),
          samplePackage.getTest(),
          countFunc,
          probabilityFunc);
    }

    void measureCount(
        int numberOfSamples,
        InferenceEngine inferenceEngine,
        SampleCollection sampleCollection,
        ToIntFunction<SampleCollection> countFunc,
        ToDoubleFunction<InferenceEngine> probabilityFunc) {
      double probOfObserved = probabilityFunc.applyAsDouble(inferenceEngine);
      double delta = Math.sqrt(numberOfSamples) * STANDARD_DEVIATIONS;
      double expected = probOfObserved * numberOfSamples;
      int counted = countFunc.applyAsInt(sampleCollection);
      assertEquals(expected, counted, delta);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStates_multipleStates(SamplePackage samplePackage) {
      Set<NodeState> measuredStates = samplePackage.getMeasuredStates();
      posteriorCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStates(measuredStates),
          e -> e.getPosteriorProbability(measuredStates));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStates_singleState(SamplePackage samplePackage) {
      if (samplePackage.isPrintMarginals()) {
        samplePackage.getEngine().printObserved();
        samplePackage.getNetwork().printNetwork();
      }
      NodeState measuredState =
          samplePackage.getMeasuredStates().stream().findFirst().orElseThrow();
      posteriorCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStates(measuredState),
          e -> e.getPosteriorProbability(measuredState));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStates_noStates(SamplePackage samplePackage) {
      priorCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStates(samplePackage.getObservedStates()),
          e -> e.getPosteriorProbability(samplePackage.getObservedStates()));
    }

    void priorCount(
        SamplePackage samplePackage,
        ToIntFunction<SampleCollection> countFunc,
        ToDoubleFunction<InferenceEngine> probabilityFunc) {
      measureCount(
          samplePackage.getNumberOfSamples(),
          samplePackage.getPriorEngine(),
          samplePackage.getJointTest(),
          countFunc,
          probabilityFunc);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamplesIncludingStateIds_singleID(SamplePackage samplePackage) {
      Serializable singleId =
          samplePackage.getMeasuredStates().stream()
              .findFirst()
              .map(NodeState::getId)
              .orElseThrow();
      posteriorCount(
          samplePackage,
          sc -> sc.countSamplesIncludingStateIds(singleId),
          e -> e.getPosteriorProbabilityById(singleId));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countSamples(SamplePackage samplePackage) {
      int numberOfSamples = samplePackage.getNumberOfSamples();
      SampleCollection test = samplePackage.getTest();
      assertEquals(numberOfSamples, test.countSamples());
    }

    @Test
    void testZeroSamples() {
      BinaryNode x = new DefaultBinaryNode("X", TRUE_FIRST);
      BinaryNode y = new DefaultBinaryNode("Y", List.of(x), TRUE_FIRST);
      BayesianNetwork net =
          BayesianNetwork.builder()
              .setNodes(x, y)
              .addConstraint(x.trueState(), 0.25)
              .addConstraint(y.trueState(), x.falseState(), 0.0)
              .build(new IPFPJunctionTreeSolver())
              .orElseThrow();
      SampleCollection collection =
          MonteCarloSampler.create(net)
              .setObserved(x.falseState(), y.trueState())
              .generateSamples(100);
      assertEquals(0, collection.countSamples());
      assertTrue(collection.getSamples().isEmpty());
    }
  }

  @Nested
  class GetSamples {
    static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStates_multipleStates(SamplePackage samplePackage) {
      Set<NodeState> measuredStates = samplePackage.getMeasuredStates();
      checkSamplesInRange(
          samplePackage,
          sc -> sc.getSamplesIncludingStates(measuredStates),
          set -> set.containsAll(measuredStates));
    }

    void checkSamplesInRange(
        SamplePackage samplePackage,
        Function<SampleCollection, List<Sample>> getSamples,
        Predicate<Set<NodeState>> assertion) {
      getSamples
          .apply(samplePackage.getTest())
          .forEach(sample -> assertTrue(assertion.test(sample.getDisplayedStates(HashSet::new))));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStates_singleState(SamplePackage samplePackage) {
      NodeState state = samplePackage.getMeasuredStates().stream().findFirst().orElseThrow();
      checkSamplesInRange(
          samplePackage, sc -> sc.getSamplesIncludingStates(state), set -> set.contains(state));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStateByIds_multipleIds(SamplePackage samplePackage) {
      Set<NodeState> measuredStates = samplePackage.getMeasuredStates();
      Set<Serializable> measuredStateIds =
          samplePackage.getMeasuredStates().stream()
              .map(NodeState::getId)
              .collect(Collectors.toSet());
      checkSamplesInRange(
          samplePackage,
          sc -> sc.getSamplesIncludingStatesById(measuredStateIds),
          set -> set.containsAll(measuredStates));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getSamplesIncludingStateByIds_singleState(SamplePackage samplePackage) {
      NodeState state = samplePackage.getMeasuredStates().stream().findFirst().orElseThrow();
      Serializable id = state.getId();
      checkSamplesInRange(
          samplePackage, sc -> sc.getSamplesIncludingStatesById(id), set -> set.contains(state));
    }
  }

  @Nested
  class SetDisplayedNodes {
    public static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodes_multipleNodes(SamplePackage samplePackage) {
      Set<Node> displayed = samplePackage.getExportNodes();
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodes(displayed), displayed);
    }

    void assertDisplayedOnly(
        SamplePackage samplePackage,
        Consumer<SampleCollection> setDisplayFunc,
        Set<Node> expectedDisplay) {
      SampleCollection test = samplePackage.getTest();
      setDisplayFunc.accept(test);
      test.getSamples()
          .forEach(
              sample ->
                  assertEquals(
                      NodeUtils.getNodes(sample.getDisplayedStates(HashSet::new)),
                      expectedDisplay));
      test.displayAllNodes();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void display_all_nodes(SamplePackage samplePackage) {
      Set<Node> displayed = samplePackage.getNetwork().getNodes(LinkedHashSet::new);
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodes(displayed), displayed);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodesById_multipleNodeIds(SamplePackage samplePackage) {
      Set<Node> displayed = samplePackage.getExportNodes();
      List<Serializable> ids = NodeUtils.getNodeIds(displayed);
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodesById(ids), displayed);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodes_singleNode(SamplePackage samplePackage) {
      Node displayed = samplePackage.getExportNodes().stream().findFirst().orElseThrow();
      assertDisplayedOnly(samplePackage, s -> s.setDisplayedNodes(displayed), Set.of(displayed));
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void setDisplayedNodesById_singleNodeId(SamplePackage samplePackage) {
      Node displayed = samplePackage.getExportNodes().stream().findFirst().orElseThrow();
      assertDisplayedOnly(
          samplePackage, s -> s.setDisplayedNodesById(displayed.getId()), Set.of(displayed));
    }
  }

  @Nested
  class SampleMethods {
    static Stream<Arguments> provideSamplePackages() {
      return SampleCollectionTest.provideSamplePackages();
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void countAll(SamplePackage samplePackage) {
      SampleCollection test = samplePackage.getTest();
      int countExpected = test.countSamples();
      int countActual = Sample.countAll(test.getSamples());
      assertEquals(countExpected, countActual);
    }

    @ParameterizedTest
    @MethodSource("provideSamplePackages")
    void getDisplayedStates(SamplePackage samplePackage) {
      SampleCollection test = samplePackage.getTest();
      Set<Node> exportNodes = samplePackage.getExportNodes();
      test.setDisplayedNodes(exportNodes);
      Set<NodeState> observedStates = samplePackage.getObservedStates();
      Map<Node, Set<NodeState>> multi = NodeUtils.generateMultiRequest(observedStates);
      test.getSamples()
          .forEach(
              sample -> {
                Map<Node, NodeState> sampled =
                    NodeUtils.generateRequest(Arrays.asList(sample.getAllStates()));

                assertTrue(
                    Arrays.stream(sample.getDisplayedStates())
                        .map(NodeState::getNode)
                        .allMatch(exportNodes::contains));

                multi.forEach((node, evidence) -> assertTrue(evidence.contains(sampled.get(node))));
              });
    }
  }
}
