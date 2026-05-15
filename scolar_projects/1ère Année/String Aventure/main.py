################
# main.py      #
# A.Turpin     #
# 01/04/2023   #
# S2-P IPI     #
################

# modules externes
import sys
import time
import select
import tty 
import termios

# mon module Game
import Game
import Animation

#interaction clavier
old_settings = termios.tcgetattr(sys.stdin)

"""
Main.init ()
	Description : initialisation du jeu
"""

def init ():
	global timeStep,end,start
	timeStep=0.015
	g=Game.create(timeStep)
	end=Animation.create("END.txt",timeStep)
	start=Animation.create("Start.txt",timeStep)
	# interaction clavier
	tty.setcbreak(sys.stdin.fileno())

	#effacer la console
	sys.stdout.write("\033[1;1H")
	sys.stdout.write("\033[2J")
	return g

"""
Main.run (g) 
	Description : boucle de simulation du jeu
"""
def run (g):
	global timeStep
	Animation.setOn(start,True)
	while Animation.getOn(start):
		Animation.show(start)
	#Boucle de simulation	
	while 1:
		# Si le joueur meurt on affiche un message 
		if Game.getHp()<=0:
			finish()
		# sinon on continu le jeu
		else:
			interact(g,timeStep)
			Game.live(g,timeStep)
			Game.show(g)
		time.sleep(timeStep)

"""
Main.isData () 
	Description : vérifie la présence d'évènements clavier
"""
def isData():
	#recuperation evenement clavier
	return select.select([sys.stdin], [], [], 0) == ([sys.stdin], [], [])

"""
Main.interact (g,dt) 
	Description : effet des événements clavier
"""
def interact (g,dt):
	t="droite"
	#gestion des evenement clavier
	#si une touche est appuyee
	if isData():
		c = sys.stdin.read(1)
		if c == '\x1b': # echap
			finish()
		elif c=='q' : # gauche
			t="gauche"
			Game.left(g,dt)
		elif c=='d' : # droite
			t="droite"
			Game.right(g,dt)
		elif c=='z' : #jump
			Game.jump(g,dt)
		elif c=='s': # descendre
			Game.down()
		elif c=='p' : # tire
			Game.fire()

"""
Main.finish () 
	Description : sortie du jeu
"""
def finish():
	#restoration parametres terminal
	global old_settings
	
	#effacer la console
	sys.stdout.write("\033[1;1H")
	sys.stdout.write("\033[2J")

	Animation.setOn(end,True)
	while Animation.getOn(end):
		Animation.show(end)

	termios.tcsetattr(sys.stdin, termios.TCSADRAIN, old_settings)
	sys.exit()

run(init())