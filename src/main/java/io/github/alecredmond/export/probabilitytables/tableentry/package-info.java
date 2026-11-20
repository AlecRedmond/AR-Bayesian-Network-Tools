/**
 * Provides the classes that expose individual probability values of a table.
 *
 * <p>A {@link io.github.alecredmond.export.probabilitytables.tableentry.TableEntry} describes one
 * probability in a table, together with the states it measures and its index in the backing array.
 * A {@link io.github.alecredmond.export.probabilitytables.tableentry.CptEntry} adds the separation
 * of event state and condition states, and a {@link
 * io.github.alecredmond.export.probabilitytables.tableentry.CptRow} groups the entries that share
 * the same condition states.
 *
 * @see io.github.alecredmond.export.probabilitytables
 */
package io.github.alecredmond.export.probabilitytables.tableentry;
