/**
 * 评论与签到管理 - Tab切换 + 列表展示 + 操作
 */

let checkinPage = 1;
let reviewPage = 1;

document.addEventListener('DOMContentLoaded', () => {
  initSidebar('reviews');
  loadCheckins(1);
  loadReviews(1);
});

// ====== Tab 切换 ======
function switchReviewTab(tabId, clickedEl) {
  document.querySelectorAll('.tab-item').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
  clickedEl.classList.add('active');
  document.getElementById(tabId).classList.add('active');
}

// ====== 签到记录 ======
async function loadCheckins(page) {
  checkinPage = page;
  const tbody = document.getElementById('checkinTableBody');
  tbody.innerHTML = `<tr><td colspan="8" class="loading-spinner"><div class="spinner"></div>加载中...</td></tr>`;

  try {
    const data = await apiGet('/reviews/checkins', { pageNum: page, pageSize: 10 });
    renderCheckinTable(data.list || []);
    renderPagination('checkinPagination', data, 'loadCheckins');
  } catch (e) {
    if (!(e instanceof ApiError)) tbody.innerHTML = emptyTable(8);
  }
}

function renderCheckinTable(list) {
  const tbody = document.getElementById('checkinTableBody');
  if (!list.length) { tbody.innerHTML = emptyTable(8); return; }

  let html = '';
  for (const c of list) {
    const photoCount = c.photoUrls ? (Array.isArray(c.photoUrls) ? c.photoUrls.length : JSON.parse(c.photoUrls || '[]').length) : 0;
    html += `
      <tr>
        <td>${c.id}</td>
        <td>${c.userId || '-'}</td>
        <td><span class="tag-chip">${c.targetType || '-'}</span></td>
        <td>${c.targetId || '-'}</td>
        <td>${c.rating ? '⭐'.repeat(c.rating) + '☆'.repeat(5 - c.rating) : '-'}</td>
        <td style="text-align:center">${photoCount} 张</td>
        <td style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${c.shortReview || '-'}</td>
        <td style="font-size:12px;color:#888;white-space:nowrap">${formatDateTime(c.checkInTime)}</td>
      </tr>`;
  }
  tbody.innerHTML = html;
}

// ====== 评论管理 ======
async function loadReviews(page) {
  reviewPage = page;
  const tbody = document.getElementById('reviewTableBody');
  tbody.innerHTML = `<tr><td colspan="7" class="loading-spinner"><div class="spinner"></div>加载中...</td></tr>`;

  try {
    const params = { pageNum: page, pageSize: 10 };
    const st = document.getElementById('reviewStatusFilter').value;
    if (st !== '') params.status = parseInt(st);

    const data = await apiGet('/reviews', params);
    renderReviewTable(data.list || []);
    renderPagination('reviewPagination', data, 'loadReviews');
  } catch (e) {
    if (!(e instanceof ApiError)) tbody.innerHTML = emptyTable(7);
  }
}

function renderReviewTable(list) {
  const tbody = document.getElementById('reviewTableBody');
  if (!list.length) { tbody.innerHTML = emptyTable(7); return; }

  let html = '';
  for (const r of list) {
    html += `
      <tr>
        <td>${r.id}</td>
        <td>${r.spotId || '-'}</td>
        <td style="max-width:240px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${r.content || '-'}</td>
        <td>${r.rating ? '⭐'.repeat(r.rating) + '☆'.repeat(5 - r.rating) : '-'}</td>
        <td>${renderStatusBadge(r.status)}</td>
        <td style="font-size:12px;color:#888;white-space:nowrap">${formatDateTime(r.createdAt)}</td>
        <td><div class="action-btns">
          ${r.status === 1
            ? `<button class="btn btn-sm btn-warning" onclick="toggleReviewStatus(${r.id},0)">隐藏</button>`
            : `<button class="btn btn-sm btn-success" onclick="toggleReviewStatus(${r.id},1)">显示</button>`}
          <button class="btn btn-sm btn-link btn-link-danger" onclick="deleteReview(${r.id})">删除</button>
        </div></td>
      </tr>`;
  }
  tbody.innerHTML = html;
}

// ====== 显示/隐藏评论 ======
function toggleReviewStatus(id, newStatus) {
  showConfirm(
    `确定要${newStatus === 1 ? '显示' : '隐藏'}该评论吗？`,
    async () => {
      try {
        await apiPut('/reviews/' + id + '/status?status=' + newStatus);
        showToast(newStatus === 1 ? '评论已显示' : '评论已隐藏');
        loadReviews(reviewPage);
      } catch (e) {
        console.error('切换评论状态失败:', e);
      }
    }
  );
}

// ====== 删除评论 ======
function deleteReview(id) {
  showConfirm(
    '确认删除该评论？',
    async () => {
      try {
        await apiDelete(`/reviews/${id}`);
        showToast('已删除');
        loadReviews(reviewPage);
      } catch (e) {
        console.error('删除评论失败:', e);
      }
    }
  );
}
