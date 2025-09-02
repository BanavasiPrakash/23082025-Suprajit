// --- ADMIN CHECK ---
const currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
if (!currentUser || currentUser.role !== "ADMIN") {
    alert("You are not authorized to access this page.");
    window.location.href = "status.html";
}

// --- FORM AND TABLE ---
const ipForm = document.getElementById("ipForm");
const ipTableBody = document.querySelector("#ipTable tbody");

// Helper fetch wrapper with auth and error handling
async function fetchWithAuth(url, options = {}) {
    const currentUser = JSON.parse(sessionStorage.getItem('currentUser'));
    if (!currentUser || !currentUser.username) {
        alert("You must be logged in! Redirecting to login page.");
        window.location.href = "auth.html";
        return null;
    }
    options.headers = {
        ...options.headers,
        'Content-Type': 'application/x-www-form-urlencoded',
    };
    options.credentials = "include";
    const response = await fetch(url, options);
    if (response.status === 401 || response.status === 403) {
        alert("Unauthorized! Please login again.");
        sessionStorage.removeItem('currentUser');
        window.location.href = "auth.html";
        return null;
    }
    return response;
}

// --- TABLE RENDERING ---
async function renderTable() {
    const response = await fetchWithAuth('/api/ip');
    if (!response) return;
    let ipData = [];
    try {
        ipData = await response.json();
    } catch (error) {
        console.error("Failed to parse IP data:", error);
        ipTableBody.innerHTML = `<tr><td colspan="3" style="text-align:center;">Failed to load data</td></tr>`;
        return;
    }

    ipTableBody.innerHTML = "";
    if (ipData.length === 0) {
        const row = document.createElement("tr");
        row.innerHTML = `<td colspan="3" style="text-align:center;">No IP addresses stored.</td>`;
        ipTableBody.appendChild(row);
    } else {
        ipData.forEach((item) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>
                    <span class="location-text">${item.location}</span>
                    <input class="edit-location" type="text" value="${item.location}" style="display:none; width: 90%;" />
                </td>
                <td>
                    <span class="ip-text">${item.ip}</span>
                    <input class="edit-ip" type="text" value="${item.ip}" style="display:none; width: 90%;" />
                </td>
                <td>
                    <button type="button" class="edit-btn">Edit</button>
                    <button type="button" class="save-btn" style="display:none;">Save</button>
                    <button type="button" class="cancel-btn" style="display:none;">Cancel</button>
                    <button type="button" class="delete-btn">Delete</button>
                </td>
            `;
            ipTableBody.appendChild(row);

            const editBtn = row.querySelector('.edit-btn');
            const saveBtn = row.querySelector('.save-btn');
            const cancelBtn = row.querySelector('.cancel-btn');
            const deleteBtn = row.querySelector('.delete-btn');
            const locSpan = row.querySelector('.location-text');
            const ipSpan = row.querySelector('.ip-text');
            const locInput = row.querySelector('.edit-location');
            const ipInput = row.querySelector('.edit-ip');

            // Edit button
            editBtn.onclick = () => {
                locSpan.style.display = 'none';
                ipSpan.style.display = 'none';
                locInput.style.display = 'inline-block';
                ipInput.style.display = 'inline-block';
                editBtn.style.display = 'none';
                saveBtn.style.display = 'inline-block';
                cancelBtn.style.display = 'inline-block';
                deleteBtn.style.display = 'none';
            };

            // Cancel button
            cancelBtn.onclick = () => {
                locInput.value = item.location;
                ipInput.value = item.ip;
                locSpan.style.display = '';
                ipSpan.style.display = '';
                locInput.style.display = 'none';
                ipInput.style.display = 'none';
                editBtn.style.display = 'inline-block';
                saveBtn.style.display = 'none';
                cancelBtn.style.display = 'none';
                deleteBtn.style.display = 'inline-block';
            };

            // Save button
            saveBtn.onclick = async () => {
                const newLoc = locInput.value.trim();
                const newIp = ipInput.value.trim();
                if (!newLoc || !newIp) {
                    alert("Both Location and IP Address are required.");
                    return;
                }
                const formData = new URLSearchParams();
                formData.append("location", newLoc);
                formData.append("ip", newIp);
                formData.append("username", currentUser.username);
                const updateResponse = await fetchWithAuth(`/api/ip/${item.id}`, {
                    method: 'PUT',
                    body: formData.toString(),
                });
                if (updateResponse && updateResponse.ok) {
                    renderTable();
                } else {
                    alert("Failed to update IP address.");
                }
            };

            // Delete button
            deleteBtn.onclick = async () => {
                const confirmDelete = confirm("Are you sure you want to delete this IP?");
                if (!confirmDelete) return;
                const deleteResponse = await fetchWithAuth(`/api/ip/${item.id}?username=${encodeURIComponent(currentUser.username)}`, {
                    method: 'DELETE',
                });
                if (deleteResponse && deleteResponse.ok) {
                    renderTable();
                } else {
                    alert("Failed to delete IP address.");
                }
            };
        });
    }
}

// IP Form Submission
ipForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const location = document.getElementById("location").value.trim();
    const ip = document.getElementById("ipAddress").value.trim();

    if (!location || !ip) return;

    const ipRegex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}$/;
    if (!ipRegex.test(ip)) {
        alert("Please enter a valid IPv4 address.");
        return;
    }

    const formData = new URLSearchParams();
    formData.append("location", location);
    formData.append("ip", ip);
    formData.append("username", currentUser.username);

    const response = await fetchWithAuth('/api/ip', {
        method: 'POST',
        body: formData.toString()
    });

    if (response) {
        if (response.status === 409) {
            const message = await response.text();
            alert(message);
        } else if (response.ok) {
            ipForm.reset();
            renderTable();
        } else {
            alert("Failed to add IP address.");
        }
    } else {
        alert("Failed to add IP address.");
    }
});

// --- NEW ADMIN UI AND ACTIONS ---
const newAdminBtn = document.getElementById('newAdminBtn');
const adminActionContainer = document.getElementById('adminActionContainer');
const adminIdentifierInput = document.getElementById('adminIdentifier');
const addAdminBtn = document.getElementById('addAdminBtn');
const removeAdminBtn = document.getElementById('removeAdminBtn');

// Toggle the admin input container on NEW ADMIN button click
newAdminBtn.addEventListener('click', () => {
    if (adminActionContainer.style.display === 'none' || adminActionContainer.style.display === '') {
        adminActionContainer.style.display = 'block';
        adminIdentifierInput.focus();
    } else {
        adminActionContainer.style.display = 'none';
    }
});

// Helper function to send promote/demote requests
async function sendAdminRequest(url, identifier) {
    if (!identifier) {
        alert('Please enter a username or email.');
        return;
    }
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({identifier: identifier.trim()})
        });
        const result = await response.json();
        if (response.ok && result.success) {
            alert(`Success: ${identifier}`);
            adminActionContainer.style.display = 'none';
            adminIdentifierInput.value = '';
        } else {
            alert(result.message || 'Operation failed.');
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

// Add Admin button click
addAdminBtn.addEventListener('click', () => {
    const identifier = adminIdentifierInput.value;
    sendAdminRequest('http://localhost:8080/api/auth/promote-admin', identifier);
});

// Remove Admin button click
removeAdminBtn.addEventListener('click', () => {
    const identifier = adminIdentifierInput.value;
    sendAdminRequest('http://localhost:8080/api/auth/demote-admin', identifier);
});

// Initial table render and periodic refresh
renderTable();
setInterval(renderTable, 9000);
