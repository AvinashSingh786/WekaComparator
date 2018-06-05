# WekaComparator

Simple tree parser, takes in a TREE or PART from Weka output from training and converts to a excel table or rules and labels with positive and negative matches as well as a percentage. The rules are then matched to the test input and is stored in a specific format. This can be filtered based on percentage and number of samples.

## Input
An example of the sample files and format are provided, the UI is very simple. An example of how to use it can be seen below:

![Sample Output](https://github.com/AvinashSingh786/WekaComparator/sample.png)


## Output
The sample format of the matching tests to the rules are stored in the following format:
    ```
    {"rule": 4,"label": "HP", "+": 41.0, "-": 0.0, "%": 100.0}
    ```


The result of the provided sample files is:

![Sample Output](https://github.com/AvinashSingh786/WekaComparator/sample_output.png)