package io.github.alecredmond.internal.method.cliquegraph.implementation;

import io.github.alecredmond.internal.application.cliquegraph.Clique;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataSolver;
import io.github.alecredmond.internal.application.cliquegraph.Separator;
import io.github.alecredmond.internal.method.cliquegraph.SolverGraph;
import io.github.alecredmond.internal.method.cliquegraph.externalwriters.SolverExternalWriter;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSolver;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SolverGraphImpl extends CliqueGraph<GraphDataSolver> implements SolverGraph {

  public SolverGraphImpl(GraphDataSolver data, SolverExternalWriter externalWriter) {
    super(data, externalWriter);
  }

  @Override
  public Map<Clique, List<ConstraintSolver>> getSolversPerClique() {
    return data.getSolversPerClique();
  }

  @Override
  public void writeTablesToNetwork() {
    externalWriter.writeToExternal();
  }

  @Override
  public void sumTransfer(Clique clique) {
    messagePasser.distributeMessages(clique);
  }

  @Override
  public void normalizeTables() {
    Arrays.stream(data.getCliques()).forEach(Clique::normalizeTable);
    Arrays.stream(data.getSeparators()).forEach(Separator::resetSeparator);
  }
}
