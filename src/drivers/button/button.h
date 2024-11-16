#ifndef BUTTON_H
#define BUTTON_H

#include <stdbool.h>

// pressed/unpressed callback
typedef void (*button_pressed_cb_t)(int target, bool pressed);

int button_init(void);
void button_set_notify(button_pressed_cb_t cb);
bool button_read(int target);

#endif /* BUTTON_H */
