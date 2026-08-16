# pay-kernel-fun

一个可插拔的支付渠道抽象内核：**渠道 SPI + 业务解耦回调**，附带一个用支付宝一次性支付跑通的案例应用。

它只是把"支付渠道能力"和"具体业务"之间应该长什么样的边界，做成一个独立、可复用的库。

## 这是什么 / 不是什么

- **是**：一套渠道 SPI（下单、回调、查单、退款、关单……）+ 一套业务回调 SPI（支付成功/退款成功后该做什么），中间用按 key 路由的 Registry 粘合起来。业务方和渠道实现互相看不见对方，只认接口。
- **不是**：一个开箱即用的支付网关产品。案例应用里的持久化、锁、支付宝渠道实现都是"示例实现"，接入到真实项目时，你大概率需要替换持久化实现、按需增删渠道。

## 模块

```
pay-kernel-fun (parent pom)
├── pay-kernel-fun-core           一次性/通用交易抽象（纯 Java 库，不可独立运行）
├── pay-kernel-fun-launcher       案例应用（Spring Boot，唯一可运行的 Java 模块）
└── pay-kernel-fun-ui             配套前端（Vue2，独立 node 项目，不进 maven 构建）
```

### pay-kernel-fun-core

一次性交易抽象的全部内容：

| 分类 | 内容 |
|---|---|
| 领域模型 | `TradeOrder`（渠道无关的交易订单，含 `expireTime` 有效期字段） |
| 渠道侧 SPI | `TradeCreateHandler`（下单）、`TradeNotifyHandler`（回调）、`TradeQueryExecutor`（查单）、`TradeRefundExecutor`（退款）、`TradeCloseExecutor`（关单）、`TradeCloseDetector`（关单探测，扩展点）、`TradeDeliveryHandler`（交付确认，扩展点）、`TradeReconcileHandler`（对账，扩展点） |
| 业务侧 SPI | `TradeOpenHandler`（业务下单入口）、`TradeSuccessListener`（支付成功回调）、`TradeRefundListener`（退款结果回调）、`TradeCloseListener`（关单回调）——四个回调覆盖订单的完整生命周期 |
| 路由 | `ChannelRegistry<T>`：一个泛型类替代了"每组 SPI 各写一个 Registry"的重复模式 |
| 持久化 SPI | `TradeOrderRepository`：core 不碰任何数据库/ORM，接入方自己实现 |
| 分布式锁 SPI | `TradeLock`：保证并发回调/退款处理的幂等性，数据库 CAS 单独用防不住并发窗口期 |
| 补偿队列 SPI | `TradeQueryCompensationQueue`（安排下一次主动查单的时间）、`TradeCloseCompensationQueue`（安排到期关单的时间），弥补渠道回调丢失/延迟 |
| 异常记录 SPI | `TradeExceptionRecorder`：默认打日志，接入方可换成落库实现 |

配置这件事完全不在 core 里出现——直接用 Spring 原生 `@ConfigurationProperties`，天然支持 yaml/环境变量/配置中心，不需要重新发明一个配置读取接口。

### pay-kernel-fun-launcher

案例应用，演示"业务方如何接入抽象"：一个有真实库存的商品下单场景，走支付宝一次性支付的下单/回调/查单/退款/关单全流程，库存预扣、锁单、超时/退款回滚全部真实实现（不是打日志充数）。

包结构按"支付基础设施 / 具体业务"两层分开，这是本项目要证明的分层——**新增一个业务只往 `biz` 下加一个包，不碰 `infra`，也不新增交易相关的 Controller**：

```
com.paykernelfun.launcher
├── infra/                     支付基础设施层，新增渠道往这里加
│   ├── config/                AlipayProperties/TradeProperties（yaml 绑定）、AlipayClientConfig、RegistryConfig（装配 ChannelRegistry）
│   ├── channel/alipay/        支付宝渠道实现：TradeCreateHandler/TradeNotifyHandler/TradeQueryExecutor/TradeRefundExecutor/TradeCloseExecutor
│   ├── persistence/           TradeOrderRepository 的 MyBatis-Plus + MySQL 实现
│   ├── lock/                  TradeLock 的 Redisson 实现
│   ├── compensation/          TradeQueryCompensationQueue/TradeCloseCompensationQueue 的 Redis ZSet 实现
│   ├── task/                  两个补偿定时任务：TradeQueryCompensationTask、TradeCloseCompensationTask
│   └── trade/                 唯一的通用 TradeController + TradePaymentConfirmer（支付成功收尾）+ TradeOrderCloser（关单收尾）
└── biz/
    └── sample/                示例业务：一个有库存的商品
        ├── persistence/       SampleGoodsPO/Mapper、SampleGoodsOrderPO/Mapper（业务自己的两张表，不进 infra）
        ├── SampleGoodsService.java      唯一操作这两张表的地方：预扣库存下单、锁单、关单/退款回滚库存
        ├── SampleGoodsOpenHandler.java   实现 TradeOpenHandler：CAS 扣库存 + 落 PENDING 业务订单
        ├── SampleGoodsSuccessListener.java 实现 TradeSuccessListener：锁单（写 PAID + 支付时间）
        ├── SampleGoodsRefundListener.java  实现 TradeRefundListener：回滚库存到 REFUNDED
        ├── SampleGoodsCloseListener.java   实现 TradeCloseListener：回滚库存到 CLOSED
        └── SampleGoodsController.java      业务自己的管理接口：新增/查看商品、查看某商品下的所有订单
```

`TradeController`（`infra/trade`）不属于任何具体业务：下单请求体里带 `bizType`/`channel`，由它去 `ChannelRegistry` 里查到对应的 `TradeOpenHandler`/`TradeCreateHandler` 执行。回调、查单、退款、关单同理都是这一个 Controller 里的通用方法。`biz/sample` 如果你要接入自己的业务，可以整体删掉换成自己的实现，`infra` 一行都不用改。

### 示例业务：库存怎么扣、怎么回滚

`sample_goods`（商品，含 `stock` 库存）+ `sample_goods_order`（业务订单，记录哪个买家在什么时候支付了哪个商品，`order_no` 对应 `trade_order.order_no`）两张表，状态机是业务自己的一套（`PENDING`/`PAID`/`CLOSED`/`REFUNDED`），不复用 `TradeOrder` 的 `TradeStatus`——理由很简单：业务自己的生命周期不该依赖抽象层的订单状态定义。

- **下单预扣**：`SampleGoodsOpenHandler.buildOrder` 从 `TradeOpenRequest.extra` 读 `goodsId`/`quantity`/`buyerId`，调用 `SampleGoodsService.reserveStock`——用 `UPDATE sample_goods SET stock=stock-? WHERE id=? AND stock>=?` 做 CAS 扣减，影响行数为 0 说明库存不足，抛 `TradeOpenRejectedException`（`core` 里的通用"下单被拒绝"异常，`TradeController` 统一接住返回 409），同时插入一条 `PENDING` 状态的业务订单，金额按商品当前单价现算，不信任客户端传的金额。
- **锁单**：支付成功后 `SampleGoodsSuccessListener` 把业务订单从 `PENDING` 置为 `PAID` 并写入支付时间，不再碰库存（库存已经在下单时扣过了）。
- **超时关单回滚**：这是本次实现里补齐的一环——之前 `TradeCloseExecutor` 关单只影响 `trade_order` 表，从未通知过业务方，导致预扣的库存在订单超时关闭后永久卡死。现在 `core` 新增了 `TradeCloseListener`（与 `TradeSuccessListener`/`TradeRefundListener` 对称，构成订单生命周期的第三个业务回调），`infra` 新增 `TradeOrderCloser`（调渠道关单 + 清理补偿队列 + 路由 `TradeCloseListener`，是手动关单接口和到期关单定时任务共用的唯一收尾实现），`SampleGoodsCloseListener` 实现它把库存加回去。
- **退款回滚**：`SampleGoodsRefundListener` 同样把库存加回去，和关单共用 `SampleGoodsService.release(orderNo, targetStatus)`，只是最终状态不同（`CLOSED` vs `REFUNDED`）。

下单请求体示例（`extra` 承载业务参数，`core` 的 DTO 不用改）：
```json
{"bizOrderNo":"o-1","bizType":"SAMPLE_GOODS","channel":"ALIPAY","extra":{"goodsId":1,"quantity":2,"buyerId":"u1001"}}
```

**已知限制**：如果库存预扣成功后，紧接着的渠道下单调用（`TradeCreateHandler.create`）失败，`trade_order` 从未落库、也不会进补偿队列，预扣的库存会一直占用，需要人工介入或换个 `bizOrderNo` 重新下单——这是本案例明确不处理的边界情况。

### 补偿机制：回调丢失和超时未支付怎么办

支付渠道的异步回调不保证一定送达，超时未支付的订单也需要主动关闭，光靠"下单 -> 等回调"这一条路径是不够的。这里用两条独立的 Redis ZSet 队列（score = 下次该处理的时间戳，member = orderNo）分别兜底：

- **`TradeQueryCompensationQueue`（补偿查询）**：下单时把订单和"第一次检查时间"（`queryRetryIntervalSeconds` 之后）写进去。`TradeQueryCompensationTask` 每次触发一直从队列取一条到期成员处理，直到取空为止（不做批量），主动查渠道支付状态：查到已支付，交给 `TradePaymentConfirmer` 走和渠道回调完全相同的收尾逻辑（CAS 翻转 + 路由业务回调 + 清理两个补偿队列）；查到未支付，把执行时间往后顺延 `queryRetryIntervalSeconds`，重新写回队列；一旦当前时间超过订单的 `expireTime`，不再重新入队，该订单从这条队列里"自然消失"，后续交给下面的关单任务处理。
- **`TradeCloseCompensationQueue`（到期关单）**：下单时把订单和它的 `expireTime`（有效期截止时间）写进去。`TradeCloseCompensationTask` 同样每次一条一条取到期成员处理，若订单仍是 PENDING，调用 `TradeCloseExecutor.close()` 关单（内部会 CAS 翻转为 CLOSED），关单成功后把该订单也从查询补偿队列移除。

两条队列各自独立取用（Redisson `RScoredSortedSet` 的"取出即 remove"是原子操作），谁先摘到订单谁负责收尾，不会重复处理；轮询间隔在 `pay-kernel-fun.trade.*` 里配置。

### pay-kernel-fun-ui

配套前端，把后端的每一个接口摆到页面上，方便测试和校准（不做登录鉴权、不做分页，纯粹为调试服务）。Vue2 + vue-cli + Element UI，独立 node 项目，与两个 Maven 模块平级，不进 Maven 构建，也不依赖它们的产物。

```
pay-kernel-fun-ui/
├── .env.development / .env.production   VUE_APP_BASE_API=/api
├── vue.config.js                        devServer.proxy 把 /api 转发到后端，改 target 即可换代理/网关
└── src/
    ├── api/            request.js（axios 实例 + 统一错误提示）、trade.js、goods.js
    ├── router/          三个路由：/goods、/orders、/trade-lab
    ├── layout/          BasicLayout.vue，自定义浅色卡片风格的左侧菜单
    └── views/
        ├── GoodsManage.vue   商品管理：新增商品、商品列表、查看某商品下的所有订单
        ├── OrderManage.vue   订单管理：按商品筛选查看业务订单（只读），可展开查看 trade_order 视角的交易详情
        └── TradeLab.vue      交易测试台：下单（自动跳转支付宝收银台）+ 单笔订单的查询本地状态/查询渠道状态/退款/关单，
                              每次调用的接口返回原始 JSON 直接展示，用来校准接口行为
```

**baseURL / 代理**：`VUE_APP_BASE_API=/api`，`vue.config.js` 里 `devServer.proxy` 把 `/api` 转发到 `http://localhost:8080`（可用 `PROXY_TARGET` 环境变量覆盖，不用改代码）。生产环境同样约定前端只请求 `/api` 前缀，由部署时的 nginx/网关做反向代理，前端代码不用因为后端地址变化而重新构建。

启动：
```bash
cd pay-kernel-fun-ui
npm install
npm run dev   # 默认 http://localhost:8081，需要 pay-kernel-fun-launcher 已在本地 8080 启动
```

## 快速开始

### 1. 准备依赖

- MySQL：执行 `pay-kernel-fun-launcher/sql/pay_kernel_fun.sql` 建库建表
- Redis：本地起一个默认端口的 Redis（用于并发幂等锁）
- 支付宝沙箱账号：[支付宝开放平台 - 沙箱环境](https://open.alipay.com/develop/sandbox/app) 申请 `app-id`、应用私钥、支付宝公钥

### 2. 配置

编辑 `pay-kernel-fun-launcher/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/pay_kernel_fun_db?...
    username: root
    password: root
  redis:
    host: 127.0.0.1
    port: 6379

pay-kernel-fun:
  alipay:
    app-id: 你的沙箱 app-id
    private-key: 你的应用私钥
    alipay-public-key: 沙箱环境提供的支付宝公钥
    sandbox: true   # true 用沙箱网关，false 用正式网关；网关地址不作为可填字段，由这个布尔值决定，避免手填错
    charset: UTF-8
    format: json
    notify-url: http://your-public-host:8080/trade/notify/ALIPAY   # 需要外网可达，可用内网穿透工具
    sign-type: RSA2
    # encrypt-type / encrypt-key：仅在支付宝开放平台开启了「接口内容加密」时才需要，默认不填
  trade:
    order-validity-seconds: 900        # 订单有效期，超时未支付会被 TradeCloseCompensationTask 自动关单
    query-retry-interval-seconds: 10   # 补偿查询任务：一次未查到支付成功后，多久再查一次
    poll-interval-millis: 5000         # 两个补偿定时任务的轮询间隔
    return-url: http://your-public-host:8080/trade/return   # 用户支付完成后浏览器同步跳转回的地址，渠道无关
```

### 3. 构建与启动

```bash
mvn clean package -DskipTests
mvn spring-boot:run -pl pay-kernel-fun-launcher
```

### 4. 试跑一遍完整链路

用 `pay-kernel-fun-ui`（`npm run dev`，见上文）在浏览器里点点点即可，下面是等价的 curl 版本，两者选一种跑通即可：

```bash
# 建一个库存为 5 的商品
curl -X POST http://localhost:8080/sample/goods \
  -H 'Content-Type: application/json' \
  -d '{"goodsName":"测试商品","price":9.9,"stock":5}'
# 返回 {"id": 1}

# 下单（bizType 指定路由到 sample 业务，channel 指定走支付宝，extra 带商品/数量/买家），拿到自动提交支付表单
curl -X POST http://localhost:8080/trade/orders \
  -H 'Content-Type: application/json' \
  -d '{"bizOrderNo":"o-1","bizType":"SAMPLE_GOODS","channel":"ALIPAY","extra":{"goodsId":1,"quantity":2,"buyerId":"u1001"}}'

# 库存立即变为 3（下单预扣），sample_goods_order 出现一条 PENDING 记录
curl http://localhost:8080/sample/goods/1

# 把返回的 payload（一段 HTML 表单）保存成文件用浏览器打开，用沙箱买家账号完成支付
# 支付宝异步回调会打到 /trade/notify/ALIPAY，处理完成后：

curl http://localhost:8080/trade/orders/{orderNo}
# trade_order 状态变为 SUCCESS

curl http://localhost:8080/sample/goods/1/orders
# sample_goods_order 对应记录状态变为 PAID，写了 pay_time——这就是"锁单"

curl http://localhost:8080/trade/orders/{orderNo}/remote-status
# 主动查询支付宝侧的真实交易状态（TradeQueryExecutor）

curl -X POST http://localhost:8080/trade/orders/{orderNo}/refund
# 触发退款，sample_goods_order 状态变 REFUNDED，库存加回 2

# 另开一笔不支付的订单，验证超时/主动关单也会回滚库存
curl -X POST http://localhost:8080/trade/orders \
  -H 'Content-Type: application/json' \
  -d '{"bizOrderNo":"o-2","bizType":"SAMPLE_GOODS","channel":"ALIPAY","extra":{"goodsId":1,"quantity":1,"buyerId":"u1002"}}'
curl -X POST http://localhost:8080/trade/orders/{orderNo2}/close
curl http://localhost:8080/sample/goods/1
# 库存加回 1，sample_goods_order 对应记录状态变 CLOSED
```

## 明确不做的事情

- 不做任何具体业务逻辑，这些属于接入方职责，不属于抽象本身
- 只做支付宝一次性支付；`TradeCloseDetector`/`TradeDeliveryHandler`/`TradeReconcileHandler` 作为面向未来渠道（如需要主动探测关单状态、需要发货确认、需要拉取账单对账的渠道）的扩展点保留在 core 里，但案例应用不实现、不接线
- 没有单元测试
- `pay-kernel-fun-ui` 不做登录鉴权、不做分页、不引入 Vuex/Pinia——纯粹是给后端接口配的调试台，不是一个成熟的中后台产品。移动端做了基本适配（窄屏下侧边栏收起为可展开的抽屉菜单、表格/弹窗自适应宽度），但没有针对移动端做专门的交互优化（比如支付宝跳转在移动浏览器上新开标签页的体验不如 PC）

## 开源协议

[MIT](LICENSE) © 2026 fan

任何人都可以自由使用、修改、分发、商用本软件（包括闭源商用），唯一的要求是保留版权声明和协议文本。软件按"现状"提供，不附带任何担保。
