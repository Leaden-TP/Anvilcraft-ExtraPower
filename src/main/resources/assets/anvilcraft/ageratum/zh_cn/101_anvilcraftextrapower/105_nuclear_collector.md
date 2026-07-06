---
navigation:
    title: "集核器"
    icon: "anvilcraftextrapower:nuclear_collector"
    parent: anvilcraft_guideme:nuclear_theme.md
items:
  - anvilcraftextrapower:nuclear_collector
---


# 集核器

<item id="anvilcraftextrapower:nuclear_collector"/>

-集核器是核反应堆的核心部件，它负责将核反应堆产生的能量转化为电能。
-它拥有复杂的机制和较为繁多的信息显示，处理不慎会带来巨大的危险。

# 合成

<recipe id="anvilcraftextrapower:nuclear_collector"/>

# 机制

- ## (1).核反应堆
- 在它5x5x3的范围内，若有合法的<ref item="anvilcraftextrapower.uranium_rod"/>(即<ref item="anvilcraftextrapower.uranium_rod"/>的中部在该范围内)
- 就会激活<ref item="anvilcraftextrapower.uranium_rod"/>构成核反应堆，并开始产生能量。
- ## (2).热与降温
- 在运行时，它会不断产生热量，在经过一段时间后会以自身为起点向上检查水体，并以正上方的最高水面为基准，消耗水方块(或冰)
- 来降低热量.当它过热时，它会加快消耗水方块的速度，以更快降温。
- ## (3).降温水库
- 在你放置集核器时，它会自动检查周围的水体，逐层检查水体是否合法，当你佩戴<translate key="item.anvilcraft.anvil_hammer"/>时，它的电网会提示你最大平面水体范围，
- 不超出该范围的相连水体(以集核器的正上方为基准)都会被视为合法水体（仅水平，垂直方向可任意超出）。如果你的水体不合法，集核器的[降温系统]自动停止运行!
- 当成功构建水库时，会给与部分产热减免，并延长耗水的时间间隔。
- ## (4).发电，产热与降温规则
- 发电量与有效铀棒挂钩，一个未被<ref item="anvilcraftextrapower.frost_controller"/>削弱有效<ref item="anvilcraftextrapower.uranium_rod"/>的发电值为5，
- <ref item="anvilcraftextrapower.nuclear_bomb"/>为1，总发电量为发电值总和乘以一个系数（在配置文件中为powerOutput_of_a_uraniumRod）
- 产热为发电值总和*5，
-水源提供1降温值，任意冰块提供2点降温值，单次降温为总和乘以5
- ## (5).异常情况以及处理方法
- 1.**这个位置离另一个集核器太近了**|检查该集核器周围是否有其他集核器，如果有，请移动它们
- 2.**过热!需要冷却!**|此时集核器过热，破坏或热量集满都会导致爆炸，请立即启用<ref item="anvilcraftextrapower.frost_controller"/>削弱或停用<ref item="anvilcraftextrapower.uranium_rod"/>
-   或者移除<ref item="anvilcraftextrapower.uranium_rod"/>，并确保[降温系统]正常，静待降温
- 3.**无效水范围(它太大了!)**|你的水体不合法，集核器的[降温系统]会自动停止运行!请确保水库的每一层都不超出集核器提示的最大平面范围
- 4.**没有铀棒在附近!**|请放置<ref item="anvilcraftextrapower.uranium_rod"/>在集核器的5x5x3范围内,使之构成核反应堆
- 5.**当你挖掉已经构成核反应堆的集核器时，周边的<ref item="anvilcraftextrapower.uranium_rod"/>会产生大范围辐射**
-   **所以当你想要拆除集核器时，请先拆除<ref item="anvilcraftextrapower.uranium_rod"/>**
