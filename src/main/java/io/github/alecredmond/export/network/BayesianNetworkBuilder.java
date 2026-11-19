package io.github.alecredmond.export.network;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.network.cptdefinition.CptDefinition;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.ConstraintFitter;
import io.github.alecredmond.export.solver.DirectCptWriter;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.export.solver.IPFPSolver;
import io.github.alecredmond.export.solver.MultiStageSolver;
import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Defines and builds a {@link BayesianNetwork}.
 *
 * <p>A builder is created with {@link BayesianNetwork#builder()}. The network is described by
 * setting its name and nodes, by defining the conditional probability tables (CPTs) of its nodes,
 * and by adding {@link ProbabilityConstraint}s. The network is then created with {@link #build()}
 * or {@link #build(ConstraintFitter)}.
 *
 * <p>Each method that configures the builder returns the builder itself, so calls can be chained.
 *
 * @author Alec Redmond
 * @see BayesianNetwork
 * @see CptDefinition
 * @see ConstraintFitter
 */
public interface BayesianNetworkBuilder {

  /**
   * Sets the name of the network to be built.
   *
   * @param networkName the name of the network
   * @return this builder for method chaining
   */
  BayesianNetworkBuilder setNetworkName(String networkName);

  /**
   * Builds the network using the default {@link ConstraintFitter}. This is equivalent to calling
   * {@link #build(ConstraintFitter)} with a {@link MultiStageSolver} as the argument. The
   * multi-stage procedure first attempts direct CPT imputation (entering all {@link
   * ProbabilityConstraint}s as CPT entries), with a backup Iterative Proportional Fitting Procedure
   * (IPFP) if CPT imputation fails. This is useful in situations where CPT imputation would be
   * preferred but its viability cannot be guaranteed. In circumstances where CPT imputation is
   * known to be inviable, it is preferable to directly use an {@link IPFPSolver} (such as {@link
   * IPFPJunctionTreeSolver}).
   *
   * @return an {@link Optional} containing the new {@link BayesianNetwork}, or an empty {@code
   *     Optional} if the network could not be built
   * @see #build(ConstraintFitter)
   */
  default Optional<BayesianNetwork> build() {
    return build(new MultiStageSolver());
  }

  /**
   * Builds the network using the specified {@link ConstraintFitter}.
   *
   * @param constraintFitter the constraint fitter to be used
   * @return an {@link Optional} containing the new {@link BayesianNetwork}, or an empty {@code
   *     Optional} if the network could not be built
   * @see DirectCptWriter
   * @see IPFPJunctionTreeSolver
   * @see MultiStageSolver
   */
  Optional<BayesianNetwork> build(ConstraintFitter constraintFitter);

  /**
   * Sets the nodes of the network to be built.
   *
   * @param nodes the nodes of the network
   * @return this builder for method chaining
   */
  BayesianNetworkBuilder setNodes(Node... nodes);

  /**
   * Sets the nodes of the network to be built.
   *
   * @param nodes the nodes of the network
   * @return this builder for method chaining
   */
  BayesianNetworkBuilder setNodes(Collection<Node> nodes);

  /**
   * Defines the conditional probability table (CPT) of a node from a user-supplied {@link
   * CptDefinition}. The node whose table is defined is the measured node of the definition.
   *
   * @param cptDefinition the user-defined CPT
   * @return this builder for method chaining
   * @see CptDefinition#measuredNode()
   */
  BayesianNetworkBuilder defineCPT(CptDefinition cptDefinition);

  // ----------------------------------------------------------------------------------------------
  // ----------------------------------CONSTRAINT ADDERS-------------------------------------------
  // ----------------------------------------------------------------------------------------------

  /**
   * Adds a new {@link MarginalConstraint} to the network to be built. During the solving process,
   * the solver will attempt to find a solution that minimizes the divergence from the constraints
   * assigned to it.
   *
   * <p>A {@code MarginalConstraint} is an unconditional constraint of the form {@code P(e) = p},
   * where:
   *
   * <ul>
   *   <li>{@code e} is the event {@link NodeState}.
   *   <li>{@code p} is the probability of the event.
   * </ul>
   *
   * <p>Constraints do not need to follow the network's graph ordering, {@code P(X|Pa(X))}. However,
   * constraints that span conditionally independent nodes cannot be losslessly projected back to
   * individual CPT entries, and may produce different inference results after fitting. Such
   * constraints may also add another virtual edge to the graph's structure during solving, which
   * can increase the treewidth.
   *
   * @param eventState the event node state in the constraint
   * @param probability the probability of the event
   * @return this builder for method chaining
   * @throws ConstraintValidationException if the constraint is in an illegal configuration
   */
  BayesianNetworkBuilder addConstraint(NodeState eventState, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to the network to be built. During the solving
   * process, the solver will attempt to find a solution that minimizes the divergence from the
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
   * @param eventState the event node state in the constraint
   * @param conditionState the condition node state in the constraint
   * @param probability the probability of the event given the condition
   * @return this builder for method chaining
   * @throws ConstraintValidationException if the constraint is in an illegal configuration
   */
  BayesianNetworkBuilder addConstraint(
      NodeState eventState, NodeState conditionState, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to the network to be built. During the solving
   * process, the solver will attempt to find a solution that minimizes the divergence from the
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
   * @param eventState the event node state in the constraint
   * @param conditionStates the condition node states in the constraint
   * @param probability the probability of the event given the conditions
   * @return this builder for method chaining
   * @throws ConstraintValidationException if the constraint is in an illegal configuration
   */
  BayesianNetworkBuilder addConstraint(
      NodeState eventState, Collection<NodeState> conditionStates, double probability);

  /**
   * Adds a new {@link ProbabilityConstraint} to the network to be built. During the solving
   * process, the solver will attempt to find a solution that minimizes the divergence from the
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
   * @param eventStates the event node states in the constraint
   * @param conditionStates the condition node states in the constraint
   * @param probability the probability of the events given the conditions
   * @return this builder for method chaining
   * @throws ConstraintValidationException if the constraint is in an illegal configuration
   */
  BayesianNetworkBuilder addConstraint(
      Collection<NodeState> eventStates, Collection<NodeState> conditionStates, double probability);

  /**
   * Adds a {@link ProbabilityConstraint} to the network to be built. During the solving process,
   * the solver will attempt to find a solution that minimizes the divergence from the constraints
   * assigned to it.
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
   * @param probabilityConstraint a constraint containing node states from the network to be built
   * @return this builder for method chaining
   * @throws ConstraintValidationException if the constraint is in an illegal configuration
   */
  BayesianNetworkBuilder addConstraint(ProbabilityConstraint probabilityConstraint);

  /**
   * Adds a collection of {@link ProbabilityConstraint}s to the network to be built. During the
   * solving process, the solver will attempt to find a solution that minimizes the divergence from
   * the constraints assigned to it.
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
   * @param probabilityConstraints the constraints to add
   * @return this builder for method chaining
   * @throws ConstraintValidationException if any constraint is in an illegal configuration
   */
  BayesianNetworkBuilder addConstraints(Collection<ProbabilityConstraint> probabilityConstraints);

  /**
   * Returns the {@link ProbabilityConstraint}s that have been added to this builder.
   *
   * @return the set of constraints added so far
   */
  Set<ProbabilityConstraint> getConstraints();

  /** Removes all {@link ProbabilityConstraint}s that have been added to this builder. */
  void clearConstraints();
}
