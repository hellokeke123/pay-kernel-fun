import axios from 'axios'
import { Message } from 'element-ui'

const request = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 15000
})

request.interceptors.response.use(
  response => response.data,
  error => {
    const res = error.response
    const message = (res && res.data && (res.data.message || res.data.msg)) || error.message || '请求失败'
    Message.error(message)
    return Promise.reject(Object.assign(new Error(message), { response: res }))
  }
)

export default request
