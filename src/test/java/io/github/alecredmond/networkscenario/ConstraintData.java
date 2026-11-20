package io.github.alecredmond.networkscenario;

import io.github.alecredmond.export.node.NodeState;
import java.util.Collection;

public record ConstraintData(
    Collection<NodeState> events, Collection<NodeState> conditions, double probability) {}
