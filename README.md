# JavaNet

这是大三上网络编程的课程实训项目仓库。

### 代码结构

- **SimpleNet** 单线程阻塞服务器和客户端
- **Multithread** 多线程阻塞echo服务器，不使用线程池技术
- **SingleThreadReactor** 使用Reactor模式的单线程非阻塞echo服务器，及其客户端
- **MultiThreadReactor** 使用Reactor模式的多线程非阻塞echo服务器，及其客户端
- **HttpClient** HTTP demo
- **MixEchoServer** 同时使用了阻塞和非阻塞模式的单线程echo服务器
- **NIOEchoClient** 使用了非阻塞模式的单线程echo客户端