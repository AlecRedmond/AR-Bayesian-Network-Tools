package io.github.alecredmond.networkscenario.scenarios;

import static io.github.alecredmond.export.node.implementation.BinaryNode.StateOrder.*;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.implementation.BinaryNode;
import io.github.alecredmond.networkscenario.ObservationPackage;
import io.github.alecredmond.networkscenario.templates.BinaryNodeScenario;
import io.github.alecredmond.networkscenario.templates.CptDefinedScenario;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AsiaNetwork extends CptDefinedScenario implements BinaryNodeScenario {
  /*
  a     s
  |    / \
  t   l   b
   \ /    |
    e    /
   /  \ /
  x    d
   */

  public final BinaryNode asiaVisit; // a
  public final BinaryNode smoking; // s
  public final BinaryNode tuberculosis; // t
  public final BinaryNode lungCancer; // l
  public final BinaryNode bronchitis; // b
  public final BinaryNode eitherTbOrCancer; // e
  public final BinaryNode positiveXRay; // x
  public final BinaryNode dyspnoea; // d

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
    addHalfCpt(dyspnoea, 0.10, 0.80, 0.70, 0.90);
  }

  @Override
  public BinaryNode.StateOrder getBinaryNodeOrder() {
    return FALSE_FIRST;
  }

  @Override
  public List<ObservationPackage> buildObservationPackages() {
    Set<Node> diseases = Set.of(tuberculosis, lungCancer, bronchitis);
    Set<NodeState> diseasesTrue =
        diseases.stream()
            .filter(BinaryNode.class::isInstance)
            .map(BinaryNode.class::cast)
            .map(BinaryNode::trueState)
            .collect(Collectors.toSet());

    List<Set<NodeState>> evidence =
        List.of(
            Set.of(
                asiaVisit.trueState(),
                smoking.trueState(),
                positiveXRay.trueState(),
                dyspnoea.trueState()),
            Set.of(
                asiaVisit.trueState(),
                smoking.trueState(),
                positiveXRay.trueState(),
                dyspnoea.falseState()));
    return evidence.stream().map(e -> new ObservationPackage(diseases, e, diseasesTrue)).toList();
  }

  @Override
  protected String getNetworkName() {
    return "VISIT_TO_ASIA";
  }

  @Override
  public Node[] getOrderedNodes() {
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
