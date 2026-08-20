package io.github.alecredmond.export.method.probabilitytables;

import static io.github.alecredmond.TestConfigs.DOUBLE_EQUALITY;
import static io.github.alecredmond.TestConfigs.SOLVE_LONG_TESTS;
import static io.github.alecredmond.export.method.network.NetworkScenario.FANTASY_GRAPH;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.method.network.NetworkScenario;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.internal.method.node.NodeUtils;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.StateCombinationGenerator;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class TableQueryToolTest {

  public static Stream<Arguments> provideNormalizeTableArgs() {
    return provideArgs(
        (t, n) -> Stream.of(Arguments.of(t)), s -> s.get().buildNetworkData(), t -> true);
  }

  static Stream<Arguments> provideArgs(
      BiFunction<NetworkTable, BayesianNetwork, Stream<Arguments>> function,
      Function<NetworkScenario, BayesianNetwork> networkInit,
      Predicate<? super NetworkTable> tableFilter) {
    return getBayesianNetworkStream(networkInit)
        .flatMap(
            network ->
                network.getNetworkData().getNetworkTablesMap().values().stream()
                    .filter(tableFilter)
                    .map(t -> Optional.ofNullable(function.apply(t, network)))
                    .filter(Optional::isPresent)
                    .flatMap(Optional::get));
  }

  private static Stream<BayesianNetwork> getBayesianNetworkStream(
      Function<NetworkScenario, BayesianNetwork> networkInit) {
    return Arrays.stream(NetworkScenario.values())
        .filter(networkScenario -> !FANTASY_GRAPH.equals(networkScenario) || SOLVE_LONG_TESTS)
        .map(networkInit);
  }

  public static Stream<Arguments> provideOverloadedCptQueryNetworks() {
    return getBayesianNetworkStream(s -> s.get().solveNetwork()).map(Arguments::of);
  }

  public static Stream<Arguments> provideNetworkTables() {
    return provideTableOnly(NetworkTable.class::isInstance);
  }

  static Stream<Arguments> provideTableOnly(Predicate<? super NetworkTable> tableFilter) {
    return provideSolvedArgs((table, network) -> Stream.of(Arguments.of(table)), tableFilter);
  }

  static Stream<Arguments> provideSolvedArgs(
      BiFunction<NetworkTable, BayesianNetwork, Stream<Arguments>> function,
      Predicate<? super NetworkTable> tableFilter) {
    return provideArgs(function, scenario -> scenario.get().solveNetwork(), tableFilter);
  }

  public static Stream<Arguments> provideSafeModeArgs() {
    return provideTableOnly(ConditionalTable.class::isInstance);
  }

  public static Stream<Arguments> provideCopyTableArgs() {
    return provideTableOnly(t -> true);
  }

  public static Stream<Arguments> provideGetConditionProbArgs() {
    return provideSolvedArgs(TableQueryToolTest::allConditionsForTable, t -> true);
  }

  private static Stream<Arguments> allConditionsForTable(NetworkTable t, BayesianNetwork n) {
    List<ProbabilityConstraint> eventStateConstraints =
        n.getNetworkData().getConstraints().stream()
            .filter(c -> c.getEventNodes().equals(t.getEvents()))
            .filter(c -> c.getEventStates().size() == 1)
            .filter(c -> c.getConditionNodes().equals(t.getConditions()))
            .toList();

    if (eventStateConstraints.isEmpty()) return null;

    List<Collection<NodeState>> conditions =
        new StateCombinationGenerator(t.getVector())
            .generateCombos(t.getConditions(), ArrayList::new);

    if (conditions.isEmpty()) {
      Map<NodeState, ProbabilityConstraint> map = new HashMap<>();
      eventStateConstraints.forEach(
          c -> map.put(c.getEventStates().stream().findFirst().orElseThrow(), c));
      return Stream.of(Arguments.of(t, map, List.of()));
    }
    return conditions.stream()
        .map(
            conditionStates -> {
              Map<NodeState, ProbabilityConstraint> map = new HashMap<>();
              eventStateConstraints.stream()
                  .filter(c -> c.getConditionStates().equals(new HashSet<>(conditionStates)))
                  .forEach(
                      constraint ->
                          map.put(
                              constraint.getEventStates().stream().findFirst().orElseThrow(),
                              constraint));
              return map.isEmpty()
                  ? Optional.empty()
                  : Optional.of(Arguments.of(t, map, conditionStates));
            })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Arguments.class::cast);
  }

  static Stream<Arguments> provideGetProbArgs() {
    return provideSolvedArgs(TableQueryToolTest::buildGetProbArgs, t -> true);
  }

  static Stream<Arguments> buildGetProbArgs(NetworkTable table, BayesianNetwork network) {
    Set<Node> eventNodes = table.getEvents();
    Set<Node> conditionNodes = table.getConditions();
    return network.getNetworkData().getConstraints().stream()
        .filter(c -> c.getEventNodes().equals(eventNodes))
        .filter(c -> c.getEventStates().size() == 1)
        .filter(c -> c.getConditionNodes().equals(conditionNodes))
        .map(c -> Arguments.of(table, c));
  }

  @ParameterizedTest
  @MethodSource("provideGetProbArgs")
  void getProbability_shouldSucceed(NetworkTable table, ProbabilityConstraint constraint) {
    Set<NodeState> allStates = constraint.getAllStates();
    assertEquals(constraint.getProbability(), table.getProbability(allStates), DOUBLE_EQUALITY);
  }

  @ParameterizedTest
  @MethodSource("provideGetProbArgs")
  void getProbabilityFromIDs_shouldSucceed(NetworkTable table, ProbabilityConstraint constraint) {
    List<Serializable> stateIds = NodeUtils.getNodeStateIds(constraint.getAllStates());
    assertEquals(
        constraint.getProbability(), table.getProbabilityFromIDs(stateIds), DOUBLE_EQUALITY);
  }

  @ParameterizedTest
  @MethodSource("provideCopyTableArgs")
  void copyTable(NetworkTable networkTable) {
    NetworkTable copied = networkTable.copyTable();
    assertNotSame(copied, networkTable);
    assertEquals(copied, networkTable);
  }

  @ParameterizedTest
  @MethodSource("provideNormalizeTableArgs")
  void normalizeTable(NetworkTable networkTable) {
    double[] probs = networkTable.getProbabilities();
    Arrays.fill(probs, 1.0);
    networkTable.normalizeTable();
    int numOfConditionCombos =
        networkTable.getConditions().stream()
            .mapToInt(c -> c.getNodeStates().size())
            .reduce((x, y) -> x * y)
            .orElse(1);
    assertEquals(numOfConditionCombos, Arrays.stream(probs).sum(), DOUBLE_EQUALITY);
  }

  @ParameterizedTest
  @MethodSource("provideGetConditionProbArgs")
  void getConditionalProb(
      NetworkTable networkTable,
      Map<NodeState, ProbabilityConstraint> constraintMap,
      Collection<NodeState> conditionStates) {
    Map<NodeState, Double> probs = networkTable.getConditionalProb(conditionStates);
    constraintMap.forEach(
        (event, constraint) ->
            assertEquals(constraint.getProbability(), probs.get(event), DOUBLE_EQUALITY));
  }

  @ParameterizedTest
  @MethodSource("provideGetConditionProbArgs")
  void getConditionalProbByIds(
      NetworkTable networkTable,
      Map<NodeState, ProbabilityConstraint> constraintMap,
      Collection<NodeState> conditionStates) {
    Map<NodeState, Double> probs =
        networkTable.getConditionalProbByIds(NodeUtils.getNodeStateIds(conditionStates));
    constraintMap.forEach(
        (event, constraint) ->
            assertEquals(constraint.getProbability(), probs.get(event), DOUBLE_EQUALITY));
  }

  @ParameterizedTest
  @MethodSource("provideSafeModeArgs")
  void setSafeMode(NetworkTable networkTable) {
    Node networkNode = networkTable.getNetworkNode();
    double[] probs = networkTable.getProbabilities();
    List<NodeState> eventStates = networkNode.getNodeStates();
    eventStates.forEach(
        state -> {
          assertTrue(networkTable.getConditionalProb(List.of(state)).isEmpty());
          assertNull(networkTable.getProbability(List.of(state)));
        });
    networkTable.setSafeMode(false);
    IntStream.range(0, eventStates.size())
        .forEach(
            i -> {
              NodeState state = eventStates.get(i);
              assertEquals(probs[i], networkTable.getProbability(List.of(state)), DOUBLE_EQUALITY);
            });
  }

  @ParameterizedTest
  @MethodSource("provideNetworkTables")
  void testListCptEntries(NetworkTable networkTable) {
    double[] probabilities = networkTable.getProbabilities();
    List<CptEntry> entries = networkTable.getCptEntries();
    for (int i = 0; i < probabilities.length; i++) {
      CptEntry entry = entries.get(i);
      Set<NodeState> allStates = new HashSet<>(entry.conditionStates());
      allStates.add(entry.eventState());
      assertEquals(entry.probability(), probabilities[i], DOUBLE_EQUALITY);
      assertEquals(entry.probability(), networkTable.getProbability(allStates), DOUBLE_EQUALITY);
    }
  }

  @ParameterizedTest
  @MethodSource("provideNetworkTables")
  void testIterateOverConditions(NetworkTable networkTable) {
    networkTable.iterateOverConditions(
        cptRow -> {
          Set<NodeState> conditions = cptRow.conditionStates();
          Map<NodeState, Double> conditionalProbMap = networkTable.getConditionalProb(conditions);
          cptRow
              .rowEntries()
              .forEach(
                  cptEntry -> {
                    NodeState eventState = cptEntry.eventState();
                    double probability = cptEntry.probability();
                    assertEquals(conditionalProbMap.get(eventState), probability, DOUBLE_EQUALITY);
                  });
        });
  }

  @ParameterizedTest
  @MethodSource("provideOverloadedCptQueryNetworks")
  void testOverloadedCptQueries(BayesianNetwork network) {
    List<Node> nodes = network.getNetworkData().getNodes();
    ProbabilityVector vector = new ProbabilityVectorFactory().build(nodes);
    StateCombinationGenerator generator = new StateCombinationGenerator(vector);

    List<Set<NodeState>> allStates =
        generator.generateCombos(new LinkedHashSet<>(nodes), LinkedHashSet::new);
    List<NetworkTable> networkTables = network.getNetworkTables().values().stream().toList();

    for (Set<NodeState> stateSet : allStates) {
      Map<Node, NodeState> request = NodeUtils.generateRequest(stateSet);
      for (NetworkTable networkTable : networkTables) {
        List<NodeState> allInTable = limitToOverlapOnly(request, networkTable.getNodes());
        List<NodeState> conditionsOnly = limitToOverlapOnly(request, networkTable.getConditions());

        networkTable.setSafeMode(false);
        double probabilityA = networkTable.getProbability(stateSet);
        double probabilityB = networkTable.getProbability(allInTable);
        assertEquals(probabilityA, probabilityB, DOUBLE_EQUALITY);
        assertNotNull(networkTable.getProbability(conditionsOnly));

        networkTable.setSafeMode(true);
        double probabilityC = networkTable.getProbability(stateSet);
        assertEquals(probabilityA, probabilityC, DOUBLE_EQUALITY);
        assertNull(networkTable.getProbability(conditionsOnly));
      }
    }
  }

  private List<NodeState> limitToOverlapOnly(Map<Node, NodeState> request, Collection<Node> nodes) {
    return nodes.stream().map(request::get).toList();
  }
}
