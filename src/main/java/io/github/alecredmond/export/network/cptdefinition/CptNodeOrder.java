package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * Specifies the position of the measured {@link Node} in the ordered array of nodes of a {@link
 * CptDefinition}.
 *
 * <p>A user-defined probability array for a CPT is assumed to be indexed with the measured (event)
 * node at either the start or the end of the node ordering. The remaining nodes are the parents of
 * the measured node, in the order returned by {@link Node#getParents()}. Whether the measured node
 * then has the longest or the shortest stride between its {@link NodeState}s depends on the {@link
 * CptStrideOrder}.
 *
 * @author Alec Redmond
 * @see CptStrideOrder
 */
public enum CptNodeOrder {
  /** The measured node is at the start of the ordering, followed by its parents. */
  EVENT_BEFORE_CONDITIONS,
  /** The measured node is at the end of the ordering, preceded by its parents. */
  CONDITIONS_BEFORE_EVENT
}
