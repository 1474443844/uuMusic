package cn.wantu.uumusic.test

class Test {
    var a = 0
}

fun main() {
    val t = Test()
    val m = t
    m.a = 1
    println(t.a)
}
