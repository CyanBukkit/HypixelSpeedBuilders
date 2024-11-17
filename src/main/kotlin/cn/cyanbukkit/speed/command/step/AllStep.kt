package cn.cyanbukkit.speed.command.step

import org.bukkit.entity.Player

object AllStep {

    fun Player.tell(text: String) = this.sendMessage("§b[SpeedBuild]§6${text}")

    val stepMap = mutableMapOf<Player, Int>()


    //  根据存储的步骤来进行判断 并调用下面带有注解的方法
    fun Player.nextStep() {
        // 获取步骤
        val step = if (stepMap.containsKey(this)) {
            stepMap[this]!! + 1
        } else {
            stepMap[this] = 0
            0
        }
        // 获取所有的方法
        val methods = this::class.java.methods

        var allStep = 0
        // 遍历所有的方法
        methods.forEach {
            // 判断是否有注解
            if (it.isAnnotationPresent(Step::class.java)) {
                allStep++ // 所有的步骤
                // 获取注解
                val annotation = it.getAnnotation(Step::class.java)
                // 判断注解的值是否等于步骤
                if (annotation.value == step) {
                    // 调用方法
                    //  上边java的是你的位置
                    it.invoke(this)
                    return
                }
            }
        }
        //
        if (allStep == step) {
            this.tell("§a恭喜你完成了所有的步骤")
        }


    }


    @Step(0) // 第一步创建竞技场
    fun Player.createArena() {
        this.tell("请使用点击两个点来创建竞技场")
    }

}