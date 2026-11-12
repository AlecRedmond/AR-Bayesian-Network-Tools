package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.networkscenario.ObservationPackage;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;
import java.util.List;
import java.util.Set;

public class SimpleLinearNetwork extends CptDefinedScenario implements BinaryNodeScenario {
  public final BinaryNode a;
  public final BinaryNode b;
  public final BinaryNode c;
  public final BinaryNode d;

  public SimpleLinearNetwork() {
    a = binaryNode("A");
    b = binaryNode("B", a);
    c = binaryNode("C", b);
    d = binaryNode("D", c);

    addHalfCpt(a, 0.4);
    addHalfCpt(b, 0.8, 0.4);
    addHalfCpt(c, 0.9, 0.25);
    addHalfCpt(d, 0.6, 0.3);
  }

  @Override
  public BinaryNode.StateOrder getBinaryNodeOrder() {
    return BinaryNode.StateOrder.TRUE_FIRST;
  }

  @Override
  public List<ObservationPackage> buildObservationPackages() {
    return List.of(new ObservationPackage(Set.of(b, c), Set.of(), Set.of(d.trueState())));
  }

  @Override
  protected String getNetworkName() {
    return "SIMPLE LINEAR NETWORK";
  }

  @Override
  public Node[] getOrderedNodes() {
    return new Node[] {a, b, c, d};
  }
}
