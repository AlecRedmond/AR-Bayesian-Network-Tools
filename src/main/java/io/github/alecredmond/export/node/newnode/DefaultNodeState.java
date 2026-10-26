package io.github.alecredmond.export.node.newnode;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.Objects;

public record DefaultNodeState(Node node, int position, String name, Serializable id)
    implements NodeState {
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof DefaultNodeState that)) return false;
    return position == that.position
        && Objects.equals(name, that.name)
        && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, name, id);
  }

  @Override
  public String toString() {
    return "DefaultNodeState{"
        + "position="
        + position
        + ", name='"
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
}
