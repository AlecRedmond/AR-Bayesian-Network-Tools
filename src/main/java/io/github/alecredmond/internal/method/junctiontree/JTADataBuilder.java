package io.github.alecredmond.internal.method.junctiontree;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.inference.InferenceAlgorithm;
import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.network.BayesianNetworkData;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.solver.SolverConfigs;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import io.github.alecredmond.internal.method.inference.NodeObservationFactory;
import io.github.alecredmond.internal.method.junctiontree.treebuilding.CliqueBuilder;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.ObservedTableBuilder;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory.TransferIteratorFactory;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@NoArgsConstructor
public class JTADataBuilder {

  public JunctionTreeData buildNewSolverConfiguration(
      BayesianNetworkData bayesianNetworkData, SolverConfigs configs) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    junctionTreeData.setSolverConfig(true);
    junctionTreeData.setSolverAlgorithm(configs.getSolverAlgorithm());
    buildCommon(junctionTreeData, bayesianNetworkData);
    buildSolversPerClique(junctionTreeData, bayesianNetworkData);
    logBuilt(bayesianNetworkData, "SOLVER", junctionTreeData);
    return junctionTreeData;
  }

  private void buildCommon(
      JunctionTreeData junctionTreeData, BayesianNetworkData bayesianNetworkData) {
    junctionTreeData.setNetworkData(bayesianNetworkData);
    new CliqueBuilder().buildCliques(junctionTreeData);
    buildExternalMessagePassers(junctionTreeData, bayesianNetworkData);
    buildCollectionDistributionPlaceHolders(junctionTreeData);
  }

  private void buildSolversPerClique(JunctionTreeData jtd, BayesianNetworkData bnd) {
    Collection<ProbabilityConstraint> constraints = bnd.getConstraints();
    Map<Clique, List<ConstraintSolver>> map =
        Arrays.stream(jtd.getCliques())
            .map(clique -> Map.entry(clique, matchConstraints(clique, constraints)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    jtd.setSolversPerClique(map);
  }

  private void logBuilt(BayesianNetworkData bnd, String type, JunctionTreeData jtd) {
    log.info(
        "NETWORK '{}' SUCCESSFULLY BUILT IN {} CONFIGURATION; No. CLIQUES {}; EQUIVALENT TREEWIDTH = 2^{}",
        bnd.getNetworkName(),
        type,
        jtd.getCliques().length,
        "%.2f".formatted(jtd.getEquivalentTreeWidth()));
  }

  private void buildExternalMessagePassers(JunctionTreeData jtd, BayesianNetworkData bnd) {
    TransferIteratorFactory iteratorFactory = new TransferIteratorFactory();
    Clique[] cliques = jtd.getCliques();
    Map<Node, NetworkTable> networkTables = bnd.getNetworkTablesMap();
    Map<Node, ObservedTable> observedTables = jtd.getObservedTablesMap();
    boolean writeBackToCPTs = jtd.isSolverConfig();

    for (Node node : bnd.getNodes()) {
      NetworkTable networkTable = networkTables.get(node);
      Clique bestClique = getContainsScope(cliques, networkTable.getNodes());
      JunctionTreeTable cliqueTable = bestClique.getTable();

      bestClique
          .getWriteFromCPTs()
          .add(iteratorFactory.buildMultiplyInTransfer(networkTable, cliqueTable));

      if (writeBackToCPTs) {
        bestClique
            .getWriteToCPTs()
            .add(iteratorFactory.buildMarginalTransfer(cliqueTable, networkTable));
      } else {
        bestClique
            .getWriteToObserved()
            .add(iteratorFactory.buildMarginalTransfer(cliqueTable, observedTables.get(node)));
      }
    }
  }

  private void buildCollectionDistributionPlaceHolders(JunctionTreeData jtd) {
    int cliquesLength = jtd.getCliques().length;
    jtd.setDistributionRuns(new Runnable[cliquesLength][]);
    jtd.setCollectionRuns(new Runnable[cliquesLength][]);
  }

  private List<ConstraintSolver> matchConstraints(
      Clique clique, Collection<ProbabilityConstraint> constraints) {
    ConstraintRegistry registry = new ConstraintRegistry();
    return constraints.stream()
        .filter(constraint -> clique.getNodes().containsAll(constraint.getAllNodes()))
        .map(constraint -> buildConstraintHandler(constraint, clique, registry))
        .toList();
  }

  private Clique getContainsScope(Clique[] cliques, Set<Node> nodesInScope) {
    return Arrays.stream(cliques)
        .filter(c -> c.getNodes().containsAll(nodesInScope))
        .min(Comparator.comparing(clique -> clique.getNodes().size()))
        .orElseThrow();
  }

  private <T extends ProbabilityConstraint> ConstraintSolver buildConstraintHandler(
      @NonNull T constraint, Clique clique, ConstraintRegistry registry) {
    return registry.getStrategy(constraint).buildSolverHandler(clique.getTable(), constraint);
  }

  public JunctionTreeData buildNewInferenceConfiguration(
      BayesianNetworkData bayesianNetworkData, InferenceAlgorithm inferenceAlgorithm) {
    JunctionTreeData junctionTreeData = new JunctionTreeData();
    buildInferenceConfiguration(junctionTreeData, bayesianNetworkData, inferenceAlgorithm);
    return junctionTreeData;
  }

  public void buildInferenceConfiguration(
      JunctionTreeData jtd, BayesianNetworkData bnd, InferenceAlgorithm inferenceAlgorithm) {
    jtd.setSolverConfig(false);
    jtd.setInferenceAlgorithm(inferenceAlgorithm);
    buildObserved(jtd, bnd);
    buildCommon(jtd, bnd);
    logBuilt(bnd, "INFERENCE", jtd);
  }

  private void buildObserved(JunctionTreeData jtd, BayesianNetworkData bnd) {
    ObservedTableBuilder builder = new ObservedTableBuilder();
    jtd.setObservedTablesMap(
        bnd.getNodes().stream()
            .map(node -> Map.entry(node, builder.buildTable(node)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

    NodeObservationFactory observationFactory = new NodeObservationFactory();
    Map<Node, NodeObservation> defaultObservations =
        observationFactory.buildUnobservedNetwork(bnd.getNodes());
    jtd.setObservedEvidence(defaultObservations);
    jtd.setUnobservedBackup(defaultObservations);
  }
}
