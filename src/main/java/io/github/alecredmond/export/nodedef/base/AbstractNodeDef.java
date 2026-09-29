package io.github.alecredmond.export.nodedef.base;

import io.github.alecredmond.export.node.Node;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import io.github.alecredmond.export.nodedef.NodeDefinition;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
abstract class AbstractNodeDef implements NodeDefinition {
  @EqualsAndHashCode.Include protected final Serializable id;
  protected String name;
  @ToString.Exclude protected List<NodeDefinition> parentDefs;
  @ToString.Exclude protected Node node;

  protected AbstractNodeDef() {
    this.id = UUID.randomUUID();
  }

  protected <T extends Serializable> AbstractNodeDef(T id) {
    this.id = id;
  }

  @Override
  public Serializable getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
    updateNode();
  }

  @Override
  public List<NodeDefinition> getParentDefs() {
    return this.parentDefs;
  }

  @Override
  public Node getNode() {
    return this.node;
  }

  @Override
  public void setNode(Node node) {
    this.node = node;
    updateNode();
  }

  public void setParentDefs(List<NodeDefinition> parentDefs) {
    this.parentDefs = parentDefs;
    updateNode();
  }
}
