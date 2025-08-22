const statusTableBody = document.querySelector("#statusTable tbody");
const lastUpdatedEl = document.getElementById("lastUpdated");

async function renderStatusTable() {
    const resp = await fetch('/api/ip');
    const ipData = await resp.json();

    statusTableBody.innerHTML = "";

    // Sort so that "Down" items are shown first (optional)
    ipData.sort((a, b) => {
        if (a.status === "Down" && b.status !== "Down") return -1;
        if (a.status !== "Down" && b.status === "Down") return 1;
        return 0;
    });

    ipData.forEach(item => {
        let statusClass = "status-orange"; // default color
        if (item.status === "Online") statusClass = "status-green";
        else if (item.status === "Down") statusClass = "status-red";
        else if (item.status === "Error") statusClass = "status-orange";
        else if (item.status === "Unknown") statusClass = "status-yellow";

        const row = document.createElement("tr");
        // Display location name instead of status text
        // Color the cell based on status
        row.innerHTML = `
            <td class="${statusClass}">${item.location}</td>
        `;
        statusTableBody.appendChild(row);
    });

    lastUpdatedEl.textContent = "Last Updated: " + new Date().toLocaleTimeString();
}

renderStatusTable();
setInterval(renderStatusTable, 30000); // update every 30 seconds
