#ifndef SB1602B_H
#define SB1602B_H

int sb1602b_init(void);
int sb1602b_clear(void);
int sb1602b_move(int x, int y);
void sb1602b_print(const char *p_str);
void sb1602b_output(const char *p_str, uint16_t len);

#endif /* SB1602B_H */
