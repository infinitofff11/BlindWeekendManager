/**
 * 认证管理模块
 * 负责：登录、登出、Token 管理、认证守卫
 */

const AUTH_KEY = 'admin_token';
const LOGIN_URL = '/admin/auth/login';

// ========== 登录功能 ==========

async function handleLogin(event) {
  event.preventDefault();

  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;
  const btn = document.getElementById('btnLogin');
  const errorMsg = document.getElementById('errorMsg');

  // 前端基本校验
  if (!username || !password) {
    showError('请输入用户名和密码');
    return;
  }

  // UI 状态：加载中
  btn.disabled = true;
  btn.textContent = '登录中...';
  hideError();

  try {
    // 调用登录接口（不经过 apiGet/apiPost，因为还没有 token）
    const response = await fetch(LOGIN_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const result = await response.json();

    if (result.code === 200 && result.data) {
      // 登录成功：保存 token 到 localStorage
      localStorage.setItem(AUTH_KEY, result.data.token);

      // 保存管理员信息（用于顶部栏显示昵称）
      if (result.data.admin) {
        localStorage.setItem('admin_info', JSON.stringify(result.data.admin));
      }

      // 跳转到首页
      window.location.href = '/';
    } else {
      // 业务错误（密码错误等）
      showError(result.message || '登录失败，请检查用户名和密码');
    }
  } catch (err) {
    console.error('登录失败:', err);
    showError('网络错误，请检查网络连接');
  } finally {
    btn.disabled = false;
    btn.textContent = '登 录';
  }
}

// ========== 登出功能 ==========

function logout() {
  localStorage.removeItem(AUTH_KEY);
  localStorage.removeItem('admin_info');
  window.location.href = '/login.html';
}

// ========== 认证守卫 ==========

function checkAuth() {
  const token = localStorage.getItem(AUTH_KEY);
  if (!token) {
    // 未登录，跳转到登录页
    window.location.href = '/login.html';
    return false;
  }
  return true;
}

/**
 * 页面初始化时自动执行守卫检查 + 显示管理员昵称
 * - 非登录页面且无 token → 跳转登录页
 * - 已在登录页且有 token → 直接跳转首页
 */
function initAuthGuard() {
  var isLoginPage = window.location.pathname.endsWith('login.html');

  if (!isLoginPage && !checkAuth()) {
    // checkAuth 会自动跳转
    return;
  }

  // 如果已在登录页且有有效 token，直接跳转首页
  if (isLoginPage && localStorage.getItem(AUTH_KEY)) {
    window.location.href = '/';
    return;
  }

  // 在所有非登录页面显示管理员昵称
  var info = getAdminInfo();
  if (info && info.nickname) {
    var el = document.getElementById('adminName');
    if (el) el.textContent = info.nickname;
  }
}

// ========== 工具函数 ==========

function showError(msg) {
  var el = document.getElementById('errorMsg');
  if (el) {
    el.textContent = msg;
    el.style.display = 'block';
  }
  // 也调用全局 toast（如果 common.js 已加载）
  if (typeof showToast === 'function') {
    showToast(msg, 'error', 3000);
  }
}

function hideError() {
  var el = document.getElementById('errorMsg');
  if (el) el.style.display = 'none';
}

/**
 * 获取当前登录的管理员信息（从 localStorage）
 */
function getAdminInfo() {
  var info = localStorage.getItem('admin_info');
  return info ? JSON.parse(info) : null;
}

// 页面加载时自动执行守卫检查
document.addEventListener('DOMContentLoaded', initAuthGuard);
