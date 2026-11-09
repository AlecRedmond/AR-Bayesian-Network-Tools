package io.github.alecredmond.internal.method.cliquegraph.factory.separatorfactory;

import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.export.inference.Clique;
import io.github.alecredmond.export.inference.Separator;
import java.util.Map;

public interface SeparatorFactory {
  Separator buildSeparator(Clique cliqueA, Clique cliqueB, Map<Clique, CliqueData> cliqueDataMap);
}
