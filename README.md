<div align="center">

# AR Bayesian Network Tools

### A Java library for Bayesian Network construction, proportional fitting, sampling, and inference.

![Java](https://img.shields.io/badge/Java-21+-orange)

</div>

# Overview

AR Bayesian Network Tools is a Java library providing a high-level, easy-use toolset for working with Bayesian Networks. 
Bayesian Networks can be constructed from either full or partial domain knowledge of the network's Conditional 
Probability Tables (CPTs), and queried using direct inference or Monte Carlo sampling. Networks with only partial domain 
knowledge are solved using an Iterative Proportional Fitting Procedure (IPFP). The solving and inference processes are 
accelerated using the Junction Tree Algorithm by default, and high performance can be expected for networks into 
the low hundreds of nodes.

# Features

- Simple API for constructing Bayesian Network structures.
- Allows full CPT imputation or CPT estimation from a partially-constrained network.
- Support for probability constraints that are independent of the network's parent/child structure.
- Perform direct probabilistic inference to query prior or posterior probabilities.
- Generate random samples, with or without fixed observations.

# Installation

## Maven

Add the latest release as a dependency in your pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.alecredmond</groupId>
        <artifactId>ar-bayes-solver</artifactId>
        <version>1.1.2</version>
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

### 1. Create a Network

Most interactions use the `BayesianNetwork` interface.

```Java
BayesianNetwork wetGrassNetwork = BayesianNetwork.newNetwork("WET GRASS NETWORK");
```

### 2. Adding Nodes

Add nodes with their states using the desired identifiers.

```Java 
wetGrassNetwork
        .addNewNode("RAIN",List.of("RAIN:TRUE", "RAIN:FALSE"))
        .addNewNode("SPRINKLER",List.of("SPRINKLER:TRUE","SPRINKLER:FALSE"))
        .addNewNode("WET_GRASS",List.of("WET_GRASS:TRUE","WET_GRASS:FALSE"));
```

**Note:** All node and node states may use any Serializable type as their identifier, but each identifier must be
unique in the network. If using descriptive Strings, as in the above example, it is advisable to prefix the state
identifiers with the node name (e.g.`"RAIN:TRUE"`).

### 3. Defining the graph structure:

Define parent/child relationships using the node identifiers.

```Java
wetGrassNetwork
        .addParents("SPRINKLER","RAIN")
        .addParents("WET_GRASS",List.of("SPRINKLER","RAIN"));
```

### 4. Adding Constraints

Constraints are built using the node state identifiers. **Constraints do not have to be aligned with the
network structure**; for example, a marginal constraint can be defined on a node with parents, or an ancestor node's
state can be conditional on a descendant node's state. 

For this demo, we will define CPT entries within the graph, but we will provide only the CPT entries from Fig.1. where 
the node's measured state is `true`. The complementary constraints (e.g., `P(RAIN:FALSE) = 0.8`) are inferred 
automatically and do not need to be defined.

```Java
wetGrassNetwork
        // Non-conditional probability P(event) = p
        .addConstraint("RAIN:TRUE", 0.2)
        // Conditional Probabilities P(event|conditions) = p
        .addConstraint("SPRINKLER:TRUE", "RAIN:TRUE", 0.01)
        .addConstraint("SPRINKLER:TRUE", "RAIN:FALSE", 0.4)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:TRUE"), 0.99)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:TRUE", "SPRINKLER:FALSE"), 0.9)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:TRUE"), 0.9)
        .addConstraint("WET_GRASS:TRUE", List.of("RAIN:FALSE", "SPRINKLER:FALSE"), 0.0);
```

### 5. Using BayesSolver

Networks can be solved from the instance, or we can create a BayesSolver for more fine
control:

```java
// Version 1.
// Uses default configurations found in app.properties under app.bayes.solver
wetGrassNetwork.solveNetwork(); 

// Version 2.
// Manually create a BayesSolver
BayesSolver solver = BayesSolver.create(wetGrassNetwork);

// 2.a
// If every CPT entry can be inferred from the constraints, write them directly.
boolean directWriteSuccess = solver.writeCPTsFromConstraints();

// 2.b
// If we can't write them directly, run IPFP
if (!directWriteSuccess) {
    solver.solve(SolverAlgorithm.JUNCTION_TREE_IPFP);
}

// 2.c
// Equivalent to running 1. or 2.a & 2.b
solver.solve();
```

As we defined the constraints in such a way that every `FALSE` entry can be inferred, our network will be written 
directly to the CPTs, with no IPFP required.

### 6. Direct Inference

Once solved, we can perform direct inference by building an inference engine from the solved network.

```Java 
InferenceEngine engine = wetGrassNetwork.buildInferenceEngine();
```

This can be used to query for specific posterior or prior probabilities.
For example, let's set the engine to observe `WET_GRASS:TRUE`. 

```Java 
engine.setObservedById("WET_GRASS:TRUE");
```

Further queries will measure the posterior probability, conditional on `WET_GRASS:TRUE`. In other words, all 
measurements now assume the `WET_GRASS` node is locked in the `TRUE` position. Observations can be cleared with the 
following call:

```Java
engine.resetObservations(); 
```

For the next steps, we will keep the observation `WET_GRASS:TRUE`.

If we now want to know the probability of `RAIN:TRUE`, conditional on `WET_GRASS:TRUE`, we can call the following method:

```java
double posteriorRainTrue = engine.getPosteriorProbabilityById("RAIN:TRUE");
System.out.printf("P(RAIN:TRUE|WET_GRASS:TRUE) = %.3f", posteriorRainTrue);

>> P(RAIN:TRUE|WET_GRASS:TRUE) = 0.385
```

### 7. Printing the CPTs and Posterior Probabilities

You can print the network's solved CPTs or the inference engine's observed probability tables to a .txt file. 
By default, the printer will save files to the directory ```$user_home$/AR_Tools/bayes_solver/output/```

```Java
network.printNetwork();   // Prints the solved CPTs
engine.printObserved();   // Prints the posterior probabilities over each node
```

This should automatically open two files, which will look like this:

```
NETWORK TABLES:

P(RAIN)
-------------------------
|  RAIN:TRUE| RAIN:FALSE|
|      0.200|      0.800|
-------------------------

P(SPRINKLER|RAIN)
-----------------------------------------------
|           |  SPRINKLER:TRUE| SPRINKLER:FALSE|
|RAIN:TRUE  |           0.010|           0.990|
|RAIN:FALSE |           0.400|           0.600|
-----------------------------------------------

P(WET_GRASS|RAIN,SPRINKLER)
---------------------------------------------------------------------
[...]
```

```
OBSERVED TABLES:

P(RAIN|WET_GRASS:TRUE)
-------------------------
|  RAIN:TRUE| RAIN:FALSE|
|      0.385|      0.615|
-------------------------

P(SPRINKLER|WET_GRASS:TRUE)
-----------------------------------
[...]
```

Settings for the printer can be modified within `app.properties` under the section `app.bayes.printer`.

### 8. Generating Random Samples

Build a sampler from the BayesianNetwork instance:

```Java
Sampler sampler = wetGrassNetwork.buildSampler();
```

You can apply the observations from the inference engine to generate samples with certain fixed states:

```java
int numberOfSamples = 1000;

SampleCollection sampleCollection = sampler
        .setObserved(engine.getCurrentObservations())
        .generateSamples(numberOfSamples);

int samplesWithWetGrassTrue = sampleCollection.countSamplesIncludingStateIds("WET_GRASS:TRUE");

System.out.println(samplesWithWetGrassTrue);

>> 1000
```

Samples contain a specific combination of node states and the frequency of its occurrence in the sampler run.

```java
List<Sample> samples = sampleCollection.getSamples();
Sample firstSample = samples.getFirst();
System.out.println(firstSample);

>> RAIN:TRUE, SPRINKLER:TRUE, WET_GRASS:TRUE : 4

// Let's compare that to the true probability via direct inference.
List<NodeState> sampledStates = firstSample.getDisplayedStates(ArrayList::new);
double directInferenceProb = engine.getPosteriorProbability(sampledStates);
// We should expect a probability of ~4 in 1000, similar to our sample count.
System.out.printf("%.2f", directInferenceProb * numberOfSamples);

// P(RAIN:TRUE,SPRINKLER:TRUE|WET_GRASS:TRUE) * 1000
>> 4.23 
```

### 9. Using ProbabilityTables from the network.

You can extract the probability tables for use in your application.

```Java
// Extract and query a CPT from the BayesianNetwork
NetworkTable wetGrassCpt = wetGrassNetwork.getNetworkTable("WET_GRASS");
List<String> cptRequestIds = List.of("RAIN:TRUE","SPRINKLER:FALSE","WET_GRASS:TRUE");
double cptRequestProb = wetGrassCpt.getProbabilityFromIDs(cptRequestIds);
System.out.printf("%.2f",cptRequestProb);

// P(WET_GRASS:TRUE|RAIN:TRUE, SPRINKLER:FALSE)
>> 0.90

// Extract and query an Observed table from the InferenceEngine.
ObservedTable rainObservedTable = engine.getObservedTableById("RAIN");
double rainFalsePosterior = rainObservedTable.getProbabilityById("RAIN:FALSE");
System.out.printf("%.2f", rainFalsePosterior);

// P(RAIN:FALSE|WET_GRASS:TRUE)
>> 0.62
```

# API

[The API documentation can be found here.](https://www.javadoc.io/doc/io.github.alecredmond/ar-bayes-solver/latest/BayesSolver/module-summary.html)

