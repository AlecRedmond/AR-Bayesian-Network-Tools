package io.github.alecredmond.internal.method.cliquegraph.implementation;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.cliquegraph.SeparatorData;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.inference.Separator;
import io.github.alecredmond.internal.method.probabilitytables.JunctionTreeTable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

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

  @Override
  public double sumPotentials(Collection<NodeState> states) {
    return data.getTable().sumProbabilities(states);
  }

  @Override
  public NodeState[] getStatesFromIndex(int index) {
    return data.getTable().getStatesFromIndex(index);
  }

  @Override
  public <T extends Collection<NodeState>, U extends T> T getStatesFromIndex(
      int index, Supplier<U> collectionSupplier) {
    return data.getTable().getStatesFromIndex(index, collectionSupplier);
  }
}
