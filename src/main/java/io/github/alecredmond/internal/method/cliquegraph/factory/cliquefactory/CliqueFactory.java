package io.github.alecredmond.internal.method.cliquegraph.factory.cliquefactory;

import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import java.util.Map;

public interface CliqueFactory {
  Map<Clique, CliqueData> buildCliques(GraphData graphData);
}
