package io.github.alecredmond.internal.method.network.changehandlers;

import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.constraints.NetworkConstraintHandler;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RemovedNodeChangeHandler implements NetworkChangeHandler {

  @Override
  public void applyChange(PropertyChangeEvent evt, BayesianNetworkData networkData) {
    networkData.setSolved(false);
    networkData.getNetworkTablesMap().clear();

    Node toRemove = (Node) evt.getOldValue();
    networkData.getNodeIDsMap().remove(toRemove.getId());
    networkData.getNodes().remove(toRemove);

    toRemove
        .getStates()
        .forEach(state -> networkData.getNodeStateIDsMap().remove(state.getId()));

    networkData
        .getNodeIDsMap()
        .values()
        .forEach(
            node -> {
              removeNode(node::setParents, node::getParents, toRemove);
              removeNode(node::setChildren, node::getChildren, toRemove);
            });

    NetworkConstraintHandler.removeConstraints(
        constraint -> constraint.getAllNodes().contains(toRemove), networkData);
  }

  private void removeNode(Consumer<List<Node>> setter, Supplier<List<Node>> getter, Node toRemove) {
    setter.accept(getter.get().stream().filter(n -> !n.equals(toRemove)).toList());
  }
}
