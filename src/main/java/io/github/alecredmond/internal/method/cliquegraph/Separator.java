package io.github.alecredmond.internal.method.cliquegraph;

public interface Separator extends InferenceTable {
  void passMessageFrom(Clique start);
}
