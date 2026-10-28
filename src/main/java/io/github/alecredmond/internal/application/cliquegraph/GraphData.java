package io.github.alecredmond.internal.application.cliquegraph;

import io.github.alecredmond.export.network.BayesianNetwork;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class GraphData {
  protected BayesianNetwork network;
  protected Clique[] cliques;
  protected Separator[] separators;
  protected double equivalentTreeWidth;
  protected Runnable[][] collectionRuns;
  protected Runnable[][] distributionRuns;
}
