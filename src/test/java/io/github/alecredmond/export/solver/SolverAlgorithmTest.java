package io.github.alecredmond.export.solver;

import static io.github.alecredmond.export.solver.SolverAlgorithm.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class SolverAlgorithmTest {
  static final boolean SOLVER_TYPE_SPEED_CHECK = false;
  static final int RACE_TIMEOUT_SECS = 1;
  BayesianNetwork network;
  BayesSolver solver;
  Map<SolverAlgorithm, Boolean> useSolver = new EnumMap<>(SolverAlgorithm.class);

  @Test
  void checkSolverSpeeds() {
    if (!SOLVER_TYPE_SPEED_CHECK) return;
    network = BayesianNetwork.newNetwork("TEST SOLVER");
    solver = BayesSolver.create(network);
    useSolver.put(SINGLE_TABLE_IPFP, true);
    useSolver.put(JUNCTION_TREE_IPFP, true);
    Queue<Node> nodeQueue = new ArrayDeque<>();
    int numNodes = 0;
    while (numNodes < 64) {
      if (nodeQueue.isEmpty()) {
        initNodeQueue(nodeQueue, numNodes);
        numNodes++;
        continue;
      }
      boolean cont = checkSolve(numNodes);
      if (!cont) break;
      addTwoParents(nodeQueue, numNodes);
      numNodes += 2;
    }
    System.out.printf("FINAL COUNT WAS %s NODES IN %d SECONDS!", numNodes, RACE_TIMEOUT_SECS);
  }

  private void initNodeQueue(Queue<Node> nodeQueue, int numNodes) {
    String nodeId = String.valueOf(numNodes);
    network.addNewNode(nodeId, List.of(nodeId + ":TRUE", nodeId + ":FALSE"));
    network.addConstraint(nodeId + ":TRUE", 0.6);
    nodeQueue.add(network.getNodeById(nodeId));
  }

  private boolean checkSolve(int numNodes) {
    Duration timeJoint = timeSolution(SINGLE_TABLE_IPFP, numNodes);
    boolean timeJointNull = timeJoint == null;
    Duration timeJTA = timeSolution(JUNCTION_TREE_IPFP, numNodes);
    boolean timeJtaNull = timeJTA == null;
    if (timeJointNull || timeJtaNull) return !timeJtaNull;
    String faster = timeJTA.compareTo(timeJoint) > 0 ? "JOINT IPFP" : "JTA IPFP";
    System.out.printf("%d nodes, %s is faster%n", numNodes, faster);
    return true;
  }

  private void addTwoParents(Queue<Node> nodeQueue, int numNodes) {
    Node child = nodeQueue.poll();
    Node parentA = addParentToNet(numNodes);
    nodeQueue.add(parentA);
    Node parentB = addParentToNet(numNodes + 1);
    nodeQueue.add(parentB);
    assert child != null;
    child.addParent(parentA);
    child.addParent(parentB);
    network.addConstraint(
        child.getId() + ":TRUE",
        List.of(parentA.getId() + ":TRUE", parentB.getId() + ":TRUE"),
        0.75);
    network.addConstraint(
        child.getId() + ":TRUE",
        List.of(parentA.getId() + ":TRUE", parentB.getId() + ":FALSE"),
        0.66);
    network.addConstraint(
        child.getId() + ":TRUE",
        List.of(parentA.getId() + ":FALSE", parentB.getId() + ":TRUE"),
        0.45);
    network.addConstraint(
        child.getId() + ":TRUE",
        List.of(parentA.getId() + ":FALSE", parentB.getId() + ":FALSE"),
        0.20);
  }

  private Duration timeSolution(SolverAlgorithm solverAlgorithm, int numNodes) {
    if (!useSolver.get(solverAlgorithm)) return null;
    Instant now = Instant.now();
    solver.forceSolve(solverAlgorithm);
    Duration duration = Duration.between(now, Instant.now());
    if (duration.getSeconds() > RACE_TIMEOUT_SECS) {
      useSolver.put(solverAlgorithm, false);
      System.out.printf("TIMING OUT %s AT %d NODES!%n", solverAlgorithm, numNodes);
    }
    return duration;
  }

  private Node addParentToNet(int numNodes) {
    String nodeId = String.valueOf(numNodes);
    network.addNewNode(nodeId, List.of(nodeId + ":TRUE", nodeId + ":FALSE"));
    return network.getNodeById(nodeId);
  }
}
