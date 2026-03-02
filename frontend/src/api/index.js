import request from './request'

// 用户相关
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getUserInfo = () => request.get('/auth/info')  // 修正：使用auth/info而不是user/info
export const getUsers = (params) => request.get('/users', { params })
export const updateProfile = (data) => request.put('/users/profile', data)
export const createUser = (data) => request.post('/users', data)
export const updateUser = (id, data) => request.put(`/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/users/${id}`)
export const toggleUserStatus = (id, status) => request.put(
  `/users/${id}/status`,
  status,
  { headers: { 'Content-Type': 'application/json' } }
)
export const resetPassword = (id, newPassword) => request.put(`/users/${id}/password`, { newPassword })
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    params: { dir: 'avatars' },
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 部门相关
export const getDepartments = () => request.get('/department/list')
export const createDepartment = (data) => request.post('/department/create', data)
export const updateDepartment = (data) => request.put('/department/update', data)
export const deleteDepartment = (id) => request.delete(`/department/${id}`)

// 折旧规则相关
export const getDepreciationRules = () => request.get('/depreciation-rules')  // 修正：使用depreciation-rules而不是depreciation/list
export const getActiveDepreciationRules = () => request.get('/depreciation-rules/active')  // 获取启用的折旧规则列表，用于下拉选择
export const createDepreciationRule = (data) => request.post('/depreciation-rules', data)  // 修正
export const updateDepreciationRule = (data) => request.put(`/depreciation-rules/${data.id}`, data)  // 修正
export const disableDepreciationRule = (id) => request.put(
  `/depreciation-rules/${id}/status`,
  false,
  { headers: { 'Content-Type': 'application/json' } }
)  // 修正
export const enableDepreciationRule = (id) => request.put(
  `/depreciation-rules/${id}/status`,
  true,
  { headers: { 'Content-Type': 'application/json' } }
)
export const deleteDepreciationRule = (id) => request.delete(`/depreciation-rules/${id}`)

// 资产相关
export const getAssets = (params) => request.get('/assets', { params })  // 修正：使用assets而不是asset/list
export const getAssetDetail = (id) => request.get(`/assets/${id}`)  // 修正
export const createAsset = (data) => request.post('/assets', data)  // 修正
export const updateAsset = (data) => request.put(`/assets/${data.id}`, data)  // 修正
export const deleteAsset = (id) => request.delete(`/assets/${id}`)  // 修正
export const recalculateAssetValue = (id) => request.post(`/assets/${id}/recalculate`)  // 修正
export const approveAsset = (data) => request.post('/asset/approve', data)
export const uploadAssetImages = (assetId, files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })

  // 注意：`request` 的 baseURL 已经是 `/api`，这里不要再重复拼 `/api`
  return request.post(`/asset-images/upload/${assetId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 后端使用的是 `@RequestParam String directory`，这里用 query 参数传递
export const importAssetImagesFromDirectory = (assetId, directory) =>
  request.post(`/asset-images/import-from-directory/${assetId}`, null, {
    params: { directory }
  })

export const getAssetImages = (assetId) => request.get(`/asset-images/asset/${assetId}`)
export const getPendingAssets = () => request.get('/asset-approval/pending');
export const approveAssetById = (assetId, approverId) => request.post(
  `/asset-approval/${assetId}/approve`,
  null,
  { params: { approverId } }
);
export const rejectAssetById = (assetId, reason, approverId) => request.post(
  `/asset-approval/${assetId}/reject`,
  null,
  { params: { reason, approverId } }
);

// 拍卖相关
export const getAuctions = (params) => request.get('/auction', { params })  // 修改为正确的路径
export const getAuctionDetail = (id) => request.get(`/auction/${id}`)
export const getAuctionBids = (auctionId) => request.get(`/auction/${auctionId}/bids`)
export const createAuction = (data) => request.post('/auction', data)  // 修正：使用/auction而不是/auction/create
export const updateAuction = (data) => request.put('/auction', data)  // 修正
export const deleteAuction = (id) => request.delete(`/auction/${id}`)
// 参与竞价：后端路径为 POST /api/auction/{id}/bid，body: { price }
export const bid = (data) => {
  // 确保auctionId为数字类型，price为数字类型
  const auctionId = Number(data.auctionId);
  const price = Number(data.price);
  return request.post(`/auction/${auctionId}/bid`, { price });
}

// 一键出价：后端路径为 POST /api/auction/{id}/quick-bid
export const quickBid = (auctionId) => request.post(`/auction/${auctionId}/quick-bid`)
export const confirmTransaction = (id) => request.post(`/auction/${id}/confirm`)

// 财务审核相关
export const getPendingApprovals = (params) => request.get('/finance/assets/pending', { params })
export const confirmPayment = (data) => {
  const formData = new FormData();
  formData.append('passed', true);
  if (data.voucher) formData.append('voucher', data.voucher);
  if (data.voucherUrl) formData.append('voucherUrl', data.voucherUrl);
  if (data.remark) formData.append('remark', data.remark);
  return request.post(`/finance/transactions/${data.transactionId}/payment`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
export const rejectPayment = (data) => {
  const formData = new FormData();
  formData.append('passed', false);
  // 后端参数名为 remark（不是 reason）
  formData.append('remark', data.reason);
  return request.post(`/finance/transactions/${data.transactionId}/payment`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

// 财务交易单相关
export const getFinanceTransactions = (params) => request.get('/finance/transactions', { params })

// 资产处置相关
export const getPendingDisposalTransactions = (params) => request.get('/disposal/transactions', { params })
export const getCompletedDisposalTransactions = (params) => request.get('/disposal/transactions/completed', { params })
export const getDisposalHistory = () => request.get('/disposal/history')
export const getDisposedArchiveAssets = (params) => request.get('/disposal/assets/disposed', { params })
export const getDisposedArchiveDetail = (id) => request.get(`/disposal/assets/${id}`)
export const getDisposals = (params = {}) => {
  if (params.id != null) {
    return request.get(`/disposal/assets/${params.id}`)
  }
  return request.get('/disposal/assets', { params })
}
export const confirmDisposal = (data) => {
  const formData = new FormData();
  formData.append('voucher', data.voucher);
  if (data.remark) formData.append('remark', data.remark);
  return request.post(`/disposal/assets/${data.assetId}/confirm`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

// 统计相关
export const getAssetDisposalStatistics = (params) => request.get('/statistics/disposal', { params })
export const getDepartmentDisposalStatistics = (params) => request.get('/statistics/disposal/department', { params })
export const getEmployeeStatistics = (params) => request.get('/statistics/employees', { params })
export const getBidsSummary = (params) => request.get('/statistics/bids/summary', { params })
export const getBiddingStatistics = (params) => request.get('/statistics/bids', { params })
export const getAssetHistory = (id) => request.get(`/statistics/asset/${id}/history`)
export const getDashboardStats = () => request.get('/statistics/dashboard')
export const getDealTrend = (params) => request.get('/statistics/trend', { params })

// 员工交易单
export const getMyPendingPaymentCount = () => request.get('/transactions/my/pending-payment-count')
export const getMyTransactions = (params) => request.get('/transactions/my', { params })
export const submitMyPaymentVoucher = (transactionId, data) => {
  const formData = new FormData()
  if (data?.voucher) formData.append('voucher', data.voucher)
  if (data?.remark) formData.append('remark', data.remark)
  return request.post(`/transactions/${transactionId}/pay`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
