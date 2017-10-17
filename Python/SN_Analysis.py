import numpy as np 												#For using pandas
import matplotlib.pyplot as plt 								#For using with pandas 
import pandas as pd 											#For managing the articles tables
import networkx as nx 											#For creating the social network
import os 														#For removing unused files
def	createSocialNetwork(authorsTable):

	G 					= nx.Graph()
	authorsDictionary 	= {}
	connectedAuthors	= []
	articleNumber		= 0
	authorColumn 		= authorsTable.columns.get_loc('Author')
	articleColumn 		= authorsTable.columns.get_loc('Articles')

	columnHeaders 		= list(authorsTable.columns.values)

	grouped 	= authorsTable.groupby('Articles')
	groupList 	= grouped.groups


	##LOOPS THROUGH EACH ARTICLE AND CREATES A DICTIONARY FOR EACH AUTHOR ===========================================================
	for name, group in grouped:
		connectedAuthors.append([])
		#FOR EACH ARTICLE, INSERTS ITS AUTHORS IN THE NETWORK =======================================================================
		for i in range(len(group)):
			currentAuthorName = group.iloc[i,authorColumn]
			
			connectedAuthors[articleNumber].append(currentAuthorName)
			if not currentAuthorName in authorsDictionary:			
				authorsDictionary[currentAuthorName] 		= 	{}
			authorsDictionary[currentAuthorName]["Label"] 	= currentAuthorName
			#FOR EACH COLUMN, ADD ITS DATA TO THE AUTHOR DICTIONARY =================================================================
			for j in range(len(authorsTable.columns)):
				if(j!= authorColumn):
					if(j != articleColumn):
						authorsDictionary[currentAuthorName][columnHeaders[j]] = group.iloc[i,j]
					##WHEN INSERTING THE ARTICLE, TRIES TO APPEND TO THE CURRENT ARTICLES STRING,			=========================
					##IF IT CAN'T, IT'S BECAUSE THERE IS NO ARTICLE CURRENTLY FOR THIS AUTHOR, SO CREATE ITS ========================
					else:
						try:
							authorsDictionary[currentAuthorName][columnHeaders[j]] += "; " + group.iloc[i,j]
						except:
							authorsDictionary[currentAuthorName][columnHeaders[j]] = group.iloc[i,j]
			
			G.add_node(currentAuthorName,authorsDictionary[currentAuthorName])
		
		articleNumber += 1

	##LOOPS THROUGH ALL THE INSERTED ARTICLES AND CONNECTS ITS AUTHORS ==============================================================
	for article in connectedAuthors:
		edges = defineAuthorsEdges(article)
		if (len(edges) > 1):
			for l in range(len(edges)):
				G.add_edge(edges[l][0],edges[l][1])

	nx.write_graphml(G,'simpleSN.graphml')
	fixGraphmlKeyBug()


def fixGraphmlKeyBug():
	f = open('simpleSN.graphml','r')
	o = open('../gephiToolkit9.0.2/simpleSN.graphml','w+')	
	keys = {}	
	for line in f:
		hasChanged = False
		if "<key attr.name" in line:
			split = line.split('"')
			attrname = split[1]
			attrkey =  split[7]
			keys[attrkey] = attrname
			o.write(line.replace(attrkey,attrname))
			hasChanged = True
		else:
			for attr in keys:
				if attr in line:
					newLine = line.replace(attr,keys[attr].title())
					o.write(newLine)
					hasChanged = True
		if(not hasChanged):
			o.write(line)

	f.close()
	o.close()
	os.remove("simpleSN.graphml")	

def defineAuthorsEdges(list):
    """Returns the authors edges from a list of authors. There will be n! edges considering n authors.
    """

    outputTuples = []
    
    for i in range(len(list)):
        for j in range(len(list)):
            if(i != j):
                outputTuples.append( (list[i], list[j]) )

    k = len(outputTuples)
    return outputTuples