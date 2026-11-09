package io.github.alecredmond.export.inference;

public interface Separator extends InferenceTable {
  void passMessageFrom(Clique start);
}
