/**
 * 方案模板管理 - 含动态时段编辑
 */

let tplPage = 1;
let editingTplId = null;
let segmentCounter = 0;

document.addEventListener('DOMContentLoaded', () => {
  initSidebar('templates');
  loadTemplates(1);
});

// ====== 加载列表 ======
async function loadTemplates(page) {
  tplPage = page;
  const tbody = document.getElementById('tplTableBody');
  tbody.innerHTML = `<tr><td colspan="9" class="loading-spinner"><div class="spinner"></div>加载中...</td></tr>`;

  try {
    const params = { pageNum: page, pageSize: 10 };
    const kw = document.getElementById('tplKeyword').value.trim();
    if (kw) params.keyword = kw;

    const data = await apiGet('/templates', params);
    renderTplTable(data.list || []);
    renderPagination('tplPagination', data, 'loadTemplates', page);
  } catch (e) {
    if (!(e instanceof ApiError)) tbody.innerHTML = emptyTable(9);
  }
}

function renderTplTable(list) {
  const tbody = document.getElementById('tplTableBody');
  if (!list.length) { tbody.innerHTML = emptyTable(9); return; }

  let html = '';
  for (const t of list) {
    html += `
      <tr>
        <td>${t.id}</td>
        <td><strong>${escapeHtml(t.name)}</strong></td>
        <td><span class="tag-chip">${escapeHtml(t.themeType || '-')}</span></td>
        <td>${t.totalDuration ? t.totalDuration + '分钟' : '-'}</td>
        <td>${t.consumeLevel === 'high' ? '高端' : '经济'}</td>
        <td style="text-align:center">${t._segmentCount != null ? t._segmentCount : '-'}</td>
        <td style="text-align:center">${t.sortOrder ?? 0}</td>
        <td>${renderStatusBadge(t.status)}</td>
        <td><div class="action-btns">
          <button class="btn btn-sm btn-outline" onclick="viewTemplateDetail(${t.id})">详情</button>
          <button class="btn btn-sm btn-warning" onclick="openEditTemplateModal(${t.id})">编辑</button>
          ${t.status === 1
            ? `<button class="btn btn-sm btn-danger" onclick="toggleTplStatus(${t.id},0)">禁用</button>`
            : `<button class="btn btn-sm btn-success" onclick="toggleTplStatus(${t.id},1)">启用</button>`}
          <button class="btn btn-sm btn-link btn-link-danger" onclick="deleteTemplate(${t.id})">删除</button>
        </div></td>
      </tr>`;
  }
  tbody.innerHTML = html;
}

// ====== 新增模板 ======
function openAddTemplateModal() {
  editingTplId = null;
  segmentCounter = 0;
  document.getElementById('tplFormTitle').textContent = '新增方案模板';
  document.getElementById('tplForm').reset();
  // 默认添加2个空时段
  document.getElementById('segmentList').innerHTML = '';
  addSegmentRow(); addSegmentRow();
  openModal('templateFormModal');
}

// ====== 编辑模板 ======
async function openEditTemplateModal(id) {
  try {
    const result = await apiGet('/templates/' + id);
    const tpl = result.template; // 后端返回 { template, segments }
    const segments = result.segments || [];

    editingTplId = id;
    segmentCounter = 0;
    document.getElementById('tplFormTitle').textContent = '编辑方案模板';

    document.getElementById('tf_name').value = tpl.name || '';
    document.getElementById('tf_themeType').value = tpl.themeType || '文艺';
    document.getElementById('tf_totalDuration').value = tpl.totalDuration || '';
    document.getElementById('tf_consumeLevel').value = tpl.consumeLevel || 'low';
    document.getElementById('tf_sortOrder').value = tpl.sortOrder ?? 0;
    document.getElementById('tf_description').value = tpl.description || '';

    // 填充时段
    const listEl = document.getElementById('segmentList');
    listEl.innerHTML = '';
    if (segments.length === 0) { addSegmentRow(); addSegmentRow(); }
    else {
      for (const seg of segments) {
        addSegmentRow(seg.segmentOrder, seg.startTime, seg.endTime, seg.activityTypes, seg.segmentName);
      }
    }

    openModal('templateFormModal');
  } catch (e) {
    console.error('加载模板详情失败:', e);
    showToast('加载模板详情失败', 'error');
  }
}

// ====== 动态添加/删除时段行 ======
function addSegmentRow(order, startTime, endTime, activityTypes, name) {
  segmentCounter++;
  order = order || segmentCounter;
  startTime = startTime || '09:00';
  endTime = endTime || '11:00';
  activityTypes = activityTypes ? jsonArrToCommaStr(activityTypes) : '';
  name = name || `第${order}段`;

  const div = document.createElement('div');
  div.className = 'segment-item';
  div.dataset.idx = segmentCounter;
  div.innerHTML = `
    <input type="number" placeholder="序号" value="${order}" min="1" style="width:100%">
    <input type="time" value="${escapeHtml(startTime)}" style="flex:1">
    <input type="time" value="${escapeHtml(endTime)}" style="flex:1">
    <input type="text" placeholder="名称" value="${escapeHtml(name)}" style="flex:2">
    <input type="text" placeholder="活动类型(逗号分隔)" value="${escapeHtml(activityTypes)}" style="flex:2">
    <button type="button" class="btn btn-sm btn-link btn-link-danger" onclick="this.parentElement.remove()">×</button>`;
  document.getElementById('segmentList').appendChild(div);
}

// ====== 收集表单数据(含时段) ======
function collectFormData() {
  const name = document.getElementById('tf_name').value.trim();
  if (!name) { showToast('请输入模板名称', 'error'); return null; }

  // 收集时段
  const segments = [];
  const rows = document.querySelectorAll('#segmentList .segment-item');
  for (const row of rows) {
    const inputs = row.querySelectorAll('input');
    const sOrder = parseInt(inputs[0].value);
    const sStart = inputs[1].value;
    const sEnd = inputs[2].value;
    const sName = inputs[3].value.trim();
    const sTypes = inputs[4].value.trim();

    if (!sName || !sStart || !sEnd) continue;
    segments.push({
      segmentOrder: sOrder || segments.length + 1,
      startTime: sStart,
      endTime: sEnd,
      segmentName: sName,
      activityTypes: makeJsonArray(sTypes),
    });
  }

  if (!segments.length) { showToast('请至少添加一个时间段', 'error'); return null; }

  return {
    name,
    themeType: document.getElementById('tf_themeType').value,
    totalDuration: parseInt(document.getElementById('tf_totalDuration').value) || null,
    consumeLevel: document.getElementById('tf_consumeLevel').value,
    sortOrder: parseInt(document.getElementById('tf_sortOrder').value) || 0,
    description: document.getElementById('tf_description').value.trim(),
    segments,
  };
}

// ====== 保存 ======
async function saveTemplate() {
  const body = collectFormData();
  if (!body) return;

  try {
    if (editingTplId) {
      await apiPut('/templates/' + editingTplId, body);
      showToast('模板更新成功！');
    } else {
      await apiPost('/templates', body);
      showToast('模板创建成功！');
    }
    closeModal('templateFormModal');
    loadTemplates(tplPage);
  } catch (e) {
    console.error('保存模板失败:', e);
  }
}

// ====== 详情 ======
async function viewTemplateDetail(id) {
  try {
    const result = await apiGet('/templates/' + id);
    const tpl = result.template;
    const segments = result.segments || [];

    let segHtml = '<h5 style="margin:12px 0 8px;color:#4a90d9;font-size:14px">⏱️ 时间段配置</h5>';
    if (!segments.length) {
      segHtml += '<p style="color:#aaa">无时段数据</p>';
    } else {
      segHtml += '<table class="data-table" style="font-size:12px"><thead><tr><th>#</th><th>名称</th><th>开始</th><th>结束</th><th>允许类型</th></tr></thead><tbody>';
      for (const s of segments) {
        segHtml += `<tr><td>${s.segmentOrder}</td><td>${escapeHtml(s.segmentName)}</td><td>${escapeHtml(s.startTime)}</td><td>${escapeHtml(s.endTime)}</td><td>${renderTags(s.activityTypes)}</td></tr>`;
      }
      segHtml += '</tbody></table>';
    }

    const contentEl = document.getElementById('tplDetailContent');
    contentEl.innerHTML = `
      <div class="detail-grid" style="margin-bottom:16px">
        <div class="detail-label">ID</div><div class="detail-value">${tpl.id}</div>
        <div class="detail-label">名称</div><div class="detail-value"><strong>${escapeHtml(tpl.name)}</strong></div>
        <div class="detail-label">主题类型</div><div class="detail-value">${escapeHtml(tpl.themeType || '-')}</div>
        <div class="detail-label">描述</div><div class="detail-value">${escapeHtml(tpl.description || '-')}</div>
        <div class="detail-label">总时长</div><div class="detail-value">${tpl.totalDuration ? tpl.totalDuration + '分钟' : '-'}</div>
        <div class="detail-label">消费水平</div><div class="detail-value">${tpl.consumeLevel === 'high' ? '高端' : '经济'}</div>
        <div class="detail-label">排序权重</div><div class="detail-value">${tpl.sortOrder ?? 0}</div>
        <div class="detail-label">状态</div><div class="detail-value">${renderStatusBadge(tpl.status)}</div>
        <div class="detail-label">创建时间</div><div class="detail-value">${formatDateTime(tpl.createdAt)}</div>
      </div>
      ${segHtml}`;
    openModal('tplDetailModal');
  } catch (e) {
    console.error('查看模板详情失败:', e);
    showToast('加载模板详情失败', 'error');
  }
}

// ====== 启用/禁用 ======
function toggleTplStatus(id, newStatus) {
  showConfirm(
    `确定要${newStatus === 1 ? '启用' : '禁用'}该模板吗？`,
    async () => {
      try {
        await apiPut('/templates/' + id + '/status?status=' + newStatus);
        showToast(newStatus === 1 ? '已启用' : '已禁用');
        loadTemplates(tplPage);
      } catch (e) {
        console.error('切换模板状态失败:', e);
      }
    }
  );
}

// ====== 删除 ======
function deleteTemplate(id) {
  showConfirm(
    '删除后不可恢复，确认要删除该模板及其所有时段吗？',
    async () => {
      try {
        await apiDelete(`/templates/${id}`);
        showToast('已删除');
        loadTemplates(tplPage);
      } catch (e) {
        console.error('删除模板失败:', e);
      }
    }
  );
}
