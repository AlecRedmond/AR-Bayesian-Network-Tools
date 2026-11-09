package io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory;

import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.export.inference.Clique;
import java.util.Map;

public interface CliqueFactory {
  Map<Clique, CliqueData> buildCliques(GraphData graphData);
}
