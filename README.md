# service-sate

入口：
    1、RegisterBootstrap:  节点树初始化; 同步节点状态.   注册SyncDataListener.
    2、ObserverBootstrap:  同步节点状态.    注册SyncDataListener.
    3、ControllerBootstrap: 
             同步节点状态, 响应各层节点的变化, 调控下层节点的状态.  注册ToggleNodeListener和ServerChangeListener

节点：  level下实例节点state有优先级 MonitorFactory.EventSource
     1、servers: 服务运行实例集合
     2、providers: 提供者集合
                   state: OFF@-@SERVER; DISABLED@-@PROVIDER_ALL; 
                          LIMIT@-@PROVIDER_INSTANCE; DISABLED@-@PROVIDER_INSTANCE
     3、apis: 服务集合
                    state: LIMIT@-@API_INSTANCE;  DISABLED@-@API_INSTANCE
     4、log: 日志集合, 每天产生一个日期格式<2017-06-26>的子日志节点记录变动过程

监听：
 TreeCacheListener:   监控TreeCache下节点树的变动
           1、SyncDataListener:   新增事件 -> add新Map.Entry至本地缓存; 更新事件 -> modify本地缓存.
           2、ToggleNodeListener: 新增事件 -> add新Map.Entry至本地缓存; 更新事件 -> modify本地缓存, 变更状态向下传导.
 PathChildrenCacheListener： 监听指定节点下子节点列表的变动
           1、ServerChangeListener:  监听服务运行实例的存亡, 判定是否标记offline.
 