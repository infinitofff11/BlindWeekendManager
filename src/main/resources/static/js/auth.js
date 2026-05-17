/**
 * 认证管理模块
 * 负责：登录、登出、认证守卫
 *
 * 安全架构：
 * - JWT Token 通过 HttpOnly Cookie 传递（浏览器自动携带，JS 无法读取，防 XSS）
 * - 认证状态通过 admin_auth_status Cookie 检测（非 HttpOnly，供前端 guard 使用）
 * - CSRF 防护通过 XSRF-TOKEN Cookie + X-XSRF-TOKEN Header（Double Submit Cookie）
 */

const AUTH_STATUS_COOKIE = 'admin_auth_status';
const LOGIN_URL = '/admin/auth/login';
const LOGOUT_URL = '/admin/auth/logout';

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
      // 登录成功：
      // - JWT Token 已通过 Set-Cookie (HttpOnly) 自动设置，无需手动存储
      // - admin_auth_status Cookie 也已自动设置
      // - CSRF Token (XSRF-TOKEN) Cookie 也已自动设置

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

async function logout() {
  try {
    // 调用后端登出接口，清除 HttpOnly Cookie
    // 需要附带 CSRF Token（写操作）
    const csrfToken = getCsrfTokenFromCookie();
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken) {
      headers['X-XSRF-TOKEN'] = csrfToken;
    }
    await fetch(LOGOUT_URL, { method: 'POST', headers });
  } catch (e) {
    console.warn('调用登出接口失败，继续前端清理:', e);
  }

  // 前端清理：清除认证状态 Cookie 和管理员信息
  document.cookie = 'admin_auth_status=; Path=/; SameSite=Strict; Max-Age=0';
  document.cookie = 'XSRF-TOKEN=; Path=/; SameSite=Strict; Max-Age=0';
  localStorage.removeItem('admin_info');
  window.location.href = '/login.html';
}

// ========== 认证守卫 ==========

/**
 * 检查认证状态
 * 通过 admin_auth_status Cookie 检测（该 Cookie 由后端登录时设置，非 HttpOnly）
 */
function checkAuth() {
  const match = document.cookie.match(new RegExp('(^| )' + AUTH_STATUS_COOKIE + '=([^;]+)'));
  if (!match) {
    // 未登录，跳转到登录页
    window.location.href = '/login.html';
    return false;
  }
  return true;
}

/**
 * 页面初始化时自动执行守卫检查 + 显示管理员昵称
 * - 非登录页面且无认证 Cookie → 跳转登录页
 * - 已在登录页且有认证 Cookie → 直接跳转首页
 */
function initAuthGuard() {
  var isLoginPage = window.location.pathname.endsWith('login.html');
  var isAuthenticated = document.cookie.match(new RegExp('(^| )' + AUTH_STATUS_COOKIE + '=([^;]+)'));

  if (!isLoginPage && !isAuthenticated) {
    // 未登录，跳转
    window.location.href = '/login.html';
    return;
  }

  // 如果已在登录页且已登录，直接跳转首页
  if (isLoginPage && isAuthenticated) {
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
