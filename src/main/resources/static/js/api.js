/**
 * API 请求封装模块
 * 统一 fetch 调用、错误处理、Token 管理
 */

const API_BASE = '/admin';

/**
 * 发送 GET 请求
 * @param {string} endpoint - 如 /users, /spots 等
 * @param {Object} params - 查询参数对象
 */
async function apiGet(endpoint, params = {}) {
  const query = new URLSearchParams(params).toString();
  const url = `${API_BASE}${endpoint}${query ? '?' + query : ''}`;
  return _request(url, 'GET');
}

/**
 * 发送 POST 请求
 * @param {string} endpoint
 * @param {Object} data - 请求体 JSON
 */
async function apiPost(endpoint, data) {
  return _request(`${API_BASE}${endpoint}`, 'POST', data);
}

/**
 * 发送 PUT 请求
 */
async function apiPut(endpoint, data) {
  return _request(`${API_BASE}${endpoint}`, 'PUT', data);
}

/**
 * 发送 DELETE 请求
 */
async function apiDelete(endpoint) {
  return _request(`${API_BASE}${endpoint}`, 'DELETE');
}

/**
 * 内部请求函数
 */
async function _request(url, method, body) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 从 localStorage 读取 token
  const token = localStorage.getItem('admin_token');
  if (token) {
    options.headers['Authorization'] = 'Bearer ' + token;
  }

  if (body && (method === 'POST' || method === 'PUT')) {
    options.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(url, options);

    // 处理非JSON响应（如HTML错误页面、404等）
    const contentType = response.headers.get('content-type') || '';
    if (!contentType.includes('application/json')) {
      const errText = await response.text().catch(() => '');
      console.error(`[API] ${method} ${url} → HTTP ${response.status} (非JSON响应):`, errText.substring(0, 200));
      showToast(`请求失败(HTTP ${response.status}): 服务器返回了非预期响应`, 'error');
      throw new ApiError(response.status, `HTTP ${response.status}: 非JSON响应`);
    }

    const result = await response.json();

    // 业务层错误处理
    if (result.code !== 200) {
      console.error(`[API] ${method} ${url} → 业务错误 code=${result.code}:`, result.message);
      showToast(result.message || '请求失败', 'error');
      throw new ApiError(result.code, result.message);
    }
    return result.data;
  } catch (err) {
    if (err instanceof ApiError) throw err;
    console.error(`[API] ${method} ${url} → 网络异常:`, err);
    showToast('网络错误：' + (err.message || '连接失败'), 'error');
    throw err;
  }
}

class ApiError {
  constructor(code, message) {
    this.code = code;
    this.message = message;
  }
}
