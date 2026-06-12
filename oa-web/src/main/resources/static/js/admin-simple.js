const adminPage = document.body.dataset.adminPage;
const crudForm = document.getElementById("crudForm");

const configs = {
  roles: {
    base: "/api/admin/roles",
    columns: [
      { key: "id", title: "ID" },
      { key: "roleCode", title: "角色编码" },
      { key: "roleName", title: "角色名称" },
      { key: "permissionIds", title: "权限ID", render: r => (r.permissionIds || []).join(",") },
      { key: "remark", title: "备注" }
    ]
  },
  permissions: {
    base: "/api/admin/permissions",
    columns: [
      { key: "id", title: "ID" },
      { key: "permissionCode", title: "权限编码" },
      { key: "permissionName", title: "权限名称" },
      { key: "path", title: "路径" },
      { key: "type", title: "类型" }
    ]
  },
  attendance: {
    base: "/api/admin/attendance",
    noAdd: true,
    columns: [
      { key: "id", title: "ID" },
      { key: "username", title: "姓名" },
      { key: "workDate", title: "日期" },
      { key: "clockInTime", title: "上班", render: r => formatDatetimeForInput(r.clockInTime) },
      { key: "clockOutTime", title: "下班", render: r => formatDatetimeForInput(r.clockOutTime) },
      { key: "status", title: "状态", render: r => badge(r.status) },
      { key: "remark", title: "备注" }
    ]
  },
  salary: {
    base: "/api/admin/salary",
    columns: [
      { key: "id", title: "ID" },
      { key: "userId", title: "用户ID" },
      { key: "username", title: "姓名" },
      { key: "salaryMonth", title: "月份" },
      { key: "baseSalary", title: "基本工资" },
      { key: "bonus", title: "奖金" },
      { key: "deduction", title: "扣款" },
      { key: "totalSalary", title: "实发工资" }
    ]
  }
};

const config = configs[adminPage];

async function loadCrud() {
  const result = await postJson(`${config.base}/page`, {
    page: 1,
    limit: 100,
    keyword: document.getElementById("keyword").value
  });
  renderTable(document.getElementById("crudTable"), config.columns, result.data || [], row =>
    `<button class="layui-btn layui-btn-xs" onclick='editCrud(${JSON.stringify(row)})'>编辑</button>` +
    `<button class="layui-btn layui-btn-danger layui-btn-xs" onclick="deleteCrud(${row.id})">删除</button>`
  );
}

function editCrud(row) {
  fillForm(crudForm, row);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function deleteCrud(id) {
  if (!confirm("确定删除这条记录吗？")) return;
  const result = await deleteJson(`${config.base}/${id}`);
  toast(result.msg);
  loadCrud();
}

crudForm.addEventListener("submit", async event => {
  event.preventDefault();
  if (!validateSmartDateInputs(crudForm)) return;
  const data = formData(crudForm);
  const id = data.id;
  delete data.id;
  if (config.noAdd && !id) {
    toast("考勤管理不能新增，请先选择一条记录修改");
    return;
  }
  const result = id ? await putJson(`${config.base}/${id}`, data) : await postJson(config.base, data);
  toast(result.msg);
  if (result.code === 0) {
    resetForm(crudForm);
    loadCrud();
  }
});

document.getElementById("resetBtn").addEventListener("click", () => resetForm(crudForm));
document.getElementById("searchBtn").addEventListener("click", loadCrud);
loadCrud();
