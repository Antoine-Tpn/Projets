#include "main.h"
#include "lps22hb.h"

//###################################################################
#define VL6180X_PRESS_HUM_TEMP	1
#define MPU9250	0
#define DYN_ANEMO 0

// ===================================================================
/* Exemple de commandes a envoyer pour configurer le terminal :
 *
 *  ifconfig
 *  ip_set_can
 *  candump can0
 *
 *  Puis pour communiquer avec cette carte :
 *  cansend can0 002#02 01    Température
 *  cansend can0 002#02 02	  Humidité
 *  cansend can0 002#02 03    Pression
 *  cansend can0 002#02 04    Luminosité
 *  cansend can0 002#02 05	  Distance
 *
*/
// ===================================================================

//###################################################################

//====================================================================
//			CAN ACCEPTANCE FILTER
//====================================================================
#define USE_FILTER	2
// Can accept until 4 Standard IDs
#define ID_1	0x01
#define ID_2	0x02
#define ID_3	0x03
#define ID_4	0x04
//====================================================================
extern void systemClock_Config(void);

void (*rxCompleteCallback) (void);
void can_callback(void);

CAN_Message      rxMsg;
CAN_Message      txMsg;
long int        counter = 0;
volatile int mode = 0 ;
volatile int can_request = 0;
uint8_t* aTxBuffer[2];

extern float magCalibration[3];

void VL6180x_Init(void);
void VL6180x_Step(void);

int status;
int new_switch_state;
int switch_state = -1;

// === Partie configuration Capteurs ===
float temperature = 0.0f;
float humidity = 0.0f;
float pression =0.0f;
int distance=0;
int luminosity=0;
int16_t value = 0 ;
//====================================================================
// >>>>>>>>>>>>>>>>>>>>>>>>>> MAIN <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
//====================================================================
int main(void)
{
	HAL_Init();

	systemClock_Config();
    SysTick_Config(HAL_RCC_GetHCLKFreq() / 1000); //SysTick end of count event each 1ms
	uart2_Init();
	uart1_Init();
	i2c1_Init();

	__HAL_RCC_GPIOC_CLK_ENABLE();
	GPIO_InitTypeDef GPIO_InitStruct = {0};
	GPIO_InitStruct.Pin = GPIO_PIN_13;
	GPIO_InitStruct.Mode = GPIO_MODE_INPUT;
	GPIO_InitStruct.Pull = GPIO_PULLUP;
	HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);


	// Configuration du HTS221
    hts221_conf();
    hts221_storeCalibration();
    hts221_bduActivate();
    hts221_activate();

	// Configuration LPS22HB
	lps22hb_setup();
	uint8_t lps_id = lps22hb_whoAmI();

	HAL_Delay(1000); // Wait
	// =====================================

#if VL6180X_PRESS_HUM_TEMP
    VL6180x_Init();
    VL6180x_Prepare(theVL6180xDev);
    InitAlsMode();
#endif


    can_Init();
    can_SetFreq(CAN_BAUDRATE); // CAN BAUDRATE : 500 MHz -- cf Inc/config.h
#if USE_FILTER
    can_Filter_list((ID_1<<21)|(ID_2<<5) , (ID_3<<21)|(ID_4<<5) , CANStandard, 0); // Accept until 4 Standard IDs
#else
    can_Filter_disable(); // Accept everybody
#endif
    can_IrqInit();
    can_IrqSet(&can_callback);
    txMsg.id=0x55;
    txMsg.data[0]=1;
    txMsg.data[1]=2;
    txMsg.len=2;
    txMsg.format=CANStandard;
    txMsg.type=CANData;

    can_Write(txMsg);
    // Décommenter pour utiliser ce Timer ; permet de déclencher une interruption toutes les N ms
    //tickTimer_Init(1000); // period in ms
    //HAL_StatusTypeDef ret;
    // Pour tester uniquement
    //can_request = 1;
    //mode = 2;

    while (1) {
        if (can_request)
		{
			if(mode == 1) {   // mode Température
				hts221_getTemperature(&temperature);
				value = (int16_t)(temperature * 100);
			}

			else if(mode == 2) { // mode Humidité
				hts221_getHumidity(&humidity);
				value = (int16_t)(humidity * 100);
			}

			else if(mode == 3) { // mode Pression
				lps22hb_getPressure(&pression);
				value = (int16_t)(pression);
			}

			else if (mode == 4) // mode Luminosité
			{
				AlsState();
				luminosity = (int)Als.lux;
				value = (int16_t)luminosity;
			}

			else if (mode == 5) // mode Distance
			{
				RangeState();
				distance = (int)range;
				value = (int16_t)distance;
			}
			//----------------------------
			// ECRITURE REPONSE
			//----------------------------
			 if (mode >= 1 && mode <= 5)
				{
				txMsg.id = 0x02;

				// Numéro du capteur / mode
				txMsg.data[0] = mode;
				// Valeur sur 2 octets
				txMsg.data[1] = (value >> 8) & 0xFF;
				txMsg.data[2] = value & 0xFF;
				txMsg.len = 3;
				txMsg.format = CANStandard;
				txMsg.type = CANData;

				can_Write(txMsg);
				can_request = 0;

			}
		}
        HAL_Delay(1000);
		#if VL6180X_PRESS_HUM_TEMP
			VL6180x_Step();
		#endif
    }
	return 0;
}


//====================================================================
//			CAN CALLBACK RECEPT
//====================================================================

void can_callback(void)
{
    CAN_Message msg_rcv;
    can_Read(&msg_rcv);

    if(msg_rcv.id == 0x01 && msg_rcv.data[0] == 0x02) // Si c'est a moi de parler
        {
            if(msg_rcv.data[1] >= 1 && msg_rcv.data[1] <= 5)
            {
                mode = msg_rcv.data[1]; // Je récupère mes données
                can_request = 1; // Alors je peux répondre

            }
        }
    else { // Sinon pas à moi de parler
    	can_request = 0;
    }
}

//====================================================================
//            TIMER CALLBACK PERIOD
//====================================================================

void HAL_TIM_PeriodElapsedCallback(TIM_HandleTypeDef *htim)
{
    CAN_Message msg_rcv;
    txMsg.id=0x02;            // Identifiant du message à envoyer
    txMsg.data[0]=(uint16_t)(0)& 0xFF;
    //txMsg.data[1]=(uint16_t)range & 0xFF;
    txMsg.len=1;            // Nombre d'octets à envoyer
    txMsg.format=CANStandard;
    txMsg.type=CANData;
    //term_printf("from timer interrupt\n\r");
}
//====================================================================

#if VL6180X_PRESS_HUM_TEMP
void VL6180x_Init(void)
{
	uint8_t id;
	State.mode = 1;

    XNUCLEO6180XA1_Init();
    HAL_Delay(500); // Wait
    // RESET
    XNUCLEO6180XA1_Reset(0);
    HAL_Delay(10);
    XNUCLEO6180XA1_Reset(1);
    HAL_Delay(1);

    HAL_Delay(10);
    VL6180x_WaitDeviceBooted(theVL6180xDev);
    id=VL6180x_Identification(theVL6180xDev);
    term_printf("id=%d, should be 180 (0xB4) \n\r", id);
    VL6180x_InitData(theVL6180xDev);

    State.InitScale=VL6180x_UpscaleGetScaling(theVL6180xDev);
    State.FilterEn=VL6180x_FilterGetState(theVL6180xDev);

     // Enable Dmax calculation only if value is displayed (to save computation power)
    VL6180x_DMaxSetState(theVL6180xDev, DMaxDispTime>0);

    switch_state=-1 ; // force what read from switch to set new working mode
    State.mode = AlrmStart;
}
//====================================================================
void VL6180x_Step(void)
{
    DISP_ExecLoopBody();

    new_switch_state = XNUCLEO6180XA1_GetSwitch();
    if (new_switch_state != switch_state) {
        switch_state=new_switch_state;
        status = VL6180x_Prepare(theVL6180xDev);
        // Increase convergence time to the max (this is because proximity config of API is used)
        VL6180x_RangeSetMaxConvergenceTime(theVL6180xDev, 63);
        if (status) {
            AbortErr("ErIn");
        }
        else{
            if (switch_state == SWITCH_VAL_RANGING) {
                VL6180x_SetupGPIO1(theVL6180xDev, GPIOx_SELECT_GPIO_INTERRUPT_OUTPUT, INTR_POL_HIGH);
                VL6180x_ClearAllInterrupt(theVL6180xDev);
                State.ScaleSwapCnt=0;
                DoScalingSwap( State.InitScale);
            } else {
                 State.mode = RunAlsPoll;
                 InitAlsMode();
            }
        }
    }

    switch (State.mode) {
    case RunRangePoll:
        RangeState();
        break;

    case RunAlsPoll:
        AlsState();
        break;

    case InitErr:
        TimeStarted = g_TickCnt;
        State.mode = WaitForReset;
        break;

    case AlrmStart:
       GoToAlaramState();
       break;

    case AlrmRun:
        AlarmState();
        break;

    case FromSwitch:
        // force reading swicth as re-init selected mode
        switch_state=!XNUCLEO6180XA1_GetSwitch();
        break;

    case ScaleSwap:

        if (g_TickCnt - TimeStarted >= ScaleDispTime) {
            State.mode = RunRangePoll;
            TimeStarted=g_TickCnt; /* reset as used for --- to er display */
        }
        else
        {
        	DISP_ExecLoopBody();
        }
        break;

    default: {
    	 DISP_ExecLoopBody();
          if (g_TickCnt - TimeStarted >= 5000) {
              NVIC_SystemReset();
          }
    }
    }
}

#endif
//====================================================================

