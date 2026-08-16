package io.github.alecredmond.export.inference;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NodeObservation {
  private final Node node;
  private final Set<NodeState> observedStates;
  private final Set<NodeState> eliminatedStates;
  private final ObservationStatus status;

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

  public static Map<Node, NodeObservation> createMap(BayesianNetwork network) {
    return NodeObservation.createMap(network.getNetworkData());
  }

  public static Map<Node, NodeObservation> createMap(BayesianNetworkData networkData) {
    return new NodeObservationFactory().buildUnobservedNetwork(networkData.getNodes());
  }

  public static Map<Node, NodeObservation> observe(
      Map<Node, NodeObservation> previousObservations, Collection<NodeState> toObserve) {
    return new NodeObservationFactory()
        .buildFromObservedStates(previousObservations, NodeUtils.generateMultiRequest(toObserve));
  }

  public static Map<Node, NodeObservation> eliminate(
      Map<Node, NodeObservation> previousObservations, Collection<NodeState> toEliminate) {
    return new NodeObservationFactory()
        .buildFromEliminatedStates(
            previousObservations, NodeUtils.generateMultiRequest(toEliminate));
  }

  public Node node() {
    return node;
  }

  public Set<NodeState> observedStates() {
    return observedStates;
  }

  public Set<NodeState> eliminatedStates() {
    return eliminatedStates;
  }

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
