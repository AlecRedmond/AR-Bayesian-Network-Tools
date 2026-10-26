package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;

public class AHNetwork extends CptDefinedScenario implements BinaryNodeScenario {
  // A   B   C
  //  \ / \ /
  //   D   E
  //  / \ / \
  // F   G   H
  public final BinaryNode a;
  public final BinaryNode b;
  public final BinaryNode c;
  public final BinaryNode d;
  public final BinaryNode e;
  public final BinaryNode f;
  public final BinaryNode g;
  public final BinaryNode h;

  public AHNetwork() {
    a = binaryNode("A");
    b = binaryNode("B");
    c = binaryNode("C");
    d = binaryNode("D", a, b);
    e = binaryNode("E", b, c);
    f = binaryNode("F", d);
    g = binaryNode("G", d, e);
    h = binaryNode("H", g);

    addHalfCpt(a, 0.80);
    addHalfCpt(b, 0.40);
    addHalfCpt(c, 0.55);
    addHalfCpt(d, 0.90, 0.00, 0.20, 0.00);
    addHalfCpt(e, 0.85, 0.05, 0.00, 0.00);
    addHalfCpt(f, 0.95, 0.30);
    addHalfCpt(g, 0.15, 0.40, 0.75, 0.60);
    addHalfCpt(h, 0.95, 0.55);
  }

  @Override
  public BinaryNode.StateOrder getBinaryNodeOrder() {
    return BinaryNode.StateOrder.TRUE_FIRST;
  }

  @Override
  protected String getNetworkName() {
    return "A-H Network";
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[0];
  }
}
