/**
 * 活动地点管理 - 完整CRUD逻辑
 */

let spotPage = 1;
let editingSpotId = null;

document.addEventListener('DOMContentLoaded', () => {
  initSidebar('spots');
  loadSpots(1);
});

// ====== 加载列表 ======
async function loadSpots(page) {
  spotPage = page;
  const tbody = document.getElementById('spotTableBody');
  tbody.innerHTML = `<tr><td colspan="8" class="loading-spinner"><div class="spinner"></div>加载中...</td></tr>`;

  try {
    const params = { pageNum: page, pageSize: 10 };
    const kw = document.getElementById('spotKeyword').value.trim();
    if (kw) params.keyword = kw;
    const typeTag = document.getElementById('spotTypeTag').value;
    if (typeTag) params.typeTag = typeTag;
    const consumeLevel = document.getElementById('spotConsumeLevel').value;
    if (consumeLevel) params.consumeLevel = consumeLevel;
    const city = document.getElementById('spotCity').value;
    if (city) params.city = city;
    const status = document.getElementById('spotStatus').value;
    if (status !== '') params.status = parseInt(status);

    const data = await apiGet('/spots', params);
    renderSpotTable(data.list || []);
    renderPagination('spotPagination', data, 'loadSpots', page);
  } catch (e) {
    if (!(e instanceof ApiError)) {
      tbody.innerHTML = emptyTable(8);
    }
  }
}

function renderSpotTable(spots) {
  const tbody = document.getElementById('spotTableBody');
  if (!spots.length) { tbody.innerHTML = emptyTable(8); return; }

  let html = '';
  for (const s of spots) {
    html += `
      <tr>
        <td>${s.id}</td>
        <td><strong>${s.name}</strong></td>
        <td>${renderTags(s.typeTags)}</td>
        <td>${s.city || '-'}/${s.district || '-'}</td>
        <td>${s.consumePerPerson != null ? '¥' + s.consumePerPerson : '-'}</td>
        <td>${s.recommendDuration ? s.recommendDuration + '分钟' : '-'}</td>
        <td>${renderStatusBadge(s.status)}</td>
        <td><div class="action-btns">
          <button class="btn btn-sm btn-outline" onclick="viewSpotDetail(${s.id})">详情</button>
          <button class="btn btn-sm btn-warning" onclick="openEditSpotModal(${s.id})">编辑</button>
          ${s.status === 1
            ? `<button class="btn btn-sm btn-danger" onclick="toggleSpotStatus(${s.id},0)">禁用</button>`
            : `<button class="btn btn-sm btn-success" onclick="toggleSpotStatus(${s.id},1)">启用</button>`}
          <button class="btn btn-sm btn-link btn-link-danger" onclick="deleteSpot(${s.id})">删除</button>
        </div></td>
      </tr>`;
  }
  tbody.innerHTML = html;
}

// ====== 新增 ======
function openAddSpotModal() {
  editingSpotId = null;
  document.getElementById('spotFormTitle').textContent = '新增活动地点';
  document.getElementById('spotForm').reset();
  openModal('spotFormModal');
}

// ====== 编辑 ======
async function openEditSpotModal(id) {
  try {
    const spot = await apiGet('/spots/' + id);
    editingSpotId = id;
    document.getElementById('spotFormTitle').textContent = '编辑活动地点';

    // 填充表单
    document.getElementById('sf_name').value = spot.name || '';
    document.getElementById('sf_city').value = spot.city || '';
    document.getElementById('sf_district').value = spot.district || '';
    document.getElementById('sf_address').value = spot.address || '';
    document.getElementById('sf_consumePerPerson').value = spot.consumePerPerson || '';
    document.getElementById('sf_recommendDuration').value = spot.recommendDuration || '';
    document.getElementById('sf_consumeLevel').value = spot.consumeLevel || 'low';
    document.getElementById('sf_longitude').value = spot.longitude || '';
    document.getElementById('sf_latitude').value = spot.latitude || '';
    document.getElementById('sf_suggestTimePeriod').value = spot.suggestTimePeriod || 'all_day';
    document.getElementById('sf_suitableCapacity').value = spot.suitableCapacity || 'both';
    // typeTags JSON数组 → 逗号分隔友好显示
    document.getElementById('sf_typeTags').value = jsonArrToCommaStr(spot.typeTags);
    document.getElementById('sf_coverImageUrl').value = spot.coverImageUrl || '';
    document.getElementById('sf_description').value = spot.description || '';

    openModal('spotFormModal');
  } catch (e) {}
}

// ====== 保存(新增/编辑) ======
async function saveSpot() {
  const name = document.getElementById('sf_name').value.trim();
  const address = document.getElementById('sf_address').value.trim();
  if (!name) { showToast('请输入地点名称', 'error'); return; }
  if (!address) { showToast('请输入详细地址', 'error'); return; }

  const body = {
    name,
    city: document.getElementById('sf_city').value.trim(),
    district: document.getElementById('sf_district').value.trim(),
    address,
    consumePerPerson: parseFloat(document.getElementById('sf_consumePerPerson').value) || null,
    recommendDuration: parseInt(document.getElementById('sf_recommendDuration').value) || null,
    consumeLevel: document.getElementById('sf_consumeLevel').value,
    longitude: parseFloat(document.getElementById('sf_longitude').value) || null,
    latitude: parseFloat(document.getElementById('sf_latitude').value) || null,
    suggestTimePeriod: document.getElementById('sf_suggestTimePeriod').value,
    suitableCapacity: document.getElementById('sf_suitableCapacity').value,
    typeTags: makeJsonArray(document.getElementById('sf_typeTags').value.trim()),
    coverImageUrl: document.getElementById('sf_coverImageUrl').value.trim(),
    description: document.getElementById('sf_description').value.trim(),
  };

  try {
    if (editingSpotId) {
      await apiPut('/spots/' + editingSpotId, body);
      showToast('地点更新成功！');
    } else {
      await apiPost('/spots', body);
      showToast('地点创建成功！');
    }
    closeModal('spotFormModal');
    loadSpots(spotPage);
  } catch (e) {
    console.error('保存地点失败:', e);
  }
}

// ====== 详情 ======
async function viewSpotDetail(id) {
  try {
    const s = await apiGet('/spots/' + id);
    showDetailModal('活动地点详情', [
      ['ID', s.id],
      ['名称', s.name],
      ['地址', `${s.city||''} ${s.district||''} ${s.address}`.trim()],
      ['类型标签', renderTags(s.typeTags)],
      ['人均消费', s.consumePerPerson != null ? '¥' + s.consumePerPerson : '-'],
      ['推荐时长', s.recommendDuration ? s.recommendDuration + '分钟' : '-'],
      ['消费水平', s.consumeLevel === 'high' ? '高端消费' : '经济实惠'],
      ['建议时段', s.suggestTimePeriod || '-'],
      ['适合人数', s.suitableCapacity || '-'],
      ['坐标', (s.longitude && s.latitude) ? `${s.latitude}, ${s.longitude}` : '-'],
      ['封面图', s.coverImageUrl || '-'],
      ['简介', s.description || '-'],
      ['状态', s.status === 1 ? '正常' : '已禁用'],
      ['创建时间', formatDateTime(s.createdAt)],
    ]);
  } catch (e) {}
}

// ====== 启用/禁用 ======
function toggleSpotStatus(id, newStatus) {
  showConfirm(
    `确定要${newStatus === 1 ? '启用' : '禁用'}该地点吗？`,
    async () => {
      try {
        await apiPut('/spots/' + id + '/status?status=' + newStatus);
        showToast(newStatus === 1 ? '已启用' : '已禁用');
        loadSpots(spotPage);
      } catch (e) {
        console.error('切换地点状态失败:', e);
      }
    }
  );
}

// ====== 删除 ======
function deleteSpot(id) {
  showConfirm(
    '删除后不可恢复，确认要删除该地点吗？',
    async () => {
      try {
        await apiDelete(`/spots/${id}`);
        showToast('已删除');
        loadSpots(spotPage);
      } catch (e) {
        console.error('删除地点失败:', e);
      }
    }
  );
}
