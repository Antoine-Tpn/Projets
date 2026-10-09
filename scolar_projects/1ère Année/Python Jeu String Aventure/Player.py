################
# Player.py    #
# A.Turpin     #
# 01/04/2023   #
# S2-P IPI     #
################

import sys,os


class Player : pass

def create():
    global f
    p=Player()
    p.x=5.0
    p.y=25.0
    p.shoot=list()
    # [x,y,limit,show,direction]
    p.hp=3
    p.last_move="droite"
    p.speed=10.0
    p.h=0
    return p

"""
Accesseurs
"""
def getH(p):
    return p.h
def getX(p) :
    return p.x
def getY(p) :
    return p.y
def getHp(p) :
    return p.hp
def getpos(p):
    return getX(p),getY(p)
def getLast_move(p):
    return p.last_move
def getSpeed(p):
    return p.speed
def getShoot(p,i):
    return p.shoot[i]
"""
Mutateurs
"""
def setX(p,n):
    p.x=n
def setY(p,n):
    p.y=n
def setHp(p,n):
    p.hp=n
def setLast_move(p,n):
    p.last_move=n
def setH(p,n):
    p.h=n

def setXShoot(p,i,n):
    p.shoot[i][0]=n
def setYShoot(p,i,n):
    p.shoot[i][1]=n
def setLimitShoot(p,i,n):
    p.shoot[i][2]=n
def setShowShoot(p,i,n):
    p.shoot[i][3]=n
def setDirectionShoot(p,i,n):
    p.shoot[i][4]=n
"""
Player.jump (p)
Description : permet au joueur de sauter (saut limité)
"""
def jump (p,dt):
    y=getY(p)
    if p.y>=2:
        p.h=p.h-p.speed*dt*1.5
        setY(p,y-p.speed*dt*1.5)
    else :
        setH(p,0)
    
"""
Player.chute(p,dt)
Description : permet au joueur de chuter 
"""
def chute (p,dt):
    y=getY(p)
    setY(p,y+p.speed*dt)

"""
Player.showShoot(p)
Description : supprime l'affichage du projectile du joueur
"""
def showShoot(p,i):
    setShowShoot(p,i,False)
    setXShoot(p,i,0)
    setYShoot(p,i,0)

def NewShoot(p):
                   #[x,y,limit,show,dir]
    p.shoot.append([p.x+5.0 , p.y , 155 , False , "droite"])
    i=len(p.shoot)-1
    return i

"""
Player.fire(p)
Description : permet au joueur de créer un nouveau projectile devant le joueur ou derrière lui
"""
def fire(p,i):
    # intitialisation projectile à l'origine
    # on forme un nouveau projectile devant le héros quand la touche 'p' est préssé
    # on valide le tire
    setShowShoot(p,i,True)
    # puis on tire dans la direction de la dernière touche utilisé par le joueur
    if p.last_move=="droite": # move à droite
        setDirectionShoot(p,i,"droite")
        setLimitShoot(p,i,p.x+40.0)
        setXShoot(p,i,p.x+5.0)

    elif p.last_move=="gauche": # move à gauche
        setDirectionShoot(p,i,"gauche")
        if p.x>=20.0:
            setLimitShoot(p,i,p.x-40.0)
        else:
            setLimitShoot(p,i,1.0)
        setXShoot(p,i,p.x)
    setYShoot(p,i,p.y)    
    
"""
Player.move (p,x,y) 
Description : change la position du joueur donné (une fois la collision vérifiée dans Game)
"""
def move (p,x,y):
    setX(p,x)
    setY(p,y)
    
"""
Player.getposFire (p)
Description : retourne la position du projectile du joueur
"""
def getposFire(p):
    S=list()
    for j in range (len(p.shoot)):
        S.append(getShoot(p,j))
    return S  

"""
Player.show(p)-> rien
	Description : affiche le joueur et le projectile
"""
def show (p):
    # affichage animat
    #on se place a la position de l animat dans le terminal
    txt="\033["+str(round(p.y))+";"+str(round(p.x))+"H"
    sys.stdout.write(txt)
    #couleur fond noire
    sys.stdout.write("\033[40m")
    sys.stdout.write("(っ)つ")  
    txt1="\033["+str(round(p.y-1))+";"+str(round(p.x))+"H"
    sys.stdout.write(txt1)
    sys.stdout.write("(▀¯▀)")

    for j in range (len(p.shoot)):
        S=getShoot(p,j)
        xf=S[0]
        yf=S[1]
        xf,yf=round(xf),round(yf)

        if S[3]==True:
            txt="\033["+str(yf)+";"+str(xf)+"H"
            sys.stdout.write(txt)
            #couleur fond noire
            sys.stdout.write("\033[40m")
            sys.stdout.write("¤")  

"""
Player.live (p,dt)
Description : calcul les déplacements du projectile
"""
def live(p,dt):
    for j in range (len(p.shoot)):
        S=getShoot(p,j)
        xS=S[0]
        limit=S[2]
        direction = S[4]
        # tant que le projectile n'est pas arrivé à sa distance maximale
        # on l'affiche vers la droite ou vers la gauche
        if direction =="droite":
            if limit > xS:
                setXShoot(p,j,xS+p.speed*2*dt)
            else:
               showShoot(p,j)
        elif direction == "gauche": 
            if limit < xS:
                setXShoot(p,j,xS-p.speed*2*dt)
            else:
                showShoot(p,j)
"""
Player.testFull() :
	Description : teste si les fonctions associées à la structure de Player fonctionnent
"""
def testFull():
    #effacer la console
    sys.stdout.write("\033[1;1H")
    sys.stdout.write("\033[2J")

    player=create()
    print(getX(player),getY(player))
    move (player,5,5)
    print(getX(player),getY(player))
    show(player)
    chute(player)
    fire(player,NewShoot(player))
    show(player)

#testFull()