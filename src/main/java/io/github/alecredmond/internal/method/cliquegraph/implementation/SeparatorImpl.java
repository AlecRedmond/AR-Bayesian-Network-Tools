package io.github.alecredmond.internal.method.cliquegraph.implementation;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cliquegraph.SeparatorData;
import io.github.alecredmond.internal.method.cliquegraph.Clique;
import io.github.alecredmond.internal.method.cliquegraph.Separator;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import java.util.Arrays;
import java.util.Set;

public class SeparatorImpl implements Separator {
  private final SeparatorData data;

  public SeparatorImpl(SeparatorData separatorData) {
    this.data = separatorData;
  }

  @Override
  public void passMessageFrom(Clique start) {
    data.getMessagePassers().get(start).transfer();
  }

  @Override
  public Set<Node> getNodes() {
    return data.getNodes();
  }

  @Override
  public void resetPotentials() {
    JunctionTreeTable table = data.getTable();
    double marginalised = 1.0 / table.getProbabilities().length;
    Arrays.fill(table.getProbabilities(), marginalised);
  }

  @Override
  public ProbabilityVector getVector() {
    return data.getTable().getVector();
  }
}
