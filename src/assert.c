#include <zephyr/kernel.h>

#include "drivers/led/led.h"

#define ERR_LED_BLINK_MSEC      (100)

#ifdef CONFIG_ASSERT
#ifdef CONFIG_ASSERT_NO_FILE_INFO
void assert_post_action(void)
{
#else // CONFIG_ASSERT_NO_FILE_INFO
void assert_post_action(const char *file, unsigned int line)
{
    (void)file;
    (void)line;
#endif // CONFIG_ASSERT_NO_FILE_INFO
    for (;;) {
        k_sleep(K_MSEC(ERR_LED_BLINK_MSEC));
        led_blink(0);
    }
}
#endif // CONFIG_ASSERT
