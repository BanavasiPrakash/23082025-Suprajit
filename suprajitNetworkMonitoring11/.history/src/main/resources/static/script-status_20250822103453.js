// Get current logged-in user from sessionStorage
function getCurrentUser() {
  const user = sessionStorage.getItem('currentUser');
  return user ? JSON.parse(user) : null;
}

// Fetch helper without Basic Auth (password not stored)
async function fetchWithAuth(url, options = {}) {
  const currentUser = getCurrentUser();
  if (!currentUser || !currentUser.username) {
    alert("Please login first.");
    window.location.href = "auth.html";
    return null;
  }

  options.headers = {
    ...options.headers,
    'Content-Type': 'application/x-www-form-urlencoded',
  };

  // If you use cookie/session authentication, send credentials
  options.credentials = "include";

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

// Render the IP status table
async function renderStatusTable() {
  const resp = await fetchWithAuth('/api/ip');
  if (!resp) return; // fetchWithAuth already handles redirect on failure

  const ipData = await resp.json();
  statusTableBody.innerHTML = "";

  ipData.sort((a, b) => {
    if (a.status === "Down" && b.status !== "Down") return -1;
    if (a.status !== "Down" && b.status === "Down") return 1;
    return 0;
  });

  const columns = 6;  // number of columns per row
  for (let i = 0; i < ipData.length; i += columns) {
    const row = document.createElement("tr");
    for (let j = 0; j < columns; j++) {
      const cell = document.createElement("td");
      const item = ipData[i + j];
      if (item) {
        let statusClass = "status-orange";
        if (item.status === "Online") statusClass = "status-green";
        else if (item.status === "Down") statusClass = "status-red";
        else if (item.status === "Error") statusClass = "status-orange";
        else if (item.status === "Unknown") statusClass = "status-yellow";

        cell.className = statusClass;
        cell.textContent = item.location || "Unknown Location";
      } else {
        cell.textContent = ""; // empty cell if no data
      }
      row.appendChild(cell);
    }
    statusTableBody.appendChild(row);
  }

  lastUpdatedEl.textContent = "Last Updated: " + new Date().toLocaleTimeString();
}

// Wait for DOM ready and check login session before rendering
document.addEventListener('DOMContentLoaded', () => {
  const currentUser = getCurrentUser();
  if (!currentUser || !currentUser.username) {
    alert("Please login first.");
    window.location.href = "auth.html";
    return;
  }
  renderStatusTable();
  setInterval(renderStatusTable, 10000); // update every 10 seconds
});
