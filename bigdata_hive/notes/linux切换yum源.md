# linux切换yum源

##### 1、需要提前安装wget命令

```
yum install wget -y
```

##### 2、切换到yum的安装目录

```
/etc/yum.repos.d/
```

##### 3、将所有的已经存在的文件添加备份

​		1、给文件该名称添加.bak
​		2、创建backup目录，将所有的文件移动进去

##### 4、打开镜像网站  https://mirrors.aliyun.com 

```
wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-6.repo
```

##### 5、清除yum的已有缓存

```
yum clean all
```

##### 6、生成yum的缓存

```
yum makecache
```

**注意：yum缓存这块说的多一点，yum的安装方式其实也是rpm包的安装方式，但是rpm包在安装的时候需要很多rpm包的依赖，因此需要将rpm包的依赖关系下载到本地，防止每次频繁的向镜像网站发送请求，因此在第6步骤的时候需要将rpm的依赖关系下载到本地，放置到缓存中**

