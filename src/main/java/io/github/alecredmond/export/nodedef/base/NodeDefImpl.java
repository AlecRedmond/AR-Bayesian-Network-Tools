package io.github.alecredmond.export.nodedef.base;

import io.github.alecredmond.export.nodedef.NodeDefinition;
import io.github.alecredmond.export.nodedef.StateDef;
import io.github.alecredmond.export.nodedef.attachedcpt.AttachedCpt;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(callSuper = true)
public class NodeDefImpl extends AbstractNodeDef implements NodeDefinition {
  protected List<StateDef> stateDefs;

  public NodeDefImpl() {
    super();
  }

  public <T extends Serializable> NodeDefImpl(T id) {
    super(id);
  }

  @Override
  public List<StateDef> getStateDefs() {
    return this.stateDefs;
  }

  public void setStateDefs(List<StateDef> stateDefs) {
    this.stateDefs = stateDefs;
    updateNode();
  }

  @Override
  public Optional<AttachedCpt> getAttachedCpt() {
    return Optional.empty();
  }

}
