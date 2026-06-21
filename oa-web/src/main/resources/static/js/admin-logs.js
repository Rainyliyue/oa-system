const logState = {
  page: 1,
  limit: 10,
  count: 0,
  rows: []
};
const logColumns = [
  { key: "id", title: "ID" },
  { key: "operatorName", title: "操作人" },
  { key: "moduleName", title: "模块" },
  { key: "operationType", title: "操作类型", render: r => operationText(r.operationType) },
  { key: "targetType", title: "对象" },
  { key: "targetId", title: "对象ID" },
  { key: "content", title: "内容", className: "table-content", render: r => contentText(r.content) },
  { key: "createTime", title: "时间", render: r => formatDatetimeForInput(r.createTime) }
];

async function loadLogs() {
  const params = queryParams();
  if (!params) return;
  const result = await postJson("/api/admin/operation-logs/page", Object.assign({
    page: logState.page,
    limit: logState.limit
  }, params));
  logState.rows = result.data || [];
  logState.count = result.count || 0;
  const table = document.getElementById("logTable");
  renderTable(table, logColumns, logState.rows, () => "-");
  renderPager(ensurePager(table, "logPager"), logState, loadLogs);
}

function searchLogs() {
  logState.page = 1;
  loadLogs();
}

function operationText(type) {
  const map = {
    CREATE: "新增",
    UPDATE: "修改",
    DELETE: "删除",
    APPROVE: "审批",
    ASSIGN_PERMISSION: "分配权限"
  };
  return map[type] || type || "-";
}

function contentText(value) {
  return `<div class="table-content-text">${escapeHtml(value || "-")}</div>`;
}

document.getElementById("searchBtn").addEventListener("click", searchLogs);
setupQueryControls(searchLogs);
ensureExportButton(() => exportRowsToCsv("operation-logs.csv", logColumns, logState.rows));
loadLogs();
