// 测试模式配置 - 不使用MetaMask

// 在CreateOrderView.vue的script部分添加以下配置：

const TEST_MODE = true // 设置为false以使用MetaMask
const TEST_ACCOUNT = '0xb9721E9961Ac19A0362543733281A6db8d47d26A' // Ganache测试账户

// 修改submitForm函数：
const submitForm = async () => {
  if (!orderFormRef.value) return

  if (TEST_MODE) {
    // 测试模式：使用固定地址
    if (!connected.value) {
      currentAccount.value = TEST_ACCOUNT
      connected.value = true
    }
  } else {
    // 正常模式：需要连接MetaMask
    if (!connected.value) {
      ElMessage.warning('请先连接MetaMask钱包')
      return
    }
  }

  await orderFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const params = new URLSearchParams()
        params.append('amount', orderForm.amount)
        params.append('price', orderForm.price)
        params.append('sellerAddress', TEST_MODE ? TEST_ACCOUNT : currentAccount.value)
        
        const response = await axios.post('/api/order/create', params)
        ElMessage.success('挂单发布成功')
        orderForm.amount = 0
        orderForm.price = 0
        orderFormRef.value.resetFields()
      } catch (error) {
        console.error('发布挂单失败:', error)
        const errorMessage = error.response?.data?.message || error.message || '发布挂单失败'
        ElMessage.error(errorMessage)
        
        if (!TEST_MODE && errorMessage.includes('MetaMask')) {
          ElMessage.info('提示：请确保MetaMask已安装并连接到Ganache网络')
        }
      } finally {
        submitting.value = false
      }
    }
  })
}