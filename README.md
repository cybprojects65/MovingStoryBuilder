
# Moving Story Builder

This is a Java project for generating stories for the MOVING project out of a CSV input containing textual information on value chains.

**Input**: a CSV file with textual information on the MOVING value chains, with one value chain description for each row, e.g., [MOVING_VCs_DATASET_FINAL_V2.csv](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/MOVING_VCs_DATASET_FINAL_V2.csv)

**Output**: a sequence of CSV files (one for each story) in the "stories" local folder, with 11 enriched story maps, with punctual geographic references for each event, e.g., those available in the [stories.zip](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/stories.zip) archive.


## Prerequisites

This project was developed with Java [JDK-1.8](https://www.oracle.com/it/java/technologies/javase/javase8u211-later-archive-downloads.html) and the [Eclipse IDE](https://www.eclipse.org/downloads/download.php?file=/oomph/epp/2024-06/R/eclipse-inst-jre-win64.exe) on a Linux-Ubuntu 18.4 machine.

Accessory libraries are available in the lib folder and in the Maven POM.

The software requires internet connection to invoke the NLPHub and the Wikidata SPARQL endpoint. 

**Important: The NLPHub performance may vary depending on the connected service availability and accessibility.**
## Data explanation

- [Example of annotations produced by the NLP Hub](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/Annotations_example.txt)
- [Original unstructured collection of value chain information data](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/Dataset_VC%20card_Inventory_102021_db_updated01122021.csv)
 - [Software input data: Original semi-structured collection of value chain information data](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/MOVING_VCs_DATASET_FINAL_V2.csv)
 - [Mapping table between the semi-structured data collection and the story structure]( https://github.com/cybprojects65/MovingStoryBuilder/blob/main/mappingtable.csv)
 - [Sample text to test the NLPHub NER extraction](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/sampleTextBBC.txt)
 - [Latest produced stories](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/stories.zip)
## How to run

Clone the GitHub repository
```sh
git clone https://github.com/cybprojects65/MovingStoryBuilder.git
```

Install Java 1.8

Import the project into Eclipse ("import project from folder or archive")

Check that the input dataset is in the root folder of the project.

The current input is [MOVING_VCs_DATASET_FINAL_V2.csv](https://github.com/cybprojects65/MovingStoryBuilder/blob/main/MOVING_VCs_DATASET_FINAL_V2.csv)

From Eclipse:

```sh
Open org.gcube.moving.inventory.InventoryManager

If necessary, change the input data location at line 24:
    try (CSVReader reader = new CSVReader(new FileReader("MOVING_VCs_DATASET_FINAL_V2.csv")))

Run "InventoryManager" (the main method will be executed) from Eclipse
```
Notes for Windows users: change the MOVING_VCs_DATASET_FINAL_V2.csv to ANSI text coding; change the NLPHubCaller resouce properties to UTF-8.


## Authors

- [Gianpaolo Coro](https://github.com/cybprojects65)


## Acknowledgements
The software was developed thanks to the [MOVING EU Project](https://www.moving-h2020.eu/)
 
## Further reading
 - [From unstructured texts to semantic story maps](https://www.tandfonline.com/doi/full/10.1080/17538947.2023.2168774)
 - [Using semantic story maps to describe a territory beyond its map](https://content.iospress.com/articles/semantic-web/sw233485)

