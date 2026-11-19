package io.github.alecredmond.export.node;

import io.github.alecredmond.export.inference.BayesianInference;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
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
 * <p><b>Changing any data in a {@code Node} after building a {@link BayesianNetwork} will result in
 * undefined behavior on that network</b>, as well as any {@link BayesianInference} utilising that
 * network. {@code Node} instances should be treated as effectively final once {@link
 * BayesianNetworkBuilder#build()} is called.
 *
 * <p><b>Note:</b> Only the <i>parents</i> of a {@code Node} are processed when building a network
 * through {@link BayesianNetworkBuilder}. The children of the node will be automatically set during
 * the build process.
 *
 * @author Alec Redmond
 * @see DefaultNode
 * @see BinaryNode
 * @see NodeState
 */
public interface Node extends CartesianVariable {

  /**
   * Returns the unique identifier for this node.
   *
   * @return the {@link Serializable} identifier of this node.
   */
  Serializable getId();

  /**
   * Returns the name of this node.
   *
   * @return the name of this node
   */
  String getName();

  /**
   * Returns the states that this node can exhibit, in order.
   *
   * @return a {@link List} of the states of this node
   */
  @Override
  List<NodeState> getStates();

  /**
   * Returns the parent nodes of this node in the network structure.
   *
   * @return a {@link List} of the parent nodes of this node
   */
  List<Node> getParents();

  /**
   * Returns the children of this node in the network structure.
   *
   * @return a {@link List} of the child nodes of this node
   */
  List<Node> getChildren();

  /**
   * Replaces the children of this node with the specified nodes.
   *
   * @param children the new children of this node
   */
  void setChildren(List<Node> children);

  /**
   * Returns the state of this node that has the specified name.
   *
   * @param stateName the name of the requested state
   * @return the state with the specified name, or {@code null} if this node has no such state
   */
  NodeState getStateByName(String stateName);

  /**
   * Returns the state of this node that has the specified identifier.
   *
   * @param stateId the identifier of the requested state
   * @return the state with the specified identifier, or {@code null} if this node has no such state
   */
  NodeState getStateById(Serializable stateId);
}
