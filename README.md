# drabme

This module performs a drug combination response analysis to boolean model 
ensembles. It's purpose is to evaluate synergies on the models generated by the
`Gitsbe` module.

## Input

- A drug panel file (name of drugs tested and their corresponding target nodes)
- A drug perturbations file (the drugs and their combinations for testing)
- A model outputs file (nodes with weights that affect the calculation of the 
model's global output/growth)
- A configuration file
- A **models** directory with `.gitsbe` files (an ensemble of logical models)

## Output

- **Model-wise** results
    - A model-wise responses file (for every tested perturbation all models' 
    globaloutput responses)
    - A model-wise synergies file (for every drug combination tested, the number
    of models that found it synergistic and non-synergistic)
- **Ensemble-wise** results
    - A ensemble-wise responses file (for every tested perturbation the average 
    response of all models that gave a response - had an attractor)
    - A ensemble-wise synergies file (for every drug combination tested, a 
    synergy score number indicating how much synergistic that combination was - 
    a more negative number means more synergistic)
- A **model predictions** file (for every model and drug combination tested,
 a value: 0 = no synergy predicted, 1 = synergy predicted, NA = couldn't find 
 attractors in either the drug combination inhibited model or in any of the 
 two single-drug inhibited models)

## Method

For each `.gitsbe` model, all perturbations specified are simulated. For each 
perturbation, the drug panel is consulted to fix the state of the specified 
node(s) to the value 0 (the node state could also be fixed to the value 1 
for a drug that activates a signaling entity).

After simulating a perturbation (calculating attractors), the **global output 
parameter (growth)** is computed by integrating a weighted score across the states 
of model output nodes. For example, if two output nodes `A` (weight: 1) and `B` 
(weight: -1) were found to have the states `A = 0, B = 1` for a perturbation, 
the global output would evaluate to: 
**A_state x A_weight + B_state x B_weight = 0 x 1 + 1 x (-1) = -1**.

The global output (growth) is then used to compute synergies.

**Synergy** is defined as the effect not expected from a null reference model of 
drug combination responses. Both for in silico simulations and in vitro experiments 
an observed combination effect can be formally defined as the effect *E(a,b)* 
observed for two drugs a and b, while *A(a,b)* is the drug combination effect 
expected from each individual drug’s properties (given by an appropriate 
mathematical model, e.g. Bliss Independence), and *S(a,b)* is any difference 
between the observed and the expected drug combination effect. In the case of 
excess effects observed for a combination, *S(a,b)* is positive and **synergy** 
is called. Conversely, for attenuated effects, *S(a,b)* is negative and 
**antagonism** is called. Finally, for drug combinations where *E(a,b)* equals 
*A(a,b)*, the drug combination effect can fully be explained by each drug 
independently (there is no interaction between the drugs).

In model simulations, the null reference drug combination response is defined as 
the minimum value of the two single perturbations with respect to global output 
growth (similar to the **highest single agent** model for in vitro studies). 
The global output values mentioned are the average values for all models that 
had attractors for the specified perturbations. So, an **ensemble-wise 
synergy** is called when the average global output value for a combination is 
less than the minimum of the two average values of the single perturbations.

## Install

```
# tested with maven 3.6.0
git clone https://bitbucket.org/asmundf/drabme.git
mvn clean install
```

## Run example

From the drabme root directory, run (remember to change the `{version}` to the 
appropriate one, e.g. `1.0.5`):

```
cd example_run_ags
java -cp ../target/drabme-{version}-jar-with-dependencies.jar eu.druglogics.drabme.Launcher --project=test --modelsDir=models --drugs=toy_ags_drugpanel.tab --perturbations=toy_ags_perturbations.tab --config=toy_ags_config.tab --modeloutputs=toy_ags_modeloutputs.tab
```

or run the mvn profile from the drabme root directory:
```
mvn compile -P runExampleAGS
```

- Note that its best that all input files are on the same directory like in the 
example above. The output directory with all the generated result files when 
running the `drabme.Launcher` will be in the same directory where the 
configuration file is.
- Running the `drabme.Launcher` with no parameters generates a usage/options 
message.