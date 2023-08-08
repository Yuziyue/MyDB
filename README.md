# MyDB
数据库项目
参考何人听我楚狂声大佬

1 首先执行以下命令编译源码：
mvn compile
2 接着执行以下命令以 /tmp/mydb 作为路径创建数据库：
mvn exec:java -Dexec.mainClass="backend.Launcher" -Dexec.args="-create /tmp/mydb"


3 随后通过以下命令以默认参数启动数据库服务：
mvn exec:java -Dexec.mainClass="backend.Launcher" -Dexec.args="-open /tmp/mydb"

4 这时数据库服务就已经启动在本机的 9999 端口。重新启动一个终端，执行以下命令启动客户端连接数据库

mvn exec:java -Dexec.mainClass="backend.Launcher"


例子
create database db;
commit;
use db;
create table tb
    (name string,id INT(11));
insert into tb values (10);
 select * from tb where id>10;
 delete from tb where id>10;
