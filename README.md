<div align="center">

# AR Bayesian Network Tools

### A Java library for Bayesian Network construction, proportional fitting, sampling, and inference.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR Bayesian Network Tools is a Java library providing a high-level, easy-use toolset for working with Bayesian
Networks.
Bayesian Networks can be constructed from either full or partial domain knowledge of the network's Conditional
Probability Tables (CPTs), and queried using direct inference or Monte Carlo sampling. Networks with only partial domain
knowledge are solved using an Iterative Proportional Fitting Procedure (IPFP). The solving and inference processes are
accelerated using the Junction Tree Algorithm by default, and high performance can be expected for networks into
the hundreds of nodes.

# Features

- Support for Discrete Bayesian Networks.
- Framework for implementing user-defined Bayesian nodes and discrete states.
- Smart probability table imputation can infer missing entries.
- Fit network from local or non-local constraints using fast, parallelized Iterative Proportional Fitting.
- Scalable direct inference using the Junction Tree Algorithm.
- Monte Carlo Sampling for indirect inference.
- Networks can be serialized, and saved to/loaded from the disk as binary files.

# Installation

## Maven

Add the latest release as a dependency in your pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.alecredmond</groupId>
        <artifactId>ar-bayes-solver</artifactId>
        <version>1.2.0</version>
    </dependency>
</dependencies>
```

And add BayesSolver as a requirement to your module-info:

```java
module MyNewProject {
    requires BayesSolver;
}
```

# Quick Start

<figure>
	<img src="https://i.imgur.com/p7yt72o.png" width="512" alt="Simple Bayesian Network diagram">
	<figcaption>
		<p><i>Fig. 1 - Simple Bayesian Network, <a href="https://commons.wikimedia.org/wiki/File:SimpleBayesNet.svg">src. Wikipedia</a></i></p>
	</figcaption>
</figure>
<br>

This demonstration will show how to build the Rain - Sprinkler - Wet Grass Bayesian Network (Fig. 1). We will create the
network, define its structure, add partial constraints, and then perform inference and sampling.

### 1. Creating a Network Builder

Use `BayesianNetwork.builder()` to begin structuring the network.

```Java
BayesianNetworkBuilder builder = BayesianNetwork.builder().setNetworkName("RAIN, SPRINKLER, WET GRASS");
```

### 2. Adding Nodes

Define the nodes and their parents. These Nodes use true/false states, so we can use the `BinaryNode` type:

```Java
BinaryStateOrder stateOrder = BinaryStateOrder.TRUE_FIRST; // How states 0 and 1 are arranged
// Rain has no parents
BinaryNode rain = new DefaultBinaryNode("RAIN", stateOrder);
// Sprinkler is conditional on rain
BinaryNode sprinkler = new DefaultBinaryNode("SPRINKLER", List.of(rain), stateOrder);
// Wet grass is conditional on rain and sprinkler
BinaryNode wetGrass = new DefaultBinaryNode("WET GRASS", List.of(rain, sprinkler), stateOrder);
// Set the nodes in the builder
builder.setNodes(rain, sprinkler, wetGrass);
```

### 3. Adding Constraints

Constraints are built from the states of each node. **Constraints do not have to be aligned with the
network structure**; for example, a marginal constraint can be defined on a node with parents, or an ancestor node's
state can be conditional on a descendant node's state.

For this demo, we will define CPT entries within the graph, but we will provide only the CPT entries from Fig.1. where
the node's measured state is `true`. The complementary constraints (e.g., `P(RAIN:FALSE) = 0.8`) are inferred
automatically and do not need to be defined.

```Java
builder
    .addConstraint(rain.trueState(), 0.2)
    .addConstraint(sprinkler.trueState(), rain.trueState(), 0.01)
    .addConstraint(sprinkler.trueState(), rain.falseState(), 0.4)
    .addConstraint(wetGrass.trueState(), List.of(rain.trueState(), sprinkler.trueState()), 0.99)
    .addConstraint(wetGrass.trueState(), List.of(rain.trueState(), sprinkler.falseState()), 0.8)
    .addConstraint(wetGrass.trueState(), List.of(rain.falseState(), sprinkler.trueState()), 0.9)
    .addConstraint(wetGrass.trueState(), List.of(rain.falseState(), sprinkler.falseState()), 0);
```

### 4. Defining CPTs manually

Alternatively, each CPT in the network can be defined using a flat double array.

```java
double[] rainCpt = new double[]{0.2, 0.8};
double[] sprinklerCpt = new double[]{0.01, 0.99, 0.4, 0.6};
double[] wetGrassCpt = new double[]{0.99, 0.01, 0.8, 0.2, 0.9, 0.1, 0.0, 1.0};
builder
    .defineCPT(new OrderedCptDefinition(rainCpt, rain))
    .defineCPT(new OrderedCptDefinition(sprinklerCpt, rain, sprinkler))
    .defineCPT(new OrderedCptDefinition(wetGrassCpt, rain, sprinkler, wetGrass));
```

The nodes are ordered in the constructor by descending stride length between node states.
In the last CPT, for example, `rain → sprinkler → wet grass` addresses the table as follows:

```
P(WET GRASS|RAIN, SPRINKLER)
---------------------------------------
|RAIN |SPRINKLER|WET GRASS|PROBABILITY|
|-----|---------|---------|-----------|
|TRUE |TRUE     |TRUE     |   0.990000|
|TRUE |TRUE     |FALSE    |   0.010000|
|TRUE |FALSE    |TRUE     |   0.800000|
|TRUE |FALSE    |FALSE    |   0.200000|
|FALSE|TRUE     |TRUE     |   0.900000|
|FALSE|TRUE     |FALSE    |   0.100000|
|FALSE|FALSE    |TRUE     |   0.000000|
|FALSE|FALSE    |FALSE    |   1.000000|
---------------------------------------
```

### 5. Fitting the Network

Once all the constraints are entered, the CPTs can be created.
If the constraints match every CPT entry, or every CPT entry can be inferred from the constraints,
a `DirectCptWriter` may be used:

```java
Optional<BayesianNetwork> optNetwork = builder.build(new DirectCptWriter());
```

For our network, this will succeed. If the network is only partially constrained, or non-local constraints
exist, an Iterative Proportional Fitting Procedure (IPFP) will be required. This provides a best fit for the CPTs
from the constraints supplied.

```java
Optional<BayesianNetwork> optNetwork = builder.build(new IPFPJunctionTreeSolver());
```

If unsure, we could use a `MultiStageSolver`, which attempts to write the CPTs directly, falling back to IPFP if that
fails:

```java
Optional<BayesianNetwork> optNetwork = builder.build(new MultiStageSolver());
```

### 6. Analyze the CPTs

Once the CPTs are fitted, we can read data from them:

```java
BayesianNetwork network = optNetwork.get();
NetworkTable sprinklerCPT = network.getNetworkTableByNodeName("SPRINKLER");
double pSprinklerFalseGivenRain = sprinklerCPT.getProbability(sprinkler.falseState(), rain.trueState());
System.out.printf("P(!S|R) = %.2f%n",pSprinklerFalseGivenRain);
```

```
>> P(!S|R) = 0.99
```

It is also possible to print all CPTs to a text file by calling `network.printNetwork();`
In our demonstration, this should automatically open a file which looks like this:

```
NETWORK TABLES

P(RAIN)
-------------------
|RAIN |PROBABILITY|
|-----|-----------|
|TRUE |   0.200000|
|FALSE|   0.800000|
-------------------

P(SPRINKLER|RAIN)
-----------------------------
|RAIN |SPRINKLER|PROBABILITY|
|-----|---------|-----------|
|TRUE |TRUE     |   0.010000|
|TRUE |FALSE    |   0.990000|
|-----|---------|-----------|
|FALSE|TRUE     |   0.400000|
|FALSE|FALSE    |   0.600000|
-----------------------------

P(WET GRASS|RAIN, SPRINKLER)
---------------------------------------
[etc...]
```

By default, the printer will save files to the directory ```$user_home$/AR_Tools/bayes_solver/output/```
Settings for the printer can be modified within `app.properties` under the section `app.bayes.printer`.

### 7. Running Direct Inference

For further analysis, we can create an `InferenceEngine`:

```java
InferenceEngine engine = new JunctionTreeAlgorithm(network);
```

We can give more complex queries to the engine. For example, let's apply the evidence `WET_GRASS = TRUE` and then
find the posterior probility of `RAIN = TRUE`:

```java
engine.setObserved(wetGrass.trueState());
double posteriorRainTrue = engine.getPosteriorProbability(rain.trueState());
System.out.printf("P(R|W) = %.3f%n",posteriorRainTrue);
```

```
>> P(R|W) = 0.358
```

We can see that the probability of rain goes from 20% to 35.8% when we observe the grass to be wet.

We can print the posterior probabilities to a text file by calling `engine.printObserved();`

```
OBSERVED

P(RAIN|WET GRASS=TRUE)
-------------------
|RAIN |PROBABILITY|
|-----|-----------|
|TRUE |   0.357688|
|FALSE|   0.642312|
-------------------

P(SPRINKLER|WET GRASS=TRUE)
-----------------------
|SPRINKLER|PROBABILITY|
|---------|-----------|
|TRUE     |   0.646728|
|FALSE    |   0.353272|
-----------------------
```

### 8. Generating Random Samples

Build a `MonteCarloSampler` from the BayesianNetwork instance:

```Java
MonteCarloSampler sampler = new LikelihoodWeightingSampler(network);
```

You can apply the observations from the inference engine to generate samples with certain fixed states:

```java
int numberOfSamples = 1000;

SampleCollection samples = sampler
        .setObserved(engine.getCurrentObservations())
        .generateSamples(numberOfSamples);

int samplesWithRainTrue = samples.countSamplesIncludingStates(rain.trueState());
    
System.out.println(samplesWithRainTrue);
```

```
>> 369
```

Samples contain a specific combination of node states and the frequency of its occurrence in the sampler run.
We can see that 369 out of our 1000 samples contained `RAIN = TRUE`, close to the true posterior probability
of 35.8%.

# API

[The API documentation can be found here.](https://www.javadoc.io/doc/io.github.alecredmond/ar-bayes-solver/latest/BayesSolver/module-summary.html) 