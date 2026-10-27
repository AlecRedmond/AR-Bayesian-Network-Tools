package io.github.alecredmond.networkscenario.scenarios;

import static io.github.alecredmond.export.node.newnode.BinaryNode.*;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;

public class RainNetwork extends CptDefinedScenario implements BinaryNodeScenario {
  public static final String NETWORK_NAME = "RAIN NETWORK";
  // Node
  public final BinaryNode rain;
  public final BinaryNode sprinkler;
  public final BinaryNode wetGrass;

  public RainNetwork() {
    this.rain = binaryNode("RAIN");
    this.sprinkler = binaryNode("SPRINKLER", rain);
    this.wetGrass = binaryNode("WET_GRASS", rain, sprinkler);
    addCpt(rain, 0.2, 0.8);
    addCpt(sprinkler, 0.01, 0.99, 0.4, 0.6);
    addCpt(wetGrass, 0.99, 0.01, 0.9, 0.1, 0.9, 0.1, 0.0, 1.0);
  }

  @Override
  public StateOrder getBinaryNodeOrder() {
    return StateOrder.TRUE_FIRST;
  }

  @Override
  protected String getNetworkName() {
    return NETWORK_NAME;
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[] {rain, sprinkler, wetGrass};
  }
}
