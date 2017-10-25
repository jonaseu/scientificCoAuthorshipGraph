import numpy as np 					#For using pandas
import matplotlib.pyplot as plt 	#For using with pandas 
import pandas as pd 				#For managing the articles tables
import Levenshtein 					#For calculating strings distance
from bs4 import BeautifulSoup 		#For reading the articles title from  HTML

minAuthorLevenshteinDistance	= 0.7
minEmailLevenshteinDistance		= 0.8

def getTableFromEasyChair(authorsPath,articlesPath):
	''' Outputs a table that is a mix of 'authors.xlsx' and 'articles.html' files extracted manually from EasyChair. First input must be
	the authors file, and the second must be the articles path. This is for having a single table that contains all the data of the conference,
	containing all the names of the published articles and who wrote it.
	'''
	##OPEN ACCEPTED AUTHORS FILE AND GROUPS THE TABLE BY ARTICLE ID =================================================================
	authorsTable	= pd.read_excel(authorsPath, 'Accepted', index_col=None, na_values=['NA'])
	authorsTable.loc[:,'Articles'] = 'x'
	grouped 		= authorsTable.groupby('#')

	##OPEN ACCEPTED ARTICLES HTML AND EXTRACTS THEIR NAMES ==========================================================================
	html = open(articlesPath,'r')
	articlesPage 	= BeautifulSoup(html,'lxml')
	articlesList 	= articlesPage.findAll('span', { 'class' : 'title' })
	articlesNames 	= [temp.text for temp in articlesList]
	
	##CHANGE ARTICLE GROUP ID TO ARTICLE NAME =======================================================================================
	articlePos	= 0
	groupPos 	= 0
	for name, group in grouped:
		authorsTable.loc[groupPos:+groupPos+len(group),'Articles'] = articlesNames[articlePos].title()
		groupPos 	+= len(group)
		articlePos 	+= 1

	##REMOVES ARTICLE ID ============================================================================================================
	authorsTable.drop('#', axis=1, inplace=True)
	
	#authorsTable.to_csv('Authors and Articles.csv',index=False)
	return authorsTable



def removeAlikeAuthors(fileTable):
	''' Takes a list of all the authors and checks for very alike names based on Levenshtein distance. If the names are very alike and 
	so does the author email, then the authors are considered equals and the names are made completly equal.
	'''
	alikeAuthors 	= []
	alikeAuthorsPos = []

	authorColumn 	= fileTable.columns.get_loc('Author')+1
	emailColumn 	= fileTable.columns.get_loc('Email')+1

	for i,rowI in enumerate(fileTable.itertuples()):
		currentAuthor	= rowI[authorColumn]
		currentEmail 	= rowI[emailColumn]
		alikeAuthors.append(rowI[authorColumn])

		for j,rowJ in enumerate(fileTable.itertuples()):
			if (i != j):
				if ( (not pd.isnull(currentEmail)) and (not pd.isnull(rowJ[emailColumn])) ):
					authorsNameDistance = Levenshtein.ratio(currentAuthor,rowJ[authorColumn])
					emailsDistance = Levenshtein.ratio(currentEmail.partition("@")[0],rowJ[emailColumn].partition("@")[0])

					#DECIDE HOW CLOSE THE NAMES AND EMAIL SHOULD BE TO BE CONSIDERED EQUAL AUTHORS ==================================
					if( (authorsNameDistance >= minAuthorLevenshteinDistance)  and (authorsNameDistance < 1) and (emailsDistance >= minEmailLevenshteinDistance) ):
						alikeAuthors.append(rowJ[authorColumn])
						alikeAuthorsPos.append(j)
				else:
					if (pd.isnull(currentEmail)):
						fileTable.loc[i,'Email'] = "-"

		##IF THERE IS ALIKE AUTHORS, CHOOSE WICH NAME WILL BE THE CORRECT ===========================================================
		if ( len(alikeAuthors) 	> 1):
			correctAuthorName 	= chooseCorrectAuthor(alikeAuthors)
			for pos in alikeAuthorsPos:
				fileTable.loc[pos,'Author'] = correctAuthorName
			
			#print('Choosen as correct name: ', correctAuthorName)

		alikeAuthors 	= []
		alikeAuthorsPos = []

	#fileTable.to_csv('Authors and articles (removed duplicates).csv',index=False)
	return fileTable



def chooseCorrectAuthor(authorNameList):
	'''Chooses among the input list of alike names, what is the correct author name.
	It chooses by selecting the most seen name, or, if there is no repeated names, it chooses the longest. 
	'''

	mostAppearances 	= -1
	mostAppearancesPos 	= 0
	currentAppearences 	= 0
	#print("\n\n")
	#print("Considered equals:\n")
	#print(authorNameList)
	##CHECKS WHAT IS THE MOST SEEN NAME =============================================================================================
	for i in range(len(authorNameList)):
		currentAppearences 	= 0
		for j in range(len(authorNameList)):
			if(i != j):
				if( authorNameList[i] 	== authorNameList[j]):
					currentAppearences 	+= 1
					if( currentAppearences 	> mostAppearances):
						mostAppearances 	= currentAppearences
						mostAppearancesPos 	= i

	##IF THERE IS NO REPEATED NAME, CHOOSES IT BY THE LONGEST NAME ==================================================================
	if(mostAppearances != -1):
		return authorNameList[mostAppearancesPos]
	else:
		longest = -1
		longestPos = 0
		for i in range(len(authorNameList)):
			if( len(authorNameList[i]) > longest ):
				longest 	= len(authorNameList[i])
				longestPos 	= i

	return authorNameList[longestPos]