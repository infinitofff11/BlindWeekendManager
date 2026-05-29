/**
 * Dashboard 首页逻辑
 * 加载各模块统计数据
 */

document.addEventListener('DOMContentLoaded', () => {
  initSidebar('dashboard');

  // 显示管理员昵称
  var adminInfo = getAdminInfo && getAdminInfo();
  if (adminInfo && adminInfo.nickname) {
    var el = document.getElementById('adminName');
    if (el) el.textContent = adminInfo.nickname;
  }

  loadAllStats();
});

async function loadAllStats() {
  try {
    // 并行请求各模块统计接口
    const [userStats, spotStats, templateStats, boxStats] = await Promise.all([
      apiGet('/users/stats').catch(e => (console.error(e), null)),
      apiGet('/spots/stats').catch(e => (console.error(e), null)),
      apiGet('/templates/active').catch(e => (console.error(e), null)),
      apiGet('/blindboxes/stats').catch(e => (console.error(e), null)),
    ]);

    if (userStats) {
      document.getElementById('userTotal').textContent = userStats.total || 0;
      document.getElementById('todayUsers').textContent = userStats.todayNew || 0;
    }
    if (spotStats) {
      document.getElementById('spotTotal').textContent = spotStats.total || 0;
    }
    if (templateStats) {
      document.getElementById('templateTotal').textContent = Array.isArray(templateStats) ? templateStats.length : 0;
    }
    if (boxStats) {
      document.getElementById('boxTotal').textContent = boxStats.total || 0;
      document.getElementById('boxOpen').textContent = boxStats.open || 0;
    }
  } catch (e) {
    console.error('加载统计失败:', e);
    showToast('部分统计数据加载失败', 'warning');
  }
}
