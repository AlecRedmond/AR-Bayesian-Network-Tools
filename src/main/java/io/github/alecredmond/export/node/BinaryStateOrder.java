package io.github.alecredmond.export.node;

/** Specifies which of the two states of a {@link BinaryNode} comes first in its list of states. */
public enum BinaryStateOrder {
  /** The {@code "FALSE"} state is at position 0 and the {@code "TRUE"} state is at position 1. */
  FALSE_FIRST,
  /** The {@code "TRUE"} state is at position 0 and the {@code "FALSE"} state is at position 1. */
  TRUE_FIRST
}
