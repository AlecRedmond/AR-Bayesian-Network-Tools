package io.github.alecredmond.networkscenario.scenarios;

import static io.github.alecredmond.export.node.newnode.BinaryNode.StateOrder.*;

import io.github.alecredmond.export.network.cptdefinition.CptNodeOrder;
import io.github.alecredmond.export.network.cptdefinition.CptStrideOrder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.newnode.BinaryNode;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;

public class AsiaNetwork extends CptDefinedScenario implements BinaryNodeScenario {
  public final BinaryNode asiaVisit; // a
  public final BinaryNode smoking; // s
  public final BinaryNode tuberculosis; // t
  public final BinaryNode lungCancer; // l
  public final BinaryNode bronchitis; // b
  public final BinaryNode eitherTbOrCancer; // e
  public final BinaryNode positiveXRay;
  public final BinaryNode dyspnoea;

  public AsiaNetwork() {
    this.asiaVisit = binaryNode("ASIA_VISIT");
    this.smoking = binaryNode("SMOKING");
    this.tuberculosis = binaryNode("TUBERCULOSIS", asiaVisit);
    this.lungCancer = binaryNode("LUNG_CANCER", smoking);
    this.bronchitis = binaryNode("BRONCHITIS", smoking);
    this.eitherTbOrCancer = binaryNode("EITHER_TB_OR_CANCER", lungCancer, tuberculosis);
    this.positiveXRay = binaryNode("POSITIVE_X-RAY", eitherTbOrCancer);
    this.dyspnoea = binaryNode("DYSPNOEA", eitherTbOrCancer, bronchitis);

    addHalfCpt(asiaVisit, 0.1);
    addHalfCpt(smoking, 0.5);
    addHalfCpt(tuberculosis, 0.01, 0.05);
    addHalfCpt(lungCancer, 0.01, 0.10);
    addHalfCpt(bronchitis, 0.3, 0.6);
    addHalfCpt(eitherTbOrCancer, 0, 1, 1, 1);
    addHalfCpt(positiveXRay, 0.05, 0.98);
  }

  @Override
  public BinaryNode.StateOrder getBinaryNodeOrder() {
    return FALSE_FIRST;
  }

  @Override
  protected String getNetworkName() {
    return "VISIT_TO_ASIA";
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[] {
      asiaVisit,
      smoking,
      tuberculosis,
      lungCancer,
      bronchitis,
      eitherTbOrCancer,
      positiveXRay,
      dyspnoea
    };
  }
}
