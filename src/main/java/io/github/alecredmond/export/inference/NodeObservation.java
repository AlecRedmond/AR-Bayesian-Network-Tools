package io.github.alecredmond.export.inference;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;

/**
 * Represents the observed state of a single {@link Node} within an {@link Observable} type. {@code
 * NodeObservation} tracks the current observed and eliminated {@link NodeState} values, and assigns
 * an {@link ObservationStatus} dependent on how many eliminated states there are on the given node.
 */
public class NodeObservation {
  /** The measured {@link Node} */
  private final Node node;

  /** All states within the {@link Node} which are set to be observable. */
  private final Set<NodeState> observedStates;

  /** All states within the {@link Node} which are set as eliminated. */
  private final Set<NodeState> eliminatedStates;

  /** The {@link ObservationStatus} of the {@link Node}. */
  private final ObservationStatus status;

  /**
   * Constructs a {@link NodeObservation} from the given inputs. This constructor is package-private
   * to ensure it is only called by factory methods.
   *
   * @param node The measured {@link Node}.
   * @param observedStates All states within the {@link Node} which are set to be observable.
   * @param eliminatedStates All states within the {@link Node} which are set as eliminated.
   * @param status The {@link ObservationStatus} of the {@link Node}.
   */
  NodeObservation(
      Node node,
      Set<NodeState> observedStates,
      Set<NodeState> eliminatedStates,
      ObservationStatus status) {
    this.node = node;
    this.observedStates = observedStates;
    this.eliminatedStates = eliminatedStates;
    this.status = status;
  }

  /**
   * Creates a new map keying each {@link Node} in a network to an {@link
   * ObservationStatus#UNOBSERVED} {@code NodeObservation}.
   *
   * @param network a {@link BayesianNetwork} instance.
   * @return a new, unmodifiable, {@link LinkedHashMap} in network node order, each paired to an
   *     {@code UNOBSERVED} {@code NodeObservation}, or an empty {@link Map} if the network is not
   *     solved.
   */
  public static Map<Node, NodeObservation> createUnobservedMap(BayesianNetwork network) {
    return NodeObservation.createUnobservedMap(network.getNetworkData());
  }

  /**
   * Creates a new map keying each {@link Node} in a network to an {@link
   * ObservationStatus#UNOBSERVED} {@code NodeObservation}.
   *
   * @param networkData a {@link BayesianNetworkData} instance.
   * @return a new, unmodifiable, {@link LinkedHashMap} in network node order, each paired to an
   *     {@code UNOBSERVED} {@code NodeObservation}, or an empty {@link HashMap} if the network is
   *     not solved.
   */
  public static Map<Node, NodeObservation> createUnobservedMap(BayesianNetworkData networkData) {
    if (!networkData.isSolved()) return Map.of();
    return new NodeObservationFactory().buildUnobservedNetwork(networkData.getNodes());
  }

  /**
   * Adds the declared observations to the existing map. Each {@link Node} represented by one or
   * more {@link NodeState}s in the collection will be remapped to a new {@link NodeObservation},
   * with those states set as observed.
   *
   * @param currentObservations the current observation map.
   * @param toObserve a collection of {@link NodeState} values to set as observed.
   * @return a new, unmodifiable, {@link LinkedHashMap}, maintaining the original order, with each
   *     {@link Node} keyed to its new {@code NodeObservation}.
   */
  public static Map<Node, NodeObservation> observe(
      Map<Node, NodeObservation> currentObservations, Collection<NodeState> toObserve) {
    return new NodeObservationFactory()
        .buildFromObservedStates(currentObservations, NodeUtils.generateMultiRequest(toObserve));
  }

  /**
   * Eliminates the declared {@link NodeState}s from the existing map. Each {@link Node} represented
   * by one or more {@link NodeState}s in the collection will be remapped to a new {@code
   * NodeObservation}, containing a union of the new and previous eliminated state sets.
   *
   * @param currentObservations the current observation map.
   * @param toEliminate a collection of {@link NodeState} values to set as eliminated.
   * @return a new, unmodifiable, {@link LinkedHashMap}, maintaining the original order, with each
   *     {@link Node} keyed to its new {@code NodeObservation}.
   */
  public static Map<Node, NodeObservation> eliminate(
      Map<Node, NodeObservation> currentObservations, Collection<NodeState> toEliminate) {
    return new NodeObservationFactory()
        .buildFromEliminatedStates(
            currentObservations, NodeUtils.generateMultiRequest(toEliminate));
  }

  /**
   * Returns the measured {@link Node} in this {@code NodeObservation}.
   *
   * @return the measured {@link Node}.
   */
  public Node node() {
    return node;
  }

  /**
   * Returns all {@link NodeState}s within the {@link Node} which are set to be observable.
   *
   * @return an unmodifiable {@link LinkedHashSet} of the observed states in this {@code
   *     NodeObservation}.
   */
  public Set<NodeState> observedStates() {
    return observedStates;
  }

  /**
   * Returns all {@link NodeState}s within the {@link Node} which are set as eliminated.
   *
   * @return an unmodifiable {@link LinkedHashSet} of the eliminated states in this {@code
   *     NodeObservation}.
   */
  public Set<NodeState> eliminatedStates() {
    return eliminatedStates;
  }

  /**
   * Returns the {@link ObservationStatus} of this {@code NodeObservation}. This can take the
   * following values:
   *
   * <ul>
   *   <li>{@code UNOBSERVED}: All {@link NodeState}s associated with the {@link Node} are active
   *       within the {@link Observable}.
   *   <li>{@code PARTIALLY_OBSERVED}: Some {@link NodeState}s associated with the {@link Node} have
   *       been eliminated, but more than one active state remains.
   *   <li>{@code OBSERVED}: A single {@link NodeState} associated with the {@link Node} remains
   *       active, with all other states eliminated.
   *   <li>{@code NEGATED}: All {@link NodeState}s associated with the {@link Node} have been
   *       eliminated. This will set the joint probability of the entire {@link Observable} to 0.
   * </ul>
   *
   * @return the {@link ObservationStatus} of this {@code NodeObservation}.
   */
  public ObservationStatus status() {
    return status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(node, observedStates, eliminatedStates, status);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (NodeObservation) obj;
    return Objects.equals(this.node, that.node)
        && Objects.equals(this.observedStates, that.observedStates)
        && Objects.equals(this.eliminatedStates, that.eliminatedStates)
        && Objects.equals(this.status, that.status);
  }

  @Override
  public String toString() {
    return switch (status) {
      case UNOBSERVED -> "%s:%s".formatted(node, UNOBSERVED);
      case NEGATED -> "%s:%s".formatted(node, NEGATED);
      case OBSERVED -> "%s=%s".formatted(node, observedStates.iterator().next());
      case PARTIALLY_OBSERVED -> formatPartiallyObservedString();
    };
  }

  /**
   * Helper function to stringify a partially observed {@code NodeObservation}. This will take the
   * shorter of {@link #observedStates} or {@link #eliminatedStates}, and return either {@code $Node
   * == {$observed}} or {@code $Node != {eliminated}} respectively.
   *
   * @return the {@link String} representation of this partially-observed {@code NodeObservation}.
   */
  private String formatPartiallyObservedString() {
    String format;
    String stringStates;
    if (observedStates.size() >= eliminatedStates.size()) {
      format = "%s!={%s}";
      stringStates = NodeUtils.formatStatesToString(eliminatedStates);
    } else {
      format = "%s=={%s}";
      stringStates = NodeUtils.formatStatesToString(observedStates);
    }
    return format.formatted(node, stringStates);
  }
}
