package io.github.alecredmond.export.nodedef.cptdef;

public interface StrideOrderedCpt extends CptDef {
  AddressingOrder getAddressingOrder();

  enum AddressingOrder {
    LONGEST_STRIDE_FIRST,
    SHORTEST_STRIDE_FIRST
  }
}
