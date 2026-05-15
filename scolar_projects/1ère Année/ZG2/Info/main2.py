import TimeSeries
import sys


def main():

    #retreive first parameter
    if len(sys.argv)>=2:
        filename = str(sys.argv[1])
    else :
        print("Usage : $python main.py file.csv [timestamp_col_number]")
        quit()

    #retreive second parameter
    if len(sys.argv)==3:
        timestamp_col_number = int(sys.argv[2])
    else :
        timestamp_col_number = 0

    #build time series
    ts=TimeSeries.create_from_csv(csv_filename=filename,
                                  time_stamp_column_number=timestamp_col_number,
                                  xlabel='temps en secondes',
                                  ylabel='distance en milimètres')

    #swap curves
    TimeSeries.swap_column(ts,1,2)

    #show and/or save curves
    TimeSeries.plot(ts,show=True,save=True)

if __name__=="__main__":

    main()