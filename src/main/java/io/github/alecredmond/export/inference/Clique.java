package io.github.alecredmond.export.inference;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.observable.NodeObservation;

import java.util.Map;

public interface Clique extends InferenceTable {

  void applyPotentials(Map<Node, NodeObservation> observationMap);

  Map<Clique, Separator> getSeparatorMap();

  void writePotentialsFromNetwork();

  void normalizeTable();

  void exportPotentialsToTables();

  int getCliqueIndex();
}
