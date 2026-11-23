package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.export.node.BinaryStateOrder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.networkscenario.ObservationPackage;
import io.github.alecredmond.networkscenario.templates.AbstractNetworkScenario;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import java.util.List;

/**
 * Scenario from 'Modifying Bayesian Networks By Probability Constraints' by Yun Peng and Zhongli
 * Ding. From figure 1.
 */
public class PengAndDingDiamond extends AbstractNetworkScenario implements BinaryNodeScenario {
  public final BinaryNode a;
  public final BinaryNode b;
  public final BinaryNode c;
  public final BinaryNode d;

  public PengAndDingDiamond() {
    a = binaryNode("A");
    b = binaryNode("B", a);
    c = binaryNode("C", a);
    d = binaryNode("D", b, c);
    addConstraint(a.trueState(), 0.4);

    addConstraint(b.trueState(), List.of(a.trueState()), 0.6);
    addConstraint(b.trueState(), List.of(a.falseState()), 0.35);

    addConstraint(c.trueState(), List.of(a.trueState()), 0.6);
    addConstraint(c.trueState(), List.of(a.falseState()), 0.35);

    addConstraint(d.trueState(), List.of(b.trueState(), c.trueState()), 0.10);
    addConstraint(d.trueState(), List.of(b.trueState(), c.falseState()), 0.85);
    addConstraint(d.trueState(), List.of(b.falseState(), c.trueState()), 0.45);
    addConstraint(d.trueState(), List.of(b.falseState(), c.falseState()), 0.70);
  }

  @Override
  public BinaryStateOrder getBinaryNodeOrder() {
    return BinaryStateOrder.TRUE_FIRST;
  }

  @Override
  public List<ObservationPackage> buildObservationPackages() {
    return List.of();
  }

  @Override
  protected String getNetworkName() {
    return "PENG_AND_DING_DIAMOND";
  }

  @Override
  public Node[] getOrderedNodes() {
    return new Node[] {a, b, c, d};
  }
}
