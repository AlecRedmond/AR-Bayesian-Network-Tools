package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.exceptions.ProbabilityTableRequestException;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.internal.application.probabilitytables.ConditionalTableData;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.FastIndexFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.SafeConditionFinder;
import io.github.alecredmond.internal.method.cartesianvector.indexfinder.VectorIndexFinder;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.ConditionalMatrixGenerator;
import io.github.alecredmond.internal.method.probabilitytables.printerformat.PrinterMatrixGeneratorBase;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.ConditionalTableFactory;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.CptConditionIterator;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.TableNormalizer;
import java.io.Serializable;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ConditionalTableImpl extends NetworkTableBase<ConditionalTableData>
    implements ConditionalTable {
  private final TableNormalizer normalizer;
  private VectorIndexFinder<NodeState> conditionIndexFinder;

  public ConditionalTableImpl(ConditionalTableData tableData) {
    super(tableData);
    this.normalizer = new TableNormalizer(this);
  }

  @Override
  public void normalizeTable() {
    normalizer.normalize();
  }

  @Override
  public ConditionalTable copyTable() {
    return new ConditionalTableFactory().copyTable(this);
  }

  @Override
  public void setSafeMode(boolean safeMode) {
    super.setSafeMode(safeMode);
    this.conditionIndexFinder =
        safeMode
            ? SafeConditionFinder.create(tableData.getVector())
            : FastIndexFinder.create(tableData.getVector());
  }

  @Override
  protected PrinterMatrixGeneratorBase buildMatrixGenerator() {
    return new ConditionalMatrixGenerator(this);
  }

  @Override
  protected CptConditionIterator supplyConditionIterator() {
    return new CptConditionIterator(this);
  }

  @Override
  public <S extends Serializable> Map<NodeState, Double> getConditionalProbByIds(
      Collection<S> conditionIDs) {
    return getConditionalProb(getStates(conditionIDs));
  }

  @Override
  public Map<NodeState, Double> getConditionalProb(Collection<NodeState> conditionStates) {
    try {
      Map<NodeState, Double> map = new LinkedHashMap<>();
      double[] probabilities = tableData.getProbabilities();
      int index = conditionIndexFinder.getIndexFromStates(conditionStates);
      for (NodeState eventState : tableData.getEventNode().getStates()) {
        map.put(eventState, probabilities[index++]);
      }
      return map;
    } catch (ProbabilityTableRequestException e) {
      log.error(e.getMessage());
      return new HashMap<>();
    }
  }
}
