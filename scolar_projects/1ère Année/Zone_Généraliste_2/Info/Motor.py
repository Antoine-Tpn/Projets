#motor.py
import TimeSeries

class Motor : pass

def create(R=0.5,K=0.1,L=0.3,J=0.0044,U=10.):
    motor = Motor()
    motor.R=R
    motor.K=K
    motor.L=L 
    motor.J=J 
    motor.U=U
    motor.omega0=0.0
    motor.omega1=0.0
    motor.omega2=0.0 
    return motor

def get_R(ts) : return ts.R
def get_K(ts) : return ts.K
def get_L(ts) : return ts.L
def get_J(ts) : return ts.J
def get_U(ts) : return ts.U
def get_omega0(ts) : return ts.omega0
def get_omega1(ts) : return ts.omega1
def get_omega2(ts) : return ts.omega2

def set_R(ts, value) : ts.R=value
def set_K(ts, value) : ts.K=value
def set_L(ts, value) : ts.L=value
def set_J(ts, value) : ts.J=value
def set_U(ts, value) : ts.U=value
def set_omega0(ts,value) : ts.omega0=value
def set_omega1(ts,value) : ts.omega1=value
def set_omega2(ts,value) : ts.omega2=value



def simulate_slow(motor,sim_dt=0.0001,log_dt=0.0,duration=7.):
    data=[]
    
    log_timer=0
    t=0
    
    while(t<=duration):
  
        motor.omega2=((motor.U/motor.K) + motor.omega0*(-1.0) + motor.omega1*(-(motor.R*motor.J)/(motor.K*motor.K)))*((motor.K*motor.K)/(motor.L*motor.J))
        motor.omega1+=motor.omega2*sim_dt
        motor.omega0+=motor.omega1*sim_dt        
        
        t+=sim_dt
        log_timer+=sim_dt

        if log_timer>log_dt:
            log_timer=0
            data.append([t,motor.omega0,motor.omega1,motor.omega2])
 
    ts= TimeSeries.create()

    TimeSeries.set_data(ts,data)
    TimeSeries.set_labels(ts,["t(s)","Omega(t)","dOmega(t)/dt","d2Omega(t)/d2t",])
    TimeSeries.plot(ts,title='Courbe du moteur',filename='motor.png')
    TimeSeries.dump(ts,'motor.csv')
    return ts

def simulate(motor,sim_dt=0.0001,log_dt=0.0,duration=7.):
    data=[]
    
    log_timer=0
    t=0
    
    R=motor.R
    U=motor.U
    K=motor.K
    J=motor.J
    L=motor.L

    C0=U/K
    C1=-1.0
    C2=-(R*J)/(K*K)
    factor=(K*K)/(L*J)

    while(t<=duration):
        motor.omega2=(C0 + motor.omega0*C1 + motor.omega1*C2)*factor
        motor.omega1+=motor.omega2*sim_dt
        motor.omega0+=motor.omega1*sim_dt        
        t+=sim_dt
        log_timer+=sim_dt
        if log_timer > log_dt:
            log_timer=0
            data.append([t,motor.omega0,motor.omega1,motor.omega2])
 
    ts= TimeSeries.create()

    TimeSeries.set_data(ts,data)
    TimeSeries.set_labels(ts,["t(s)","Omega(t)","dOmega(t)/dt","d2Omega(t)/d2t",])
    TimeSeries.plot(ts,title='Courbe du moteur',filename='motor.png')
    TimeSeries.dump(ts,'motor.csv')
    return ts


def simulate_fast(motor,sim_dt=0.0001,log_dt=0.0,duration=7.):
    data=[]
    
    log_timer=0
    t=0
    
    R=motor.R
    U=motor.U
    K=motor.K
    J=motor.J
    L=motor.L

    C0=U/K
    C1=-1.0
    C2=-(R*J)/(K*K)
    factor=(K*K)/(L*J)

    omega0=motor.omega0
    omega1=motor.omega1
    omega2=motor.omega2

    while(t<=duration):
        omega2=(C0 + omega0*C1 + omega1*C2)*factor
        omega1+=omega2*sim_dt
        omega0+=omega1*sim_dt        
        t+=sim_dt
        log_timer+=sim_dt
        if log_timer>log_dt:
            log_timer=0
            data.append([t,omega0,omega1,omega2])
 
    ts= TimeSeries.create()

    TimeSeries.set_data(ts,data)
    TimeSeries.set_labels(ts,["t(s)","Omega(t)","dOmega(t)/dt","d2Omega(t)/d2t",])
    TimeSeries.plot(ts,title='Courbe du moteur',filename='motor.png')
    TimeSeries.dump(ts,'motor.csv')
    return ts


if __name__=="__main__":
    
    #sans profilage

    Ks=[0.01+x/1000. for x in range(2000) ]

    for k in Ks:
        m=create(K=k)
        ts=simulate_slow(m,0.0001,0.1,7.0)
        TimeSeries.plot(ts,title='Courbe du moteur',filename='motor.png')
        TimeSeries.dump(ts,'motor.csv')
        #print(k, TimeSeries.compute_period(ts,1))
  


    '''
    m=create()
    ts=simulate_slow(m,0.01,0.1,7.0)
    TimeSeries.dump(ts,'motor2.csv')
   
    m=create()
    ts=simulate_slow(m,0.1,0.1,7.0)
    TimeSeries.dump(ts,'motor3.csv')
   
    #profilage
    import cProfile
    #import timeit
   
    m1=create()
    m2=create()
    m3=create()
    
    #c_profile
    cProfile.run('ts1=simulate_slow(m2,0.00001,0.001,7.0)')
    cProfile.run('ts=simulate(m1,0.00001,0.001,7.0)')
    cProfile.run('ts2=simulate_fast(m3,0.00001,0.001,7.0)')
    
    #en utilisant timeit
    #print(timeit.timeit('m=Motor.create();ts1=Motor.simulate_slow(m,0.0001,0.001,7.0)',setup='import Motor',number=20))
   ''' 
