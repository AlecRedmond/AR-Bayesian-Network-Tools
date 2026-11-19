package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A table measuring the prior or posterior probability distribution over a single {@link Node}
 * within an {@link InferenceEngine}.
 *
 * <p>An {@code ObservedTable} contains one entry per {@link NodeState} of the measured node, each
 * recording {@code P(X=x|Obs)}, where {@code X} is the measured node, {@code x} is one of its
 * possible states, and {@code Obs} is the set of active observations on the engine.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see ProbabilityTable
 * @see InferenceEngine
 */
public interface ObservedTable extends CloneableTable<ObservedTable> {

  /**
   * Returns the single {@link Node} whose prior or posterior probabilities are measured by this
   * table.
   *
   * @return the {@code Node} that this table measures
   */
  Node getMeasuredNode();

  /**
   * Returns the observations currently active on the underlying {@link InferenceEngine}. The
   * posterior probabilities in this table are explicitly conditioned on these observations.
   *
   * @return an unmodifiable {@link LinkedHashMap} mapping each observed node to its active states,
   *     or an empty map if no observations are present
   */
  Map<Node, NodeObservation> getObservations();

  /**
   * Builds a copy of this {@code ObservedTable}. This deep-copies everything except the {@link
   * Node} and {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table
   */
  ObservedTable copyTable();

  /**
   * Returns the probability associated with a state of {@link #getMeasuredNode()} in the form
   * {@code P(X=x|Obs)}, where {@code X} is the measured node, {@code x} is one of its possible
   * states, and {@code Obs} is the set of observations currently active on the {@link
   * InferenceEngine}.
   *
   * <p><b>This method is affected by Safe Mode.</b> When Safe Mode is enabled, the given {@link
   * NodeState} must be in the state list of the measured node, otherwise {@code null} is returned.
   *
   * @param state the state of the measured node to be queried
   * @return the probability of the specified state, or {@code null} if Safe Mode validation fails
   */
  Double getProbability(NodeState state);

  /**
   * Returns a map of each {@link NodeState} of the measured {@link Node}, paired to its associated
   * probability value {@code P(X=x|Obs)}. The map is a {@link LinkedHashMap} that maintains the
   * same order as the {@code NodeState} list of the measured node.
   *
   * @return a new {@code LinkedHashMap} linking each state of the measured node to its probability
   */
  Map<NodeState, Double> buildProbabilityMap();
}
