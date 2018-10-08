# QualityScore

This project contains an implementation of the QualityScore algorithm, designed to automatically rate next-step programming hints by comparing them to expert-authored hints. For more information on the approach, see:

[1] Price, T. W., R. Zhi, Y. Dong, N. Lytle and T. Barnes. "The Impact of Data Quantity and Source on the Quality of Data-driven Hints for Programming." International Conference on Artificial Intelligence in Education. 2018.

## Use Cases

* Testing a newly developed data-driven hint generation algorithm on new datasets in different programming languages.
* Evaluating the quality of data-driven hints against the gold standard hints.
* Comparing the quality of one or more data-driven hint algorithms.
* Evaluating how factors such as the quantity of training data impact the quality of data-driven hints (e.g. [1]).
* Testing a new feature for a hint generation algorithm and seeing if it improves hint quality (though beware of overfitting to this dataset).

## Getting Started

The project was developed using [Eclipse](https://www.eclipse.org/), [Maven](https://maven.apache.org/) and Java 1.8. Make sure you have those installed first. Once you have cloned this repo, open Eclipse and choose File->Import->Existing Projects into workspace and select the root folder of this repository. Select this projects and import it.

To use the QualityScore project, you will need training data, hint requests and gold standard data. Two such datasets are freely available for academic/research use at the [PSLC Datashop](pslcdatashop.web.cmu.edu) under the [Hint Quality Ratings Dataset](https://pslcdatashop.web.cmu.edu/Files?datasetId=2517). For more information on this data and how to access it, please see [data/README.md](data/README.md). 

Once you have downloaded an appropriate dataset from PSLC, extract it to the `data` folder, (e.g. `data/iSnapF16-F17`). These datasets come with hint generated by 6 other algorithms. To test out the data, you can run `edu.isnap.rating.RunRateHints.java`. It should read these hints and rate them, using the gold standard data in the dataset, and output the ratings to the console. If you get an error, make sure you have the data in the `data` directory.

## Rating Your Own Hints

There are two ways that you can generate your own set of hints and rate the with the QualityScore to compare against other algorithms. If you hint generation algorithm is written in Java, the easiest option is to use this project as a dependency, and operate on the built-in data-structures. Otherwise, you will need to generate a set of hints 

### Using this Project

### Using 