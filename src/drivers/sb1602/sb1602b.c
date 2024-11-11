#include <stdint.h>

#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include <zephyr/drivers/i2c.h>

#include "sb1602b.h"

LOG_MODULE_REGISTER(driver_sb1602, CONFIG_USER_DRV_SB1602_LOG_LEVEL);

#define VAL_CONTRAST            (CONFIG_USER_DEV_SB1602_CONTRAST)
#define VAL_CONTRAST_C54        ((VAL_CONTRAST >> 4) & 0x03)
#define VAL_CONTRAST_C3210      (VAL_CONTRAST & 0x0f)

#define REG(addr)               (*((volatile uint32_t*)addr))

#define CONTROL_BYTE(rs)        ((uint8_t)((rs)<<6))
#define CTRL_BYTE_CMD           CONTROL_BYTE(0)
#define CTRL_BYTE_DATA          CONTROL_BYTE(1)

#define CMD_CLEARDISPLAY            ((uint8_t)0x01)
#define CMD_RETHOME                 ((uint8_t)0x02)
#define CMD_ENTRYMODESET(id,s)      ((uint8_t)(0x04|((id)<<1)|(s)))
#define CMD_DISPONOFF(d,c,b)        ((uint8_t)(0x08|((d)<<2)|((c)<<1)|(b)))
#define CMD_FUNCSET(is)             ((uint8_t)(0x38|((0)<<2)|(is)))
#define CMD_SETDDRAMADDR(addr)      ((uint8_t)(0x80|(addr)))
#define CMD_INOSCFREQ(bs,f)         ((uint8_t)(0x10|((bs)<<3)|(f)))             //Internal OSC frequency
#define CMD_SETICONADDR(ac)         ((uint8_t)(0x40|(ac)))                      //Set ICON address
#define CMD_PICTRL_CSET(i,b,c54)    ((uint8_t)(0x50|(i)<<3|(b)<<2|(c54)))       //Power/Icon control/Contrast set
#define CMD_FLW_CTRL(f,rab)         ((uint8_t)(0x60|(f)<<3|(rab)))              //Follower control
#define CMD_CNTRSET(c3210)          ((uint8_t)(0x70|(c3210)))                   //Contrast set

#define CMD_ENTRYMODESET_NORMAL CMD_ENTRYMODESET(1,0)       //cursor move:right
//shift:off
#define CMD_DISPLAY_ON          CMD_DISPONOFF(1,0,0)
#define CMD_DISPLAY_OFF         CMD_DISPONOFF(0,0,0)
#define CMD_FUNCSET_IS0         CMD_FUNCSET(0)
#define CMD_FUNCSET_IS1         CMD_FUNCSET(1)

// delay: msec
#define DELAY_STABLE            (200000)
#define DELAY_CLEAR             (1080)
#define DELAY_CMD               (27)

#define I2C1_NODE               DT_NODELABEL(sb1602)

static const struct i2c_dt_spec dev_i2c = I2C_DT_SPEC_GET(I2C1_NODE);

static int write_cmd(uint8_t cmd, uint8_t data);
static int write_lcd(uint8_t cmd, uint8_t data, uint32_t usec);


int sb1602b_init(void)
{
    int ret;

    if (!i2c_is_ready_dt(&dev_i2c)) {
        LOG_ERR("I2C bus %s is not ready!", dev_i2c.bus->name);
        return -1;
    }

    //リセット解除から40ms必要
    k_msleep(40);

    //Function Set(IS=0)
    ret = write_cmd(CTRL_BYTE_CMD, CMD_FUNCSET_IS0);
    if (ret != 0) {
        LOG_ERR("fail Function Set(IS=0)");
        return -1;
    }

    //Function Set(IS=1)
    ret = write_cmd(CTRL_BYTE_CMD, CMD_FUNCSET_IS1);
    if (ret != 0) {
        LOG_ERR("fail Function Set(IS=1)");
        return -1;
    }

    /*
     * (Instruction table 1)internal OSC frequency
     *      BS=1(1/4bias)
     *      F2-0=0
     *      wait:26.3us
     */
    ret = write_cmd(CTRL_BYTE_CMD, CMD_INOSCFREQ(1, 0));
    if (ret != 0) {
        LOG_ERR("fail internal OSC frequency");
        return -1;
    }

    /*
     * (Instruction table 1)コントラスト調整など
     *      Power/ICON/Contrast Set
     *          Ion=1
     *          Bon=1
     *          C5-4=VAL_CONTRASTのb54
     *          wait:26.3us
     *
     *      Constrast Set:
     *          C3-0=VAL_CONTRASTのb3210
     *          wait:26.3us
     */
    ret = write_cmd(CTRL_BYTE_CMD, CMD_PICTRL_CSET(1, 1, VAL_CONTRAST_C54));
    if (ret != 0) {
        LOG_ERR("fail Power/ICON/Contrast Set");
        return -1;
    }
    ret = write_cmd(CTRL_BYTE_CMD, CMD_CNTRSET(VAL_CONTRAST_C3210));
    if (ret != 0) {
        LOG_ERR("fail Constrast Set");
        return -1;
    }

    /*
     * (Instruction table 1)Follower control
     *      Fon=1
     *      Rab=4
     *      wait:26.3us --> 電力安定のため200ms
     */
    ret = write_lcd(CTRL_BYTE_CMD, CMD_FLW_CTRL(1, 4), DELAY_STABLE);
    if (ret != 0) {
        LOG_ERR("fail Follower control");
        return -1;
    }

    //Function Set(IS=0)
    // ret = write_cmd(CTRL_BYTE_CMD, CMD_FUNCSET_IS0);
    // if (ret != 0) {
    //     LOG_ERR("fail Function Set(IS=0)");
    //     return -1;
    // }

    //display on
    ret = write_cmd(CTRL_BYTE_CMD, CMD_DISPLAY_ON);
    if (ret != 0) {
        LOG_ERR("fail display on");
        return -1;
    }

    //clear display
    ret = sb1602b_clear();
    if (ret != 0) {
        LOG_ERR("fail clear display");
        return -1;
    }

    //entry mode set
    ret = write_cmd(CTRL_BYTE_CMD, CMD_ENTRYMODESET_NORMAL);
    if (ret != 0) {
        LOG_ERR("fail entry mode set");
        return -1;
    }

    LOG_INF("sb1602b_init: done.");
    return 0;
}

/**
 * 画面クリア
 */
int sb1602b_clear(void)
{
    return write_lcd(CTRL_BYTE_CMD, CMD_CLEARDISPLAY, DELAY_CLEAR);
}

/**
 * カーソル移動
 *
 * @param[in]   x   X座標(0～15)
 * @param[in]   y   Y座標(0 or 1)
 */
int sb1602b_move(int x, int y)
{
    switch (y) {
    case 0:
        y = 0x00;
        break;
    case 1:
        y = 0x40;
        break;
    default:
        return -1;
    }
    return write_cmd(CTRL_BYTE_CMD, CMD_SETDDRAMADDR(y | x));
}

/**
 * 現在のカーソル位置から文字列出力
 *
 * @param[in]   pStr    文字列
 */
void sb1602b_print(const char *p_str)
{
    while (*p_str) {
        (void)write_cmd(CTRL_BYTE_DATA, *p_str++);
    }
}

void sb1602b_output(const char *p_str, uint16_t len)
{
    for (uint16_t lp = 0; lp < len; lp++) {
        (void)write_cmd(CTRL_BYTE_DATA, *p_str++);
    }
}

static int write_cmd(uint8_t cmd, uint8_t data)
{
    return write_lcd(cmd, data, DELAY_CMD);
}

/**
 * ST7032iへの出力
 *
 * @param[in]   ctrl    コントロールバイト
 * @param[in]   data    データ
 * @param[in]   usec    待ち時間[usec]
 */
static int write_lcd(uint8_t ctrl, uint8_t data, uint32_t usec)
{
    const uint8_t buf[2] = {ctrl, data};
    int ret = i2c_write_dt(&dev_i2c, buf, sizeof(buf));
    if (ret != 0) {
        LOG_ERR("fail i2c_write_dt: addr=%02x [%02x]<--%02x (err=%d)", dev_i2c.addr, buf[0], buf[1], ret);
        return ret;
    }
    k_usleep(usec);
    return 0;
}
