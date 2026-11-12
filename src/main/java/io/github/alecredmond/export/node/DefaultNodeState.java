package io.github.alecredmond.export.node;

import java.io.Serializable;
import java.util.Objects;

public final class DefaultNodeState implements NodeState {
  private final Node node;
  private final int position;
  private final String name;
  private final Serializable id;
  private final int hashCode;

  public DefaultNodeState(Node node, int position, String name, Serializable id) {
    this.node = node;
    this.position = position;
    this.name = name;
    this.id = id;
    this.hashCode = Objects.hash(node.getId(), node.getName(), id, name, position);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof DefaultNodeState that)) return false;
    return position == that.position
        && Objects.equals(name, that.name)
        && Objects.equals(id, that.id);
  }

  @Override
  public String toString() {
    return "DefaultNodeState{"
        + "position="
        + position
        + ", node = '"
        + node.getName()
        + "', name='"
        + name
        + '\''
        + ", id="
        + id
        + '}';
  }

  @Override
  public Serializable getId() {
    return id;
  }

  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public String getName() {
    return name;
  }

  public Node node() {
    return node;
  }

  public int position() {
    return position;
  }

  public String name() {
    return name;
  }

  public Serializable id() {
    return id;
  }
}
