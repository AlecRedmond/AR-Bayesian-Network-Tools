package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.implementation.BinaryNode;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.networkscenario.ObservationPackage;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;
import java.util.List;

public class LopsidedNetwork extends CptDefinedScenario implements BinaryNodeScenario {
  /*
      V
     / \
    W   X
    |   |
    Y   |
     \ /
      Z
  */
  public final BinaryNode v;
  public final BinaryNode w;
  public final BinaryNode x;
  public final BinaryNode y;
  public final BinaryNode z;

  public LopsidedNetwork() {
    v = binaryNode("V");
    w = binaryNode("W", v);
    x = binaryNode("X", v);
    y = binaryNode("Y", w);
    z = binaryNode("Z", x, y);

    addHalfCpt(v, 0.70);
    addHalfCpt(w, 0.60, 0.25);
    addHalfCpt(x, 0.40, 0.90);
    addHalfCpt(y, 0.33, 0.33);
    addHalfCpt(z, 0.20, 0.40, 0.60, 0.80);
  }

  @Override
  public BinaryNode.StateOrder getBinaryNodeOrder() {
    return BinaryNode.StateOrder.FALSE_FIRST;
  }

  @Override
  public BayesSolver getDefaultSolver() {
    return new IPFPJunctionTreeSolver();
  }

    @Override
    public List<ObservationPackage> buildObservationPackages() {
        return List.of();
    }

  @Override
  protected String getNetworkName() {
    return "LOPSIDED NETWORK";
  }

  @Override
  public Node[] getOrderedNodes() {
    return new Node[] {v, w, x, y, z};
  }
}
