0. com.redaggr.agent.Agent 新增agent入口
0. 新增实际agent类,修改监听类,修改执行visitor
0. 新增visitor，修正访问者中逻辑
0. 修正paramUtil中的数据接受逻辑

1. 如何设计trace的结构
2. 埋点的地方如何选,需要基于哪些原则去选择
3. 字节码框架如何选择
4. spanId的逻辑是什么,
5. 如何界定什么时候session开始，什么时候session结束
6. dubbo参数如何拿 兼容apache和阿里的
7. void返回的时候 是否应该要处理node stack
8. rabbitmq 中head是unmodefiyMap 那traceId怎么传递?
9. 如何做到无侵入性
10. 异常的情况下怎么记录的
11. 监控的serviceImpl中存在符合预期的埋点，但是此埋点是main主线程
    ,且此埋点会被main线程提前执行,导致main线程内保存了一份ThreadLocal，
    在这种场景下比如spring的afterPropertiesSet等就导致问题
    ，main线程创建的所有子线程都将继承此session出现问题,因为使用了(InheritableThreadLocal)
12. 因为打印入参是Object... 如果是入参是基本类型的时候怎么办

