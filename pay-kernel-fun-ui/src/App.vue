<template>
  <router-view />
</template>

<style>
* {
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  margin: 0;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f4f6fb;
}

/* el-dialog 挂载在 body 下，scoped 样式够不到，统一在这里约束最大宽度，兼顾移动端(width=90%)和桌面端 */
.pk-dialog--sm {
  max-width: 420px;
}

.pk-dialog--lg {
  max-width: 900px;
}

.pk-dialog .el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
}
</style>
