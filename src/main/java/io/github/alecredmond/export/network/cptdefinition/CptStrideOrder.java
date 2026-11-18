package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * The change in indexing stride length along the ordered {@link Node} array.
 *
 * <p>Consider 4 binary nodes {@code A,B,C,D} with True being state 0. The stride length (number of
 * index steps required to iterate the {@link NodeState}) of either the first or last {@link Node}
 * will always be 1. Therefore, as we step through each probability index it will iterate between
 * True/False on every iteration. Each subsequent {@link Node} can only iterate its current {@link
 * NodeState} when the previous finishes a cycle of its states.
 *
 * <p>For {@link #DESCENDING_STRIDE_LENGTH}, the stride ordering would be {@code {8,4,2,1}}. Such
 * that the first 8 indexes would be {@code D=true}, while {@code A=true} would occur on every even
 * index. {@link #ASCENDING_STRIDE_LENGTH} reverses the ordering, making it {@code {1,2,4,8}}.
 *
 * @see CptNodeOrder
 */
public enum CptStrideOrder {
  ASCENDING_STRIDE_LENGTH,
  DESCENDING_STRIDE_LENGTH
}
