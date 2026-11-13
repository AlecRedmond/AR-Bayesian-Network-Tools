package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.node.serialization.DefaultBinaryNodeSerializer;
import io.github.alecredmond.export.node.serialization.DefaultNodeSerializer;
import io.github.alecredmond.export.node.serialization.NodeSerializer;
import java.util.function.Supplier;
import lombok.Getter;

public enum DefaultSerializerType {
  DEFAULT_NODE(DefaultNodeSerializer::new),
  DEFAULT_BINARY_NODE(DefaultBinaryNodeSerializer::new);

  @Getter final Supplier<NodeSerializer<?>> supplier;

  DefaultSerializerType(Supplier<NodeSerializer<?>> supplier) {
    this.supplier = supplier;
  }
}
