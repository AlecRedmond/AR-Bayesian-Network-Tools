/**
 * Provides the classes that convert nodes to and from a serializable form.
 *
 * <p>A {@link io.github.alecredmond.export.node.serialization.NodeSerializer} flattens a {@link
 * io.github.alecredmond.export.node.Node} into a {@link
 * io.github.alecredmond.export.node.serialization.SerializedNode} and reconstructs the node from
 * it. Each serializer handles one type of node. The default node types are handled by {@link
 * io.github.alecredmond.export.node.serialization.DefaultNodeSerializer} and {@link
 * io.github.alecredmond.export.node.serialization.DefaultBinaryNodeSerializer}.
 *
 * <p>Custom node types are supported by providing a serializer, usually by extending {@link
 * io.github.alecredmond.export.node.serialization.AbstractNodeSerializer}, and registering it with
 * {@link io.github.alecredmond.export.node.serialization.NodeSerializer#register}.
 *
 * @see io.github.alecredmond.export.node
 */
package io.github.alecredmond.export.node.serialization;
