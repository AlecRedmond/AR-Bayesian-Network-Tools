package io.github.alecredmond.internal.method.probabilitytables.tablefactory;

import io.github.alecredmond.exceptions.TableBuilderException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.probabilitytables.JunctionTreeTableData;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTableImpl;
import java.util.*;

public class JunctionTreeTableFactory extends BaseTableFactory<JunctionTreeTable> {

  public JunctionTreeTable buildTable(Set<Node> events, BayesianNetwork network) {
    List<Node> orderedNodes = network.streamNodes().filter(events::contains).toList();
    return buildTable(orderedNodes, new ArrayList<>());
  }

  @Override
  public JunctionTreeTable copyTable(JunctionTreeTable table) {
    JunctionTreeTable copied = super.copyTable(table);
    double[] oldProbBackup = table.getBackupVector().getProbabilities();
    double[] newProbBackup = copied.getBackupVector().getProbabilities();
    System.arraycopy(oldProbBackup, 0, newProbBackup, 0, oldProbBackup.length);
    return copied;
  }

  @Override
  protected void performInstanceValidation(List<Node> events, List<Node> conditions) {
    if (!conditions.isEmpty()) {
      throw new TableBuilderException(
          "Attempted to build a joint table with conditions %s!".formatted(conditions));
    }
    if (events.isEmpty()) {
      throw new TableBuilderException("Attempted to build a joint table without events!");
    }
  }

  @Override
  protected JunctionTreeTable buildTableFromFactoryData(TableFactoryData tableFactoryData) {
    ProbabilityVector backupVector = copyProbabilityVector(tableFactoryData.getVector());
    return new JunctionTreeTableImpl(new JunctionTreeTableData(tableFactoryData, backupVector));
  }
}
