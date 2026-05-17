/**
 * API 请求封装模块
 * 统一 fetch 调用、错误处理、Token 管理、CSRF 防护
 */

const API_BASE = '/admin';
const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN';

/**
 * 从 Cookie 中读取指定名称的值（用于 CSRF Double Submit Cookie）
 */
function getCsrfTokenFromCookie() {
  const match = document.cookie.match(new RegExp('(^| )' + CSRF_COOKIE_NAME + '=([^;]+)'));
  return match ? match[2] : null;
}

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

  // JWT Token 已通过 HttpOnly Cookie 自动携带（浏览器行为，JS无法读取）
  // 此处不再需要从 localStorage 读取 token 并设置 Authorization Header
  // Android 客户端仍使用 Authorization Header 方式，由后端 JwtAuthenticationFilter 兼容

  // CSRF 防护：写操作（POST/PUT/DELETE）自动附加 X-XSRF-TOKEN Header
  if (method === 'POST' || method === 'PUT' || method === 'DELETE') {
    const csrfToken = getCsrfTokenFromCookie();
    if (csrfToken) {
      options.headers[CSRF_HEADER_NAME] = csrfToken;
    }
  }

  if (body && (method === 'POST' || method === 'PUT')) {
    options.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(url, options);

    // ========== 401 未认证拦截（Token无效/过期 → 跳转登录页） ==========
    if (response.status === 401) {
      console.warn('[API] Token无效或已过期，跳转到登录页');
      // 清除前端认证状态标记
      document.cookie = 'admin_auth_status=; Path=/; SameSite=Strict; Max-Age=0';
      localStorage.removeItem('admin_info');
      window.location.href = '/login.html';
      throw new ApiError(401, '登录已过期，请重新登录');
    }

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
