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
    const resp = await fetchWithAuth('/api/ip');
    const ipData = await resp.json();

    statusTableBody.innerHTML = "";

    ipData.sort((a, b) => {
        if (a.status === "Down" && b.status !== "Down") return -1;
        if (a.status !== "Down" && b.status === "Down") return 1;
        return 0;
    });

    const columns = 5;  // number of columns per row
    for (let i = 0; i < ipData.length; i += columns) {
        const row = document.createElement("tr");
        // For each column in this row
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
                cell.textContent = item.location || "Unknown Location";  // Show location name here
            } else {
                cell.textContent = ""; // empty cell if no data
            }
            row.appendChild(cell);
        }
        statusTableBody.appendChild(row);
    }

    lastUpdatedEl.textContent = "Last Updated: " + new Date().toLocaleTimeString();
}

renderStatusTable();
setInterval(renderStatusTable, 10000); // update every 10 seconds