### 前言
    在数据库DBA千变一律的处境下，感觉DBA工作很多都是重复性工作。
    因为从事的是DBA行业，有此一下感悟
            DBA遇到的问题：
            1.上线操作都是人肉执行；
            2.上线流程不成熟不规范
            3.DBA做为数据库管理员没有做到对数据库权限的绝对把控
            4.开发和DBA沟通问题
            5.技术人员线上数据操作失误，DBA人肉垫底
            6.数据库出了问题，DBA人肉排除且解决
            7.数据库迁移，人肉
            8.大表分库分表人肉
            9.大数据的表解决方式依旧人肉
            。。。。。
        可以说百分之90以上都是一样的操作逻辑。去任务公司都是一样的操作对于DBA的理解：之前的工作经验，一开始是做开发，之后转的DBA。
    对于业务的理解：产品经理>开发人员>测试人员>项目经理>DBA>运维工程师。产品功能在设计初期---》开发工程师完成功能--》DBA在整个流程
    里面只在Create表的时候可以审核下表的规范。是完全涉及不了开发功能的，甚至连表的设计DBA是干涉不了的，表设计直接关联到开发功能，
    除非
        DBA比开发更懂开发，
        比架构师更懂架构优化，才可以去设计表。
    很多it人员对DBA不是很了解，这种我觉得作为DBA必须要hole住所有数据库风险问题，以及提早做出完全的准备，而不是写写文档，开开会
    涉及到运维的相关职业几乎很少涉及到业务；凡是设计到业务的运维，一般多少已经是管理岗位或者打酱油。所以，面对这个不为人知的岗位，
    以及被误解的风险，决定把所有DBA的操作都自动化。
    分成两个系统
    1.DBA日常操作系统:包含日常百分之90的工作，具体工作下面介绍
    2.DBA问题解决系统；
            (1)mycat可视化配置，自动化配置表分片规则
            (2)数据库主从切换功能
            (3)大表处理-可视化历史归档
            (4)Mysqldump和xtrabackup定时备份监控，可视化配置
            (5)异常数据丢失恢复策略
                ①库恢复
                ②表恢复
                ③部分数据恢复
    (6)自动巡检：每隔一小时自动巡检所有数据库运行状态，发送DBA
    其他监控通过zabbix，或者其他系统报警来处理。
    此项目目前不考虑开源，个人使用
    数据库自动化系统
### 整个流程DBA只需要审核，不需要做其他动作
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/%E8%87%AA%E5%8A%A8%E5%8C%96%E6%B5%81%E7%A8%8B.png)
### 逻辑架构
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/%E5%9B%BE%E7%89%871.png)
### 项目功能介绍
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/title.gif)
### 数据源权限控制
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/datasource.gif)
### 数据变更操作
    增删改都有备份，支持闪退 上篇。
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/dml1.gif)
    增删改都有备份，支持闪退 下篇
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/dml1.gif)
### 数据表结构变更
![image](https://github.com/dongjiashun/auto_dba/blob/master/image/title.gif)
### 数据查询--支持navicate和MySqlWorkBench客户端操作命令
![image]()
