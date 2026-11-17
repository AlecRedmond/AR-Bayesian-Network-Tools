package io.github.alecredmond.internal.method.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.tableentry.CptEntry;
import io.github.alecredmond.export.probabilitytables.tableentry.CptRow;
import io.github.alecredmond.internal.application.probabilitytables.base.SingleEventTableData;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.CptRowGenerator;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class NetworkTableBase<D extends SingleEventTableData>
    extends ProbabilityTableBase<D> implements NetworkTable {

  protected NetworkTableBase(D tableData) {
    super(tableData);
  }

  public Node getNetworkNode() {
    return tableData.getEventNode();
  }

  @Override
  public CptEntry[] getEntries() {
    return Arrays.stream(getRows())
        .map(CptRow::rowEntries)
        .flatMap(Collection::stream)
        .toArray(CptEntry[]::new);
  }

  @Override
  public CptRow[] getRows() {
    return new CptRowGenerator(this).buildRows();
  }
}
