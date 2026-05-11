# Tactical nuke detonation orchestration.
particle nwa:shockwave_ring ~ ~1 ~
particle minecraft:huge_explosion_emitter ~ ~1 ~
playsound random.explode @a ~ ~ ~ 1.0 0.4

# Fallout cloud stays for ~2 minutes.
summon minecraft:area_effect_cloud ~ ~ ~ nwa:fallout_cloud

# EMP pulse: temporarily disables redstone-like systems and tagged vehicles in a 40-block sphere.
tag @e[family=redstone_disablable,r=40] add emp_disabled
effect @e[tag=emp_disabled,r=40] slowness 12 255 true
effect @e[tag=emp_disabled,r=40] weakness 12 255 true

# Optional hard shutdown for scripted setups.
scoreboard players set @e[tag=emp_disabled,r=40] emp_timer 240
