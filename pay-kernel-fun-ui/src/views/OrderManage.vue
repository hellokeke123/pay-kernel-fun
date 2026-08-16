<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>订单管理</h2>
        <p class="subtitle">按商品查看业务订单（sample_goods_order 视角），只读展示</p>
      </div>
      <el-select v-model="selectedGoodsId" placeholder="选择商品" filterable class="goods-select" @change="fetchOrders">
        <el-option v-for="g in goodsList" :key="g.id" :label="`#${g.id} ${g.goodsName}`" :value="g.id" />
      </el-select>
    </div>

    <el-card shadow="never" class="card">
      <div class="table-scroll">
        <el-table :data="orders" v-loading="loading" style="width: 100%; min-width: 780px">
        <el-table-column prop="orderNo" label="订单号" width="220" />
        <el-table-column prop="buyerId" label="买家" width="120" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="180" />
        <el-table-column label="操作" width="140">
          <template slot-scope="{ row }">
            <el-button type="text" @click="viewTradeDetail(row)">查看交易详情</el-button>
          </template>
        </el-table-column>
        </el-table>
      </div>
      <el-empty v-if="!loading && !selectedGoodsId" description="请先选择一个商品" />
    </el-card>

    <el-dialog title="交易详情（trade_order 视角）" :visible.sync="detailVisible" width="90%" top="10vh" class="pk-dialog pk-dialog--sm">
      <el-descriptions :column="1" border v-if="tradeDetail">
        <el-descriptions-item label="订单号">{{ tradeDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="业务订单号">{{ tradeDetail.bizOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ tradeDetail.bizType }}</el-descriptions-item>
        <el-descriptions-item label="渠道">{{ tradeDetail.channel }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ tradeDetail.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(tradeDetail.status)">{{ tradeDetail.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="渠道交易号">{{ tradeDetail.channelTradeNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ tradeDetail.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ tradeDetail.refundTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="有效期截止">{{ tradeDetail.expireTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listGoods, listOrdersByGoods } from '@/api/goods'
import { getOrder } from '@/api/trade'

export default {
  name: 'OrderManage',
  data() {
    return {
      goodsList: [],
      selectedGoodsId: null,
      orders: [],
      loading: false,
      detailVisible: false,
      tradeDetail: null
    }
  },
  async created() {
    this.goodsList = await listGoods()
    if (this.goodsList.length) {
      this.selectedGoodsId = this.goodsList[0].id
      this.fetchOrders()
    }
  },
  methods: {
    async fetchOrders() {
      if (!this.selectedGoodsId) return
      this.loading = true
      try {
        this.orders = await listOrdersByGoods(this.selectedGoodsId)
      } finally {
        this.loading = false
      }
    },
    async viewTradeDetail(row) {
      this.tradeDetail = await getOrder(row.orderNo)
      this.detailVisible = true
    },
    statusTagType(status) {
      return {
        PENDING: 'info',
        PAID: 'success',
        SUCCESS: 'success',
        CLOSED: 'warning',
        REFUNDED: 'danger'
      }[status] || 'info'
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

.card {
  border-radius: 14px;
  border: 1px solid #ebeef5;
}

.table-scroll {
  overflow-x: auto;
}

.goods-select {
  width: 260px;
}

@media (max-width: 768px) {
  .page-header {
    flex-wrap: wrap;
    gap: 12px;
  }

  .goods-select {
    width: 100%;
  }
}
</style>
