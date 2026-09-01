package io.github.alecredmond.export.nodedef.base;

import io.github.alecredmond.export.node.NodeState;
import java.io.Serializable;
import java.util.UUID;

import io.github.alecredmond.export.nodedef.NodeDef;
import io.github.alecredmond.export.nodedef.StateDef;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StateDefImpl implements StateDef {
  @EqualsAndHashCode.Include @ToString.Include protected final Serializable id;
  @EqualsAndHashCode.Include protected final NodeDef nodeDef;
  @ToString.Include protected String name;
  protected NodeState nodeState;

  public StateDefImpl(Serializable id, NodeDef nodeDef) {
    this.id = id;
    this.nodeDef = nodeDef;
  }

  public StateDefImpl(NodeDef nodeDef) {
    this.id = UUID.randomUUID();
    this.nodeDef = nodeDef;
  }

  public void setName(String name) {
    this.name = name;
    nodeDef.updateNode();
  }
}
