const pageState = {
  type: document.body.dataset.type,
  admin: document.body.dataset.admin === "true",
  page: 1,
  limit: 8,
  count: 0,
  rows: []
};
const form = document.getElementById("applyForm");

function applyColumns() {
  if (pageState.type === "leave") {
    return [
      { key: "id", title: "编号" },
      { key: "username", title: "姓名" },
      { key: "startDate", title: "开始" },
      { key: "endDate", title: "结束" },
      { key: "dayCount", title: "天数" },
      { key: "reason", title: "事由" },
      { key: "status", title: "状态", render: r => badge(r.status) }
    ];
  }
  if (pageState.type === "trip") {
    return [
      { key: "id", title: "编号" },
      { key: "username", title: "姓名" },
      { key: "destination", title: "目的地" },
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
    { key: "amount", title: "金额" },
    { key: "detail", title: "明细" },
    { key: "status", title: "状态", render: r => badge(r.status) }
  ];
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
  const result = await postJson(`${prefix}/applications/${pageState.type}/page`, {
    page: pageState.page,
    limit: pageState.limit,
    keyword: document.getElementById("keyword").value,
    status: document.getElementById("status").value
  });
  pageState.rows = result.data || [];
  pageState.count = result.count || 0;
  renderTable(document.getElementById("applyTable"), applyColumns(), pageState.rows, actionButtons);
  document.getElementById("pageInfo").textContent = `第 ${pageState.page} 页 / 共 ${pageState.count} 条`;
}

function actionButtons(row) {
  const edit = `<button class="layui-btn layui-btn-xs" onclick='editApply(${JSON.stringify(row)})'>编辑</button>`;
  const del = `<button class="layui-btn layui-btn-danger layui-btn-xs" onclick="deleteApply(${row.id})">删除</button>`;
  if (!pageState.admin) return edit + del;
  const pass = row.status === "PENDING" ? `<button class="layui-btn layui-btn-normal layui-btn-xs" onclick="approveApply(${row.id},true)">通过</button>` : "";
  const reject = row.status === "PENDING" ? `<button class="layui-btn layui-btn-warm layui-btn-xs" onclick="approveApply(${row.id},false)">不通过</button>` : "";
  return pass + reject + edit + del;
}

function editApply(row) {
  fillForm(form, row);
  window.scrollTo({ top: 0, behavior: "smooth" });
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
  const result = await postJson(`/api/admin/applications/${pageState.type}/${id}/approve`, { passed, auditComment });
  toast(result.msg);
  loadData();
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
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
    loadData();
  }
});

document.getElementById("searchBtn").addEventListener("click", () => {
  pageState.page = 1;
  loadData();
});
document.getElementById("resetBtn").addEventListener("click", () => resetForm(form));
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
loadData();

