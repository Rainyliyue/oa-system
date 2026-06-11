let userRows = [];
const userForm = document.getElementById("crudForm");
async function loadUsers() {
  const result = await postJson("/api/admin/users/page", {
    page: 1,
    limit: 100,
    keyword: document.getElementById("keyword").value
  });
  userRows = result.data || [];
  renderTable(document.getElementById("crudTable"), [
    { key: "id", title: "ID" },
    { key: "username", title: "用户名" },
    { key: "realName", title: "姓名" },
    { key: "department", title: "部门" },
    { key: "roleCodes", title: "角色", render: r => (r.roleCodes || []).join(",") },
    { key: "enabled", title: "状态", render: r => r.enabled ? "启用" : "禁用" }
  ], userRows, row => `<button class="layui-btn layui-btn-xs" onclick='editUser(${JSON.stringify(row)})'>编辑</button><button class="layui-btn layui-btn-danger layui-btn-xs" onclick="deleteUser(${row.id})">删除</button>`);
}
function editUser(row) {
  fillForm(userForm, row);
  userForm.username.disabled = true;
}
async function deleteUser(id) {
  if (!confirm("确定删除用户吗？")) return;
  const result = await deleteJson(`/api/admin/users/${id}`);
  toast(result.msg);
  loadUsers();
}
userForm.addEventListener("submit", async event => {
  event.preventDefault();
  const data = formData(userForm);
  if (data.enabled !== undefined) data.enabled = data.enabled === "true";
  const id = data.id;
  delete data.id;
  if (id) {
    delete data.username;
  }
  const result = id ? await putJson(`/api/admin/users/${id}`, data) : await postJson("/api/admin/users", data);
  toast(result.msg);
  if (result.code === 0) {
    resetForm(userForm);
    userForm.username.disabled = false;
    loadUsers();
  }
});
document.getElementById("resetBtn").addEventListener("click", () => { resetForm(userForm); userForm.username.disabled = false; });
document.getElementById("searchBtn").addEventListener("click", loadUsers);
loadUsers();
