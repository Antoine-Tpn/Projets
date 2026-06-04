####################
# G.Desmeulles
# 22/03/2023
# ZG2-1-2 seance 1
####################
import csv
import matplotlib.pyplot as plt

class TimeSeries : pass

def create():
    ts=TimeSeries()
    ts.data=[]
    ts.labels=[]
    return ts

def create_from_csv(csv_filename,
                  time_stamp_column_number,
                  xlabel,
                  ylabel):
    ts=create()
    ts.xlabel=xlabel

    with open(csv_filename, newline='') as csvfile:
        spamreader = csv.reader(csvfile, delimiter=',', quotechar='"')
        for row in spamreader:
            ts.data.append(row)
    
        
        ts.labels=ts.data.pop(0)
   
        if time_stamp_column_number>0:
            swap_column(ts,time_stamp_column_number,0)
   
        for i in range(len(ts.data)):
            for j in range(len(ts.data[i])):
                ts.data[i][j]=float(ts.data[i][j])

        return ts
    
def get_data(ts): return ts.data
def get_labels(ts): return ts.labels

def set_data(ts,data): ts.data=data
def set_labels(ts,labels): ts.labels=labels

def swap_column(ts,n1,n2):
    for row in ts.data:
        x=row[n1]
        row[n1]=row[n2]
        row[n2]=x
    x=ts.labels[n1]
    ts.labels[n1]=ts.labels[n2]
    ts.labels[n2]=x

def plot(ts,x_label=None,y_label="",title="",filename=["Tracer_ressort_a","Tracer_ressort_b","Tracer_ressort_c"]):
    '''trace each curve with matplotlib'''
    color=["blue","red","green"]
    if x_label==None:
        x_label=ts.labels[0]
 
    nb_columns=len(ts.data[0])
    columns=[[] for x in range(nb_columns)]

    for i in range(0,len(ts.data)):
        for j in range(nb_columns) :
            columns[j].append(float(ts.data[i][j]))

    fig, ax = plt.subplots()

    for i in range(nb_columns-1):
        ax.plot(columns[0], columns[i+1], color[i] , label=ts.labels[i+1], linewidth=1.5)
        ax.set(xlabel=x_label, ylabel=y_label, title=title)
        ax.legend()
        if filename:
            fig.savefig(filename[i])
        plt.cla()


if __name__=="__main__":
    print("test TimeSeries")
    ts=create_from_csv(csv_filename="./HCSR04_data4_ressort_2022_03_10.csv",
                        time_stamp_column_number=3,
                        xlabel='temps en secondes',
                        ylabel='distance en milimètres')
    plot(ts)
    