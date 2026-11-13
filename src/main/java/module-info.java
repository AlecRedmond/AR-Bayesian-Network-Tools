/**
 * Defines the API for AR_BayesSolver, containing tools for construction, proportional fitting,
 * solving, and inference on Bayesian Networks.
 *
 * @author Alec Redmond
 */
module BayesSolver {
  requires java.datatransfer;
  requires static lombok;
  requires org.slf4j;
  requires java.desktop;
  requires java.sql;
  requires java.rmi;
  requires org.apache.commons.lang3;
  requires com.carrotsearch.hppc;

  exports io.github.alecredmond.exceptions;
  exports io.github.alecredmond.export.solver.constraint;
  exports io.github.alecredmond.export.solver.constraint.serialized;
  exports io.github.alecredmond.export.inference;
  exports io.github.alecredmond.export.network;
  exports io.github.alecredmond.export.network.cptdefinition;
  exports io.github.alecredmond.export.network.serialized;
  exports io.github.alecredmond.export.node;
  exports io.github.alecredmond.export.node.serialization;
  exports io.github.alecredmond.export.probabilitytables;
  exports io.github.alecredmond.export.probabilitytables.cptentry;
  exports io.github.alecredmond.export.probabilitytables.serialized;
  exports io.github.alecredmond.export.solver;
  exports io.github.alecredmond.export.solver.ipfp;
  exports io.github.alecredmond.export.inference.direct;
  exports io.github.alecredmond.export.inference.indirect;
  exports io.github.alecredmond.export.solver.directinput;
}
