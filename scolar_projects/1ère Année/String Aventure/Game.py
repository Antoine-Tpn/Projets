################
# Game.py      #
# A.Turpin     #
# 01/04/2023   #
# S2-P IPI     #
################

import sys,os
import Level
import Player

class Game : pass

def create(dt):
    global g,lvl,p
    g=Game()
    lvl=Level.create(["level1.txt","level2.txt","level3.txt"],0,dt)
    p=Player.create()
    return g

"""
Game.show(g)
	Description : affiche le visuel de la partie en cours
"""
def show(g):
    Level.show(lvl)
    Player.show(p)

    #affichage points de vie joueur
    txt="\033["+str(37)+";"+str(75)+"H"
    sys.stdout.write(txt)
    #couleur fond noire
    sys.stdout.write("\033[40m")
    sys.stdout.write("PV : "+str(Player.getHp(p))+"")

    # affichage commandes à utiliser
    txt="\033["+str(35)+";"+str(40)+"H"
    sys.stdout.write(txt)
    #couleur fond noire
    sys.stdout.write("\033[40m")
    sys.stdout.write("Z : Sauter , Q : Gauche , D : Droite , S : Descendre , P : Tirer\n")
    
    
"""
Game.live(g,dt)
	Description : calcul toutes les collisions du jeu
"""
def live (g,dt):
    Level.live(lvl,dt)
    Player.live(p,dt)

    # Vérification contact avec monstre et projectile
    x,y=Player.getpos(p)
    x,y=round(x),round(y)
    l=Level.pos_monsters(lvl) 
    S=Player.getposFire(p)

    for j in range (len(S)):
        xf,yf=S[j][0],S[j][1]
        xf,yf=round(xf),round(yf)
        collShoot=(collision(g,[round(xf+1.0),round(yf)]))
        if collShoot=="|":      # si le projectile est sur un mur
            Player.showShoot(p,j)  # alors on arrête de l'afficher
    
    for i in range (len(l)): # pour chaque monstres dans la liste
        xm,ym=l[i]
        xm,ym=round(xm),round(ym)
        
        # on regarde si le joueur est en collision avec un monstre
        if (x<=xm+5 and x+5>=xm ) and y==ym :
            Player.setHp(p,Player.getHp(p)-1)
            Level.Hp_1(i)
        # on regarde si le projectile est sur la position d'un monstre
        S=Player.getposFire(p)
        for j in range (len(S)):
            xf,yf=S[j][0],S[j][1]
            xf,yf=round(xf),round(yf)

            if  yf==ym and (xf==xm or xf==xm+7):
                Level.Hp_1(i)
                Player.showShoot(p,j)

    # création projectiles des montres et vérification collisions avec joueur
    xy=Level.posShoot()
    for i in range(len(xy)):
        if y==round(xy[i][1]) and (round(xy[i][0])==x or round(xy[i][0])==x+5):
            Level.delShoot(lvl,i)
            Player.setHp(p,Player.getHp(p)-1)
    # Nouveau niveau si collision avec la porte
    if collision(g,[x,y])=="[":
        Player.setX(p,5)
        Player.setY(p,5)
        Level.Newlvl(lvl)

    h=Player.getH(p)        
    if h>=0:
        Player.jump(p,dt)

    # Chute du joueur
    collisions=[collision(g,[x,y]),
                collision(g,[x+1,y]),
                collision(g,[x+2,y]),
                collision(g,[x+3,y]),
                collision(g,[x+4,y]),
                collision(g,[x+5,y])]
    for i in range (len(collisions)):
            if collisions[i]=="_" :
                return None 
            
    
    if h<=0:
        Player.chute(p,dt)

"""
Game.collision(g,xy)
Description : indique l élément présent sur la grille au point x,y
"""
def collision(g,xy):
    return Level.getChar(lvl,xy[0],xy[1])
        
"""
Game.jump (g)
Description : saut du joueur en fonction des collisions
"""
def jump (g,dt):
    x,y=Player.getpos(p)
    x,y=round(x),round(y)
    collisions=[collision(g,[x,y]),collision(g,[x+1,y]),
                collision(g,[x+2,y]),collision(g,[x+3,y]),
                collision(g,[x+4,y])]
    for i in range (len(collisions)):
        if collisions[i]=="_":
            Player.setH(p,4.0)
            Player.jump(p,dt)
            return None

"""
Game.down ()
Description : le joueur descend d'une ligne
"""
def down():
    y=Player.getY(p)
    if y<=31:
        Player.setY(p,y+1)

"""
Game.getHp ()
Description : retourne la santé restante au joueur
"""
def getHp() :
    return Player.getHp(p)
        
"""
Game.left (g,dt)
Description : déplacement du joueur vers la gauche en fonction des collisions
"""
def left (g,dt):
    x,y=Player.getpos(p)
    x,y=round(x),round(y)
    if collision(g,[x-1,y]) !="|":
        Player.setLast_move(p,"gauche")
        Player.move(p,Player.getX(p)-Player.getSpeed(p)*4*dt,Player.getY(p))
        
"""
Game.right (g,dt)
Description : déplacement du joueur vers la droite en fonction des collisions
"""
def right(g,dt):
    x,y=Player.getpos(p)
    x,y=round(x),round(y)
    if collision(g,[x+5,y]) !="|":
        Player.setLast_move(p,"droite")
        Player.move(p,Player.getX(p)+Player.getSpeed(p)*4*dt,Player.getY(p))

"""
Game.firet (g,dt)
Description : appel de la fonction fire de player
"""
def fire():
    i=Player.NewShoot(p)
    Player.fire(p,i)
