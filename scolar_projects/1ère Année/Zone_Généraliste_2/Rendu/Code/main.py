####################
# ZG2 Equipe 2 
# Turpin Campos 
# Jannin Andro
# 12/06/2023
# ZG2
####################

import csv
import matplotlib as plt
from math import *
import TimeSeries as TS
import sys

# fonction permettant d'initialiser les paramètres de l'équation différentielle
def run ():
    
    parameters={} #liste étant les paramètres de l'équation différentielle de simulation
    
    # paramètres de l'équation différentielle
    # distance jeu amortisseur 614 mm
    g = 9.81
    print("\n D'après les valeurs déterminées nous avons pour une simulation du rafale : ")
    print("m=3062.5 où 4036 à vide k=90 800 et lambda=29 616.9 \n")
    print(" Nous avons pour une simulation de la maquette : ")
    print("m=1.6 kg k=87 \n")
    masse = float(input('1 : Entrez la valeur de la masse m: '))
    k=float(input('2 : Entrez la valeur de k raideur du ressort : '))
    amortissement = float(input('3 : Entrez la valeur de lambda : '))
    delta = amortissement **2 - 4 * masse * k

    parameters["g"]=g
    parameters["k"]=k
    parameters["delta"]=delta
    parameters["masse"]=masse
    parameters["amortissement"]=amortissement
    print("souhaitez vous tracer une courbe théorique du rafale ou des courbes expérimentales de la maquette ? ")
    n=int(input("veuillez indiquer 0 pour une courbe théorique ou 1 pour une courbe expérimentale: "))
    if n==1:
        i=int(input("veuillez indiquer l'indice i du fichier que vous souhaitez utiliser (entre 0 et 6) "))
        fichiers=["data_air/resultatsZG2_0_deg.csv","data_air/resultatsZG2_30_deg.csv","data_air/resultatsZG2_60_deg.csv","data_eau/resultatsZG2_0_deg.csv",
            "data_eau/resultatsZG2_20_deg.csv","data_eau/resultatsZG2_40_deg.csv","data_eau/resultatsZG2_60_deg.csv"]

        ts=TS.create_from_csv(csv_filename=fichiers[i],           
                            time_stamp_column_number=0,
                            xlabel='temps en secondes',
                            ylabel='distance en milimètres')
        TS.set_parameters(ts,parameters)          # on modifie dans notre objet TimeSeries les paramètres
        print(parameters)                         # on affiche pour s'assurer d'avoir les bons paramètres
        TS.plot(ts,parameters)                               # puis on trace les courbes
    else:
        ts= TS.create()
        TS.plot_theorique(ts,parameters)


run()