package io.github.alecredmond.export.network;

import static io.github.alecredmond.export.node.implementation.BinaryNode.StateOrder.TRUE_FIRST;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.exceptions.BayesNetIDException;
import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.constraints.ConditionalConstraint;
import io.github.alecredmond.export.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.implementation.BinaryNode;
import io.github.alecredmond.export.node.implementation.DefaultBinaryNode;
import io.github.alecredmond.export.node.implementation.DefaultNode;
import io.github.alecredmond.export.node.implementation.DefaultNodeState;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.internal.method.constraints.types.marginalconstraint.ValidatedMarginalConstraint;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BayesianNetworkBuilderTest {

  BayesianNetworkBuilder builder;

  @BeforeEach
  void setUp() {
    builder = BayesianNetwork.builder().setNetworkName("TestNetwork");
  }

  @Nested
  class NodeManipulationTests {
    DefaultBinaryNode a;
    DefaultBinaryNode b;
    DefaultBinaryNode c;
    DefaultBinaryNode d;
    BayesSolver solver = new IPFPJunctionTreeSolver();

    @BeforeEach
    void setUpBinaryNodes() {
      a = new DefaultBinaryNode("A", TRUE_FIRST);
      b = new DefaultBinaryNode("B", List.of(a), TRUE_FIRST);
      c = new DefaultBinaryNode("C", List.of(b), TRUE_FIRST);
      d = new DefaultBinaryNode("D", List.of(c), TRUE_FIRST);
    }

    @Test
    void addNode_singleNode_shouldSucceed() {
      builder.setNodes(a);
      Optional<BayesianNetwork> optBn = builder.build(solver);
      assertTrue(optBn.isPresent());
      BayesianNetwork bn = optBn.orElseThrow();
      assertNotNull(bn.getNodeByName("A"));
      assertEquals(1, bn.getNodes().length);

      builder.setNodes(a, b);
      optBn = builder.build(solver);
      assertTrue(optBn.isPresent());
      bn = optBn.orElseThrow();
      assertNotNull(bn.getNodeByName("B"));
      assertEquals(2, bn.getNodes().length);
    }

    @Test
    void addNode_duplicateID_shouldThrowException() {
      b.setId(a.getId());
      builder.setNodes(a, b);
      assertThrows(BayesNetIDException.class, () -> builder.build(solver));
    }

    @Test
    void addNode_nullID_shouldThrowException() {
      b.setId(null);
      builder.setNodes(a, b);
      assertThrows(BayesNetIDException.class, () -> builder.build(solver));
    }

    @Test
    void addNode_withStates_shouldSucceed() {
      Node e = new DefaultNode("E", List.of("TRUE", "MAYBE", "FALSE"), List.of(a));
      builder.setNodes(a, e);
      BayesianNetwork net = builder.build(solver).orElseThrow();

      assertNotNull(net.getNodeByName("E"));
      assertNotNull(net.getNodeById(e.getId()));
      assertEquals(3, e.getStates().size());
      IntStream.range(0, 3)
          .forEach(
              i -> {
                NodeState state = e.getStates().get(i);
                NodeState fromNetwork = net.getNodeStateById(state.getId());
                assertNotNull(fromNetwork);
                assertEquals(state, fromNetwork);
              });
    }

    @Test
    void addNode_withStates_duplicateStateIDs_shouldThrowException() {
      DefaultNode e = new DefaultNode("E", List.of(), List.of(a));
      List<NodeState> states =
          List.of(
              new DefaultNodeState(e, 0, "TRUE", 0xDEADBEEF),
              new DefaultNodeState(e, 1, "MAYBE", 0xDEADBEEF),
              new DefaultNodeState(e, 2, "FALSE", 0xBEEFCAFE));
      e.setStates(states);
      builder.setNodes(a, e);
      assertThrows(BayesNetIDException.class, () -> builder.build(solver));
    }

    @Test
    void addNode_withStates_stateSharingIdWithNode_shouldThrowException() {
      DefaultNode e = new DefaultNode("E", List.of(), List.of(a));
      List<NodeState> states =
          List.of(
              new DefaultNodeState(e, 0, "TRUE", 0xDEADBEEF),
              new DefaultNodeState(e, 1, "MAYBE", 0xFEEDBEEF),
              new DefaultNodeState(e, 2, "FALSE", 0xBEEFCAFE));
      e.setStates(states);
      e.setId(0xBEEFCAFE);
      builder.setNodes(a, e);
      assertThrows(BayesNetIDException.class, () -> builder.build(solver));
    }

    @Test
    void addNode_withEmptyStates_shouldThrowException() {
      DefaultNode e = new DefaultNode("E", List.of(), List.of(a));
      builder.setNodes(a, e);
      assertThrows(NetworkStructureException.class, () -> builder.build(solver));
    }

    @Test
    void addNode_withNullStatesCollection_shouldThrowException() {
      assertThrows(NullPointerException.class, () -> new DefaultNode("E", null, List.of(a)));
    }

    @Test
    void addNodes_gapInChain_shouldThrowStructureException() {
      builder.setNodes(a, b, d);
      assertThrows(NetworkStructureException.class, () -> builder.build(solver));
    }

    @Test
    void addCircularReference_shouldThrowStructureException() {
      b.setParents(List.of(a, d));
      builder.setNodes(a, b, c, d);
      assertThrows(NetworkStructureException.class, () -> builder.build(solver));
    }
  }

  @Nested
  class ProbabilityConstraintTests {
    DefaultBinaryNode a;
    DefaultBinaryNode b;
    DefaultBinaryNode c;
    DefaultBinaryNode d;
    BayesSolver solver = new IPFPJunctionTreeSolver();

    @BeforeEach
    void setUpBinaryNodes() {
      a = new DefaultBinaryNode("A", TRUE_FIRST);
      b = new DefaultBinaryNode("B", List.of(a), TRUE_FIRST);
      c = new DefaultBinaryNode("C", List.of(b), TRUE_FIRST);
      d = new DefaultBinaryNode("D", List.of(c), TRUE_FIRST);
      builder.setNodes(a, b, c, d);
    }

    @Test
    void addConstraint_prior_shouldSolve() {
      assertDoesNotThrow(() -> builder.addConstraint(a.trueState(), 0.2));
      BayesianNetwork network = builder.build(solver).orElseThrow();
      double prob = network.getConstraints().iterator().next().getProbability();
      assertEquals(0.2, prob);
    }

    @Test
    void addConstraint_prior_invalidProbability_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class, () -> builder.addConstraint(a.trueState(), 1.5));
      assertThrows(
          ConstraintValidationException.class, () -> builder.addConstraint(a.falseState(), -0.5));
    }

    @Test
    void addConstraint_prior_forNonExistentState_shouldThrowException() {
      BinaryNode e = new DefaultBinaryNode("E", TRUE_FIRST);
      assertThrows(
          ConstraintValidationException.class, () -> builder.addConstraint(e.trueState(), 0.5));
    }

    @Test
    void addConstraint_conditional_shouldSucceed() {
      assertDoesNotThrow(() -> builder.addConstraint(b.trueState(), List.of(a.trueState()), 0.8));
      assertDoesNotThrow(() -> builder.addConstraint(b.trueState(), List.of(a.falseState()), 0.1));
      assertDoesNotThrow(() -> builder.build(solver));
    }

    @Test
    void addConstraint_overwritesExisting_shouldThrowException() {
      assertDoesNotThrow(() -> builder.addConstraint(b.trueState(), List.of(a.trueState()), 0.8));
      assertThrows(
          ConstraintValidationException.class,
          () -> builder.addConstraint(b.trueState(), List.of(a.trueState()), 0.1));
    }

    @Test
    void addConstraint_conditional_invalidProbability_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class,
          () -> builder.addConstraint(b.trueState(), List.of(a.trueState()), 1.5));
      assertThrows(
          ConstraintValidationException.class,
          () -> builder.addConstraint(b.trueState(), List.of(a.trueState()), -0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentEventState_shouldThrowException() {
      BinaryNode e = new DefaultBinaryNode("E", TRUE_FIRST);
      assertThrows(
          ConstraintValidationException.class,
          () -> builder.addConstraint(e.trueState(), List.of(a.trueState()), 0.5));
    }

    @Test
    void addConstraint_conditional_nonExistentConditionState_shouldThrowException() {
      BinaryNode e = new DefaultBinaryNode("E", TRUE_FIRST);
      assertThrows(
          ConstraintValidationException.class,
          () -> builder.addConstraint(b.trueState(), List.of(e.trueState()), 0.5));
    }

    @Test
    void addConstraint_conditional_withEmptyConditions_shouldSucceedAsPrior() {
      assertDoesNotThrow(() -> builder.addConstraint(a.trueState(), List.of(), 0.2));
      ProbabilityConstraint constraint = builder.getConstraints().iterator().next();
      assertInstanceOf(MarginalConstraint.class, constraint);
      assertInstanceOf(ValidatedMarginalConstraint.class, constraint);
    }

    @Test
    void addConstraint_conditionalOnSelf_shouldThrowException() {
      assertThrows(
          ConstraintValidationException.class,
          () -> builder.addConstraint(b.trueState(), List.of(a.trueState(), b.trueState()), 0.5));
    }

    @Test
    void addConstraint_nonLocalConstraint_shouldSucceed() {
      assertDoesNotThrow(() -> builder.addConstraint(a.trueState(), List.of(b.trueState()), 0.6));
      assertDoesNotThrow(() -> builder.build(solver));
    }

    @Test
    void addConstraint_manuallyBuilt_shouldSucceed() {
      buildTestConstraints()
          .forEach(constraint -> assertDoesNotThrow(() -> builder.addConstraint(constraint)));
      assertDoesNotThrow(() -> builder.build(solver));
    }

    List<ProbabilityConstraint> buildTestConstraints() {
      return List.of(
          new MarginalConstraint(a.falseState(), 0.4),
          new ConditionalConstraint(a.trueState(), List.of(b.trueState()), 0.6),
          new ConditionalConstraint(b.falseState(), List.of(a.falseState()), 0.3),
          new JointProbabilityConstraint(List.of(b.trueState(), c.trueState()), List.of(), 0.37));
    }

    @Test
    void addConstraints_manuallyBuilt_shouldSucceed() {
      assertDoesNotThrow(() -> builder.addConstraints(buildTestConstraints()));
      assertDoesNotThrow(() -> builder.build(solver));
    }

    @Test
    void addconstraint_sneakyUsingGetter_shouldThrowException() {
      List<ProbabilityConstraint> constraints = buildTestConstraints();
      builder.getConstraints().addAll(constraints);
      assertThrows(ConstraintValidationException.class, () -> builder.build(solver));
    }

    @Test
    void getConstraints_validConstraints_shouldMaintainSameOrder() {
      List<ProbabilityConstraint> testConstraints = buildTestConstraints();
      builder.addConstraints(testConstraints);
      assertSameConstraintOrder(builder.getConstraints(), testConstraints);
      BayesianNetwork network = builder.build(solver).orElseThrow();
      assertSameConstraintOrder(network.getConstraints(), testConstraints);
    }

    private void assertSameConstraintOrder(
        Collection<ProbabilityConstraint> queriedConstraints,
        List<ProbabilityConstraint> testConstraints) {
      Iterator<ProbabilityConstraint> ci = queriedConstraints.iterator();
      int idx = 0;
      while (ci.hasNext()) {
        ProbabilityConstraint expected = testConstraints.get(idx++);
        ProbabilityConstraint actual = ci.next();
        assertEquals(expected.getProbability(), actual.getProbability());
        assertEquals(expected.getEventStates(), actual.getEventStates());
        assertEquals(expected.getConditionStates(), actual.getConditionStates());
      }
    }
  }
}
