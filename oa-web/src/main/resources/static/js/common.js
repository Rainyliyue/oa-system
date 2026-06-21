async function requestJson(url, options) {
  let response;
  try {
    response = await fetch(url, Object.assign({
      headers: { "Content-Type": "application/json" }
    }, options || {}));
  } catch (error) {
    return { code: 1, msg: "网络请求失败，请检查服务是否已启动", data: null };
  }
  if (response.status === 401) {
    location.href = "/login";
    return { code: 401, msg: "请先登录" };
  }
  const body = await readResponseBody(response);
  if (!response.ok) {
    return { code: response.status, msg: responseMessage(response, body), data: null };
  }
  return body || { code: 0, msg: "操作成功" };
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

async function readResponseBody(response) {
  const contentType = response.headers.get("content-type") || "";
  if (contentType.includes("application/json")) {
    try {
      return await response.json();
    } catch (error) {
      return null;
    }
  }
  try {
    return await response.text();
  } catch (error) {
    return null;
  }
}

function responseMessage(response, body) {
  if (body && typeof body === "object") {
    return body.msg || body.message || body.error || `请求失败：${response.status}`;
  }
  if (typeof body === "string" && body.trim()) {
    return body.length > 80 ? `请求失败：${response.status}` : body;
  }
  return `请求失败：${response.status}`;
}

function queryParams(root) {
  const container = root || document;
  const data = {};
  for (const input of container.querySelectorAll("[data-query]")) {
    if (input.disabled) continue;
    let value = input.value;
    if (value === undefined || value === null || String(value).trim() === "") continue;
    value = String(value).trim();
    if (input.dataset.dateInput !== undefined) {
      value = formatDateForInput(value);
      input.value = value;
      syncNativePicker(input);
      if (!validDateText(value)) {
        toast("查询日期格式应为 yyyy-MM-dd");
        input.focus();
        return null;
      }
    }
    if (input.dataset.queryNumber !== undefined) {
      const numberValue = Number(value);
      if (!Number.isNaN(numberValue)) {
        data[input.dataset.query] = numberValue;
      }
      continue;
    }
    if (input.dataset.queryBoolean !== undefined) {
      data[input.dataset.query] = value === "true";
      continue;
    }
    data[input.dataset.query] = value;
  }
  return data;
}

function setupQueryControls(loadFn) {
  document.querySelectorAll("[data-query]").forEach(input => {
    input.addEventListener("keydown", event => {
      if (event.key === "Enter") {
        event.preventDefault();
        loadFn();
      }
    });
  });
  const resetQueryBtn = document.getElementById("resetQueryBtn");
  if (resetQueryBtn) {
    resetQueryBtn.addEventListener("click", event => {
      event.preventDefault();
      clearQueryControls();
      loadFn();
    });
  }
}

function clearQueryControls(root) {
  const container = root || document;
  container.querySelectorAll("[data-query]").forEach(input => {
    input.value = "";
    if (input.dataset.dateInput !== undefined || input.dataset.datetimeInput !== undefined) {
      syncNativePicker(input);
    }
    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.dispatchEvent(new Event("change", { bubbles: true }));
  });
}

function scrollToForm(form) {
  const target = form.closest(".layui-card") || form;
  target.scrollIntoView({ behavior: "smooth", block: "start" });
}

function formData(form) {
  const data = {};
  new FormData(form).forEach((value, key) => {
    const field = form.elements[key];
    const input = field && field.length !== undefined && !field.dataset ? field[0] : field;
    const includeEmpty = input && input.dataset && input.dataset.includeEmpty !== undefined;
    if (value === "" && !includeEmpty) {
      return;
    }
    if (input && input.dataset && input.dataset.datetimeInput !== undefined) {
      value = value.trim().replace(" ", "T");
    }
    if (key.endsWith("Ids")) {
      data[key] = value.split(",").map(v => Number(v.trim())).filter(Boolean);
    } else {
      data[key] = value;
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
    } else if (el.dataset && el.dataset.datetimeInput !== undefined) {
      el.value = formatDatetimeForInput(row[el.name]);
      syncNativePicker(el);
    } else if (el.dataset && el.dataset.dateInput !== undefined) {
      el.value = formatDateForInput(row[el.name]);
      syncNativePicker(el);
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

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#39;");
}

function renderTable(table, columns, rows, actions) {
  const head = columns.map(col => `<th>${col.title}</th>`).join("") + "<th>操作</th>";
  const body = (rows || []).map(row => {
    const cells = columns.map(col => {
      const value = col.render ? col.render(row) : escapeHtml(row[col.key] ?? "");
      const className = col.className ? ` class="${col.className}"` : "";
      return `<td${className}>${value}</td>`;
    }).join("");
    return `<tr>${cells}<td><div class="actions">${actions(row)}</div></td></tr>`;
  }).join("");
  table.innerHTML = `<thead><tr>${head}</tr></thead><tbody>${body || `<tr><td colspan="${columns.length + 1}">暂无数据</td></tr>`}</tbody>`;
}

function ensurePager(table, id) {
  let pager = document.getElementById(id);
  if (pager) return pager;
  pager = document.createElement("div");
  pager.id = id;
  pager.className = "layui-page";
  const body = table.closest(".layui-card-body") || table.parentNode;
  body.appendChild(pager);
  return pager;
}

function renderPager(pager, state, loadFn) {
  const totalPages = Math.max(1, Math.ceil((state.count || 0) / state.limit));
  pager.innerHTML = `
    <button class="layui-btn layui-btn-primary" type="button" data-page-action="prev" ${state.page <= 1 ? "disabled" : ""}>上一页</button>
    <span>第 ${state.page} 页 / 共 ${totalPages} 页，共 ${state.count || 0} 条</span>
    <button class="layui-btn layui-btn-primary" type="button" data-page-action="next" ${state.page >= totalPages ? "disabled" : ""}>下一页</button>
  `;
  pager.querySelector("[data-page-action='prev']").addEventListener("click", () => {
    if (state.page > 1) {
      state.page--;
      loadFn();
    }
  });
  pager.querySelector("[data-page-action='next']").addEventListener("click", () => {
    if (state.page < totalPages) {
      state.page++;
      loadFn();
    }
  });
}

function ensureExportButton(handler) {
  const toolbar = document.querySelector(".toolbar");
  if (!toolbar || document.getElementById("exportPageBtn")) return;
  const button = document.createElement("button");
  button.id = "exportPageBtn";
  button.className = "layui-btn layui-btn-primary";
  button.type = "button";
  button.textContent = "导出当前页";
  button.addEventListener("click", handler);
  toolbar.appendChild(button);
}

function exportRowsToCsv(filename, columns, rows) {
  const csvRows = [];
  csvRows.push(columns.map(col => csvValue(col.title)).join(","));
  (rows || []).forEach(row => {
    csvRows.push(columns.map(col => csvValue(row[col.key] ?? "")).join(","));
  });
  const blob = new Blob(["\uFEFF" + csvRows.join("\r\n")], { type: "text/csv;charset=utf-8" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(link.href);
}

function csvValue(value) {
  return `"${String(value ?? "").replaceAll("\"", "\"\"")}"`;
}

function formatDateForInput(value) {
  if (!value) return "";
  const digits = String(value).replace(/\D/g, "").slice(0, 8);
  return formatDateDigits(digits);
}

function formatDatetimeForInput(value) {
  if (!value) return "";
  const text = String(value).replace("T", " ");
  const digits = text.replace(/\D/g, "").slice(0, 12);
  return formatDatetimeDigits(digits);
}

function formatDateDigits(digits) {
  if (digits.length <= 4) return digits;
  if (digits.length <= 6) return `${digits.slice(0, 4)}-${digits.slice(4)}`;
  return `${digits.slice(0, 4)}-${digits.slice(4, 6)}-${digits.slice(6, 8)}`;
}

function formatDatetimeDigits(digits) {
  const date = formatDateDigits(digits.slice(0, 8));
  if (digits.length <= 8) return date;
  if (digits.length <= 10) return `${date} ${digits.slice(8)}`;
  return `${date} ${digits.slice(8, 10)}:${digits.slice(10, 12)}`;
}

function validDateText(value) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value || "");
  if (!match) return false;
  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.getUTCFullYear() === year && date.getUTCMonth() === month - 1 && date.getUTCDate() === day;
}

function validDatetimeText(value) {
  const match = /^(\d{4}-\d{2}-\d{2})[ T](\d{2}):(\d{2})$/.exec(value || "");
  if (!match || !validDateText(match[1])) return false;
  const hour = Number(match[2]);
  const minute = Number(match[3]);
  return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
}

function validateSmartDateInputs(form) {
  const dateInputs = [...form.querySelectorAll("[data-date-input]")];
  const datetimeInputs = [...form.querySelectorAll("[data-datetime-input]")];
  for (const input of dateInputs) {
    if (input.disabled || input.value.trim() === "") continue;
    input.value = formatDateForInput(input.value);
    if (!validDateText(input.value)) {
      toast("日期格式应为 yyyy-MM-dd，且月份和日期有效");
      input.focus();
      return false;
    }
  }
  for (const input of datetimeInputs) {
    if (input.disabled || input.value.trim() === "") continue;
    input.value = formatDatetimeForInput(input.value);
    if (!validDatetimeText(input.value)) {
      toast("时间格式应为 yyyy-MM-dd HH:mm，且日期时间有效");
      input.focus();
      return false;
    }
  }
  return true;
}

function setupSmartDateInputs() {
  document.querySelectorAll("[data-date-input]").forEach(input => {
    enhanceDateInput(input, "date");
    input.addEventListener("input", () => {
      input.value = formatDateForInput(input.value);
      syncNativePicker(input);
    });
    input.addEventListener("blur", () => {
      input.value = formatDateForInput(input.value);
      syncNativePicker(input);
    });
  });
  document.querySelectorAll("[data-datetime-input]").forEach(input => {
    enhanceDateInput(input, "datetime-local");
    input.addEventListener("input", () => {
      input.value = formatDatetimeForInput(input.value);
      syncNativePicker(input);
    });
    input.addEventListener("blur", () => {
      input.value = formatDatetimeForInput(input.value);
      syncNativePicker(input);
    });
  });
}

function enhanceDateInput(input, pickerType) {
  if (input.closest(".smart-date-field")) return;
  const wrapper = document.createElement("div");
  wrapper.className = "smart-date-field";
  input.parentNode.insertBefore(wrapper, input);
  wrapper.appendChild(input);
  input.classList.add("smart-date-text");

  const button = document.createElement("button");
  button.type = "button";
  button.className = "layui-btn layui-btn-primary date-picker-btn";
  button.textContent = "选择";
  button.title = pickerType === "date" ? "选择日期" : "选择时间";
  wrapper.appendChild(button);

  const picker = document.createElement("input");
  picker.type = pickerType;
  picker.className = "native-date-picker";
  picker.tabIndex = -1;
  picker.setAttribute("aria-hidden", "true");
  wrapper.appendChild(picker);

  button.addEventListener("click", () => {
    if (input.disabled) return;
    syncNativePicker(input);
    if (picker.showPicker) {
      picker.showPicker();
    } else {
      picker.focus();
      picker.click();
    }
  });
  picker.addEventListener("change", () => {
    input.value = pickerType === "date" ? formatDateForInput(picker.value) : formatDatetimeForInput(picker.value);
    input.dispatchEvent(new Event("input", { bubbles: true }));
  });
  syncNativePicker(input);
}

function syncNativePicker(input) {
  const wrapper = input.closest(".smart-date-field");
  if (!wrapper) return;
  const picker = wrapper.querySelector(".native-date-picker");
  if (!picker) return;
  if (input.dataset.datetimeInput !== undefined) {
    picker.value = validDatetimeText(input.value) ? input.value.replace(" ", "T") : "";
    return;
  }
  picker.value = validDateText(input.value) ? input.value : "";
}

setupSmartDateInputs();
