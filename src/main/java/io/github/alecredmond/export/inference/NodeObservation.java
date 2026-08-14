package io.github.alecredmond.export.inference;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.Set;

public record NodeObservation(
    Node node,
    Set<NodeState> observedStates,
    Set<NodeState> eliminatedStates,
    ObservationStatus status) {

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
