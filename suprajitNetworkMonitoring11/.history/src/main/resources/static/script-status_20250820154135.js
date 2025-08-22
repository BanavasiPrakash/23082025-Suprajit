function getCurrentUser() {
  const user = sessionStorage.getItem('currentUser');
  return user ? JSON.parse(user) : null;
}

async function fetchWithAuth(url, options = {}) {
  const currentUser = getCurrentUser();
  if (!currentUser || !currentUser.username || !currentUser.password) {
    alert("Please login first.");
    window.location.href = "auth.html";
    return null;
  }

  const authHeader = 'Basic ' + btoa(currentUser.username + ':' + currentUser.password);

  options.headers = {
    ...options.headers,
    'Authorization': authHeader,
    'Content-Type': 'application/x-www-form-urlencoded'
  };

  const response = await fetch(url, options);

  if (response.status === 401) {
    alert('Unauthorized! Please login again.');
    sessionStorage.removeItem('currentUser');
    window.location.href = 'auth.html';
    return null;
  }

  return response;
}

const statusTableBody = document.querySelector("#statusTable tbody");
const lastUpdatedEl = document.getElementById("lastUpdated");

async function renderStatusTable() {
  const response = await fetchWithAuth('/api/ip');
  if (!response) return;

  const ipData = await response.json();

  statusTableBody.innerHTML = "";

  ipData.sort((a, b) => {
    if (a.status === "Down" && b.status !== "Down") return -1;
    if (a.status !== "Down" && b.status === "Down") return 1;
    return 0;
  });

  ipData.forEach(item => {
    let statusClass = "status-orange";
    if (item.status === "Online") statusClass = "status-green";
    else if (item.status === "Down") statusClass = "status-red";
    else if (item.status === "Error") statusClass = "status-orange";
    else if (item.status === "Unknown") statusClass = "status-yellow";

    const row = document.createElement("tr");
    row.innerHTML = `
      <td class="${statusClass}">${item.location}</td>
    `;
    statusTableBody.appendChild(row);
  });

  lastUpdatedEl.textContent = "Last Updated: " + new Date().toLocaleTimeString();
}

renderStatusTable();
setInterval(renderStatusTable, 30000);
