async function requestJson(url, options) {
  const response = await fetch(url, Object.assign({
    headers: { "Content-Type": "application/json" }
  }, options || {}));
  if (response.status === 401) {
    location.href = "/login";
    return { code: 401, msg: "请先登录" };
  }
  return response.json();
}

function postJson(url, data) {
  return requestJson(url, { method: "POST", body: JSON.stringify(data || {}) });
}

function putJson(url, data) {
  return requestJson(url, { method: "PUT", body: JSON.stringify(data || {}) });
}

function deleteJson(url) {
  return requestJson(url, { method: "DELETE" });
}

function formData(form) {
  const data = {};
  new FormData(form).forEach((value, key) => {
    if (value !== "") {
      if (key.endsWith("Ids")) {
        data[key] = value.split(",").map(v => Number(v.trim())).filter(Boolean);
      } else {
        data[key] = value;
      }
    }
  });
  return data;
}

function fillForm(form, row) {
  form.reset();
  [...form.elements].forEach(el => {
    if (!el.name || row[el.name] === undefined || row[el.name] === null) return;
    if (Array.isArray(row[el.name])) {
      el.value = row[el.name].join(",");
    } else {
      el.value = row[el.name];
    }
  });
}

function resetForm(form) {
  form.reset();
  const id = form.querySelector("[name=id]");
  if (id) id.value = "";
}

function toast(text) {
  const div = document.createElement("div");
  div.className = "layui-toast";
  div.textContent = text || "操作完成";
  document.body.appendChild(div);
  setTimeout(() => div.remove(), 1800);
}

function badge(status) {
  const map = {
    PENDING: ["待审批", "layui-badge-orange"],
    APPROVED: ["已通过", "layui-badge-green"],
    REJECTED: ["未通过", "layui-badge-red"],
    FINISHED: ["已结束", "layui-badge-blue"],
    NORMAL: ["正常", "layui-badge-green"],
    ABNORMAL: ["异常", "layui-badge-red"]
  };
  const item = map[status] || [status || "-", ""];
  return `<span class="layui-badge ${item[1]}">${item[0]}</span>`;
}

function renderTable(table, columns, rows, actions) {
  const head = columns.map(col => `<th>${col.title}</th>`).join("") + "<th>操作</th>";
  const body = (rows || []).map(row => {
    const cells = columns.map(col => {
      const value = col.render ? col.render(row) : (row[col.key] ?? "");
      return `<td>${value}</td>`;
    }).join("");
    return `<tr>${cells}<td><div class="actions">${actions(row)}</div></td></tr>`;
  }).join("");
  table.innerHTML = `<thead><tr>${head}</tr></thead><tbody>${body || `<tr><td colspan="${columns.length + 1}">暂无数据</td></tr>`}</tbody>`;
}

