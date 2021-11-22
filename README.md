# framework-support

java 框架扩展，对日常的开发做一些基础的工具类的封装。

## 功能介绍

1. **access-crypto:** 加解密的一些集成封装，只要包含有 aes rsa hash 等。
2. **alibaba-nacos:** 对 nacos 的一些基础封装，主要包含任务调度在配置中心力动态更改值和一些服务注册事件监听等。
3. **commons:** 对常用工具类进行封装
4. **idempotent:** 对幂等或并发控制的一些基础封装
5. **spring-security:** 对 spring security 的一些基础配置进行一个基础的常用封装，包含对用户的登陆审计管理等。
6. **spring-web-mvc:** 对 spring mvc 的一些 rest 接口进行统一规范和一些基础封装。
7. **minio:** 对 minio 文件存储客户端进行统一规范和一些基础封装。

项目例子的使用在 [basic-service](https://github.com/dactiv/basic-service) 进行引入。