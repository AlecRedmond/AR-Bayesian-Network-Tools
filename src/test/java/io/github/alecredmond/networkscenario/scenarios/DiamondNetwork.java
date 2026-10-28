package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.networkscenario.templates.AbstractNetworkScenario;
import java.util.List;

public class DiamondNetwork extends AbstractNetworkScenario {
  //   A
  //  / \
  // B   C
  //  \ /
  //   D
  public final Node a;
  public final Node b;
  public final Node c;
  public final Node d;

  public DiamondNetwork() {
    a = defaultNode("A", List.of("+", "-", "x"));
    b = defaultNode("B", List.of("+", "-", "x"), a);
    c = defaultNode("C", List.of("+", "-", "x"), a);
    d = defaultNode("D", List.of("++", "+-", "--", "x"), b, c);

    addConstraint(state(a, "-"), 0.05);
    addConstraint(state(c, "x"), List.of(state(a, "x")), 0.7);
    addConstraint(state(c, "+"), List.of(state(a, "x")), 0.025);
    addConstraint(state(c, "+"), List.of(state(a, "+")), 0.625);
    addConstraint(state(c, "+"), List.of(state(a, "-")), 0.68);
    addConstraint(state(b, "x"), List.of(state(a, "x")), 1.0);
    addConstraint(List.of(state(b, "+"), state(b, "-")), List.of(state(a, "+")), 1.0);
    addConstraint(state(c, "+"), List.of(state(d, "++")), 1.0);
    addConstraint(List.of(state(d, "++"), state(d, "+-")), 0.325);
  }

    @Override
    protected BayesSolver defaultSolver() {
        return new IPFPJunctionTreeSolver();
    }

  protected String getNetworkName() {
    return "DIAMOND NETWORK";
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[] {a, b, c, d};
  }
}
