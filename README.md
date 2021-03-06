# QualityScore

This project contains an implementation of the QualityScore algorithm, designed to automatically rate next-step programming hints by comparing them to expert-authored hints. For more information on the approach, see:

[1] Price, T. W., R. Zhi, Y. Dong, N. Lytle and T. Barnes. "The Impact of Data Quantity and Source on the Quality of Data-driven Hints for Programming." International Conference on Artificial Intelligence in Education. 2018.

## Use Cases

The primary purpose of this project is allow researchers to benchmark the quality of a hint generation algorithm, specifically using gold standard data found at http://go.ncsu.edu/hint-quality-data. There are a number of way you might use hint ratings:

* Testing a newly developed data-driven hint generation algorithm on new datasets in different programming languages.
* Evaluating the quality of data-driven hints against the gold standard hints.
* Comparing the quality of one or more data-driven hint algorithms.
* Evaluating how factors such as the quantity of training data impact the quality of data-driven hints (e.g. [1]).
* Testing a new feature for a hint generation algorithm and seeing if it improves hint quality (though beware of overfitting to this dataset).

## Getting Started

The project was developed using [Eclipse](https://www.eclipse.org/), [Maven](https://maven.apache.org/) and Java 1.8. Make sure you have those installed first. Once you have cloned this repo, open Eclipse and choose File->Import->Existing Projects into workspace and select the root folder of this repository. Select this projects and import it.

To use the QualityScore project, you will need training data, hint requests and gold standard data. Two such datasets are freely available for academic/research use at the [PSLC Datashop](pslcdatashop.web.cmu.edu) under the [Hint Quality Ratings Dataset](https://pslcdatashop.web.cmu.edu/Files?datasetId=2517). For more information on this data and how to access it, please see [data/README.md](data/README.md). 

Once you have downloaded an appropriate dataset from PSLC, extract it to the `data` folder, (e.g. `data/iSnapF16-F17`). These datasets come with hint generated by 6 other algorithms. To test out the data, you can run the [`RunHintRater`](src/edu/isnap/rating/RunHintRater.java) class. It should read these hints and rate them, using the gold standard data in the dataset, and output the ratings to the console. If you get an error, make sure you have the data in the `data` directory.

The console output will display the QualityScore ratings for each algorithm on each hint request and problem. Afterwards, you can check the `data/<dataset>/ratings/MultipleTutors` directory, and you will find a spreadsheet with this hint rating data for each algorithm.

### Understanding the output

The `RunRateHints` file will output something like this for each dataset, assignment and algorithm:

```
SourceCheck
----- guess1Lab -----
125224: 0.00 (0.00)v [n=04]
131257: 0.32 (0.39)v [n=14]
134052: 1.00 (1.00)v [n=01]
140078: 0.30 (0.43)v [n=09]
140986: 0.32 (0.32)v [n=08]
142799: 0.00 (0.00)v [n=05]
144900: 1.00 (1.00)v [n=01]
145234: 0.00 (0.00)v [n=07]
[...]
236480: 0.46 (0.46)v [n=10]
240935: 0.00 (0.14)v [n=07]
TOTAL: 0.374 (0.426)v
```

Each line represents one hint request, and takes the form: `requestID: score (partial_score)v [n=number_of_hints_generated]`. Please see [1] for a description of the difference between the QualityScore calculated with and without partial matches (the second and first values, respectively).

If you turn debugging on, you will see more detailed output:

```
+====+ squiralHW / 119126 +====+
Snap!shot {                      <----- Hint Request
  [stage=Stage] {
    [sprite=Sprite] {
      script {
        receiveGo
        [evaluateCustomBlock=squiralmaker sides %s length %s]([literal=4], [literal=10])
        clear
      }
    }
  }
  [customBlock=squiralmaker] {
    script
  }
  [customBlock=squiralmaker sides %s length %s] {
    script {
      clear
      gotoXY([literal=0], [literal=0])
      down
      doRepeat([var=sides], script {
        forward([var=length])
      })
      turn(reportQuotient([literal=360], [var=sides]))
    }
    [varDec=sides]
    [varDec=length]
  }
  [varDec=sides]
  [varDec=legnth]
}
               === None ===
Hint ID: 1947435415              <----- First Hint
Weight: 1.0
          [evaluateCustomBlock=squiralmaker sides %s length %s]([literal=4], [literal=10])
-         clear
        }
...

-------
[...]
               === Full ===
Hint ID: 1459271195             <----- First Valid Hint
Weight: 1.0
        turn(reportQuotient([literal=360], [var=sides]))
+       up
      }

-------
[...]
119126: 0.78 (0.78)v [n=09]
```

This prints each hint request, followed by diffs representing each hint generated by the algorithm, grouped by what kind of match it is (None, Partial, Full) to a gold standard hint.

## Rating Your Own Hints

There are two ways that you can generate your own set of hints and rate the with the QualityScore to compare against other algorithms. If you hint generation algorithm is written in Java, the easiest option is to use this project as a dependency. Otherwise, you will need to generate a set of hints and output them to the .json format, just like the downloadable datasets under the `data/<dataset>/algorithms/` directory. Make sure you've gotten the `RunHintRater` class to run before attempting to rate your own hints.

### Using Java and this Project

If your hint generation algorithm is written in Java, you can simple use this project as a dependency. Then extend the [`HintGenerator`](src/edu/isnap/rating/data/HintGenerator.java) class and add your own hint generation logic. An very simple example of a one-nearest-neighbor hint generation algorithm is given in the [`OneNNAlgorithm`](src/edu/isnap/rating/example/OneNNAlgorithm.java) class. If you run the main method of this class, it will generate hints and rate them. Note, this naive algorithm unsurprisingly performs quite poorly.

### Using Another Implementation

If your implementation will not easily interface with Java, you can also generate your hints as .json files and rate them using the `RunHintRater` class, just like the example algorithms that came with the datasets. 

To do so, you will need to read in the `training.csv` and `requests.csv` files for the dataset for which you will generate hints. More information on these files' format can be found in the [data/README.md](data/README.md) file. The [`TraceDataset`](src/edu/isnap/rating/data/TraceDataset.java) class shows and example of how to parse this file to use for hint generation. You may also want to look at the [`ASTNode`](src/edu/isnap/node/ASTNode.java) class for an example of how to represent individual snapshots. 

The `training.csv` file includes full traces of previous students' data, which you should use to train your hint generation algorithm. After training, read in each hint request in the `requests.csv` and generate one or more hints. Hint should be saved as .json files, representing the code state that results from applying the hint's suggestion. Each assignment should have its own folder, and each hint should be name `requestID_hintNumber.json`. Examples of the correct output format can be found in the `algorithms` folder of each dataset. You algorithm should output a folder under `<dataset>/algorithms` with this structure:

```
<dataset>
    algorithms
        algorithmName
            assignment1
                requestID1_hint1.json
                requestID1_hint2.json
                requestID2_hint1.json
                ...
            assignment2
                ...
            ...
```
One you have created this folder, run the `RunHintRater` class, either to rate all algorithms in the folder (which will include yours), or uncomment the code for running a specific algorithm.
