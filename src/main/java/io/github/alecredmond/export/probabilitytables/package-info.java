/**
 * Provides the probability tables used by Bayesian networks and inference engines.
 *
 * <p>A {@link io.github.alecredmond.export.probabilitytables.ProbabilityTable} maps combinations of
 * node states to probabilities, using a {@link
 * io.github.alecredmond.export.probabilitytables.ProbabilityVector} to index a flat array. The main
 * types of table are:
 *
 * <ul>
 *   <li>{@link io.github.alecredmond.export.probabilitytables.NetworkTable}, the conditional
 *       probability table of a node, which is either a {@link
 *       io.github.alecredmond.export.probabilitytables.ConditionalTable} or a {@link
 *       io.github.alecredmond.export.probabilitytables.RootNodeTable}
 *   <li>{@link io.github.alecredmond.export.probabilitytables.ObservedTable}, the prior or
 *       posterior distribution of a single node in an inference engine
 *   <li>{@link io.github.alecredmond.export.probabilitytables.InferenceTable}, the tables of
 *       potentials that an inference engine uses internally, which are either a {@link
 *       io.github.alecredmond.export.probabilitytables.Clique} or a {@link
 *       io.github.alecredmond.export.probabilitytables.Separator}
 * </ul>
 *
 * @see io.github.alecredmond.export.probabilitytables.tableentry
 */
package io.github.alecredmond.export.probabilitytables;
