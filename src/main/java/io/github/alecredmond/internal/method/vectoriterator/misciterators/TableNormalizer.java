package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class TableNormalizer implements ProbabilityIteratorTemplate {
  private final ProbabilityTable table;
  private final StandardCartesianIterator iterator;
  private final double[] adder = {0.0};

  public TableNormalizer(ProbabilityTable table) {
    this.table = table;
    this.iterator = StandardCartesianIterator.create(this, table.getVector());
  }

  public void normalize() {
    double[] probabilities = table.getProbabilities();
    iterator.iterateOuter(
        () -> {
          adder[0] = 0.0;
          iterator.iterateInner(i -> adder[0] += probabilities[i]);
          double sum = adder[0];
          double ratio = sum == 0.0 ? 0.0 : 1.0 / sum;
          iterator.iterateInner(i -> probabilities[i] = probabilities[i] * ratio);
        });
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node -> node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    Set<Node> events = table.getEvents();
    return events::contains;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    Set<Node> conditions = table.getConditions();
    return conditions::contains;
  }

  public Function<Node, boolean[]> updateEvidenceArrays() {
    return node -> null;
  }
}
