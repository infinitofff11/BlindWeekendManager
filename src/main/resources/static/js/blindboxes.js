/**
 * 盲盒管理 - 只读 + 状态管理 + 删除
 */

let boxPage = 1;

document.addEventListener('DOMContentLoaded', () => {
  initSidebar('blindboxes');
  loadBoxStats();
  loadBlindBoxes(1);
});

async function loadBoxStats() {
  try {
    const stats = await apiGet('/blindboxes/stats');
    document.getElementById('boxStatTotal').textContent = stats.total || 0;
    document.getElementById('boxStatOpen').textContent = stats.open || 0;
  } catch (e) {}
}

// ====== 加载列表 ======
async function loadBlindBoxes(page) {
  boxPage = page;
  const tbody = document.getElementById('boxTableBody');
  tbody.innerHTML = `<tr><td colspan="9" class="loading-spinner"><div class="spinner"></div>加载中...</td></tr>`;

  try {
    const params = { pageNum: page, pageSize: 10 };
    const kw = document.getElementById('boxKeyword').value.trim();
    if (kw) params.keyword = kw;
    const st = document.getElementById('boxStatusFilter').value;
    if (st) params.status = st;

    const data = await apiGet('/blindboxes', params);
    renderBoxTable(data.list || []);
    renderPagination('boxPagination', data, 'loadBlindBoxes');
  } catch (e) {
    if (!(e instanceof ApiError)) tbody.innerHTML = emptyTable(9);
  }
}

function renderBoxTable(list) {
  const tbody = document.getElementById('boxTableBody');
  if (!list.length) { tbody.innerHTML = emptyTable(9); return; }

  let html = '';
  for (const b of list) {
    html += `
      <tr>
        <td>${b.id}</td>
        <td><strong>${b.title}</strong>
          ${b.summary_text ? `<br><small style="color:#888">${b.summary_text}</small>` : ''}</td>
        <td>${b.district || '-'}</td>
        <td>${formatDate(b.activity_date)}<br><small style="color:#888">${b.activity_time_period || ''}</small></td>
        <td>${b.current_count || 0} / ${b.required_count || 1}</td>
        <td>${renderBlindBoxStatus(b.status)}</td>
        <td>${b.view_count || 0}</td>
        <td style="font-size:12px;color:#888">${formatDateTime(b.created_at)}</td>
        <td><div class="action-btns">
          <button class="btn btn-sm btn-outline" onclick="viewBoxDetail(${b.id})">详情</button>
          <button class="btn btn-sm btn-warning" onclick="openStatusModal(${b.id},'${b.status}')">改状态</button>
          <button class="btn btn-sm btn-link btn-link-danger" onclick="deleteBox(${b.id})">删除</button>
        </div></td>
      </tr>`;
  }
  tbody.innerHTML = html;
}

// ====== 详情 ======
async function viewBoxDetail(id) {
  try {
    const box = await apiGet('/blindboxes/' + id);
    showDetailModal(`盲盒详情 #${box.id}`, [
      ['ID', box.id],
      ['标题', box.title],
      ['摘要', box.summary_text || '-'],
      ['心情语', box.mood_text || '-'],
      ['地区', box.district || '-'],
      ['活动日期', formatDate(box.activity_date)],
      ['时间段', box.activity_time_period || '-'],
      ['人数需求', `${box.current_count || 0} / ${box.required_count || 1}`],
      ['类型标签', renderTags(box.activity_type_tags)],
      ['状态', renderBlindBoxStatus(box.status)],
      ['浏览量', box.view_count || 0],
      ['发布者ID', box.publisher_id || '-'],
      ['创建时间', formatDateTime(box.created_at)],
    ]);
  } catch (e) {}
}

// ====== 状态变更 ======
function openStatusModal(id, currentStatus) {
  document.getElementById('boxStatusId').value = id;
  document.getElementById('newBoxStatus').value = currentStatus || 'open';
  openModal('boxStatusModal');
}

async function updateBoxStatus() {
  const id = document.getElementById('boxStatusId').value;
  const newStatus = document.getElementById('newBoxStatus').value;
  try {
    await apiPut('/blindboxes/' + id + '/status?status=' + newStatus);
    showToast('状态已更新');
    closeModal('boxStatusModal');
    loadBlindBoxes(boxPage);
    loadBoxStats();
  } catch (e) {
    console.error('更新盲盒状态失败:', e);
  }
}

// ====== 删除 ======
function deleteBox(id) {
  showConfirm(
    '确认要删除该盲盒吗？此操作不可恢复。',
    async () => {
      try {
        await apiDelete(`/blindboxes/${id}`);
        showToast('已删除');
        loadBlindBoxes(boxPage);
        loadBoxStats();
      } catch (e) {
        console.error('删除盲盒失败:', e);
      }
    }
  );
}
