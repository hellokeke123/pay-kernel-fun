<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p class="subtitle">维护 sample 案例业务的商品与库存</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增商品</el-button>
    </div>

    <el-card shadow="never" class="card">
      <el-table :data="goodsList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="goodsName" label="商品名" />
        <el-table-column prop="price" label="单价" width="120">
          <template slot-scope="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="140">
          <template slot-scope="{ row }">
            <el-button type="text" @click="viewOrders(row)">查看订单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="新增商品" :visible.sync="dialogVisible" width="90%" top="10vh" class="pk-dialog pk-dialog--sm">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名">
          <el-input v-model="form.goodsName" placeholder="请输入商品名" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="`商品「${activeGoods && activeGoods.goodsName}」的订单`" :visible.sync="ordersDialogVisible" width="90%" top="8vh" class="pk-dialog pk-dialog--lg">
      <div class="table-scroll">
        <el-table :data="activeGoodsOrders" v-loading="ordersLoading" style="width: 100%; min-width: 700px">
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
        <el-table-column prop="payTime" label="支付时间" />
      </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createGoods, listGoods, listOrdersByGoods } from '@/api/goods'

export default {
  name: 'GoodsManage',
  data() {
    return {
      loading: false,
      goodsList: [],
      dialogVisible: false,
      submitting: false,
      form: { goodsName: '', price: 9.9, stock: 10 },
      ordersDialogVisible: false,
      ordersLoading: false,
      activeGoods: null,
      activeGoodsOrders: []
    }
  },
  created() {
    this.fetchGoods()
  },
  methods: {
    async fetchGoods() {
      this.loading = true
      try {
        this.goodsList = await listGoods()
      } finally {
        this.loading = false
      }
    },
    openCreateDialog() {
      this.form = { goodsName: '', price: 9.9, stock: 10 }
      this.dialogVisible = true
    },
    async submitCreate() {
      if (!this.form.goodsName) {
        this.$message.warning('请输入商品名')
        return
      }
      this.submitting = true
      try {
        await createGoods(this.form)
        this.$message.success('新增成功')
        this.dialogVisible = false
        this.fetchGoods()
      } finally {
        this.submitting = false
      }
    },
    async viewOrders(row) {
      this.activeGoods = row
      this.ordersDialogVisible = true
      this.ordersLoading = true
      try {
        this.activeGoodsOrders = await listOrdersByGoods(row.id)
      } finally {
        this.ordersLoading = false
      }
    },
    statusTagType(status) {
      return {
        PENDING: 'info',
        PAID: 'success',
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
  overflow-x: auto;
}

.table-scroll {
  overflow-x: auto;
}

@media (max-width: 768px) {
  .page-header {
    flex-wrap: wrap;
    gap: 12px;
  }

  .page-header > .el-button {
    width: 100%;
  }
}
</style>
