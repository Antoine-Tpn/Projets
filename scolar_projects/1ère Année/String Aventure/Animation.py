################
# Animation.py #
# A.Turpin     #
# 01/04/2023   #
# S2-P IPI     #
################
import sys
import random

class Animation : pass

def create(filename,dt):
    a=dict()
    a["x"]=0
    a["y"]=0
    a["frame"]=list()
    a["on"]=False
    a["timeLeft"]=10.0
    a["duration"]=10.0
    a["dt"]=dt
    parse(a,filename)
    return a

def setOn (a,n):
    a["on"]=n
def getOn(a):
    return a["on"]

def parse (a,filename):
    
    a["frame"].clear()
    file = open(filename, "r")
    chaine=file.read()
    # division en liste aux sauts de ligne
    Lignes=chaine.splitlines()

    #transformation en liste de liste
    for line in Lignes:
        char=list(line)
        a["frame"].append(char)
    file.close()

def nextFrameIndex(a):

    step=float(a["duration"])/float(len(a["frame"]))
    indice=round((a["duration"]-a["timeLeft"])/step)
    return indice

def show(a):

    if(a["on"]==False):
        return None
    
    a["timeLeft"]=a["timeLeft"]-a["dt"]

    if a["timeLeft"]<=0 :
        setOn(a,False)

    color=['\033[31m','\033[32m','\033[33m',
           '\033[34m','\033[36m']
    i=nextFrameIndex(a)

    for y in range(i):
        for x in range(i*5):
            j=random.randint(0,len(color)-1)
            txt="\033["+str(y+1)+";"+str(x+1)+"H"
            sys.stdout.write(txt)
            sys.stdout.write(color[j])
            sys.stdout.write(a["frame"][y][x])
            