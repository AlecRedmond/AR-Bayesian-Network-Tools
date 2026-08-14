package io.github.alecredmond.internal.method.sampler.picker;

import io.github.alecredmond.export.node.NodeState;

import java.util.Random;

public interface SamplePicker {
    Random RANDOM = new Random();

    double pick(NodeState[] sampleArray,double currentWeight);
}
