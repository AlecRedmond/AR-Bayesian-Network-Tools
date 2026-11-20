package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import static io.github.alecredmond.export.inference.ObservationStatus.*;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.ObservationStatus;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.ProbabilityIteratorTemplate;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardCartesianIterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class ObservationCopier implements ProbabilityIteratorTemplate {
  private final ProbabilityVector mainVector;
  private final ProbabilityVector backupVector;
  private final StandardCartesianIterator iterator;
  private final Set<NodeState> requestStates;
  private final Set<Node> requestNodes;

  public ObservationCopier(JunctionTreeTable table) {
    this.backupVector = table.getBackupVector();
    this.mainVector = table.getVector();
    this.requestNodes = new HashSet<>();
    this.requestStates = new HashSet<>();
    this.iterator = StandardCartesianIterator.create(this, mainVector);
  }

  public void observeStates(List<NodeObservation> observations) {
    this.requestStates.clear();
    this.requestNodes.clear();
    switch (addObservationsAndCheckStatus(observations)) {
      case UNOBSERVED -> resetObservations();
      case OBSERVED, PARTIALLY_OBSERVED -> resetAndRunIterator();
      case NEGATED -> negateTable();
    }
  }

  private ObservationStatus addObservationsAndCheckStatus(List<NodeObservation> observations) {
    for (NodeObservation observation : observations) {
      ObservationStatus current = observation.status();
      if (current.equals(NEGATED)) return NEGATED;
      if (current.equals(UNOBSERVED)) continue;
      requestNodes.add(observation.node());
      requestStates.addAll(observation.observedStates());
    }
    if (requestNodes.isEmpty()) return UNOBSERVED;
    return requestNodes.size() == requestStates.size() ? OBSERVED : PARTIALLY_OBSERVED;
  }

  public void resetObservations() {
    this.requestStates.clear();
    this.requestNodes.clear();
    writeFromBackupVector();
  }

  private void resetAndRunIterator() {
    iterator.reset();

    double[] observed = mainVector.getProbabilities();
    double[] backup = backupVector.getProbabilities();
    Arrays.fill(observed, 0.0);
    BooleanSupplier evidenceCheck = iterator.getIsEvidenceCheck();
    iterator.iterateOuter(
        () -> {
          if (evidenceCheck.getAsBoolean()) {
            iterator.iterateInner(i -> observed[i] = backup[i]);
          }
        });
  }

  private void negateTable() {
    Arrays.fill(mainVector.getProbabilities(), 0);
  }

  private void writeFromBackupVector() {
    double[] backup = backupVector.getProbabilities();
    double[] observed = mainVector.getProbabilities();
    System.arraycopy(backup, 0, observed, 0, backup.length);
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node ->
        requestNodes.contains(node)
            ? node.getStates().stream()
                .filter(requestStates::contains)
                .findFirst()
                .orElse(node.getStates().getFirst())
            : node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> !requestNodes.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return requestNodes::contains;
  }

  @Override
  public Function<Node, boolean[]> updateEvidenceArrays() {
    return updateEvidenceArraysCommon(requestNodes, requestStates, Node::getStates);
  }
}
