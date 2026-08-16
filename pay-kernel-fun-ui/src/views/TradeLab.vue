<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>交易测试台</h2>
        <p class="subtitle">直接调用 TradeController 的每一个接口，返回结果原样展示，方便校准</p>
      </div>
    </div>

    <div class="lab-grid">
      <el-card shadow="never" class="card">
        <div slot="header">下单</div>
        <el-form :model="createForm" label-width="90px">
          <el-form-item label="商品">
            <el-select v-model="createForm.goodsId" placeholder="选择商品" filterable style="width: 100%">
              <el-option v-for="g in goodsList" :key="g.id" :label="`#${g.id} ${g.goodsName}（库存${g.stock}）`" :value="g.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="createForm.quantity" :min="1" :step="1" />
          </el-form-item>
          <el-form-item label="买家ID">
            <el-input v-model="createForm.buyerId" placeholder="如 u1001" />
          </el-form-item>
          <el-form-item label="业务订单号">
            <el-input v-model="createForm.bizOrderNo">
              <el-button slot="append" @click="regenBizOrderNo">重新生成</el-button>
            </el-input>
          </el-form-item>
          <el-form-item label="渠道">
            <el-select v-model="createForm.channel" style="width: 100%">
              <el-option label="ALIPAY" value="ALIPAY" />
            </el-select>
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="creating" @click="submitCreateOrder">下单并跳转支付</el-button>
        <div v-if="createdOrderNo" class="created-order-no">
          最新下单：
          <el-tag>{{ createdOrderNo }}</el-tag>
          <el-button type="text" @click="useCreatedOrderNo">填入下方操作栏</el-button>
        </div>
      </el-card>

      <el-card shadow="never" class="card">
        <div slot="header">单笔订单操作</div>
        <el-form label-width="90px">
          <el-form-item label="订单号">
            <el-input v-model="orderNo" placeholder="填入 orderNo（非 bizOrderNo）" />
          </el-form-item>
          <el-form-item label="退款原因">
            <el-input v-model="refundReason" placeholder="选填，默认「案例退款」" />
          </el-form-item>
        </el-form>
        <div class="action-buttons">
          <el-button :loading="loadingKey === 'local'" @click="run('local')">查询本地订单状态</el-button>
          <el-button :loading="loadingKey === 'remote'" @click="run('remote')">查询渠道真实状态</el-button>
          <el-button type="warning" :loading="loadingKey === 'refund'" @click="run('refund')">发起退款</el-button>
          <el-button type="danger" :loading="loadingKey === 'close'" @click="run('close')">发起关单</el-button>
        </div>
      </el-card>
    </div>

    <el-card shadow="never" class="card result-card">
      <div slot="header">
        接口返回结果
        <span v-if="lastAction" class="result-meta">最近一次：{{ lastAction }}</span>
      </div>
      <pre class="result-pre">{{ resultText }}</pre>
    </el-card>

    <!-- 用于渲染支付宝下单返回的自动提交表单，新开标签页跳转收银台 -->
    <div ref="payFormHost" style="display:none" />
  </div>
</template>

<script>
import { listGoods } from '@/api/goods'
import { createOrder, getOrder, getRemoteStatus, refundOrder, closeOrder } from '@/api/trade'

function genBizOrderNo() {
  return 'lab-' + Date.now()
}

export default {
  name: 'TradeLab',
  data() {
    return {
      goodsList: [],
      createForm: {
        goodsId: null,
        quantity: 1,
        buyerId: 'u1001',
        bizOrderNo: genBizOrderNo(),
        channel: 'ALIPAY'
      },
      creating: false,
      createdOrderNo: '',
      orderNo: '',
      refundReason: '',
      loadingKey: '',
      lastAction: '',
      resultText: '（尚未调用任何接口）'
    }
  },
  async created() {
    this.goodsList = await listGoods()
    if (this.goodsList.length) {
      this.createForm.goodsId = this.goodsList[0].id
    }
  },
  methods: {
    regenBizOrderNo() {
      this.createForm.bizOrderNo = genBizOrderNo()
    },
    useCreatedOrderNo() {
      this.orderNo = this.createdOrderNo
    },
    async submitCreateOrder() {
      if (!this.createForm.goodsId) {
        this.$message.warning('请选择商品')
        return
      }
      this.creating = true
      try {
        const payload = {
          bizOrderNo: this.createForm.bizOrderNo,
          bizType: 'SAMPLE_GOODS',
          channel: this.createForm.channel,
          extra: {
            goodsId: this.createForm.goodsId,
            quantity: this.createForm.quantity,
            buyerId: this.createForm.buyerId
          }
        }
        const result = await createOrder(payload)
        this.createdOrderNo = result.orderNo
        this.orderNo = result.orderNo
        this.lastAction = 'POST /trade/orders'
        this.resultText = JSON.stringify(result, null, 2)
        this.$message.success('下单成功，即将打开支付页')
        this.openPayForm(result.payload)
        this.regenBizOrderNo()
      } finally {
        this.creating = false
      }
    },
    openPayForm(html) {
      if (!html) return
      const win = window.open('', '_blank')
      if (!win) {
        this.$message.warning('浏览器拦截了新标签页，请允许弹窗后重试')
        return
      }
      win.document.write(html)
      win.document.close()
    },
    async run(action) {
      if (!this.orderNo) {
        this.$message.warning('请先填入订单号')
        return
      }
      this.loadingKey = action
      try {
        let result
        switch (action) {
          case 'local':
            result = await getOrder(this.orderNo)
            this.lastAction = `GET /trade/orders/${this.orderNo}`
            break
          case 'remote':
            result = await getRemoteStatus(this.orderNo)
            this.lastAction = `GET /trade/orders/${this.orderNo}/remote-status`
            break
          case 'refund':
            result = await refundOrder(this.orderNo, this.refundReason)
            this.lastAction = `POST /trade/orders/${this.orderNo}/refund`
            break
          case 'close':
            result = await closeOrder(this.orderNo)
            this.lastAction = `POST /trade/orders/${this.orderNo}/close`
            break
        }
        this.resultText = JSON.stringify(result, null, 2)
      } finally {
        this.loadingKey = ''
      }
    }
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #1f2d3d;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.lab-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.card {
  border-radius: 14px;
  border: 1px solid #ebeef5;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.created-order-no {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.result-card {
  min-height: 200px;
}

.result-meta {
  float: right;
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.result-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
  color: #303133;
  max-height: 420px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .lab-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons .el-button {
    flex: 1 1 calc(50% - 10px);
  }
}
</style>
