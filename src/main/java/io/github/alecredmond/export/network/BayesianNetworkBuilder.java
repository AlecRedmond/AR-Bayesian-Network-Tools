package io.github.alecredmond.export.network;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.MarginalConstraint;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.cptdefinition.CptDefinition;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.BayesSolver;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface BayesianNetworkBuilder {

  BayesianNetworkBuilder setNetworkName(String networkName);

  Optional<BayesianNetwork> build(BayesSolver solver);

  BayesianNetworkBuilder setNodes(Node... nodes);

  BayesianNetworkBuilder setNodes(Collection<Node> nodes);

  BayesianNetworkBuilder defineCPT(CptDefinition cptDefinition);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT ADDERS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Adds a new {@link MarginalConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>{@link MarginalConstraint}s are unconditional constraints given in the form {@code P(e) =
   * p}, where:
   *
   * <ul>
   *   <li>{@code e} is the event {@link NodeState}.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventState the event {@link NodeState} in the constraint.
   * @param probability the probability of the events given the conditions.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  BayesianNetworkBuilder addConstraint(NodeState eventState, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventState the event {@link NodeState} in the constraint.
   * @param conditionState the condition {@link NodeState} in the constraint.
   * @param probability the probability of the events given the conditions.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  BayesianNetworkBuilder addConstraint(
      NodeState eventState, NodeState conditionState, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventState the event {@link NodeState} in the constraint.
   * @param conditionStates the condition {@link NodeState}s in the constraint.
   * @param probability the probability of the events given the conditions.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  BayesianNetworkBuilder addConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventStates the event {@link NodeState}s in the constraint.
   * @param conditionStates the condition {@link NodeState}s in the constraint.
   * @param probability the probability of the events given the conditions.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  BayesianNetworkBuilder addConstraint(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates, double probability);

  /**
   * Adds a {@link ProbabilityConstraint} to this {@code BayesianNetwork}. During the solving
   * process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param probabilityConstraint a {@link ProbabilityConstraint} object containing {@link
   *     NodeState}s from this {@code BayesianNetwork}.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   * @return this instance for method chaining
   */
  BayesianNetworkBuilder addConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Adds a collection of {@link ProbabilityConstraint}s to this {@code BayesianNetwork}. During the
   * solving process, the solver will attempt to find a solution minimizing the divergence from the
   * constraints assigned to it.
   *
   * <p>Constraints are given in the form {@code P(E|C) = p}, where:
   *
   * <ul>
   *   <li>{@code E} are the event {@link NodeState}s.
   *   <li>{@code C} are the condition {@link NodeState}s.
   *   <li>{@code p} is the conditional probability.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param probabilityConstraints a collection of {@link ProbabilityConstraint}s to be added.
   * @return this instance for method chaining.
   * @throws ConstraintValidationException if any {@link ProbabilityConstraint} is in an illegal
   *     configuration.
   */
  BayesianNetworkBuilder addConstraints(Collection<ProbabilityConstraint> probabilityConstraints);

  Set<ProbabilityConstraint> getConstraints();

  void clearConstraints();
}
