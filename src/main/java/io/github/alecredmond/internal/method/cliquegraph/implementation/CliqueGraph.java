package io.github.alecredmond.internal.method.cliquegraph.implementation;

import io.github.alecredmond.internal.application.cliquegraph.GraphData;
import io.github.alecredmond.internal.method.cliquegraph.externalwriters.ExternalTableWriter;
import io.github.alecredmond.internal.method.cliquegraph.messagepasser.MessagePasser;

public abstract class CliqueGraph<T extends GraphData> {
  protected final T data;
  protected final ExternalTableWriter externalWriter;
  protected final MessagePasser messagePasser;

  protected CliqueGraph(T data, ExternalTableWriter externalWriter) {
    this.data = data;
    this.externalWriter = externalWriter;
    this.messagePasser = new MessagePasser(data);
  }
}
