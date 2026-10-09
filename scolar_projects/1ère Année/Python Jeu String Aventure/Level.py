################
# Level.py     #
# A.Turpin     #
# 01/04/2023   #
# S2-P IPI     #
################

import random
import sys
import termios

import Monster
import Animation

class Level : pass

def create(levels,i,dt):
    global m,end
    lvl=Level()
    m=Monster.create()
    end=Animation.create("END.txt",dt)
    lvl.levels=levels
    lvl.indice=i
    lvl.grid=list()
    parse(lvl,lvl.levels[lvl.indice])
    return lvl

"""
Accesseurs
"""
def getLevels(lvl):
    return lvl.levels

def getI(lvl):
    return lvl.indice

"""
Mutateurs
"""
def setI(lvl,n):
    lvl.indice=n
def setLevels(lvl,n):
    lvl.levels=n
"""
Level.parse (lvl,filename)
    Description : Récupère les infos des fichiers lvl.txt et 
    les découpes pour obtenir une liste de 
    liste d éléments pour créer notre grille avec les coordonnées
"""
def parse (lvl,filename):
    lvl.grid.clear()
    file = open(filename, "r")
    chaine=file.read()

    # division en liste aux sauts de ligne
    Lignes=chaine.splitlines()

    #transformation en liste de liste
    for line in Lignes:
        char=list(line)
        lvl.grid.append(char)
    
    file.close()
    return lvl.grid

"""
Level.getChar (g,x,y)
Description : retourne l élément correspondant aux coordonnées x et y dans la grille
"""
def getChar(lvl,x,y):
    #renvoie le contenu d une case donnée
    return (lvl.grid[y-1][x-1])
    
"""
Level.setChar (g,x,y,n)
Description : modifie l élément correspondant aux coordonnées x et y dans la grille
"""
def setChar(lvl,x,y,n):
	#change le contenu d une case donnee
	lvl.grid[y-1][x-1]=n

"""
Level.Hp_1 (i)
Description : enlève 1 hp au monstre correspondant à l'indice i
"""
def Hp_1(i):
    Monster.setHp(m,i,Monster.gethp(m,i)-1)
    if Monster.gethp(m,i)==0:
         del m[i]

"""
Level.pos_monsters (lvl)
Description : retourne les positions de tous les monstres
"""
def pos_monsters(lvl):
    l=[]
    for i in range (len(m)):
        l.append(Monster.getpos(m,i))
    return l

"""
Level.show(lvl)
	Description : affiche la grille
"""
def show (lvl):
    #couleur fond
    sys.stdout.write("\033[40m")
    #couleur white
    sys.stdout.write("\033[37m")
    
    for y in range(0,len(lvl.grid)):
        for x in range(0,len(lvl.grid[y])):
            s="\033["+str(y+1)+";"+str(x+1)+"H"
            sys.stdout.write(s)
            #affiche
            sys.stdout.write(lvl.grid[y][x]) 

    for i in range (Monster.getlen(m)):
        Monster.show(m,i)  

"""
Level.live (lvl,dt)
Description : calcul les collisions des éléments de monstres et des projectiles
"""
def live (lvl,dt):
    # gestion déplacement monstre en fonction du type de monstre
    # gestion chute monstre en cas de collision

    for i in range (Monster.getlen(m)):
        posx,posy=Monster.getpos(m,i)
        objetm=getChar(lvl,round(posx),round(posy))
        objetm1=getChar(lvl,round(posx+8.0),round(posy))

        if m[i][0]==0 :
            d=Monster.getDirection(m,i)
            #if m[i][1]>=2 and m[i][1]<=154:
            if objetm!="|" and d==-1:
                Monster.move(m,i,posx+d*4*dt,posy)
            elif objetm=="|":
                Monster.setDirection(m,i,1)
            if objetm1!="|" and d==1:
                Monster.move(m,i,posx+d*4*dt,posy)
            elif objetm1=="|":
                Monster.setDirection(m,i,-1)

        if Monster.getCooldown(m,i)>=random.randint(50,400): # on veut que les monstres tirent aléatoirement
            Monster.initShoot(m,i)
        Monster.Shoot(m,i,dt)
        
        if objetm == " " :
            Monster.chute(m,i,dt)
        
        xy=posShoot()
        for i in range (len(xy)):
            if getChar(lvl,round(xy[i][0]),round(xy[i][1])) == "|":
                delShoot(lvl,i)
        
"""
Level.posShoot ()
Description : retourne les coordonées de touts les projectiles des monstres
"""
def posShoot():
    return Monster.posShoot(m)

"""
Level.delShoot ()
Description : supprime l un des projectiles
"""
def delShoot(lvl,i):
    Monster.setFire(m,i,False)

"""
Level.Newlvl (lvl)
Description : initialise le niveau suivant
"""
def Newlvl(lvl):
    i=getI(lvl)
    if i==(len(lvl.levels)-1):
        Animation.setOn(end,True)
        while Animation.getOn(end):
            Animation.show(end)
        old_settings = termios.tcgetattr(sys.stdin)
        termios.tcsetattr(sys.stdin, termios.TCSADRAIN, old_settings)
        sys.exit()
    else :
        i+=1
    #effacer la console
    sys.stdout.write("\033[1;1H")
    sys.stdout.write("\033[2J")
    setI(lvl,i)
    parse(lvl,lvl.levels[i])
    Monster.newMonster(m,i)
    return lvl

"""
Level.testFull() : booléen
	Description : teste si les fonctions associées à la structure de Level fonctionnent
"""
def testFull ():
    level=create("level.txt")
    show(level)

#testFull()