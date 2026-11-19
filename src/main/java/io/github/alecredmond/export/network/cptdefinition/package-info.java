/**
 * Provides the classes for manually defining conditional probability tables (CPTs).
 *
 * <p>A {@link io.github.alecredmond.export.network.cptdefinition.CptDefinition} describes the CPT
 * of a single node from a flat array of probabilities. The enums {@link
 * io.github.alecredmond.export.network.cptdefinition.CptNodeOrder} and {@link
 * io.github.alecredmond.export.network.cptdefinition.CptStrideOrder} describe how that array is
 * ordered. A definition is passed to the network builder to set the CPT of a node.
 *
 * @see io.github.alecredmond.export.network.BayesianNetworkBuilder
 */
package io.github.alecredmond.export.network.cptdefinition;
