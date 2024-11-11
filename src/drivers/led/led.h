#ifndef LED_H
#define LED_H

int led_init(void);
void led_set(int target, int onoff);
void led_blink(int target);

#endif /* LED_H */
