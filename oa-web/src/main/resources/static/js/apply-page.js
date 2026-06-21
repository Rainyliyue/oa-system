const pageState = {
  type: document.body.dataset.type,
  admin: document.body.dataset.admin === "true",
  page: 1,
  limit: 8,
  count: 0,
  rows: []
};
const form = document.getElementById("applyForm");
const evidenceInput = form.elements.evidenceImage;
const evidenceFileInput = document.getElementById("evidenceImageFile");
const evidencePreview = document.getElementById("evidencePreview");
const clearEvidenceBtn = document.getElementById("clearEvidenceBtn");
const detailModal = document.getElementById("detailModal");
const detailTitle = document.getElementById("detailTitle");
const detailBody = document.getElementById("detailBody");
const detailCloseBtn = document.getElementById("detailCloseBtn");
let evidenceUploading = false;

function applyColumns() {
  if (pageState.type === "leave") {
    return [
      { key: "id", title: "编号" },
      { key: "username", title: "姓名" },
      { key: "reason", title: "申请内容", className: "table-content", render: r => contentText(r.reason) },
      { key: "evidenceImage", title: "证据", render: r => evidenceCell(r.evidenceImage) },
      { key: "startDate", title: "开始" },
      { key: "endDate", title: "结束" },
      { key: "dayCount", title: "天数" },
      { key: "status", title: "状态", render: r => badge(r.status) }
    ];
  }
  if (pageState.type === "trip") {
    return [
      { key: "id", title: "编号" },
      { key: "username", title: "姓名" },
      { key: "destination", title: "目的地" },
      { key: "reason", title: "申请内容", className: "table-content", render: r => contentText(r.reason) },
      { key: "evidenceImage", title: "证据", render: r => evidenceCell(r.evidenceImage) },
      { key: "startDate", title: "开始" },
      { key: "endDate", title: "结束" },
      { key: "budget", title: "预算" },
      { key: "status", title: "状态", render: r => badge(r.status) }
    ];
  }
  return [
    { key: "id", title: "编号" },
    { key: "username", title: "姓名" },
    { key: "title", title: "标题" },
    { key: "detail", title: "申请内容", className: "table-content", render: r => contentText(r.detail) },
    { key: "evidenceImage", title: "证据", render: r => evidenceCell(r.evidenceImage) },
    { key: "amount", title: "金额" },
    { key: "status", title: "状态", render: r => badge(r.status) }
  ];
}

function contentText(value) {
  return `<div class="table-content-text">${escapeHtml(value || "-")}</div>`;
}

function evidenceCell(value) {
  if (!value) return "-";
  const url = escapeHtml(value);
  return `<a class="evidence-thumb-link" href="${url}" target="_blank" title="查看证据图片"><img class="evidence-thumb" src="${url}" alt="证据图片"></a>`;
}

function configureFields() {
  document.querySelectorAll(".field-leave,.field-trip,.field-reimbursement").forEach(el => {
    const visible = el.classList.contains("field-" + pageState.type);
    el.classList.toggle("hidden", !visible);
    el.querySelectorAll("input,textarea,select").forEach(input => input.disabled = !visible);
  });
  document.querySelectorAll(".admin-field").forEach(el => {
    el.classList.toggle("hidden", !pageState.admin);
    el.querySelectorAll("input,textarea,select").forEach(input => input.disabled = !pageState.admin);
  });
}

async function loadData() {
  const prefix = pageState.admin ? "/api/admin" : "/api/user";
  const params = queryParams();
  if (!params) return;
  const result = await postJson(`${prefix}/applications/${pageState.type}/page`, Object.assign({
    page: pageState.page,
    limit: pageState.limit
  }, params));
  pageState.rows = result.data || [];
  pageState.count = result.count || 0;
  renderTable(document.getElementById("applyTable"), applyColumns(), pageState.rows, actionButtons);
  document.getElementById("pageInfo").textContent = `第 ${pageState.page} 页 / 共 ${pageState.count} 条`;
}

function actionButtons(row) {
  const view = `<button class="layui-btn layui-btn-primary layui-btn-xs" onclick="viewApply(${row.id})">查看</button>`;
  const edit = `<button class="layui-btn layui-btn-xs" onclick="editApplyById(${row.id})">编辑</button>`;
  const del = `<button class="layui-btn layui-btn-danger layui-btn-xs" onclick="deleteApply(${row.id})">删除</button>`;
  if (!pageState.admin) return view + edit + del;
  const pass = row.status === "PENDING" ? `<button class="layui-btn layui-btn-normal layui-btn-xs" onclick="approveApply(${row.id},true)">通过</button>` : "";
  const reject = row.status === "PENDING" ? `<button class="layui-btn layui-btn-warm layui-btn-xs" onclick="approveApply(${row.id},false)">不通过</button>` : "";
  return view + pass + reject + edit + del;
}

function findApplyRow(id) {
  return pageState.rows.find(row => Number(row.id) === Number(id));
}

function editApplyById(id) {
  const row = findApplyRow(id);
  if (!row) {
    toast("记录不存在，请刷新列表");
    return;
  }
  editApply(row);
}

function editApply(row) {
  fillForm(form, row);
  updateEvidencePreview();
  scrollToForm(form);
}

async function viewApply(id) {
  const row = findApplyRow(id);
  if (!row) {
    toast("记录不存在，请刷新列表");
    return;
  }
  detailTitle.textContent = detailTitleText();
  detailBody.innerHTML = detailContent(row);
  detailModal.classList.remove("hidden");
  detailModal.setAttribute("aria-hidden", "false");
  await loadApprovalHistory(id);
}

function detailTitleText() {
  if (pageState.type === "leave") return "请假申请明细";
  if (pageState.type === "trip") return "出差申请明细";
  return "报销申请明细";
}

function detailContent(row) {
  const base = [
    ["编号", row.id],
    ["申请人", row.username],
    ["状态", badge(row.status), true],
    ["审批意见", row.auditComment],
    ["审批时间", formatDatetimeForInput(row.approveTime)],
    ["创建时间", formatDatetimeForInput(row.createTime)],
    ["更新时间", formatDatetimeForInput(row.updateTime)]
  ];
  const typeFields = detailTypeFields(row);
  return `
    <div class="detail-grid">${[...typeFields, ...base].map(detailItem).join("")}</div>
    <div class="detail-section">
      <strong>证据图片</strong>
      ${detailEvidence(row.evidenceImage)}
    </div>
    <div class="detail-section">
      <strong>审批历史</strong>
      <div id="approvalHistoryList" class="approval-history muted">加载中...</div>
    </div>
  `;
}

async function loadApprovalHistory(id) {
  const container = document.getElementById("approvalHistoryList");
  if (!container) return;
  const result = await requestJson(`/api/applications/${pageState.type}/${id}/history`);
  if (result.code !== 0) {
    container.classList.add("muted");
    container.textContent = result.msg || "审批历史加载失败";
    return;
  }
  renderApprovalHistory(container, result.data || []);
}

function renderApprovalHistory(container, rows) {
  if (!rows.length) {
    container.classList.add("muted");
    container.textContent = "暂无审批记录";
    return;
  }
  container.classList.remove("muted");
  container.innerHTML = rows.map(row => `
    <div class="approval-history-item">
      <span class="approval-level">第 ${escapeHtml(row.approvalLevel || "-")} 级</span>
      <strong>${escapeHtml(row.approverName || "审批人")}</strong>
      <span>${approvalResultText(row.result)}</span>
      <p>${escapeHtml(row.auditComment || "无审批意见")}</p>
      <time>${escapeHtml(formatDatetimeForInput(row.createTime) || "-")}</time>
    </div>
  `).join("");
}

function approvalResultText(result) {
  const map = {
    STEP_APPROVED: "本级通过",
    APPROVED: "最终通过",
    REJECTED: "审批不通过"
  };
  return map[result] || result || "-";
}

function detailTypeFields(row) {
  if (pageState.type === "leave") {
    return [
      ["事由", row.reason],
      ["开始日期", row.startDate],
      ["结束日期", row.endDate],
      ["请假天数", row.dayCount]
    ];
  }
  if (pageState.type === "trip") {
    return [
      ["目的地", row.destination],
      ["事由", row.reason],
      ["开始日期", row.startDate],
      ["结束日期", row.endDate],
      ["预算", row.budget]
    ];
  }
  return [
    ["标题", row.title],
    ["金额", row.amount],
    ["明细", row.detail]
  ];
}

function detailItem(item) {
  const [label, value, html] = item;
  const display = value === undefined || value === null || value === "" ? "-" : value;
  return `<div class="detail-item"><span>${escapeHtml(label)}</span><strong>${html ? display : escapeHtml(display)}</strong></div>`;
}

function detailEvidence(value) {
  if (!value) return `<div class="detail-empty">未上传图片</div>`;
  const url = escapeHtml(value);
  return `<a class="detail-image-link" href="${url}" target="_blank"><img src="${url}" alt="证据图片"><span>查看原图</span></a>`;
}

function closeDetailModal() {
  detailModal.classList.add("hidden");
  detailModal.setAttribute("aria-hidden", "true");
}

async function deleteApply(id) {
  if (!confirm("确定删除这条记录吗？")) return;
  const prefix = pageState.admin ? "/api/admin" : "/api/user";
  const result = await deleteJson(`${prefix}/applications/${pageState.type}/${id}`);
  toast(result.msg);
  loadData();
}

async function approveApply(id, passed) {
  const auditComment = prompt("审批意见", passed ? "同意" : "不同意") || "";
  const finalApproval = passed ? confirm("是否最终通过？点击“取消”将记录为本级通过，并继续保持待审批。") : true;
  const result = await postJson(`/api/admin/applications/${pageState.type}/${id}/approve`, { passed, finalApproval, auditComment });
  toast(result.msg);
  loadData();
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (evidenceUploading) {
    toast("图片正在上传，请稍后保存");
    return;
  }
  if (!validateSmartDateInputs(form)) return;
  const data = formData(form);
  const id = data.id;
  delete data.id;
  const prefix = pageState.admin ? "/api/admin" : "/api/user";
  const result = id
    ? await putJson(`${prefix}/applications/${pageState.type}/${id}`, data)
    : await postJson(`${prefix}/applications/${pageState.type}`, data);
  toast(result.msg);
  if (result.code === 0) {
    resetForm(form);
    updateEvidencePreview();
    pageState.page = 1;
    loadData();
  }
});

evidenceFileInput.addEventListener("change", async () => {
  const file = evidenceFileInput.files && evidenceFileInput.files[0];
  if (!file) return;
  if (!file.type || !file.type.startsWith("image/")) {
    toast("请选择图片文件");
    evidenceFileInput.value = "";
    return;
  }
  if (file.size > 5 * 1024 * 1024) {
    toast("图片大小不能超过 5MB");
    evidenceFileInput.value = "";
    return;
  }

  const body = new FormData();
  body.append("file", file);
  evidenceUploading = true;
  evidencePreview.textContent = "图片上传中...";
  try {
    const response = await fetch("/api/files/images", { method: "POST", body });
    if (response.status === 401) {
      location.href = "/login";
      return;
    }
    const result = response.ok
      ? await readResponseBody(response)
      : { code: response.status, msg: responseMessage(response, await readResponseBody(response)) };
    toast(result.msg);
    if (result.code === 0) {
      evidenceInput.value = result.data || "";
      updateEvidencePreview();
    } else {
      evidenceFileInput.value = "";
      updateEvidencePreview();
    }
  } catch (error) {
    toast("图片上传失败");
    evidenceFileInput.value = "";
    updateEvidencePreview();
  } finally {
    evidenceUploading = false;
  }
});

clearEvidenceBtn.addEventListener("click", () => {
  evidenceInput.value = "";
  evidenceFileInput.value = "";
  updateEvidencePreview();
});

detailCloseBtn.addEventListener("click", closeDetailModal);
detailModal.addEventListener("click", event => {
  if (event.target === detailModal) {
    closeDetailModal();
  }
});
document.addEventListener("keydown", event => {
  if (event.key === "Escape" && !detailModal.classList.contains("hidden")) {
    closeDetailModal();
  }
});

function updateEvidencePreview() {
  const value = evidenceInput.value;
  if (!value) {
    evidencePreview.classList.add("muted");
    evidencePreview.innerHTML = "未上传图片";
    return;
  }
  const url = escapeHtml(value);
  evidencePreview.classList.remove("muted");
  evidencePreview.innerHTML = `<a href="${url}" target="_blank"><img class="evidence-preview-img" src="${url}" alt="证据图片预览"><span>查看原图</span></a>`;
}

function updateLeaveDayCount() {
  if (pageState.type !== "leave") return;
  const startInput = form.elements.startDate;
  const endInput = form.elements.endDate;
  const dayInput = form.elements.dayCount;
  if (!startInput || !endInput || !dayInput) return;

  const startValue = formatDateForInput(startInput.value);
  const endValue = formatDateForInput(endInput.value);
  if (!validDateText(startValue) || !validDateText(endValue)) return;

  const start = parseDateValue(startValue);
  const end = parseDateValue(endValue);
  const days = Math.floor((end - start) / 86400000) + 1;
  if (days > 0) {
    dayInput.value = days;
  }
}

function parseDateValue(value) {
  const parts = value.split("-").map(Number);
  return Date.UTC(parts[0], parts[1] - 1, parts[2]);
}

function setupLeaveDayCount() {
  if (pageState.type !== "leave") return;
  const startInput = form.elements.startDate;
  const endInput = form.elements.endDate;
  if (!startInput || !endInput) return;
  ["input", "change", "blur"].forEach(eventName => {
    startInput.addEventListener(eventName, updateLeaveDayCount);
    endInput.addEventListener(eventName, updateLeaveDayCount);
  });
}

document.getElementById("searchBtn").addEventListener("click", () => {
  pageState.page = 1;
  loadData();
});
setupQueryControls(() => {
  pageState.page = 1;
  loadData();
});
document.getElementById("resetBtn").addEventListener("click", () => {
  resetForm(form);
  evidenceFileInput.value = "";
  updateEvidencePreview();
});
document.getElementById("prevBtn").addEventListener("click", () => {
  if (pageState.page > 1) {
    pageState.page--;
    loadData();
  }
});
document.getElementById("nextBtn").addEventListener("click", () => {
  if (pageState.page * pageState.limit < pageState.count) {
    pageState.page++;
    loadData();
  }
});

configureFields();
setupLeaveDayCount();
updateEvidencePreview();
loadData();
