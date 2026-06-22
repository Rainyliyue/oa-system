const homeTodayInfo = document.getElementById("homeTodayInfo");
const homeClockInBtn = document.getElementById("homeClockInBtn");
const homeClockOutBtn = document.getElementById("homeClockOutBtn");
const noticeList = document.getElementById("noticeList");
const markAllNoticeBtn = document.getElementById("markAllNoticeBtn");
let homeTodayRecord = null;

async function loadHomeToday() {
  const result = await requestJson("/api/user/attendance/today");
  homeTodayRecord = result.data || null;
  renderHomeClockState();
}

function renderHomeClockState() {
  if (!homeTodayRecord) {
    homeTodayInfo.textContent = "今天还没有打卡记录，可以点击上班打卡按钮完成打卡。";
    updateHomeClockButtons(false, false);
    return;
  }

  const clockIn = formatDatetimeForInput(homeTodayRecord.clockInTime) || "-";
  const clockOut = formatDatetimeForInput(homeTodayRecord.clockOutTime) || "-";
  homeTodayInfo.innerHTML = `日期：${escapeHtml(homeTodayRecord.workDate || "-")}，上班：${escapeHtml(clockIn)}，下班：${escapeHtml(clockOut)}，状态：${badge(homeTodayRecord.status)}`;
  updateHomeClockButtons(Boolean(homeTodayRecord.clockInTime), Boolean(homeTodayRecord.clockOutTime));
}

function updateHomeClockButtons(hasClockIn, hasClockOut) {
  homeClockInBtn.disabled = hasClockIn;
  homeClockOutBtn.disabled = !hasClockIn || hasClockOut;
}

async function punchAttendance(type) {
  const url = type === "out" ? "/api/user/attendance/clock-out" : "/api/user/attendance/clock-in";
  const result = await postJson(url, {});
  toast(result.msg);
  await loadHomeToday();
  await loadDashboardStats();
}

async function loadDashboardStats() {
  const result = await requestJson("/api/dashboard/stats");
  if (result.code !== 0) return;
  const data = result.data || {};
  setStatValue("pendingApprovalCount", data.pendingApprovalCount);
  setStatValue("monthlyAttendanceCount", data.monthlyAttendanceCount);
  setStatValue("monthlyApplicationCount", data.monthlyApplicationCount);
  setStatValue("unreadNoticeCount", data.unreadNoticeCount);
}

function setStatValue(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value || 0;
}

async function loadNotices() {
  try {
    const result = await postJson("/api/notices/page", { page: 1, limit: 6 });
    if (result.code !== 0) {
      renderNoticeEmpty(result.msg || "消息加载失败");
      return;
    }
    renderNotices(result.data || []);
  } catch (error) {
    renderNoticeEmpty("消息加载失败，请稍后刷新");
  }
}

function renderNotices(rows) {
  if (!rows.length) {
    renderNoticeEmpty("暂无消息提醒");
    return;
  }
  noticeList.classList.remove("muted");
  noticeList.innerHTML = rows.map(item => `
    <div class="notice-item ${item.readFlag ? "is-read" : ""}">
      <span class="notice-dot">${item.targetUrl ? "待" : "信"}</span>
      <div class="notice-main">
        <div class="notice-title-row">
          <strong>${escapeHtml(item.title || "系统通知")}</strong>
          <span class="notice-tag">${item.targetUrl ? "待处理" : (item.readFlag ? "已读" : "未读")}</span>
        </div>
        <p>${escapeHtml(item.content || "")}</p>
        <time>${escapeHtml(formatDatetimeForInput(item.createTime) || "-")}</time>
      </div>
      ${noticeAction(item)}
    </div>
  `).join("");
}

function renderNoticeEmpty(text) {
  noticeList.classList.add("muted");
  noticeList.innerHTML = `<div class="notice-empty">${escapeHtml(text)}</div>`;
}

function noticeAction(item) {
  if (item.targetUrl) {
    return `<a class="layui-btn layui-btn-xs" href="${escapeHtml(item.targetUrl)}">去处理</a>`;
  }
  if (item.readFlag) {
    return `<span class="notice-state">已读</span>`;
  }
  if (item.id) {
    return `<button class="layui-btn layui-btn-primary layui-btn-xs" type="button" data-notice-read="${item.id}">标记已读</button>`;
  }
  return "";
}

async function markNoticeRead(id) {
  const result = await postJson(`/api/notices/${id}/read`, {});
  toast(result.msg);
  await Promise.all([loadNotices(), loadDashboardStats()]);
}

async function markAllNoticesRead() {
  const result = await postJson("/api/notices/read-all", {});
  toast(result.msg);
  await Promise.all([loadNotices(), loadDashboardStats()]);
}

homeClockInBtn.addEventListener("click", () => punchAttendance("in"));
homeClockOutBtn.addEventListener("click", () => punchAttendance("out"));
noticeList.addEventListener("click", event => {
  const button = event.target.closest("[data-notice-read]");
  if (button) {
    markNoticeRead(button.dataset.noticeRead);
  }
});
markAllNoticeBtn.addEventListener("click", markAllNoticesRead);
loadHomeToday();
loadDashboardStats();
loadNotices();
