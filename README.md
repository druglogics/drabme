# drabme

This module performs a drug combination response analysis to boolean model 
ensembles. 

## Input

- A drug panel file (name of drugs tested and their corresponding target nodes)
- A drug perturbations file (the drugs and their combinations for testing)
- A model outputs file (nodes with weights that affect the calculation of the 
model simulation output)
- A configuration file
- A `models/` directory with `.gitsbe` files

## Output

- **Model-wise** results
    - A model-wise responses file (for every tested perturbation all models' 
    globaloutput responses)
    - A model-wise synergies file (for every drug combination tested, the number
    of models that found it as synergistic and non-synergistic correspondingly)
- **Ensemble-wise** results
    - A ensemble-wise responses file (for every tested perturbation the average 
    response of all models that gave a response - had an attractor)
    - A ensemble-wise synergies file (for every drug combination tested, a 
    synergy score number indicating how much synergistic that combination was - 
    a more negative number means more synergistic)
- A **model predictions** file (for every model and drug combination tested,
 a value: 0 = no synergy predicted, 1 = synergy predicted, NA = couldn’t find 
 attractors in either the drug combination inhibited model or in any of the 
 two single-drug inhibited models)

## Install

```
# tested with maven 3.5.2
git clone https://bitbucket.org/asmundf/drabme.git
mvn clean install
```

## Run example

From the drabme root directory, run (remember to change the `{version}` to the 
appropriate one, e.g. `1.0`):

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