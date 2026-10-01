package io.github.alecredmond.export.method.network;

import static io.github.alecredmond.export.method.network.NetworkScenario.RAIN_NETWORK;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

import java.io.Serializable;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BayesianNetworkTest {
  BayesianNetwork net;

  @BeforeEach
  void setUp() {
    net = BayesianNetwork.newNetwork("TestNetwork");
  }

  @Nested
  class NetworkCreationTests {
    @Test
    void newNetwork_noName_shouldNotBeNull() {
      BayesianNetwork unnamedNet = BayesianNetwork.newNetwork();
      assertNotNull(unnamedNet);
      assertNotNull(unnamedNet.getNetworkData());
      assertEquals("UNNAMED NETWORK", unnamedNet.getNetworkData().getNetworkName());
    }

    @Test
    void newNetwork_withName_shouldNotBeNullAndHaveName() {
      assertNotNull(net);
      assertNotNull(net.getNetworkData());
      assertEquals("TestNetwork", net.getNetworkData().getNetworkName());
    }
  }

  @Nested
  class NodeManipulationTests {

    @Test
    void addNode_singleNode_shouldSucceed() {
      net.addNewNode("A");
      BayesianNetworkData data = net.getNetworkData();
      assertNotNull(net.getNode("A"));
      assertEquals(1, data.getNodeIDsMap().size());

      net.addNode(new Node("B", List.of("B+", "B-")));
      assertNotNull(net.getNode("B"));
      assertEquals(2, data.getNodeIDsMap().size());
    }

    @Test
    void addNode_duplicateID_shouldThrowException() {
      net.addNewNode("A");
      assertThrows(BayesNetIDException.class, () -> net.addNewNode("A"));
      assertThrows(BayesNetIDException.class, () -> net.addNode(new Node("A")));
    }

    @Test
    void addNode_nullID_shouldThrowException() {
      assertThrows(BayesNetIDException.class, () -> net.addNode(null));
    }

    @Test
    void addNode_withStates_shouldSucceed() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      Node nodeA = net.getNode("A");

      assertNotNull(nodeA);
      assertEquals(2, nodeA.getStates().size());
      assertNotNull(net.getNodeState("A_T"));
      assertNotNull(net.getNodeState("A_F"));
    }

    @Test
    void addNode_withStates_duplicateNodeID_shouldThrowException() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertThrows(BayesNetIDException.class, () -> net.addNewNode("A", List.of("A_T_2", "A_F_2")));
    }

    @Test
    void addNode_withStates_duplicateStateIDInNetwork_shouldThrowException() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      assertThrows(BayesNetIDException.class, () -> net.addNewNode("B", List.of("A_T", "B_F")));
    }

    @Test
    void addNode_withStates_duplicateStateIDInCollection_shouldThrowException() {
      assertThrows(BayesNetIDException.class, () -> net.addNewNode("B", List.of("B_T", "B_T")));
    }

    @Test
    void addNode_withEmptyStates_shouldSucceed() {
      net.addNewNode("A", List.of());
      Node nodeA = net.getNode("A");
      assertNotNull(nodeA);
      assertTrue(nodeA.getStates().isEmpty());
    }

    @Test
    void addNode_withNullStatesCollection_shouldThrowException() {
      assertThrows(NullPointerException.class, () -> net.addNewNode("A", null));
    }

    @Test
    void removeNode_existingNode_shouldSucceed() {
      net.addNewNode("A");
      assertNotNull(net.getNode("A"));
      net.removeNodeByID("A");
      assertNull(net.getNode("A"));
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
    }

    @Test
    void removeNode_nonExistentNode_shouldNotThrowException() {
      net.addNewNode("A");
      assertDoesNotThrow(() -> net.removeNodeByID("B"));
      assertEquals(1, net.getNetworkData().getNodeIDsMap().size());
    }

    @Test
    void removeNode_withNullID_shouldNotThrowException() {
      net.addNewNode("A");
      assertDoesNotThrow(() -> net.removeNode(null));
      assertEquals(1, net.getNetworkData().getNodeIDsMap().size());
    }

    @Test
    void removeNode_shouldAlsoRemoveStatesAndEdges() {
      net.addNewNode("A", List.of("A_T", "A_F"));
      net.addNewNode("B", List.of("B_T", "B_F"));
      net.addParents("B", "A");

      assertNotNull(net.getNode("A"));
      assertNotNull(net.getNodeState("A_T"));
      assertTrue(net.getNode("B").getParents().contains(net.getNode("A")));

      net.removeNodeByID("A");

      assertNull(net.getNode("A"));
      assertNull(net.getNodeState("A_T"));
      assertTrue(net.getNode("B").getParents().isEmpty()); // Check edge is gone
    }

    @Test
    void removeAllNodes_shouldSucceed() {
      net.addNewNode("A");
      net.addNewNode("B");
      net.removeAllNodes();
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
      assertTrue(net.getNetworkData().getNodeStateIDsMap().isEmpty());
    }

    @Test
    void removeAllNodes_onEmptyNetwork_shouldSucceed() {
      assertDoesNotThrow(() -> net.removeAllNodes());
      assertTrue(net.getNetworkData().getNodeIDsMap().isEmpty());
    }
  }

  @Nested
  class StructureManipulationTests {

    @BeforeEach
    void buildNodes() {
      net.addNewNode("A");
      net.addNewNode("B");
      net.addNewNode("C");
    }

    @Test
    void addParent_shouldSucceed() {
      net.addParents("B", "A");
      Node nodeB = net.getNode("B");
      Node nodeA = net.getNode("A");
      assertTrue(nodeB.getParents().contains(nodeA));
      assertTrue(nodeA.getChildren().contains(nodeB));
    }

    @Test
    void addParents_shouldSucceed() {
      net.addParents("C", List.of("A", "B"));
      Node nodeC = net.getNode("C");
      Node nodeA = net.getNode("A");
      Node nodeB = net.getNode("B");
      assertTrue(nodeC.getParents().containsAll(List.of(nodeA, nodeB)));
      assertTrue(nodeA.getChildren().contains(nodeC));
      assertTrue(nodeB.getChildren().contains(nodeC));
    }

    @Test
    void addParent_toNonExistentChild_shouldThrowException() {
      assertThrows(NullPointerException.class, () -> net.addParents("Z", "A"));
    }

    @Test
    void addParent_nonExistentParent_shouldThrowException() {
      assertThrows(NullPointerException.class, () -> net.addParents("A", "Z"));
    }

    @Test
    void addParent_createCycle_shouldThrowException() {
      net.addParents("B", "A");
      assertThrows(NetworkStructureException.class, () -> net.addParents("A", "B"));
    }

    @Test
    void addParent_addSelfAsParent_shouldThrowException() {
      assertThrows(NetworkStructureException.class, () -> net.addParents("A", "A"));
    }

    @Test
    void removeParent_shouldSucceed() {
      net.addParents("B", "A");
      Node nodeB = net.getNode("B");
      Node nodeA = net.getNode("A");
      assertTrue(nodeB.getParents().contains(nodeA));

      net.removeParent("B", "A");
      assertFalse(nodeB.getParents().contains(nodeA));
      assertFalse(nodeA.getChildren().contains(nodeB));
    }

    @Test
    void removeParent_nonExistentParentRelation_shouldNotThrow() {
      net.addParents("B", "A");
      assertDoesNotThrow(() -> net.removeParent("C", "A"));
      assertDoesNotThrow(() -> net.removeParent("B", "C"));
    }

    @Test
    void removeParents_shouldSucceed() {
      net.addParents("C", List.of("A", "B"));
      Node nodeC = net.getNode("C");
      assertFalse(nodeC.getParents().isEmpty());

      net.removeParents("C");
      assertTrue(nodeC.getParents().isEmpty());
    }

    @Test
    void removeParents_fromNodeWithNoParents_shouldNotThrow() {
      assertDoesNotThrow(() -> net.removeParents("A"));
    }

    @Test
    void removeParents_fromNonExistentNode_shouldThrowException() {
      assertThrows(NullPointerException.class, () -> net.removeParents("Z"));
    }
  }

  @Nested
  class ProbabilityConstraintTests {
    NodeState aT;
    NodeState aF;
    NodeState bT;
    NodeState bF;

    @BeforeEach
    void buildNetwork() {
      Node a = new Node("A", List.of("A_T", "A_F"));
      Node b = new Node("B", List.of("B_T", "B_F"));
      aT = a.getStates().getFirst();
      aF = a.getStates().getLast();
      bT = b.getStates().getFirst();
      bF = b.getStates().getLast();
      net.addNode(a);
      net.addNode(b);
      net.addParents(b, a);
    }

    @Test
    void addConstraint_prior_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", 0.2));
      double prob = net.getNetworkData().getConstraints().iterator().next().getProbability();
      assertEquals(0.2, prob);
    }

    @Test
    void addConstraint_prior_invalidProbability_shouldThrowException() {
      assertThrows(ConstraintValidationException.class, () -> net.addConstraint("A_T", 1.5));
      assertThrows(ConstraintValidationException.class, () -> net.addConstraint("A_T", -0.5));
    }

    @Test
    void addConstraint_prior_forNonExistentState_shouldThrowException() {
      assertThrows(BayesNetIDException.class, () -> net.addConstraint("Z_T", 0.5));
    }

    @Test
    void addConstraint_conditional_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("B_T", List.of("A_T"), 0.8));
      assertDoesNotThrow(() -> net.addConstraint("B_T", List.of("A_F"), 0.1));
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void addConstraint_conditional_invalidProbability_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class, () -> net.addConstraint("B_T", List.of("A_T"), 1.5));
      assertThrows(
          ConstraintValidationException.class,
          () -> net.addConstraint("B_T", List.of("A_T"), -0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentEventState_shouldThrowException() {
      assertThrows(BayesNetIDException.class, () -> net.addConstraint("Z_T", List.of("A_T"), 0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentConditionState_shouldThrowException() {
      assertThrows(BayesNetIDException.class, () -> net.addConstraint("B_T", List.of("Z_T"), 0.5));
    }

    @Test
    void addConstraint_conditional_withEmptyConditions_shouldSucceedAsPrior() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", List.of(), 0.2));
    }

    @Test
    void addConstraint_invalidConstraintBuilder_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class,
          () -> net.addConstraint("B_T", List.of("A_T", "B_F"), 0.5));
    }

    @Test
    void addConstraint_nonLocalConstraint_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraint("A_T", List.of("B_T"), 0.6));
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void addConstraint_manuallyBuilt_shouldSucceed() {
      buildTestConstraints()
          .forEach(
              constraint -> {
                assertDoesNotThrow(() -> net.addConstraint(constraint));
                assertDoesNotThrow(() -> net.solveNetwork());
              });
    }

    List<ProbabilityConstraint> buildTestConstraints() {
      List<ProbabilityConstraint> probabilityConstraints = new ArrayList<>();
      probabilityConstraints.add(new ConditionalConstraint(aT, List.of(bT), 0.6));
      probabilityConstraints.add(new MarginalConstraint(aF, 0.4));
      probabilityConstraints.add(new ConditionalConstraint(bF, List.of(aF), 0.3));
      return probabilityConstraints;
    }

    @Test
    void addConstraints_manuallyBuilt_shouldSucceed() {
      assertDoesNotThrow(() -> net.addConstraints(buildTestConstraints()));
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void getConstraints_validConstraints_shouldSucceed() {
      List<ProbabilityConstraint> probabilityConstraints = buildTestConstraints();
      net.addConstraints(probabilityConstraints);
      probabilityConstraints.stream()
          .filter(MarginalConstraint.class::isInstance)
          .map(MarginalConstraint.class::cast)
          .forEach(
              marginalConstraint -> {
                Serializable eventStateId = marginalConstraint.getEventState().getId();
                assertNotNull(net.getConstraint(eventStateId));
                assertNotNull(net.getConstraint(eventStateId, List.of()));
                assertConstraintsAreEqual(marginalConstraint, net.getConstraint(eventStateId));
              });

      probabilityConstraints.stream()
          .filter(ConditionalConstraint.class::isInstance)
          .map(ConditionalConstraint.class::cast)
          .forEach(
              conditionalConstraint -> {
                Serializable eventStateId = conditionalConstraint.getEventState().getId();
                List<Serializable> conditionStateIDs =
                    conditionalConstraint.getConditionStates().stream()
                        .map(NodeState::getId)
                        .toList();
                assertNotNull(net.getConstraint(eventStateId, conditionStateIDs));
                assertConstraintsAreEqual(
                    conditionalConstraint, net.getConstraint(eventStateId, conditionStateIDs));
              });
    }

    void assertConstraintsAreEqual(ProbabilityConstraint constraintA,ProbabilityConstraint constraintB){
        assertEquals(constraintA.getProbability(),constraintB.getProbability());
        assertEquals(constraintA.getEventStates(),constraintB.getEventStates());
        assertEquals(constraintA.getConditionStates(),constraintB.getConditionStates());
    }
  }

  @Nested
  class SolverTest {
    @BeforeEach
    void buildNetwork() {
      net = RAIN_NETWORK.get();
    }

    @Test
    void solveNetwork_onValidNetwork_shouldSucceed() {
      assertDoesNotThrow(() -> net.solveNetwork());
    }

    @Test
    void solveNetwork_onEmptyNetwork_shouldSucceed() {
      BayesianNetwork emptyNet = BayesianNetwork.newNetwork();
      assertDoesNotThrow(emptyNet::solveNetwork);
    }
  }
}
