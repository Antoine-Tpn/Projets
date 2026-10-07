#include "hts221.h"
#include "drv_i2c.h"
#include <stdint.h>

//==============================================================
// HTS221
//==============================================================

#define HTS221_I2C_ADDRESS      0xBE

#define TEMPERATURE_READY       0x01
#define HUMIDITY_READY          0x02
#define POWER_UP                0x80
#define BDU_SET                 0x04
#define ODR0_SET                0x01

//==============================================================
// REGISTERS
//==============================================================

#define WHO_AM_I                0x0F
#define AV_CONF                 0x10
#define CTRL_REG1               0x20
#define CTRL_REG2               0x21
#define CTRL_REG3               0x22
#define STATUS_REG              0x27

#define HUMIDITY_OUT_L          0x28
#define HUMIDITY_OUT_H          0x29
#define TEMP_OUT_L              0x2A
#define TEMP_OUT_H              0x2B

#define CALIB_0                 0x30    // H0_rH_x2
#define CALIB_1                 0x31    // H1_rH_x2
#define CALIB_2                 0x32    // T0_degC_x8
#define CALIB_3                 0x33    // T1_degC_x8
#define CALIB_5                 0x35    // T1/T0 MSB

#define CALIB_6                 0x36    // H0_T0_OUT LSB
#define CALIB_7                 0x37    // H0_T0_OUT MSB
#define CALIB_A                 0x3A    // H1_T0_OUT LSB
#define CALIB_B                 0x3B    // H1_T0_OUT MSB

#define CALIB_C                 0x3C    // T0_OUT LSB
#define CALIB_D                 0x3D    // T0_OUT MSB
#define CALIB_E                 0x3E    // T1_OUT LSB
#define CALIB_F                 0x3F    // T1_OUT MSB

//==============================================================
// CALIBRATION VARIABLES
//==============================================================

uint8_t _h0_rH;
uint8_t _h1_rH;

int16_t _T0_degC;
int16_t _T1_degC;

int16_t _H0_T0;
int16_t _H1_T0;

int16_t _T0_OUT;
int16_t _T1_OUT;


//==============================================================
// WHO AM I
//==============================================================

uint8_t hts221_whoAmI(void)
{
    uint8_t id = 0;

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       WHO_AM_I,
                       &id,
                       1);

    return id;
}


//==============================================================
// ACTIVATE
//==============================================================

void hts221_activate(void)
{
    uint8_t data;

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CTRL_REG1,
                       &data,
                       1);

    data |= POWER_UP;
    data |= ODR0_SET;

    i2c1_WriteRegBuffer(HTS221_I2C_ADDRESS,
                        CTRL_REG1,
                        &data,
                        1);
}


//==============================================================
// BDU
//==============================================================

void hts221_bduActivate(void)
{
    uint8_t data;

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CTRL_REG1,
                       &data,
                       1);

    data |= BDU_SET;

    i2c1_WriteRegBuffer(HTS221_I2C_ADDRESS,
                        CTRL_REG1,
                        &data,
                        1);
}


//==============================================================
// CONFIGURATION
//==============================================================

void hts221_conf(void)
{
    uint8_t data = 0x1B;

    i2c1_WriteRegBuffer(HTS221_I2C_ADDRESS,
                        AV_CONF,
                        &data,
                        1);
}


//==============================================================
// STORE CALIBRATION
//==============================================================

void hts221_storeCalibration(void)
{
    uint8_t data;
    uint8_t low;
    uint8_t high;

    // H0_rH_x2
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_0,
                       &data,
                       1);

    _h0_rH = data;

    // H1_rH_x2
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_1,
                       &data,
                       1);

    _h1_rH = data;


    // T0_degC_x8
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_2,
                       &low,
                       1);

    // T1_degC_x8
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_3,
                       &data,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_5,
                       &high,
                       1);

    _T0_degC = ((int16_t)(high & 0x03) << 8) | low;
    _T1_degC = ((int16_t)((high >> 2) & 0x03) << 8) | data;


    // H0_T0_OUT
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_6,
                       &low,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_7,
                       &high,
                       1);

    _H0_T0 = (int16_t)(((uint16_t)high << 8) | low);


    // H1_T0_OUT
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_A,
                       &low,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_B,
                       &high,
                       1);

    _H1_T0 = (int16_t)(((uint16_t)high << 8) | low);


    // T0_OUT
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_C,
                       &low,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_D,
                       &high,
                       1);

    _T0_OUT = (int16_t)(((uint16_t)high << 8) | low);


    // T1_OUT
    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_E,
                       &low,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       CALIB_F,
                       &high,
                       1);

    _T1_OUT = (int16_t)(((uint16_t)high << 8) | low);
}


//==============================================================
// GET TEMPERATURE
//==============================================================

int hts221_getTemperature(float *temperature)
{
    uint8_t status;
    uint8_t temp_lsb;
    uint8_t temp_msb;

    int16_t t_out;

    float temperature_cal;

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       STATUS_REG,
                       &status,
                       1);

    if (!(status & TEMPERATURE_READY))
        return -1;


    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       TEMP_OUT_L,
                       &temp_lsb,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       TEMP_OUT_H,
                       &temp_msb,
                       1);

    t_out = (int16_t)(((uint16_t)temp_msb << 8) | temp_lsb);


    if (_T1_OUT == _T0_OUT)
        return -1;


    temperature_cal =
        ((float)_T0_degC / 8.0f)
        +
        ((float)(t_out - _T0_OUT)
        * ((float)(_T1_degC - _T0_degC) / 8.0f))
        / (float)(_T1_OUT - _T0_OUT);


    *temperature = temperature_cal;

    return 0;
}


//==============================================================
// GET HUMIDITY
//==============================================================

int hts221_getHumidity(float *humidity)
{
    uint8_t status;
    uint8_t hum_lsb;
    uint8_t hum_msb;

    int16_t h_out;

    float humidity_cal;

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       STATUS_REG,
                       &status,
                       1);

    if (!(status & HUMIDITY_READY))
        return -1;


    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       HUMIDITY_OUT_L,
                       &hum_lsb,
                       1);

    i2c1_ReadRegBuffer(HTS221_I2C_ADDRESS,
                       HUMIDITY_OUT_H,
                       &hum_msb,
                       1);


    h_out = (int16_t)(((uint16_t)hum_msb << 8) | hum_lsb);


    if (_H1_T0 == _H0_T0)
        return -1;


    humidity_cal =
        ((float)_h0_rH / 2.0f)
        +
        ((float)(h_out - _H0_T0)
        * ((float)(_h1_rH - _h0_rH) / 2.0f))
        / (float)(_H1_T0 - _H0_T0);


    *humidity = humidity_cal;

    return 0;
}
