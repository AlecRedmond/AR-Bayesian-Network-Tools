package io.github.alecredmond.export.probabilitytables;

public interface Separator extends InferenceTable {
  void passMessageFrom(Clique start);
}
