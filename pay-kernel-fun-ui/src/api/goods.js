import request from './request'

export function createGoods(data) {
  return request.post('/sample/goods', data)
}

export function listGoods() {
  return request.get('/sample/goods')
}

export function getGoods(id) {
  return request.get(`/sample/goods/${id}`)
}

export function listOrdersByGoods(id) {
  return request.get(`/sample/goods/${id}/orders`)
}
