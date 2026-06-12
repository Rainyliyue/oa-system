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
      const field = form.elements[key];
      const input = field && field.length !== undefined && !field.dataset ? field[0] : field;
      if (input && input.dataset && input.dataset.datetimeInput !== undefined) {
        value = value.trim().replace(" ", "T");
      }
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
