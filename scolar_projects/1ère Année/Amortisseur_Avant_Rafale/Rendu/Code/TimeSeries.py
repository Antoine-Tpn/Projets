####################
# ZG2 Equipe 2 
# Turpin Campos 
# Jannin Andro
# 12/06/2023
# ZG2
####################
import csv
import matplotlib.pyplot as plt
from math import *

class TimeSeries : pass

def create():
    ts=TimeSeries()
    ts.data=[]
    ts.labels=[]
    ts.xlabel="temps en s"
    ts.ylabel="distance en cm"
    ts.parameters=[]
    return ts

def create_from_csv(csv_filename,
                  time_stamp_column_number,
                  xlabel,
                  ylabel):
    global titre
    ts=create()
    ts.xlabel=xlabel
    ts.ylabel=ylabel
    with open(csv_filename, newline='') as csvfile:           # on ouvre le fichier csv indiqué en paramètre
        spamreader = csv.reader(csvfile, delimiter=';', quotechar='"')    # on lit tous le fichier
        for row in spamreader:                               # et pour chacun des éléments on ajoute dans data
            ts.data.append(row)

    ts.labels=ts.data.pop(0)
    infos=ts.data.pop(0)             # ici on retire la première liste de data pour récupérer les titres
    #       date        type fluide     eau          degrés          valeur 
    titre=f"{infos[6][:10]} {ts.labels[4]} : {infos[4]}, {ts.labels[5][8:]} : {infos[5]} degrés" # titres que nous stockons ici
    if time_stamp_column_number>0:
        swap_column(ts,time_stamp_column_number,0)

    for i in range(len(ts.data)):
        for j in range(len(ts.data[i])):
            if j < 2:
                ts.data[i][j]=float(ts.data[i][j])     
            else:
                del ts.data[i][j:]
                break
    filtre(ts)
    return ts
    
def get_data(ts): return ts.data
def get_labels(ts): return ts.labels
def get_parameters(ts): return ts.parameters

def set_data(ts,data): ts.data=data
def set_labels(ts,labels): ts.labels=labels
def set_parameters(ts,parameters): ts.parameters=parameters

def dump(ts,filename):
    with open (filename,"w", newline="") as csvfile:
                spamwriter=csv.writer(csvfile,delimiter=";",)
                spamwriter.writerow(ts.labels)
                for line in ts.data:
                    output=[]
                    for val in line:
                        output.append(str(val))
                    spamwriter.writerow(output)


def swap_column(ts,n1,n2):
    for row in ts.data:
        x=row[n1]
        row[n1]=row[n2]
        row[n2]=x
    x=ts.labels[n1]
    ts.labels[n1]=ts.labels[n2]
    ts.labels[n2]=x

def plot(ts,parameters,title="",filename=["courbes","courbe_erreur"]):
    '''trace each curve with matplotlib'''
    
    color=["blue","red","green"] # couleur de la courbe
    x_label= ts.xlabel           # on récupère les noms des axes
    y_label= ts.ylabel

    if x_label==None and y_label==None:
        x_label=ts.labels[0]
        y_label=ts.labels[1]

    nb_columns=len(ts.data[0])   # on regarde combien de colonnes sont comprise dans la fichier
    columns=[[] for x in range(nb_columns)]

    for i in range(0,len(ts.data)): # pour chaque ligne contenu dans data
        for j in range(nb_columns) : # pour chacune des colonnes
            columns[j].append(float(ts.data[i][j])) #on ajoute dans columns l'élémentsde chaque colonnes


    # Partie Courbe De Simulation

    y0=columns[1][1]/1000 # dans la simulation on veut que le point de départ soit identique entre la courbe expérimentale et de simulation
    #print(f"y0={columns[1][1]/1000}")
    data=Delta (ts,parameters["delta"],parameters["amortissement"],parameters["masse"],y0) # on calcul delta de la courbe
    #print(data)
    parameters["c1"]=data[0]
    parameters["c2"]=data[1]

    dx = 0.01
    xs = [0]
    ys = [y0*1000]
    xmax = columns[0][-1]
    delta=parameters["delta"]
    while xs[-1] < xmax:
        x = xs[-1] + dx
        if delta < 0 :
            alpha=data[2]
            beta=data[3]
            y = exp(alpha * x) * (y0 * cos(beta * x) - y0 * (alpha / beta) * sin(beta * x))
        elif delta == 0:
            y = exp(data[2] * x)*(-data[2] * (y0) *x + y0)
        else:
            y = parameters["c1"]*exp(data[2] * x) + parameters["c2"] * exp(data[3] * x)
        ys.append(y*1000)
        xs.append(x)
    
    # Partie Courbe Expérimentale Et Courbe D'Erreur
    xa=[]
    ya=[]
    ye=[]
    x=columns[0][0]
    for i in range(nb_columns-1): # pour chaque lignes
        
        for j in range (len(columns[0])-1): # pour chaque colonnes
            if columns[0][j] != x: # si l'élément est différents de l'éléments précédent (pour éviter les doublons)
                xa.append(columns[0][j])
                ya.append(columns[1][j]) # on ajoute les éléments dans les listes associées
                x=columns[0][j]
                if j <= len(xs)-1:       # si j est plus petit que l'indice du
                    #xe.append(xs[j]-columns[0][j])
                    ye.append(ys[j]-columns[1][j])
                    #print(xs[j],columns[0][j])
                
        plt.title(titre)
        print(f"le nom du fichier est : {filename[i]}") # on indique à l'utilisateur le nom du fichier

    # Espilon moyenne des points de la courbe d'erreur
    epsilon_aire = 0.0
    y_max = ya[0]
    for k in range(len(ye)-1):
        epsilon_aire += ye[k]*(xa[k+1]-xa[k])    # Calcul de l'aire de cote dy et dx
    epsilon = epsilon_aire/len(ye)
    percent_epsilon = 100*epsilon/544            # Conversion de epsilon en pourcentage de la course utile

    print(f"epsilon = {epsilon}")
    print(f"pourcentage de la course utile = {percent_epsilon}%")

    # Tracage des courbes
    plt.xlabel(x_label)
    plt.ylabel(y_label)
    plt.plot(xa,ya, linewidth=1, color="red", label="courbe expérimentale")
    plt.plot(xs,ys, linewidth=1, color="blue", label="courbe de simulation")
    #plt.show()                                  
    plt.savefig(filename[0])
    plt.cla()

    plt.xlabel(x_label)
    plt.ylabel("distance de différence en mm")
    plt.plot(xa,ye, linewidth=2, color="green", label="courbe erreur")
    #plt.show()                               
    plt.savefig(filename[1])
    plt.cla()
    print("finish")

def plot_theorique(ts, parameters):
    data=Delta (ts,parameters["delta"],parameters["amortissement"],parameters["masse"],0) # on calcul delta de la courbe
    print(data)
    parameters["c1"]=data[0]
    parameters["c2"]=data[1]
    amortissement=parameters["amortissement"]
    masse=parameters["masse"]
    delta=parameters["delta"]
    g = 9.81
    k=parameters["k"]
    # solution de l'équation sans second membre
    alpha = -amortissement / (2* masse)
    beta = -sqrt(abs(delta))/(2 * masse)
    c1 = - masse * g / k
    c2 = (-c1 * alpha + 6.5) / beta

# initialisation des paramètres pour tracer la courbe
    dx = 0.01
    xs = [0]
    ys = [0]
    xmax = 5

# création de tous les points de la courbe
    while xs[-1] < xmax:
        x = xs[-1] + dx
        y = exp(alpha * x) * (c1 * cos(beta * x) + c2 * sin(beta * x)) + masse * g / k
        ys.append(y*1000)
        xs.append(x)
    # paramètres graphiques de la courbe
    print(delta)
    cm=614.055 #course max , 663
    cu=cm-70   # course utile
    ci=cu*0.8  # course sécurité / position isostatique
    plt.axhline(y=cm, color="red")
    plt.axhline(y=cu, color="green")
    plt.axhline(y=ci*1.05, color="purple")
    plt.axhline(y=ci*0.95, color="purple")
    plt.text(3, 20, f"k={k} lambda={amortissement} masse={masse} kg")
    plt.plot(xs, ys, linewidth=1.5,label="axc", color="blue")
    plt.xlabel("Temps (s)", size = 16,)
    plt.ylabel("Position (mm)", size = 16)
    plt.title('Courbe théorique de l\'amortissement')
    plt.title("Courbe théorique de l\'amortissement", 
            fontdict={'family': 'fantasy', 
                        'color' : 'red',
                        'weight': 'bold',
                        'size': 16},
            loc='center')
    plt.grid(True)
    plt.show()

def maxi(ts): 
    # Ici on cherche la valeur maximale de distance
    # contenu dans ts.data car on ne souhaite 
    # garder que les valeurs intéressante à étudier
    maxi=ts.data[0][0]
    for i in range(len(ts.data)):
            if ts.data[i][1] > maxi :
                maxi=ts.data[i][1]
    return maxi

def filtre(ts):
    # Comme les valeures qui nous intéresse 
    # ne sont que les valeurs à partir de la distance maximale
    # Alors nous pouvons supprimer toutes les valeurs précédente
    n=0
    t=0
    for i in range(len(ts.data)): # pour chaque éléments dans data
        n=i
        if ts.data[i][1] == maxi(ts) : # on cherche la maximum de data le plus loin dans le temps
            t=ts.data[i][0]            # on garde le t du maximum pour la suite
            break
    del(ts.data[:n])                    # puis à partir de ce maximum on supprime tous les éléments précédents

    moyenne=0
    a=0
    nb=0
    for i in range(len(ts.data)): # pour chaque éléments dans data
        ts.data[i][0]-=t          # on soustrait a chaque éléments T par rapport a son Ti
        if i >= len(ts.data)-101: # pour les derniers éléments de la liste (on prépare le calcul d'une moyenne de ces éléments)
            nb+=1                 # on compte le nombre d'éléments
            a+=ts.data[i][1]      # et on additione chacune des valeurs de l'axe des ordonnées
    
    moyenne=int(a/nb)             # puis on calcul la moyenne
    for i in range(len(ts.data)): # pour chaque éléments dans data
        ts.data[i][1]-=moyenne    # on soustrait la moyenne des points à l'équilibre pour centrer en 0 (sur l'axe des abscisses)
 
def Delta (ts,delta,amortissement,masse,y0):
    # solution de l'équation sans second membre
    if delta < 0 :
        alpha = -amortissement / (2* masse)
        beta = -sqrt(abs(delta))/(2*masse)
        c1 = y0
        c2 = -c1 * alpha * beta
        return c1,c2,alpha,beta
    
    elif delta == 0:
        racine = - amortissement/ (2* masse)
        c1 = -racine * y0
        c2 = y0
        return c1,c2,racine
    
    else :
        racine1 = (-amortissement + sqrt(delta))/(2* masse)
        racine2 = (-amortissement - sqrt(delta))/(2* masse)
        c1 = y0 + y0 * racine1 / (-racine1+racine2)
        c2 = -y0* racine1 / (-racine1+racine2)
        return c1,c2,racine1,racine2

if __name__=="__main__": # test du fichier
    print("test TimeSeries")
    ts=create_from_csv(csv_filename="./HCSR04_data4_ressort_2022_03_10.csv",
                        time_stamp_column_number=3,
                        xlabel='temps en secondes',
                        ylabel='distance en milimètres')
    plot(ts)