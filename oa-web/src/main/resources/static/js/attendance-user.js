async function loadToday() {
  const result = await requestJson("/api/user/attendance/today");
  const item = result.data;
  document.getElementById("todayInfo").innerHTML = item
    ? `日期：${item.workDate || "-"}，上班：${item.clockInTime || "-"}，下班：${item.clockOutTime || "-"}，状态：${badge(item.status)}`
    : "今天还没有打卡记录";
}
document.getElementById("clockInBtn").addEventListener("click", async () => {
  const result = await postJson("/api/user/attendance/clock-in", {});
  toast(result.msg);
  loadToday();
});
document.getElementById("clockOutBtn").addEventListener("click", async () => {
  const result = await postJson("/api/user/attendance/clock-out", {});
  toast(result.msg);
  loadToday();
});
loadToday();

