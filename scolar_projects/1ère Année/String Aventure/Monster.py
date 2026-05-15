################
# Monster.py   #
# A.Turpin     #
# 01/04/2023   #
# S2-P IPI     #
################

import sys,os

class Monster : pass

def create():
    m=list()
    # m=((type,x,y,hp,fire,cooldown,lim,xf,yf,direction,speed))
    m.append([1,20.0,5.0,2,False,0,0.0,0.0,0.0,1,10])
    m.append([1,30.0,5.0,2,False,0,0.0,0.0,0.0,1,5])
    m.append([1,55.0,5.0,2,False,0,0.0,0.0,0.0,1,5])
    m.append([0,85.0,5.0,2,False,0,0.0,0.0,0.0,-1,15])
    m.append([0,115.0,5.0,2,False,0,0.0,0.0,0.0,1,10])
    m.append([1,130.0,5.0,2,False,0,0.0,0.0,0.0,-1,10])
    return m

"""
Accesseurs
"""
def getX(m,i) :
    return m[i][1]
def getY(m,i) :
    return m[i][2]
def gethp(m,i) :
    return m[i][3]
def getlen(m):
    return len(m)
def getpos(m,i):
    return getX(m,i),getY(m,i)
def getDirection(m,i):
    return m[i][9]
def getCooldown(m,i):
    return m[i][5]

"""
Mutateurs
"""
def setX(m,i,n):
    m[i][1]=n
def setY(m,i,n):
    m[i][2]=n
def setHp(m,i,n):
    m[i][3]=n
def setDirection(m,i,n):
    m[i][9]=n
def setFire(m,i,n):
    m[i][4]=n
    m[i][7]=0
    m[i][8]=0
    
"""
Monster.posShoot (m) 
Description : retourne toutes les positions des projectiles 
"""
def posShoot(m):
    xy=[]
    for i in range(len(m)-1):
        x=m[i][7]
        y=m[i][8]
        xy.append([x,y])
    return xy

"""
Monster.move (m,i) 
Description : initialise les nouveaux monstres qui apparaîtront sur le jeu au niveaux suivant
"""
def newMonster(m,i):
    for j in range (len(m)):
        del m[0]
    if i==0:
        m.append([1,20.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([1,30.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([1,55.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([0,85.0,5.0,2,False,0,0.0,0.0,0.0,-1])
        m.append([0,115.0,5.0,2,False,0,0.0,0.0,0.0,-1])
        m.append([1,130.0,5.0,2,False,0,0.0,0.0,0.0,-1])
    elif i==1:
        m.append([1,40.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([1,55.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([0,115.0,5.0,2,False,0,0.0,0.0,0.0,-1])
        m.append([1,130.0,5.0,2,False,0,0.0,0.0,0.0,-1])
    else :
        m.append([1,20.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([1,30.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([1,55.0,5.0,2,False,0,0.0,0.0,0.0,1])
        m.append([1,85.0,5.0,2,False,0,0.0,0.0,0.0,-1])
        m.append([1,115.0,5.0,2,False,0,0.0,0.0,0.0,-1])
        m.append([1,130.0,5.0,2,False,0,0.0,0.0,0.0,-1])
    return m

"""
Monster.move (m,i,x,y) 
Description : change la position du monstre donné
"""
def move (m,i,x,y):
    setX(m,i,x)
    setY(m,i,y)

"""
Monster.chute (m,i,dt) 
Description : fait chuter le monstres correspondant
"""
def chute (m,i,dt):
    y=getY(m,i)
    setY(m,i,y+10*dt)

"""
Monster.show(g)-> rien
	Description : affiche les monstres et les projectiles
"""
def show(m,i):
    x=str(round(getX(m,i)))
    y=str(round(getY(m,i)))
    txt="\033["+y+";"+x+"H"
    sys.stdout.write(txt)
    sys.stdout.write("\033[40m")
    sys.stdout.write("ლ(•́•́ლ)")

    if m[i][4]==True :
        txt="\033["+str(round(m[i][8]))+";"+str(round(m[i][7]))+"H"
        sys.stdout.write(txt)
        sys.stdout.write("\033[40m")
        sys.stdout.write("¤")

"""
Monster.initShoot (m,i) 
Description : initialise le projectile
"""
def initShoot(m,i):
    m[i][4]=True # affichage True False
    m[i][5]=0 # initialise le cooldown
    x=m[i][1] # pos monstre
    y=m[i][2]
    m[i][7]=x # pos projectile
    m[i][8]=y
    if x>=40.0:
        m[i][6]=x-40  # lim=x-20
    else:
        m[i][6]=1     # sinon lim=1
    
"""
Monster.Shoot (m,i,dt) 
Description : modifie la position du projectile
"""
def Shoot(m,i,dt):
    if m[i][7]>m[i][6]: # x >=lim
        m[i][7]=m[i][7]-10*dt    # x-1

    elif m[i][7]<=m[i][6]:
        m[i][4]=False     
    m[i][5] +=1 # cooldown+=1

"""
Monster.testFull () 
Description : test les fonctions du module
"""
def testFull():
    #effacer la console
    sys.stdout.write("\033[1;1H")
    sys.stdout.write("\033[2J")

    monster=create()
    print(getX(monster,0),getY(monster,0))
    show(monster,0)
    move (monster,0,1,1)
    print(getX(monster,0),getY(monster,0))
    show(monster,0)
    

#testFull()