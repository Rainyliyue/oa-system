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
  const result = await postJson("/api/notices/page", { page: 1, limit: 5 });
  if (result.code !== 0) {
    noticeList.textContent = result.msg || "消息加载失败";
    return;
  }
  renderNotices(result.data || []);
}

function renderNotices(rows) {
  if (!rows.length) {
    noticeList.classList.add("muted");
    noticeList.textContent = "暂无消息提醒";
    return;
  }
  noticeList.classList.remove("muted");
  noticeList.innerHTML = rows.map(item => `
    <div class="notice-item ${item.readFlag ? "is-read" : ""}">
      <div>
        <strong>${escapeHtml(item.title || "系统通知")}</strong>
        <p>${escapeHtml(item.content || "")}</p>
        <time>${escapeHtml(formatDatetimeForInput(item.createTime) || "-")}</time>
      </div>
      ${item.readFlag ? `<span class="notice-state">已读</span>` : `<button class="layui-btn layui-btn-xs" type="button" data-notice-read="${item.id}">已读</button>`}
    </div>
  `).join("");
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
