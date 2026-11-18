package io.github.alecredmond.export.network.cptdefinition;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * The order of the {@link Node} array in a {@link CptDefinition}. An assumption is made that all
 * user-defined probability array for the CPT will be indexed with the measured (event) {@link Node}
 * at either the start or the end of the node ordering (therefore having either the longest or
 * shortest stride between {@link NodeState}s). {@link #EVENT_BEFORE_CONDITIONS} represents the
 * measured {@link Node} being present at the start of the ordered {@link Node} array, while {@link
 * #CONDITIONS_BEFORE_EVENT} represents it being at the end.
 *
 * @see CptStrideOrder
 */
public enum CptNodeOrder {
  EVENT_BEFORE_CONDITIONS,
  CONDITIONS_BEFORE_EVENT
}
