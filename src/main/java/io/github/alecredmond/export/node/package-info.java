/**
 * Provides the nodes and node states that make up a Bayesian network.
 *
 * <p>A {@link io.github.alecredmond.export.node.Node} represents a variable in the network and
 * holds its {@link io.github.alecredmond.export.node.NodeState}s, parents, and children. A {@link
 * io.github.alecredmond.export.node.BinaryNode} is a node with exactly two states, {@code "TRUE"}
 * and {@code "FALSE"}.
 *
 * <p>The classes {@link io.github.alecredmond.export.node.DefaultNode}, {@link
 * io.github.alecredmond.export.node.DefaultBinaryNode}, and {@link
 * io.github.alecredmond.export.node.DefaultNodeState} are the default implementations of these
 * interfaces.
 *
 * @see io.github.alecredmond.export.node.serialization
 */
package io.github.alecredmond.export.node;
