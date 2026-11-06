package io.github.alecredmond.internal.method.cliquegraph;

public interface Separator extends CliqueGraphTable {
  void passMessageFrom(Clique start);
}
