package io.github.alecredmond.export.nodedef.base;

import io.github.alecredmond.export.nodedef.DiscreteNodeDef;
import io.github.alecredmond.export.nodedef.StateDef;
import io.github.alecredmond.export.nodedef.cptdef.CptDef;
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
public class DiscreteNodeDefImpl extends AbstractNodeDef implements DiscreteNodeDef {
  protected List<StateDef> stateDefs;

  public DiscreteNodeDefImpl() {
    super();
  }

  public <T extends Serializable> DiscreteNodeDefImpl(T id) {
    super(id);
  }

  @Override
  public List<StateDef> getStateDefs() {
    return this.stateDefs;
  }

  @Override
  public Optional<CptDef> getCptDef() {
    return Optional.empty();
  }

  public void setStateDefs(List<StateDef> stateDefs) {
    this.stateDefs = stateDefs;
    updateNode();
  }
}
