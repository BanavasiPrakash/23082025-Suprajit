const ipForm = document.getElementById("ipForm");
const ipTableBody = document.querySelector("#ipTable tbody");

function getCurrentUser() {
  const user = sessionStorage.getItem('currentUser');
  return user ? JSON.parse(user) : null;
}

async function fetchWithAuth(url, options = {}) {
  const currentUser = getCurrentUser();
  if (!currentUser || !currentUser.username || !currentUser.password) {
    alert("You must be logged in! Redirecting to login page.");
    window.location.href = "auth.html";
    return null;
  }

  const authHeader = 'Basic ' + btoa(currentUser.username + ':' + currentUser.password);

  options.headers = {
    ...options.headers,
    'Authorization': authHeader,
    'Content-Type': 'application/x-www-form-urlencoded',
  };

  const response = await fetch(url, options);
  if (response.status === 401) {
    alert("Unauthorized! Please login again.");
    sessionStorage.removeItem('currentUser');
    window.location.href = "auth.html";
    return null;
  }

  return response;
}

// Fetch data from backend and render table
async function renderTable() {
    const response = await fetchWithAuth('/api/ip');
    const ipData = await response.json();
    ipTableBody.innerHTML = "";

    if (ipData.length === 0) {
        const row = document.createElement("tr");
        row.innerHTML = <td colspan="3" style="text-align:center;">No IP addresses stored.</td>;
        ipTableBody.appendChild(row);
    } else {
        ipData.forEach((item) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${item.location}</td>
                <td>${item.ip}</td>
                <td>
                    <button type="button" class="delete-btn" onclick="deleteIp(${item.id})">Delete</button>
                </td>
            `;
            ipTableBody.appendChild(row);
        });
    }
}

async function deleteIp(id) {
    const confirmDelete = confirm("Are you sure you want to delete this IP?");
    if (!confirmDelete) return;
    const response = await fetch(`/api/ip/${id}`, { method: 'DELETE' });
    if (response.ok) {
        renderTable();
    } else {
        alert("Failed to delete IP address.");
    }
}

ipForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const location = document.getElementById("location").value.trim();
    const ip = document.getElementById("ipAddress").value.trim();
    if (!location || !ip) return;
    const formData = new URLSearchParams();
    formData.append("location", location);
    formData.append("ip", ip);
    const response = await fetchWithAuth('/api/ip', {
  method: 'POST',
  body: formData.toString()
});

    if (response.ok) {
        ipForm.reset();
        renderTable();
    } else {
        alert("Failed to add IP address.");
    }
});

renderTable();
setInterval(renderTable, 9000);