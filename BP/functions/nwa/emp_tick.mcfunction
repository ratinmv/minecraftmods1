# Run each tick from tick.json if you want recovering EMP behavior.
scoreboard objectives add emp_timer dummy
scoreboard players remove @e[tag=emp_disabled,scores={emp_timer=1..}] emp_timer 1
tag @e[tag=emp_disabled,scores={emp_timer=..0}] remove emp_disabled
