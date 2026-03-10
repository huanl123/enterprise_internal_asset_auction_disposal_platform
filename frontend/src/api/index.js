import request from './request'

// User
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getUserInfo = () => request.get('/auth/info')
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

// Department
export const getDepartments = () => request.get('/department/list')
export const createDepartment = (data) => request.post('/department/create', data)
export const updateDepartment = (data) => request.put('/department/update', data)
export const deleteDepartment = (id) => request.delete(`/department/${id}`)

// Depreciation Rules
export const getDepreciationRules = () => request.get('/depreciation-rules')
export const getActiveDepreciationRules = () => request.get('/depreciation-rules/active')
export const createDepreciationRule = (data) => request.post('/depreciation-rules', data)
export const updateDepreciationRule = (data) => request.put(`/depreciation-rules/${data.id}`, data)
export const disableDepreciationRule = (id) => request.put(
  `/depreciation-rules/${id}/status`,
  false,
  { headers: { 'Content-Type': 'application/json' } }
)
export const enableDepreciationRule = (id) => request.put(
  `/depreciation-rules/${id}/status`,
  true,
  { headers: { 'Content-Type': 'application/json' } }
)
export const deleteDepreciationRule = (id) => request.delete(`/depreciation-rules/${id}`)

// Assets
export const getAssets = (params) => request.get('/assets', { params })
export const getAssetDetail = (id) => request.get(`/assets/${id}`)
export const createAsset = (data) => request.post('/assets', data)
export const updateAsset = (data) => request.put(`/assets/${data.id}`, data)
export const deleteAsset = (id) => request.delete(`/assets/${id}`)
export const recalculateAssetValue = (id) => request.post(`/assets/${id}/recalculate`)
export const approveAsset = (data) => request.post('/asset/approve', data)

export const uploadAssetImages = (assetId, files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post(`/asset-images/upload/${assetId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const importAssetImagesFromDirectory = (assetId, directory) =>
  request.post(`/asset-images/import-from-directory/${assetId}`, null, {
    params: { directory }
  })

export const getAssetImages = (assetId) => request.get(`/asset-images/asset/${assetId}`)
export const getPendingAssets = () => request.get('/asset-approval/pending')
export const approveAssetById = (assetId, approverId) => request.post(
  `/asset-approval/${assetId}/approve`,
  null,
  { params: { approverId } }
)
export const rejectAssetById = (assetId, reason, approverId) => request.post(
  `/asset-approval/${assetId}/reject`,
  null,
  { params: { reason, approverId } }
)

// Auctions
export const getAuctions = (params) => request.get('/auction', { params })
export const getAuctionDetail = (id) => request.get(`/auction/${id}`)
export const getAuctionBids = (auctionId) => request.get(`/auction/${auctionId}/bids`)
export const createAuction = (data) => request.post('/auction', data)
export const updateAuction = (data) => request.put('/auction', data)
export const deleteAuction = (id) => request.delete(`/auction/${id}`)
export const bid = (data) => {
  const auctionId = Number(data.auctionId)
  const price = Number(data.price)
  return request.post(`/auction/${auctionId}/bid`, { price })
}
export const quickBid = (auctionId) => request.post(`/auction/${auctionId}/quick-bid`)
export const withdrawBid = (auctionId) => request.post(`/auction/${auctionId}/withdraw`)
export const confirmTransaction = (id) => request.post(`/auction/${id}/confirm`)

// Finance
export const getPendingApprovals = (params) => request.get('/finance/assets/pending', { params })
export const confirmPayment = (data) => {
  const formData = new FormData()
  formData.append('passed', true)
  if (data.voucher) formData.append('voucher', data.voucher)
  if (data.voucherUrl) formData.append('voucherUrl', data.voucherUrl)
  if (data.remark) formData.append('remark', data.remark)
  return request.post(`/finance/transactions/${data.transactionId}/payment`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const rejectPayment = (data) => {
  const formData = new FormData()
  formData.append('passed', false)
  formData.append('remark', data.reason)
  return request.post(`/finance/transactions/${data.transactionId}/payment`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export const getFinanceTransactions = (params) => request.get('/finance/transactions', { params })

// Disposal
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
  const formData = new FormData()
  formData.append('voucher', data.voucher)
  if (data.remark) formData.append('remark', data.remark)
  return request.post(`/disposal/assets/${data.assetId}/confirm`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// Statistics
export const getAssetDisposalStatistics = (params) => request.get('/statistics/disposal', { params })
export const getDepartmentDisposalStatistics = (params) => request.get('/statistics/disposal/department', { params })
export const getDisposalTrendStatistics = (params) => request.get('/statistics/disposal/trend', { params })
export const getDepartmentDisposalComparison = (params) => request.get('/statistics/disposal/department-comparison', { params })
export const getEmployeeStatistics = (params) => request.get('/statistics/employees', { params })
export const getBidsSummary = (params) => request.get('/statistics/bids/summary', { params })
export const getBiddingStatistics = (params) => request.get('/statistics/bids', { params })
export const getAssetHistory = (id) => request.get(`/statistics/asset/${id}/history`)
export const getDashboardStats = () => request.get('/statistics/dashboard')
export const getDealTrend = (params) => request.get('/statistics/trend', { params })

// Employee transactions
export const getMyPendingPaymentCount = () => request.get('/transactions/my/pending-payment-count')
export const getMyTransactions = (params) => request.get('/transactions/my', { params })
