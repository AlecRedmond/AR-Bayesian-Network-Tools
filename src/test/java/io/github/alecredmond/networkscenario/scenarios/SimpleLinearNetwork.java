package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;

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
  protected String getNetworkName() {
    return "SIMPLE LINEAR NETWORK";
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[] {a, b, c, d};
  }
}
