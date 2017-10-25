# What is this project?

To help better and easily visualize how authors interact in a conference, this project takes a list of authors and their respective articles from EasyChair and then generate the social network from the conference. In this network, the nodes are the Authors and the authors whom this author worked with are the edges. This output social network (SN) is visible in an amazing [JavaScript project](https://github.com/raphv/gexf-js) with some small adaptations to be fitted in a conference network. This presented data visualization tool allows anyone to rapidly see who are the most connected authors, from wich country they are and many other specific characteristics from the author.

----
# How this code work?
The code is actually divided into 3 big steps in different programming languages. The first one is the data pre processor and processor in Python, the second is the [Gephi toolkit](https://github.com/gephi/gephi-toolkit) program in Java, wich improves the layout from the network and the last one is in an adapted code in JavaScript for the web visualization of the network. 
![Alt text](FilesFluxogram.png?raw=true "Files flow for the co authorship network generation")

# Python

The Python code take two input files that must be taken from EasyChair, the "articles.html" and "author_list.xlsx". For this step to work properly, the input files must precisely match from "EasyChair", this means that "articles.html" must contain all the articles names in order that they appear on the "accepted" worksheet from "author_list.xlsx".
After collecting the input and transforming it into a single table (mixing the articles table and the authorlist table), the algorithm then groups names that are probably the same, according to a threshold [Levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance), for fixing the disambiguation problem in conferences (same author referred multiple times in a different way). After this small filter, it creates the social network, the authors being the nodes and the coauthorship relation being the edges.

# Gephi Toolkit (Java)

It uses the Gephi API to improve the visualization of the network, since [networkx package](https://networkx.github.io/) does not provide a very robust layout for social networks. It distributes the nodes according to the ForceAtlas algorithm, changes the node colors to fit the author country, changes the node size and label according to its degree and then outputs a "gexf" file.This step uses the project manager [Maven](https://maven.apache.org/) as it was described in the Gephi API, so it might be a little tricky to use. 

# JavaScript

Adapted from the [already mentioned project](https://github.com/raphv/gexf-js) this step is for the web visualization, it takes the "gexf" file and makes it visible and interactive. The changes from the original project were some labels, the possibility to show the edges from a given article name, and the list of the author's articles.

