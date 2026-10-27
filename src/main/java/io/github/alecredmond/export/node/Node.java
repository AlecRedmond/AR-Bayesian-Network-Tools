package io.github.alecredmond.export.node;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.internal.application.cartesianvector.CartesianVariable;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a variable within a {@link BayesianNetwork}.
 *
 * <p>Each node contains a list of its {@link NodeState}s as well as its parents and children. A
 * {@link NodeState} is a state that a {@code Node} can exhibit, such as {@code "NODE:TRUE"} or
 * {@code "NODE:FALSE"}. A {@code Node} is conditionally dependent on its parents, while its
 * children are conditionally dependent on it.
 *
 * <p><b>Note:</b> All nodes in a network MUST be registered in the {@link BayesianNetwork} instance
 * before attempting to connect them to their parents or children through the methods defined here.
 *
 * @see NodeState
 * @see BayesianNetwork
 * @see ProbabilityConstraint
 * @author Alec Redmond
 */
public interface Node extends CartesianVariable {

  /**
   * Returns the unique identifier for this {@code Node}.
   *
   * @return the {@link Serializable} identifier of this node.
   */
  Serializable getId();

  String getName();

  /**
   * Returns the ordered list of {@link NodeState} values that this {@code Node} can exhibit.
   *
   * @return an unmodifiable {@link List} of the node's states.
   */
  @Override
  List<NodeState> getStates();

  /**
   * Returns the parent nodes of this {@code Node} in the network structure.
   *
   * @return an unmodifiable {@link List} of parent nodes.
   */
  List<Node> getParents();

  /**
   * Returns the child nodes of this {@code Node} in the network structure.
   *
   * @return an unmodifiable {@link List} of child nodes.
   */
  List<Node> getChildren();

  /**
   * Sets a list of {@link Node}s as the new children for this {@code Node}.
   *
   * @param children the new list of children connected to this {@code Node}.
   */
  void setChildren(List<Node> children);

  NetworkTable getCpt();

  void setCpt(NetworkTable cpt);

  NodeState getStateByName(String stateName);

  NodeState getStateById(Serializable stateId);
}
