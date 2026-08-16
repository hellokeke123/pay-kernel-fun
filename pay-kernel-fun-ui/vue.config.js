// 本地开发用 devServer 代理绕开跨域；后续要接真实代理/网关，只需改这里的 target
// （或用 PROXY_TARGET 环境变量覆盖），前端业务代码不用动。
const proxyTarget = process.env.PROXY_TARGET || 'http://localhost:8080'

module.exports = {
  publicPath: './',
  productionSourceMap: false,
  devServer: {
    port: 8081,
    proxy: {
      '/pay-api': {
        target: proxyTarget,
        changeOrigin: true,
        // pathRewrite: { '^/pay-api': '' }
      }
    }
  }
}
