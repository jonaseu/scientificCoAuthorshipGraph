from SN_Analysis import *
from SN_Preprocessing import *
import time 				#For counting the run time

#MAIN
start_time = time.time()

easyChairFileTable 	= getGroupedTableFromEasyChairFiles('author_list.xlsx','articles.html')
authorsList 		= removeAlikeAuthors(easyChairFileTable)
createSocialNetwork(authorsList)
#####CONFERIR O POSSIVEL PROBLEMA DE PEGAR POSICAO DA COLUNA "AUTOR" + 1 ??
print("--- %s seconds ---" % (time.time() - start_time))