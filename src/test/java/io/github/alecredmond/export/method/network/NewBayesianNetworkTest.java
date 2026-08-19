package io.github.alecredmond.export.method.network;

import static io.github.alecredmond.internal.method.constraints.ConstraintType.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.constraints.*;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.inference.InferenceEngine;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.ConstraintType;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;
import io.github.alecredmond.internal.method.network.changehandlers.CollectionChangeAnalyzer;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NewBayesianNetworkTest {
  static final List<Serializable> NODE_IDS = List.of("A", "B", "C", "D");
  BayesianNetwork test;

  static List<Serializable> listWithNull(List<Serializable> list) {
    List<Serializable> s = new ArrayList<>();
    s.add(null);
    s.addAll(list);
    return s;
  }

  @BeforeEach
  void setUp() {
    test = BayesianNetwork.newNetwork("TestNetwork");
    NODE_IDS.forEach(id -> test.addNewNode(id, List.of(id + "+", id + "-", id + "x")));
  }

  @AfterEach
  void assertNodeAndStateIdsCorrect() {
    BayesianNetworkData data = test.getNetworkData();
    assertMapCorrect(data.getNodeIDsMap(), Node::getId);
    assertMapCorrect(data.getNodeStateIDsMap(), NodeState::getId);
  }

  private <T> void assertMapCorrect(Map<Serializable, T> idMap, Function<T, Serializable> getId) {
    idMap.forEach((id, t) -> assertEquals(id, getId.apply(t)));
  }

  @Nested
  class NodeCreationTests {

    static Stream<Arguments> nodeInputArgs_successExpected() {
      return Stream.of(
          Arguments.of("E", List.of("E+", "E-")),
          Arguments.of("F", List.of("F+", "F-")),
          Arguments.of("G", List.of("G+", "G-")),
          Arguments.of("H", List.of()),
          Arguments.of('A', List.of('B', 'C')), // <- char 'A' != string "A"
          Arguments.of(-1, List.of(2.0, (long) 3)),
          Arguments.of('E', List.of("Q", 7)),
          Arguments.of(new SerializableClass(), List.of("HELLO", "WORLD")));
    }

    static Stream<Arguments> nodeInputArgs_allErrors() {
      return Stream.concat(nodeInputArgs_nodeIdErrors(), nodeInputArgs_stateIdErrors());
    }

    static Stream<Arguments> nodeInputArgs_nodeIdErrors() {
      return Stream.of(
          Arguments.of("A", List.of("A+", "A-"), BayesNetIDException.class),
          Arguments.of("B", List.of("G+", "G-"), BayesNetIDException.class),
          Arguments.of(null, List.of("G+", "G-"), NullPointerException.class));
    }

    static Stream<Arguments> nodeInputArgs_stateIdErrors() {
      return Stream.of(
          Arguments.of("F", List.of("A+", "F-"), BayesNetIDException.class),
          Arguments.of("G", listWithNull(List.of("G+", "G-")), NullPointerException.class),
          Arguments.of("H", List.of("H+", "H+", "H-"), BayesNetIDException.class));
    }

    @AfterEach
    void assertNetworkUnsolved() {
      assertFalse(test.getNetworkData().isSolved());
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_successExpected")
    void addNode_fromNodeObject_shouldSucceed(Serializable id, List<Serializable> stateIds) {
      assertDoesNotThrow(
          () -> {
            Node node = new Node(id, stateIds);
            test.addNode(node);
          });
      validateExistence(id, stateIds);
    }

    private void validateExistence(Serializable id, List<Serializable> stateIds) {
      Node node = test.getNode(id);
      assertNotNull(node);
      Set<Serializable> idSet = new HashSet<>(stateIds);
      node.getNodeStates().forEach(state -> assertTrue(idSet.contains(state.getId())));
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_allErrors")
    void addNode_fromNodeObject_shouldThrowError(
        Serializable id, List<Serializable> stateIds, Class<Exception> exceptionClass) {
      assertThrows(
          exceptionClass,
          () -> {
            Node node = new Node(id, stateIds);
            test.addNode(node);
          });
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_successExpected")
    void addNewNode_fromNodeArgs_shouldSucceed(Serializable id, List<Serializable> stateIds) {
      assertDoesNotThrow(
          () -> {
            test.addNewNode(id, stateIds);
          });
      validateExistence(id, stateIds);
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_allErrors")
    void addNewNode_fromNodeArgs_shouldThrowError(
        Serializable id, List<Serializable> stateIds, Class<Exception> exceptionClass) {
      assertThrows(exceptionClass, () -> test.addNewNode(id, stateIds));
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_successExpected")
    void addNewNode_onlyId_shouldSucceed(Serializable id) {
      assertDoesNotThrow(() -> test.addNewNode(id));
      assertNotNull(test.getNode(id));
    }

    @ParameterizedTest
    @MethodSource("nodeInputArgs_nodeIdErrors")
    void addNewNode_onlyId_shouldThrowError(
        Serializable id, List<Serializable> stateIds, Class<Exception> exceptionClass) {
      assertFalse(stateIds.isEmpty());
      assertThrows(exceptionClass, () -> test.addNewNode(id));
    }

    @NoArgsConstructor
    static class SerializableClass implements Serializable {}
  }

  @Nested
  class NodeRemovalTests {

    static Stream<Arguments> nodeRemovalArgs() {
      Stream<Arguments> expectedTrue = NODE_IDS.stream().map(id -> Arguments.of(id, true));
      Stream<Arguments> expectedFalse =
          Stream.of(
              Arguments.of("E", false),
              Arguments.of("F", false),
              Arguments.of('A', false),
              Arguments.of(3, false));
      return Stream.concat(expectedTrue, expectedFalse);
    }

    @BeforeEach
    void connectNetwork() {
      test.addParents("B", "A").addParents("C", "B").addParents("D", "C");
    }

    @ParameterizedTest
    @MethodSource("nodeRemovalArgs")
    void removeById_shouldSucceed(Serializable id, boolean existing) {
      BayesianNetworkData data = test.getNetworkData();
      test.buildNetworkData();
      assertEquals(existing, test.removeNodeByID(id));
      if (!existing) return;
      assertFalse(data.getNodes().stream().anyMatch(n -> n.getId().equals(id)));
      assertFalse(data.getNodeIDsMap().keySet().stream().anyMatch(id::equals));
      assertFalse(data.getNetworkTablesMap().keySet().stream().anyMatch(n -> n.getId().equals(id)));
    }

    @ParameterizedTest
    @MethodSource("nodeRemovalArgs")
    void removeByNode_shouldSucceed(Serializable id, boolean existing) {
      BayesianNetworkData data = test.getNetworkData();
      test.buildNetworkData();
      assertEquals(existing, test.removeNode(test.getNode(id)));
      if (!existing) return;
      assertFalse(data.getNodes().stream().anyMatch(n -> n.getId().equals(id)));
      assertFalse(data.getNodeIDsMap().keySet().stream().anyMatch(id::equals));
      assertFalse(data.getNetworkTablesMap().keySet().stream().anyMatch(n -> n.getId().equals(id)));
    }

    @Test
    void removeAllNodes_shouldSucceed() {
      BayesianNetworkData data = test.getNetworkData();
      test.buildNetworkData();
      assertTrue(test.removeAllNodes());
      assertTrue(data.getNodes().isEmpty());
      assertTrue(data.getNodeIDsMap().isEmpty());
      assertTrue(data.getNodeStateIDsMap().isEmpty());
      assertTrue(data.getNetworkTablesMap().isEmpty());
      assertTrue(data.getConstraints().isEmpty());
      assertFalse(data.isSolved());
      assertFalse(test.removeAllNodes());
    }
  }

  @Nested
  class NodeStateModificationTests {

    static Stream<Arguments> provideSuccessfulStateChanges() {
      return Stream.of(
          Arguments.of("A", List.of("A+", "A-"), 4),
          Arguments.of("A", List.of("A+", "A-", "A++"), 4),
          Arguments.of("A", List.of("A+"), 3),
          Arguments.of("A", List.of(5, 'A'), 0),
          Arguments.of("A", List.of(), 0));
    }

    static Stream<Arguments> failedStateAdditions() {
      return Stream.of(
          Arguments.of("A", "A+", null),
          Arguments.of("A", "A-", null),
          Arguments.of("A", "B", null),
          Arguments.of("A", null, NullPointerException.class),
          Arguments.of(null, "A", NullPointerException.class),
          Arguments.of("E", "A", NullPointerException.class),
          Arguments.of("A", "B+", null));
    }

    @BeforeEach
    void addConstraints() {
      test.addConstraint("A+", 0.5)
          .addConstraint("A-", 0.5)
          .addConstraint("A+", List.of("B+"), 0.5)
          .addConstraint("B+", List.of("A+"), 0.5);
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulStateChanges")
    void setStates_shouldSucceed(
        Serializable nodeId, List<Serializable> newStateIds, int expectedConstraints) {
      changeStatesCommon_shouldSucceed(
          nodeId,
          newStateIds,
          expectedConstraints,
          (n, a) -> n.setNodeStates((List<NodeState>) a.getNewCollection()),
          (n, a) -> {});
    }

    void changeStatesCommon_shouldSucceed(
        Serializable nodeId,
        List<Serializable> newStateIds,
        int expectedConstraints,
        BiConsumer<Node, CollectionChangeAnalyzer<NodeState>> addStates,
        BiConsumer<Node, CollectionChangeAnalyzer<NodeState>> removeStates) {
      Node node = test.getNode(nodeId);
      List<NodeState> oldStates = new ArrayList<>(node.getNodeStates());
      List<NodeState> newStates = newStateIds.stream().map(id -> new NodeState(id, node)).toList();
      CollectionChangeAnalyzer<NodeState> analyzer =
          new CollectionChangeAnalyzer<>(oldStates, newStates);

      assertDoesNotThrow(() -> addStates.accept(node, analyzer));
      assertDoesNotThrow(() -> removeStates.accept(node, analyzer));

      BayesianNetworkData networkData = test.getNetworkData();

      Set<NodeState> networkStates = new HashSet<>(networkData.getNodeStateIDsMap().values());
      analyzer.getAdded().forEach(added -> assertTrue(networkStates.contains(added)));
      analyzer.getRemoved().forEach(removed -> assertFalse(networkStates.contains(removed)));

      assertEquals(expectedConstraints, networkData.getConstraints().size());
      assertFalse(networkData.isSolved());
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulStateChanges")
    void addRemoveManual_shouldSucceed(
        Serializable nodeId, List<Serializable> newStateIds, int expectedConstraints) {
      changeStatesCommon_shouldSucceed(
          nodeId,
          newStateIds,
          expectedConstraints,
          (n, a) -> a.getAdded().forEach(added -> assertTrue(n.addState(added.getId()))),
          (n, a) -> a.getRemoved().forEach(added -> assertTrue(n.removeState(added.getId()))));
    }

    @ParameterizedTest
    @MethodSource("failedStateAdditions")
    void addNodeStates_shouldNotSucceed(
        Serializable nodeId, Serializable stateId, Class<Exception> exceptionClass) {
      try {
        Node node = test.getNode(nodeId);
        Set<NodeState> states = new HashSet<>(node.getNodeStates());
        assertFalse(node.addState(stateId));
        assertEquals(states, new HashSet<>(node.getNodeStates()));
      } catch (Exception e) {
        assertEquals(e.getClass(), exceptionClass);
      }
    }

    @ParameterizedTest
    @MethodSource("failedStateAdditions")
    void setNewNodeStates_shouldNotSucceed(
        Serializable nodeId, Serializable stateId, Class<Exception> exceptionClass) {
      try {
        Node node = test.getNode(nodeId);
        Set<NodeState> originalStates = new HashSet<>(node.getNodeStates());
        List<NodeState> toSet = new ArrayList<>(node.getNodeStates());
        toSet.add(new NodeState(stateId, node));
        assertFalse(node.setNodeStates(toSet));
        assertEquals(originalStates, new HashSet<>(node.getNodeStates()));
      } catch (Exception e) {
        assertEquals(e.getClass(), exceptionClass);
      }
    }
  }

  @Nested
  class ParentChildTests {
    static Stream<Arguments> provideSuccessfulRelations() {
      return successfulRelationsMaps().stream().map(Arguments::of);
    }

    static List<Map<Serializable, List<Serializable>>> successfulRelationsMaps() {
      Map<Serializable, List<Serializable>> mapA = new HashMap<>();
      mapA.put("A", List.of());
      mapA.put("B", List.of("A"));
      mapA.put("C", List.of("A"));
      mapA.put("D", List.of("B", "C"));

      Map<Serializable, List<Serializable>> mapB = new HashMap<>();
      mapB.put("A", List.of("B"));
      mapB.put("B", List.of("C"));
      mapB.put("C", List.of("D"));
      mapB.put("D", List.of());

      Map<Serializable, List<Serializable>> mapC = new HashMap<>();
      mapC.put("A", List.of());
      mapC.put("B", List.of("A"));
      mapC.put("C", List.of("B", "A"));
      mapC.put("D", List.of("C", "B", "A"));

      Map<Serializable, List<Serializable>> mapD = new HashMap<>();
      mapD.put("A", List.of());
      mapD.put("B", List.of("A", "C", "D"));
      mapD.put("C", List.of());
      mapD.put("D", List.of());

      return List.of(mapA, mapB, mapC, mapD);
    }

    static Stream<Arguments> provideSuccessfulNodeDeletionArguments() {
      List<Arguments> args = new ArrayList<>();
      NODE_IDS.forEach(
          id -> successfulRelationsMaps().forEach(map -> args.add(Arguments.of(map, id))));
      return args.stream();
    }

    static Stream<Arguments> provideSuccessfulParentRemovals() {
      List<Map<Serializable, List<Serializable>>> add = successfulRelationsMaps();
      List<Map<Serializable, List<Serializable>>> rem = successfulParentRemovalsMaps();
      return IntStream.range(0, add.size()).mapToObj(i -> Arguments.of(add.get(i), rem.get(i)));
    }

    static List<Map<Serializable, List<Serializable>>> successfulParentRemovalsMaps() {
      Map<Serializable, List<Serializable>> mapA = new HashMap<>();
      mapA.put("A", List.of());
      mapA.put("B", List.of("A"));
      mapA.put("C", List.of("A"));
      mapA.put("D", List.of("B", "C"));

      Map<Serializable, List<Serializable>> mapB = new HashMap<>();
      mapB.put("A", List.of("B"));
      mapB.put("C", List.of("D"));
      mapB.put("D", List.of());

      Map<Serializable, List<Serializable>> mapC = new HashMap<>();
      mapC.put("A", List.of());
      mapC.put("B", List.of("B"));
      mapC.put("C", List.of("B", "A"));
      mapC.put("D", List.of("C", "B", "A"));

      Map<Serializable, List<Serializable>> mapD = new HashMap<>();
      mapD.put("A", List.of("A", "C", "D"));
      mapD.put("B", List.of("A", "C", "D"));
      mapD.put("C", List.of("A", "C", "D"));
      mapD.put("D", List.of("A", "C", "D"));

      return List.of(mapA, mapB, mapC, mapD);
    }

    static Stream<Arguments> provideErrorRelations() {
      Map<Serializable, List<Serializable>> mapA = new HashMap<>();
      mapA.put("A", List.of("D"));
      mapA.put("B", List.of("A"));
      mapA.put("C", List.of("A"));
      mapA.put("D", List.of("B", "C"));

      Map<Serializable, List<Serializable>> mapB = new HashMap<>();
      mapB.put("A", List.of("B"));
      mapB.put("B", List.of("B"));
      mapB.put("C", List.of("B"));
      mapB.put("D", List.of("B"));

      Map<Serializable, List<Serializable>> mapC = new HashMap<>();
      mapC.put("A", List.of());
      mapC.put("B", List.of("Q"));
      mapC.put("C", List.of('A'));
      mapC.put("D", List.of(3));

      Map<Serializable, List<Serializable>> mapD = new HashMap<>();
      mapD.put("A", List.of());
      mapD.put("B", listWithNull(List.of("A")));
      mapD.put("C", List.of("A"));
      mapD.put("D", List.of("B", "C"));

      return Stream.of(
          Arguments.of(mapA, NetworkStructureException.class),
          Arguments.of(mapB, NetworkStructureException.class),
          Arguments.of(mapC, NullPointerException.class),
          Arguments.of(mapD, NullPointerException.class));
    }

    @AfterEach
    void assertNetworkUnsolved() {
      assertFalse(test.getNetworkData().isSolved());
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulRelations")
    void addParents_withNetwork_fromNode_shouldSucceed(Map<Serializable, List<Serializable>> map) {
      map.forEach(
          (childId, parentIds) -> {
            Node child = test.getNode(childId);
            Set<Node> parents = test.getNodes(parentIds);
            assertDoesNotThrow(() -> test.addParents(child, parents));
            assertEquals(parents, new HashSet<>(child.getParents()));
            assertTrue(parents.stream().allMatch(node -> node.getChildren().contains(child)));
          });
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulRelations")
    void addParents_withNetwork_FromIds_shouldSucceed(Map<Serializable, List<Serializable>> map) {
      map.forEach(
          (childId, parentIds) -> assertDoesNotThrow(() -> test.addParents(childId, parentIds)));
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulRelations")
    void addParents_fromNode_shouldSucceed(Map<Serializable, List<Serializable>> map) {
      map.forEach(
          (childId, parentIds) -> {
            Node child = test.getNode(childId);
            Set<Node> parents = test.getNodes(parentIds);
            assertDoesNotThrow(() -> child.setParents(new ArrayList<>(parents)));
            assertTrue(
                parents.stream()
                    .allMatch(
                        p -> {
                          boolean parentContainsChild = p.getChildren().contains(child);
                          boolean childContainsParent = child.getParents().contains(p);
                          return parentContainsChild && childContainsParent;
                        }));
          });
    }

    @ParameterizedTest
    @MethodSource("provideErrorRelations")
    void addParents_withNetwork_FromIds_shouldThrowError(
        Map<Serializable, List<Serializable>> map, Class<Exception> exceptionClass) {
      assertThrows(
          exceptionClass,
          () -> map.forEach((childId, parentIds) -> test.addParents(childId, parentIds)));
    }

    @ParameterizedTest
    @MethodSource("provideErrorRelations")
    void addParents_fromNodes_shouldThrowError(
        Map<Serializable, List<Serializable>> map, Class<Exception> exceptionClass) {
      try {
        Map<Node, List<Node>> nodeMap =
            map.entrySet().stream()
                .map(
                    e ->
                        Map.entry(
                            test.getNode(e.getKey()), new ArrayList<>(test.getNodes(e.getValue()))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertThrows(exceptionClass, () -> nodeMap.forEach(Node::setParents));
      } catch (NullPointerException e) {
        assertTrue(true);
      }
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulParentRemovals")
    void removeParents_withNetwork_fromIds_shouldSucceed(
        Map<Serializable, List<Serializable>> add, Map<Serializable, List<Serializable>> remove) {
      add.forEach(
          (childId, parentIds) -> assertDoesNotThrow(() -> test.addParents(childId, parentIds)));
      remove.forEach(
          (childId, parentIds) ->
              parentIds.forEach(
                  parentId -> assertDoesNotThrow(() -> test.removeParent(childId, parentId))));
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulParentRemovals")
    void removeParents_fromNode_shouldSucceed(
        Map<Serializable, List<Serializable>> add, Map<Serializable, List<Serializable>> remove) {
      add.forEach(
          (childId, parentIds) -> assertDoesNotThrow(() -> test.addParents(childId, parentIds)));
      remove.forEach(
          (childId, parentIds) -> {
            Node child = test.getNode(childId);
            Set<Node> parents = test.getNodes(parentIds);
            parents.forEach(
                parent -> {
                  assertDoesNotThrow(() -> child.removeParent(parent));
                  assertFalse(child.getParents().contains(parent));
                  assertFalse(parent.getChildren().contains(child));
                });
          });
    }

    @ParameterizedTest
    @MethodSource("provideSuccessfulNodeDeletionArguments")
    void removeNodes_shouldNotThrowErrors(
        Map<Serializable, List<Serializable>> add, Serializable deletionId) {
      add.forEach(
          (childId, parentIds) -> assertDoesNotThrow(() -> test.addParents(childId, parentIds)));
      Node deleted = test.getNode(deletionId);
      assertDoesNotThrow(() -> test.removeNodeByID(deletionId));

      Set<PropertyChangeListener> listeners =
          Arrays.stream(deleted.getSupport().getPropertyChangeListeners())
              .collect(Collectors.toSet());
      assertFalse(listeners.contains((BayesianNetworkImpl) test));

      Set<Node> remainingNodes =
          test.getNodes(NODE_IDS.stream().filter(id -> !deletionId.equals(id)).toList());
      remainingNodes.forEach(
          remaining -> {
            assertFalse(remaining.getParents().contains(deleted));
            assertFalse(remaining.getChildren().contains(deleted));
          });
    }
  }

  @Nested
  class ProbabilityConstraintTests {
    static ConstraintRegistry registry = new ConstraintRegistry();

    static Stream<Arguments> successfulAddGetRemove() {
      return Stream.of(
          Arguments.of(List.of("A+"), List.of(), 0.25, MARGINAL),
          Arguments.of(List.of("A+"), List.of("B+"), 0.25, CONDITIONAL),
          Arguments.of(List.of("A+", "C+"), List.of(), 0.25, JOINT),
          Arguments.of(List.of("A+", "A-"), List.of(), 0.25, SUM),
          Arguments.of(List.of("B+", "C+"), List.of("A+"), 0.25, JOINT),
          Arguments.of(List.of("A+", "A-"), List.of("B+", "C+"), 0.25, SUM),
          Arguments.of(List.of("B+", "C+", "D+"), List.of(), 0.25, JOINT),
          Arguments.of(List.of("A+", "A-", "D+"), List.of(), 0.25, SUM));
    }

    static Stream<Arguments> unsuccessfulAdd() {
      Class<BayesNetIDException> idException = BayesNetIDException.class;
      Class<ConstraintValidationException> constraintException =
          ConstraintValidationException.class;
      return Stream.of(
          Arguments.of(List.of("An"), List.of(), 0.5, idException),
          Arguments.of(List.of("A+"), List.of("A-"), 0.5, constraintException),
          Arguments.of(List.of("A+", "A-"), List.of(), 1.25, constraintException),
          Arguments.of(List.of("A+", "C+"), List.of("B+"), -0.5, constraintException),
          Arguments.of(listWithNull(List.of("A+")), List.of("B+"), 0.5, NullPointerException.class),
          Arguments.of(
              List.of("A+"), listWithNull(List.of("B+")), 0.5, NullPointerException.class));
    }

    @BeforeEach
    void setUpNet() {
      test.addParents("B", List.of("A"));
      test.addParents("C", List.of("A", "B"));
      test.addParents("D", List.of("C", "B", "A"));
    }

    @ParameterizedTest
    @MethodSource("successfulAddGetRemove")
    void testAddGetAndRemove_shouldSucceed(
        List<Serializable> events,
        List<Serializable> conditions,
        double probability,
        ConstraintType type) {
      ConstraintStrategy<?> strategy = registry.getTypedStrategy(type);
      assertDoesNotThrow(() -> test.addConstraint(events, conditions, probability));
      ProbabilityConstraint constraint = test.getConstraint(events, conditions);
      assertNotNull(strategy.safeCastConstraint(constraint));
      assertTrue(test.removeConstraint(events, conditions));
      assertFalse(test.isSolved());
      ProbabilityConstraint c = createConstraint(events, conditions, probability, test, type);
      assertDoesNotThrow(() -> test.addConstraint(c));
      constraint = test.getConstraint(events, conditions);
      assertNotNull(strategy.safeCastConstraint(constraint));
      assertTrue(test.removeConstraint(events, conditions));
      assertFalse(test.isSolved());
    }

    static ProbabilityConstraint createConstraint(
        Collection<Serializable> eventIds,
        Collection<Serializable> conditionIds,
        double probability,
        BayesianNetwork network,
        ConstraintType type) {
      Set<NodeState> events = network.getNodeStates(eventIds);
      NodeState event = events.stream().findFirst().orElseThrow();
      Set<NodeState> conditions = network.getNodeStates(conditionIds);
      return switch (type) {
        case MARGINAL -> new MarginalConstraint(event, probability);
        case CONDITIONAL -> new ConditionalConstraint(event, conditions, probability);
        case JOINT -> new JointProbabilityConstraint(events, conditions, probability);
        case SUM -> new SumProbabilityConstraint(events, conditions, probability);
      };
    }

    @ParameterizedTest
    @MethodSource("unsuccessfulAdd")
    void testAddIllegal_shouldThrow(
        List<Serializable> events,
        List<Serializable> conditions,
        double probability,
        Class<Exception> exceptionClass) {
      assertThrows(exceptionClass, () -> test.addConstraint(events, conditions, probability));
      assertFalse(test.isSolved());
    }

    @ParameterizedTest
    @MethodSource("unsuccessfulAdd")
    void sneakIllegalIntoNetworkData_shouldThrow(
        List<Serializable> events,
        List<Serializable> conditions,
        double probability,
        Class<Exception> exceptionClass) {
      Set<NodeState> eventStates = buildOrCreateStates(events);
      Set<NodeState> conditionStates = buildOrCreateStates(conditions);
      if (exceptionClass.equals(NullPointerException.class)) {
        assertThrows(
            exceptionClass,
            () -> new JointProbabilityConstraint(eventStates, conditionStates, probability));
        return;
      }
      JointProbabilityConstraint constraint =
          new JointProbabilityConstraint(eventStates, conditionStates, probability);
      BayesianNetworkData networkData = test.getNetworkData();
      networkData.getConstraints().add(constraint);
      BayesSolver solver = BayesSolver.create(test);
      assertFalse(solver.solve());
      assertFalse(networkData.getConstraints().contains(constraint));
    }

    Set<NodeState> buildOrCreateStates(List<Serializable> stateIds) {
      Set<NodeState> states = new HashSet<>();
      List<Serializable> toCreate = new ArrayList<>();
      boolean addNull = false;
      for (Serializable stateId : stateIds) {
        if (stateId == null) {
          addNull = true;
          continue;
        }
        NodeState state = test.getNodeState(stateId);
        if (state == null) toCreate.add(stateId);
        else states.add(state);
      }
      if (addNull) states.add(null);
      if (toCreate.isEmpty()) return states;
      Node node = new Node(UUID.randomUUID(), toCreate);
      states.addAll(node.getNodeStates());
      return states;
    }

    @ParameterizedTest
    @MethodSource("successfulAddGetRemove")
    void testAddTwice_shouldThrow(
        List<Serializable> events,
        List<Serializable> conditions,
        double probability,
        ConstraintType type) {
      Class<ConstraintValidationException> exceptionClass = ConstraintValidationException.class;
      assertDoesNotThrow(() -> test.addConstraint(events, conditions, probability));
      assertThrows(exceptionClass, () -> test.addConstraint(events, conditions, probability));
      ProbabilityConstraint c = createConstraint(events, conditions, probability, test, type);
      assertThrows(exceptionClass, () -> test.addConstraint(c));
      assertFalse(test.isSolved());
    }

    @ParameterizedTest
    @MethodSource("successfulAddGetRemove")
    void testSolves_shouldSucceed(
        List<Serializable> events, List<Serializable> conditions, double probability) {
      test.addConstraint(events, conditions, probability);
      InferenceEngine engine = InferenceEngine.create(test);
      assertNotNull(engine);
      engine.setObservedById(conditions);
      assertTrue(test.isSolved());
      assertEquals(probability, engine.getPosteriorProbabilityById(events), 1e-6);
    }
  }
}
