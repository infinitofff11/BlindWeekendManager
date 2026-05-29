/**
 * 用户管理页面逻辑
 */

let currentPage = 1;

document.addEventListener('DOMContentLoaded', () => {
  initSidebar('users');
  loadUsers(1);
});

async function loadUsers(page) {
  currentPage = page;
  const keyword = document.getElementById('searchKeyword').value.trim();
  const status = document.getElementById('filterStatus').value;

  const tbody = document.getElementById('userTableBody');
  tbody.innerHTML = `<tr><td colspan="8" class="loading-spinner"><div class="spinner"></div>加载中...</td></tr>`;

  try {
    const params = { pageNum: page, pageSize: 10 };
    if (keyword) params.keyword = keyword;
    if (status !== '') params.status = parseInt(status);

    const data = await apiGet('/users', params);
    renderUserTable(data.list || []);
    renderPagination('userPagination', data, 'loadUsers', page);
  } catch (e) {
    if (!(e instanceof ApiError)) {
      tbody.innerHTML = emptyTable(8);
    }
  }
}

function renderUserTable(users) {
  const tbody = document.getElementById('userTableBody');
  if (!users.length) {
    tbody.innerHTML = emptyTable(8);
    return;
  }

  let html = '';
  for (const u of users) {
    html += `
      <tr>
        <td>${u.id}</td>
        <td><strong>${escapeHtml(u.nickname || '-')}</strong></td>
        <td>${maskPhone(u.phone)}</td>
        <td>${escapeHtml(u.city || '-')}</td>
        <td>${renderStatusBadge(u.status)}</td>
        <td style="font-size:12px;color:#888">${formatDate(u.created_at)}</td>
        <td style="font-size:12px;color:#888">${formatDateTime(u.last_login_time)}</td>
        <td><div class="action-btns">
          <button class="btn btn-sm btn-outline" onclick="viewUserDetail(${u.id})">详情</button>
          ${u.status === 1
            ? `<button class="btn btn-sm btn-warning" onclick="toggleUserStatus(${u.id},0)">禁用</button>`
            : `<button class="btn btn-sm btn-success" onclick="toggleUserStatus(${u.id},1)">启用</button>`}
        </div></td>
      </tr>`;
  }
  tbody.innerHTML = html;
}

async function viewUserDetail(id) {
  try {
    const user = await apiGet('/users/' + id);
    showDetailModal('用户详情', [
      ['用户ID', user.id],
      ['昵称', user.nickname],
      ['手机号', maskPhone(user.phone)],
      ['城市', user.city || '-'],
      ['头像URL', user.avatar_url || '-'],
      ['状态', user.status === 1 ? '正常' : '已禁用'],
      ['注册时间', formatDateTime(user.created_at)],
      ['最后登录', formatDateTime(user.last_login_time)],
    ]);
  } catch (e) {
    console.error('加载用户详情失败:', e);
    showToast('加载用户详情失败', 'error');
  }
}

async function toggleUserStatus(id, newStatus) {
  showConfirm(
    `确定要${newStatus === 1 ? '启用' : '禁用'}该用户吗？`,
    async () => {
      try {
        await apiPut('/users/' + id + '/status?status=' + newStatus);
        showToast(newStatus === 1 ? '用户已启用' : '用户已禁用');
        loadUsers(currentPage);
      } catch (e) {
        console.error('切换用户状态失败:', e);
      }
    }
  );
}
