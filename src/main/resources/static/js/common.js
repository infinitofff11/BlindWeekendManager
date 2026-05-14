/**
 * 公共工具函数模块
 * 包含：Toast提示、分页器、弹窗、侧边栏、表格空状态等
 */

/* ===== Toast 提示 ===== */
function showToast(message, type = 'success', duration = 2500) {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all 0.25s';
    setTimeout(() => toast.remove(), 260);
  }, duration);
}

/* ===== 分页器组件 ===== */
/**
 * @param {string} containerId   分页容器 DOM id
 * @param {Object} pageData     后端返回的分页数据 { pageNum, pageSize, total, pages, list }
 * @param {string} onPageChange 翻页回调函数名（字符串）
 * @param {number} [forcePage]  强制指定当前页码（优先级高于 pageData.pageNum）
 */
function renderPagination(containerId, pageData, onPageChange, forcePage) {
  const el = document.getElementById(containerId);
  if (!el) return;

  // 防御性取值：兼容不同后端返回格式，避免 undefined 导致 NaN
  const pageNum = (forcePage != null ? forcePage : Number(pageData.pageNum)) || 1;
  const pageSize = Number(pageData.pageSize) || 10;
  const total = Number(pageData.total) || 0;
  const pages = Number(pageData.pages) || Math.max(1, Math.ceil(total / pageSize));

  const start = total > 0 ? (pageNum - 1) * pageSize + 1 : 0;
  const end = total > 0 ? Math.min(pageNum * pageSize, total) : 0;

  let html = `
    <div class="pagination-info">
      显示 ${total > 0 ? start : 0} - ${end} 条，共 <b>${total}</b> 条
    </div>
    <div class="pagination-btns">
      <button onclick="${onPageChange}(1)" ${pageNum <= 1 ? 'disabled' : ''}>&laquo; 首页</button>
      <button onclick="${onPageChange}(${pageNum - 1})" ${pageNum <= 1 ? 'disabled' : ''}>&lsaquo;</button>`;

  // 显示页码按钮（最多显示7个）
  let startPage = Math.max(1, pageNum - 3);
  let endPage = Math.min(pages, startPage + 6);
  if (endPage - startPage < 6) {
    startPage = Math.max(1, endPage - 6);
  }
  for (let i = startPage; i <= endPage; i++) {
    html += `<button class="${i === pageNum ? 'active' : ''}" onclick="${onPageChange}(${i})">${i}</button>`;
  }

  html += `
      <button onclick="${onPageChange}(${pageNum + 1})" ${pageNum >= pages ? 'disabled' : ''}>&rsaquo;</button>
      <button onclick="${onPageChange}(${pages})" ${pageNum >= pages ? 'disabled' : ''}>末页 &raquo;</button>
    </div>`;
  el.innerHTML = html;
}

/* ===== Modal 弹窗管理 ===== */
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('show');
}
function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('show');
}

// 确认对话框
function showConfirm(message, onConfirm) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay show';
  overlay.id = '__confirm_dialog__';
  overlay.innerHTML = `
    <div class="modal-box" style="max-width:420px">
      <div class="modal-header"><h3>确认操作</h3><button class="modal-close" onclick="closeConfirm()">&times;</button></div>
      <div class="modal-body">
        <p class="confirm-msg">${message}</p>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" onclick="closeConfirm()">取消</button>
        <button class="btn btn-danger" id="__confirm_ok_btn__">确定</button>
      </div>
    </div>`;
  document.body.appendChild(overlay);
  document.getElementById('__confirm_ok_btn__').onclick = () => {
    closeConfirm();
    if (typeof onConfirm === 'function') onConfirm();
  };
}

function closeConfirm() {
  const el = document.getElementById('__confirm_dialog__');
  if (el) el.remove();
}

// 详情展示弹窗
function showDetailModal(title, fields) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay show';
  overlay.id = '__detail_modal__';

  let gridHtml = '';
  for (const [label, value] of fields) {
    const v = value !== undefined && value !== null && value !== '' ? value : '-';
    gridHtml += `<div class="detail-label">${label}</div><div class="detail-value">${v}</div>`;
  }

  overlay.innerHTML = `
    <div class="modal-box" style="max-width:560px">
      <div class="modal-header"><h3>${title}</h3><button class="modal-close" onclick="closeDetailModal()">&times;</button></div>
      <div class="modal-body">
        <div class="detail-grid">${gridHtml}</div>
      </div>
      <div class="modal-footer"><button class="btn btn-outline" onclick="closeDetailModal()">关闭</button></div>
    </div>`;
  document.body.appendChild(overlay);
}

function closeDetailModal() {
  const el = document.getElementById('__detail_modal__');
  if (el) el.remove();
}

/* ===== 侧边栏高亮 ===== */
function initSidebar(activeKey) {
  // activeKey 对应 data-page 属性
  document.querySelectorAll('.sidebar-nav a').forEach(a => {
    a.classList.toggle('active', a.dataset.page === activeKey);
  });
}

/* ===== 表格空状态 ===== */
function emptyTable(colspan = 6) {
  return `<tr><td colspan="${colspan}" class="empty-state"><span class="empty-icon">📭</span>暂无数据</td></tr>`;
}

/* ===== 工具函数 ===== */

// 手机号脱敏
function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '';
  return phone.substring(0, 3) + '****' + phone.substring(phone.length - 4);
}

// 格式化日期时间
function formatDateTime(dt) {
  if (!dt) return '-';
  const d = new Date(dt);
  if (isNaN(d.getTime())) return dt;
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

// 格式化日期
function formatDate(dt) {
  if (!dt) return '-';
  const d = new Date(dt);
  if (isNaN(d.getTime())) return dt;
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
}

// 将逗号分隔字符串转为JSON数组字符串，用于MySQL JSON类型列
// "文艺,探店" → '["文艺","探店"]'，空值返回 null
// 若输入已经是合法JSON数组字符串则原样返回（避免二次保存损坏）
function makeJsonArray(raw) {
  if (!raw || !raw.trim()) return null;
  const trimmed = raw.trim();
  // 如果已经是合法 JSON 数组，原样返回，不做二次转换
  if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
    try {
      const parsed = JSON.parse(trimmed);
      if (Array.isArray(parsed)) return trimmed;
    } catch(e) { /* 不是合法JSON，继续走逗号分割逻辑 */ }
  }
  const arr = raw.split(',').map(s => s.trim()).filter(Boolean);
  return arr.length > 0 ? JSON.stringify(arr) : null;
}

// 将JSON数组字符串转为逗号分隔的友好格式（用于编辑回显）
// '["文艺","探店"]' → "文艺,探店"
function jsonArrToCommaStr(jsonStr) {
  if (!jsonStr || typeof jsonStr !== 'string') return '';
  try {
    const arr = JSON.parse(jsonStr);
    return Array.isArray(arr) ? arr.join(',') : jsonStr;
  } catch(e) { return jsonStr; }
}

// 渲染标签列表
function renderTags(tagStrOrArray) {
  if (!tagStrOrArray) return '<span class="badge badge-default">-</span>';
  let tags;
  if (Array.isArray(tagStrOrArray)) {
    tags = tagStrOrArray;
  } else {
    try {
      tags = JSON.parse(tagStrOrArray);
    } catch (e) {
      tags = tagStrOrArray.split(',').map(s => s.trim()).filter(Boolean);
    }
  }
  if (!tags.length) return '<span class="badge badge-default">-</span>';
  return tags.map(t => `<span class="tag-chip">${t}</span>`).join('');
}

// 状态映射
const STATUS_MAP = {
  1: { text: '正常', cls: 'badge-success' },
  0: { text: '禁用', cls: 'badge-danger' },
};
const BLINDBOX_STATUS_MAP = {
  open: { text: '招募中', cls: 'badge-success' },
  full: { text: '已满员', cls: 'badge-warning' },
  closed: { text: '已关闭', cls: 'badge-danger' },
  cancelled: { text: '已取消', cls: 'badge-default' },
};

function renderStatusBadge(status) {
  const s = STATUS_MAP[status] || { text: status, cls: 'badge-default' };
  return `<span class="badge ${s.cls}">${s.text}</span>`;
}

function renderBlindBoxStatus(status) {
  const s = BLINDBOX_STATUS_MAP[status] || { text: status, cls: 'badge-default' };
  return `<span class="badge ${s.cls}">${s.text}</span>`;
}
