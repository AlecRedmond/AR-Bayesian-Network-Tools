package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.TransferIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.OdometerResetOnlyOnBuild;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes.OdometerUpdateBlank;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class TransferReadWriteFactory<T extends TransferIterator>
    implements OdometerResetOnlyOnBuild, OdometerUpdateBlank {
  protected ProbabilityTable readTable;
  protected ProbabilityTable writeTable;
  protected double[] transferArray;
  protected Set<Node> commonNodes;

  protected TransferReadWriteFactory(ProbabilityTable readTable, ProbabilityTable writeTable) {
    super();
    this.readTable = readTable;
    this.writeTable = writeTable;
    this.commonNodes = TableUtils.getCommonNodes(readTable, writeTable);
    this.transferArray = new double[calculateTransferArrayLength()];
  }

  private int calculateTransferArrayLength() {
    return commonNodes.stream().map(node -> node.getStates().size()).reduce(1, (x, y) -> x * y);
  }

  protected TransferReadWriteFactory(
      ProbabilityTable readTable, ProbabilityTable writeTable, double[] transferArray) {
    this.readTable = readTable;
    this.writeTable = writeTable;
    this.commonNodes = TableUtils.getCommonNodes(readTable, writeTable);
    this.transferArray = transferArray;
  }

  public abstract T build();

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> !commonNodes.contains(node);
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return commonNodes::contains;
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return ResetLogicUtils.initializeToFirstNodeStates();
  }
}
