package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.*;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class NetworkStructureValidator implements NetworkValidator {
  public void checkValidRelationship(Node parent, Node child) {
    if (parent.equals(child)) {
      String e = String.format("Attempted to parent %s with itself!", child);
      throw new NetworkStructureException(e);
    }
    if (checkForLoops(parent, child)) {
      String e = String.format("Attempted to parent %s with its own ancestor %s", parent, child);
      throw new NetworkStructureException(e);
    }
  }

  private boolean checkForLoops(Node startNode, Node loopConfirm) {
    Set<Node> currentSet = new HashSet<>(startNode.getParents());
    while (!currentSet.isEmpty()) {
      if (currentSet.contains(loopConfirm)) {
        return true;
      }
      Set<Node> nextSet = new HashSet<>();
      currentSet.forEach(node -> nextSet.addAll(node.getParents()));
      currentSet = nextSet;
    }
    return false;
  }

  @Override
  public void validateData(BayesianNetworkData networkData) {
    Set<Node> remaining = new HashSet<>(networkData.getNodeIDsMap().values());
    Set<Node> visited = new HashSet<>();
    Set<Node> candidates = new HashSet<>();
    Queue<Node> queue = new ArrayDeque<>();
    remaining.stream()
        .filter(n -> n.getParents().isEmpty())
        .findAny()
        .map(queue::add)
        .orElseThrow(() -> new NetworkStructureException("NO NODES WITHOUT PARENTS!"));

    while (!queue.isEmpty()) {
      Node current = queue.poll();
      candidates.remove(current);
      remaining.remove(current);
      visited.add(current);
      Stream.concat(current.getParents().stream(), current.getChildren().stream())
          .filter(remaining::contains)
          .filter(candidates::add)
          .forEach(queue::add);
    }

    if (remaining.isEmpty()) return;
    throw new NetworkStructureException(
        "Unable to build Network data due to unconnected structure!%nCONNECTED: %s%nNO PATH: %s"
            .formatted(CollectionToString.apply(visited), CollectionToString.apply(remaining)));
  }

  public void checkExists(Node node, BayesianNetworkData networkData) {
    if (networkData.getNodeIDsMap().containsKey(node.getId())) {
      return;
    }
    throw new NetworkStructureException("Node %s does not exist!".formatted(node.getId()));
  }
}
