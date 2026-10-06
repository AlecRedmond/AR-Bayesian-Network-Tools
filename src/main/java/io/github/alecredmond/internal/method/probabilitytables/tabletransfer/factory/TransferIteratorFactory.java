package io.github.alecredmond.internal.method.probabilitytables.tabletransfer.factory;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.method.probabilitytables.TableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.TableTransfer;
import io.github.alecredmond.internal.method.probabilitytables.tabletransfer.readwriters.*;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.*;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransferIteratorFactory {
  public TableTransfer buildMessagePassTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable, ProbabilityTable separatorTable) {
    return buildCommon(
        readTable,
        writeTable,
        ((probabilities, transferArray, writeTableCachedIndexes) -> {
          double[] ratioArray = new double[transferArray.length];
          double[] separatorProbabilities = separatorTable.getProbabilities();
          return new TransferWriterMessagePass(
              probabilities,
              transferArray,
              writeTableCachedIndexes,
              ratioArray,
              separatorProbabilities);
        }));
  }

  private TableTransfer buildCommon(
      ProbabilityTable readTable, ProbabilityTable writeTable, WriterConstructor constructor) {
    CommonIndexCacher c = new CommonIndexCacher(readTable, writeTable);
    double[] transferArray = new double[calculateTransferArrayLength(c.commonNodes)];
    return new TableTransfer(
        new TransferReader(readTable.getProbabilities(), transferArray, c.cache(readTable)),
        constructor.build(writeTable.getProbabilities(), transferArray, c.cache(writeTable)));
  }

  private int calculateTransferArrayLength(Set<Node> commonNodes) {
    return commonNodes.stream().mapToInt(n -> n.getStates().size()).reduce(1, (a, b) -> a * b);
  }

  public TableTransfer buildMarginalTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    return buildCommon(readTable, writeTable, TransferWriterMarginal::new);
  }

  public TableTransfer buildMultiplyInTransfer(
      ProbabilityTable readTable, ProbabilityTable writeTable) {
    return buildCommon(readTable, writeTable, TransferWriterMultiplyIn::new);
  }

  @FunctionalInterface
  interface WriterConstructor {
    TransferWriter build(
        double[] probabilities, double[] transferArray, int[][] writeTableCachedIndexes);
  }

  record CommonIndexCacher(Set<Node> commonNodes) implements ProbabilityIteratorTemplate {
    CommonIndexCacher(ProbabilityTable readTable, ProbabilityTable writeTable) {
      this(TableUtils.getCommonNodes(readTable, writeTable));
    }

    public int[][] cache(ProbabilityTable table) {
        return StandardCartesianIterator.create(this, table.getVector()).cacheIndexesOverOuterRuns();
    }

    @Override
    public Function<Node, NodeState> initialStatePositionSetter() {
        return node -> node.getStates().getFirst();
    }

    @Override
    public Predicate<Node> checkLockOuter() {
      return node -> !commonNodes.contains(node);
    }

    @Override
    public Predicate<Node> checkLockInner() {
      return commonNodes::contains;
    }

    public Function<Node, boolean[]> updateEvidenceArrays() {
      return node -> null;
    }

  }
}
