package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.direct.SingleTableAlgorithm;
import io.github.alecredmond.export.node.Node;
import java.util.Map;

/**
 * Represents a clique in the junction tree used for exact inference.
 *
 * <p>A clique is an {@link InferenceTable} of potentials over a group of {@link Node}s. Each clique
 * is linked to its neighboring cliques by {@link Separator}s, through which the cliques pass
 * messages to one another.
 *
 * <p>In {@link SingleTableAlgorithm}, there is only one clique which represents the joint
 * distribution of the entire network, with no separators.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see Separator
 * @see InferenceTable
 */
public interface Clique extends InferenceTable {

  /**
   * Applies the specified observations to the potentials of this clique.
   *
   * @param observationMap the active observation of each observed node
   */
  void applyPotentials(Map<Node, NodeObservation> observationMap);

  /**
   * Returns the separators that link this clique to its neighboring cliques.
   *
   * @return a map from each neighboring clique to the {@link Separator} between that clique and
   *     this clique
   */
  Map<Clique, Separator> getSeparatorMap();

  /**
   * Writes the potentials of this clique from the {@link NetworkTable}s of the Bayesian network.
   * This is used internally by {@link InferenceEngine} types. If calling this method externally,
   * ensure that it is applied to all cliques in the {@link InferenceEngine}, and call {@link
   * Separator#resetPotentials()} on all separators. This will reset the engine to its
   * pre-initialized state.
   */
  void writePotentialsFromNetwork();

  /** Normalizes the potentials of this clique so that they sum to 1. */
  void normalizeTable();

  /**
   * Exports the current potentials of this clique to the {@link ObservedTable}s which hold each
   * node's current marginal values. Called internally when any {@link InferenceEngine} observation
   * methods are called.
   */
  void exportPotentialsToTables();

  /**
   * Returns the index of this clique in its junction tree.
   *
   * @return the index of this clique
   */
  int getCliqueIndex();
}
