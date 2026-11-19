package io.github.alecredmond.export.probabilitytables;

/**
 * Represents a separator in the junction tree used for exact inference.
 *
 * <p>A separator is an {@link InferenceTable} of potentials over the nodes shared by two adjacent
 * {@link Clique}s. Cliques pass messages to one another across their separator.
 *
 * <p>Instances of this interface are not thread-safe. External synchronization is required for
 * concurrent access.
 *
 * @author Alec Redmond
 * @see Clique
 * @see InferenceTable
 */
public interface Separator extends InferenceTable {

  /**
   * Passes a message from the specified clique across this separator.
   *
   * @param start the clique that sends the message
   * @throws NullPointerException if this separator is not connected to the clique
   */
  void passMessageFrom(Clique start);
}
