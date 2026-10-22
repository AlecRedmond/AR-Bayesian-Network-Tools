package io.github.alecredmond.internal.application.probabilitytables;

import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.base.ProbabilityTableData;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.TableFactoryData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JunctionTreeTableData extends ProbabilityTableData {
  @EqualsAndHashCode.Exclude private final ProbabilityVector backupVector;

  public JunctionTreeTableData(TableFactoryData tableFactoryData, ProbabilityVector backupVector) {
    super(tableFactoryData);
    this.backupVector = backupVector;
  }
}
