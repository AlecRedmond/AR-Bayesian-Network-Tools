package io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory.SeparatorFactory;
import io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory.SeparatorFactorySolver;
import java.util.Map;
import java.util.Set;

public class CliqueFactoryJTASolver extends CliqueFactoryJTAInference {
  @Override
  protected SeparatorFactory getSeparatorFactory(GraphData graphData) {
    return new SeparatorFactorySolver(graphData);
  }

  @Override
  protected Map<Node, Set<Node>> buildEdgeGraph(GraphData graphData) {
    Map<Node, Set<Node>> edgeGraph = super.buildEdgeGraph(graphData);
    graphData.getNetwork().getConstraints().stream()
        .map(ProbabilityConstraint::getAllNodes)
        .distinct()
        .forEach(nodeSet -> nodeSet.forEach(node -> edgeGraph.get(node).addAll(nodeSet)));
    edgeGraph.forEach((node, connected) -> connected.remove(node));
    return edgeGraph;
  }
}
