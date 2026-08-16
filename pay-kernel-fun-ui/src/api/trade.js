import request from './request'

export function createOrder(data) {
  return request.post('/trade/orders', data)
}

export function getOrder(orderNo) {
  return request.get(`/trade/orders/${orderNo}`)
}

export function getRemoteStatus(orderNo) {
  return request.get(`/trade/orders/${orderNo}/remote-status`)
}

export function refundOrder(orderNo, reason) {
  return request.post(`/trade/orders/${orderNo}/refund`, null, {
    params: reason ? { reason } : undefined
  })
}

export function closeOrder(orderNo) {
  return request.post(`/trade/orders/${orderNo}/close`)
}
