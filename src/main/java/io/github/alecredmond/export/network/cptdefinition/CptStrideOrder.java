package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * Specifies how the indexing stride length changes along the ordered {@link Node} array of a {@link
 * CptDefinition}.
 *
 * <p>The stride length of a node is the number of index steps required to advance to its next
 * {@link NodeState}. Consider four binary nodes {@code A,B,C,D}, in that order, where {@code true}
 * is state 0. Either the first or the last node always has a stride length of 1, so as the
 * probability index is stepped, that node alternates between {@code true} and {@code false} on
 * every index. Each node with a longer stride advances to its next state only after the node with
 * the next shorter stride has completed a cycle of its states.
 *
 * <p>For {@link #DESCENDING_STRIDE_LENGTH}, the stride lengths along the array are {@code
 * {8,4,2,1}}. The first 8 indexes therefore have {@code A=true}, while {@code D=true} occurs on
 * every even index. {@link #ASCENDING_STRIDE_LENGTH} reverses the ordering, making the stride
 * lengths {@code {1,2,4,8}}.
 *
 * @author Alec Redmond
 * @see CptNodeOrder
 */
public enum CptStrideOrder {
  /** The stride lengths increase along the node array, for example {@code {1,2,4,8}}. */
  ASCENDING_STRIDE_LENGTH,
  /** The stride lengths decrease along the node array, for example {@code {8,4,2,1}}. */
  DESCENDING_STRIDE_LENGTH
}
