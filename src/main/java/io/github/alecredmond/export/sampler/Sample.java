package io.github.alecredmond.export.sampler;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * A container for a unique sample generated during a {@link MonteCarloSampler} run. Each {@code Sample}
 * represents a particular combination of {@link NodeState} values (one per {@link Node} in a {@link
 * BayesianNetwork}) and the frequency with which that combination occurred during sampling. Each
 * {@code Sample} belongs to a specific {@link SampleCollection}, which aggregates the results of a
 * sampling run.
 *
 * @see SampleCollection
 * @see MonteCarloSampler
 * @author Alec Redmond
 */
public interface Sample {
  /**
   * Returns the sum of the counts of all distinct {@link Sample}s in the given collection.
   *
   * @param samples the collection of {@link Sample}s whose counts are to be summed.
   * @return the sum of the counts of all distinct {@code Sample} objects in {@code samples}.
   */
  static int countAll(Collection<Sample> samples) {
    return samples.stream().distinct().mapToInt(Sample::count).sum();
  }

  /**
   * Returns the number of times this {@code Sample} was selected during the sampling process.
   *
   * @return the number of times this {@code Sample} was selected.
   */
  int count();

  /**
   * Returns an array of all {@link NodeState} values represented by this {@code Sample}. The array
   * contains one {@link NodeState} from each {@link Node} in the associated {@link
   * BayesianNetwork}, ordered by network layer: root nodes appear first and leaf nodes appear last.
   *
   * @return an array of all {@link NodeState} values in this {@code Sample}, in topological order.
   */
  NodeState[] getAllStates();

  /**
   * Returns an array of the {@link NodeState} values represented by this {@code Sample}, restricted
   * to the currently displayed {@link Node}s.
   *
   * @return an array of the {@link NodeState} values associated with a currently displayed {@link
   *     Node} in this {@code Sample}.
   * @see #setDisplayedNodes(Collection)
   * @see #displayAllNodes()
   */
  NodeState[] getDisplayedStates();

  /**
   * Returns a collection of the {@link NodeState} values represented by this {@code Sample},
   * restricted to the currently displayed {@link Node}s. The type of collection returned is
   * determined by the given {@link Supplier}.
   *
   * @param <T> the declared return type; a {@link Collection} of {@link NodeState} values.
   * @param <S> the concrete collection type produced by {@code supplier}; must extend {@code T}.
   * @param supplier a {@link Supplier} that provides the collection instance to populate, for
   *     example {@code ArrayList::new}.
   * @return a collection of the {@link NodeState} values associated with the currently displayed
   *     {@link Node}s in this {@code Sample}.
   * @see #setDisplayedNodes(Collection)
   * @see #displayAllNodes()
   */
  <T extends Collection<NodeState>, S extends T> T getDisplayedStates(Supplier<S> supplier);

  /**
   * Resets the display restrictions on this {@code Sample}, returning to the default state where
   * every {@link Node} in the associated {@link BayesianNetwork} is displayed.
   *
   * @see #setDisplayedNodes(Collection)
   */
  void displayAllNodes();

  /**
   * Restricts the displayed {@link Node}s in this {@code Sample} to the given collection.
   * Subsequent calls to {@link #getDisplayedStates(Supplier)} and {@link #getDisplayedStates()}
   * will only return the {@link NodeState} values associated with the specified {@link Node}s. This
   * restriction can be reset by calling {@link #displayAllNodes()}.
   *
   * @param nodes the {@link Node}s in the {@link BayesianNetwork} to be displayed in this {@code
   *     Sample}.
   * @see #displayAllNodes()
   */
  void setDisplayedNodes(Collection<Node> nodes);

  /**
   * Returns {@code true} if this {@code Sample} contains all provided {@link NodeState}s, or {@code
   * false} otherwise.
   *
   * @param states a collection of {@link NodeState} objects to query.
   * @return {@code true} if this {@code Sample} contains all in {@code states}.
   */
  boolean containsAll(Collection<NodeState> states);
}
