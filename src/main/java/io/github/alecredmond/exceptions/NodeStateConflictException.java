package io.github.alecredmond.exceptions;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * Thrown when two or more {@link NodeState} objects share a common {@link Node} under circumstances
 * where each {@link Node} must be unique.
 */
public class NodeStateConflictException extends IllegalStateException {
  /**
   * Constructs a {@code NodeStateConflictException} with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause of the exception.
   */
  public NodeStateConflictException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a {@code NodeStateConflictException} with the specified detail message.
   *
   * @param message the detail message.
   */
  public NodeStateConflictException(String message) {
    super(message);
  }
}
