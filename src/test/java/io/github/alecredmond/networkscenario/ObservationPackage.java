package io.github.alecredmond.networkscenario;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.util.Set;

public record ObservationPackage(
    Set<Node> exportNodes, Set<NodeState> observedStates, Set<NodeState> measuredStates) {}
