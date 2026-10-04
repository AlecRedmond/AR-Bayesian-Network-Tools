package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetOnlyOnBuild;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardIteratorFactory;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class TableNormalizer implements OdometerResetOnlyOnBuild {
  private final ProbabilityTable table;
  private final VectorIterator iterator;
  private final double[] adder = {0.0};

  public TableNormalizer(ProbabilityTable table) {
    this.table = table;
    this.iterator = StandardIteratorFactory.create(this, table.getVector());
  }

  public void normalize() {
    double[] probabilities = table.getProbabilities();
    iterator.iterateOuter(
        () -> {
          adder[0] = 0.0;
          iterator.iterateInner((o, i) -> adder[0] += probabilities[i]);
          double sum = adder[0];
          double ratio = sum == 0.0 ? 0.0 : 1.0 / sum;
          iterator.iterateInner((o, i) -> probabilities[i] = probabilities[i] * ratio);
        });
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return ResetLogicUtils.initializeToFirstNodeStates();
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

  @Override
  public UpdateStateArrayLogic updateConsumerType() {
    return UpdateStateArrayLogic.NO_UPDATE;
  }
}
