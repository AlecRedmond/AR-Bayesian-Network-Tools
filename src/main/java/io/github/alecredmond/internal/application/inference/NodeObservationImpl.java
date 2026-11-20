package io.github.alecredmond.internal.application.inference;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.inference.BayesianInference;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;

/**
 * Represents the observed state of a single {@link Node} within an {@link BayesianInference} type.
 * {@code NodeObservation} tracks the current observed and eliminated {@link NodeState} values, and
 * assigns an {@link ObservationStatus} dependent on how many eliminated states there are on the
 * given node.
 *
 * @param node The measured {@link Node}
 * @param observedStates All states within the {@link Node} which are set to be observable.
 * @param eliminatedStates All states within the {@link Node} which are set as eliminated.
 * @param status The {@link ObservationStatus} of the {@link Node}.
 */
public record NodeObservationImpl(
    Node node,
    Set<NodeState> observedStates,
    Set<NodeState> eliminatedStates,
    ObservationStatus status)
    implements NodeObservation {

  /**
   * Returns the measured {@link Node} in this {@code NodeObservation}.
   *
   * @return the measured {@link Node}.
   */
  @Override
  public Node node() {
    return node;
  }

  /**
   * Returns all {@link NodeState}s within the {@link Node} which are set to be observable.
   *
   * @return an unmodifiable {@link LinkedHashSet} of the observed states in this {@code
   *     NodeObservation}.
   */
  @Override
  public Set<NodeState> observedStates() {
    return observedStates;
  }

  /**
   * Returns all {@link NodeState}s within the {@link Node} which are set as eliminated.
   *
   * @return an unmodifiable {@link LinkedHashSet} of the eliminated states in this {@code
   *     NodeObservation}.
   */
  @Override
  public Set<NodeState> eliminatedStates() {
    return eliminatedStates;
  }

  /**
   * Returns the {@link ObservationStatus} of this {@code NodeObservation}. This can take the
   * following values:
   *
   * <ul>
   *   <li>{@code UNOBSERVED}: All {@link NodeState}s associated with the {@link Node} are active
   *       within the {@link BayesianInference}.
   *   <li>{@code PARTIALLY_OBSERVED}: Some {@link NodeState}s associated with the {@link Node} have
   *       been eliminated, but more than one active state remains.
   *   <li>{@code OBSERVED}: A single {@link NodeState} associated with the {@link Node} remains
   *       active, with all other states eliminated.
   *   <li>{@code NEGATED}: All {@link NodeState}s associated with the {@link Node} have been
   *       eliminated. This will set the joint probability of the entire {@link BayesianInference}
   *       to 0.
   * </ul>
   *
   * @return the {@link ObservationStatus} of this {@code NodeObservation}.
   */
  @Override
  public ObservationStatus status() {
    return status;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (NodeObservation) obj;
    return Objects.equals(this.node, that.node())
        && Objects.equals(this.observedStates, that.observedStates())
        && Objects.equals(this.eliminatedStates, that.eliminatedStates())
        && Objects.equals(this.status, that.status());
  }

  @Override
  public String toString() {
    return switch (status) {
      case UNOBSERVED -> "%s:%s".formatted(node.getName(), UNOBSERVED);
      case NEGATED -> "%s:%s".formatted(node.getName(), NEGATED);
      case OBSERVED ->
          "%s=%s".formatted(node.getName(), observedStates.iterator().next().getName());
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
      stringStates = CollectionToString.apply(eliminatedStates, NodeState::getName);
    } else {
      format = "%s=={%s}";
      stringStates = CollectionToString.apply(observedStates, NodeState::getName);
    }
    return format.formatted(node.getName(), stringStates);
  }
}
