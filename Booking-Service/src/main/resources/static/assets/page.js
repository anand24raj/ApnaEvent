document.addEventListener("DOMContentLoaded", () => {
    if (localStorage.getItem("role") === "CUSTOMER") document.querySelectorAll("a[href*=\"form.html\"]").forEach(a=>a.remove());

    const base = "/booking";

    const count = document.getElementById("recordCount");
    const rows = document.getElementById("rows");
    const search = document.getElementById("search");

    const form = document.getElementById("recordForm");
    const customerSelect = document.getElementById("customerId");
    const eventSelect = document.getElementById("eventId");
    const seatsInput = document.getElementById("numberOfSeats");
    const paymentMode = document.getElementById("paymentMode");
    const saveButton = document.getElementById("saveButton");
    const formMessage = document.getElementById("formMessage");
    const seatInfo = document.getElementById("seatInfo");

    let records = [];
    let events = [];

    async function loadBookingOptions() {

        if (!customerSelect || !eventSelect) {
            return;
        }

        if (localStorage.getItem("role") === "CUSTOMER") {
            const customerId = Number(localStorage.getItem("customerId"));
            if (customerSelect) { customerSelect.innerHTML = `<option value="${customerId}">My account</option>`; customerSelect.value = String(customerId); customerSelect.closest(".field")?.classList.add("hidden"); }
        } else try {

            const customers = unwrap(
                await api("/customer/view-all")
            );

            customerSelect.innerHTML =
                '<option value="">Select customer</option>' +
                (
                    Array.isArray(customers)
                        ? customers.map(customer => `
                            <option value="${customer.custommerId}">
                                ${escapeHtml(customer.customerName || "Customer")}
                                — ${escapeHtml(customer.email || "")}
                            </option>
                        `).join("")
                        : ""
                );

        } catch (error) {

            customerSelect.innerHTML =
                '<option value="">Unable to load customers</option>';
        }

        try {

            const data = unwrap(
                await api("/event/view-all")
            );

            events = Array.isArray(data) ? data : [];

            const availableEvents = events.filter(event =>
                event.status !== "COMPLETED" &&
                event.status !== "CANCELLED" &&
                Number(event.availableSeats) > 0
            );

            eventSelect.innerHTML =
                '<option value="">Select event</option>' +
                availableEvents.map(event => `
                    <option
                        value="${event.eventId}"
                        data-available="${Number(event.availableSeats || 0)}"
                        data-price="${Number(event.ticketPrice || 0)}"
                    >
                        ${escapeHtml(event.eventName || "Event")}
                        — ${event.eventDate || ""}
                        — ${Number(event.availableSeats || 0)} seats
                        — ₹${Number(event.ticketPrice || 0)}
                    </option>
                `).join("");

            const preset =
                new URLSearchParams(location.search).get("eventId");

            if (preset) {
                eventSelect.value = preset;
                updateSeatInfo();
            }

        } catch (error) {

            eventSelect.innerHTML =
                '<option value="">Unable to load events</option>';
        }
    }

    function updateSeatInfo() {

        if (!eventSelect || !seatInfo || !seatsInput) {
            return;
        }

        const selected =
            eventSelect.options[eventSelect.selectedIndex];

        if (!selected || !selected.value) {
            seatInfo.textContent =
                "Choose an event to see current availability.";

            seatsInput.removeAttribute("max");
            return;
        }

        const available =
            Number(selected.dataset.available || 0);

        seatsInput.max = String(available);

        seatInfo.innerHTML =
            `Currently available: <strong>${available}</strong> seat(s).`;

        if (Number(seatsInput.value) > available) {
            seatsInput.value = available > 0 ? available : 1;
        }
    }

    if (eventSelect) {
        eventSelect.addEventListener("change", updateSeatInfo);
    }

    if (seatsInput) {
        seatsInput.addEventListener("input", () => {

            const selected =
                eventSelect?.options[eventSelect.selectedIndex];

            const available =
                Number(selected?.dataset?.available || 0);

            if (available > 0 && Number(seatsInput.value) > available) {
                seatsInput.value = available;
            }
        });
    }

    async function loadBookings() {

        if (!rows) {
            return;
        }

        try {

            const data = unwrap(
                await api(base + "/view-all")
            );

            records = Array.isArray(data) ? data : [];

            if (count) {
                count.textContent = records.length;
            }

            render();

        } catch (error) {

            rows.innerHTML = `
                <tr>
                    <td colspan="7" class="empty">
                        ${escapeHtml(error.message)}
                    </td>
                </tr>
            `;
        }
    }

    function render() {

        if (!rows) {
            return;
        }

        const query =
            (search?.value || "").toLowerCase();

        const filtered =
            records.filter(record =>
                JSON.stringify(record)
                    .toLowerCase()
                    .includes(query)
            );

        rows.innerHTML =
            filtered.length
                ? filtered.map(record => `
                    <tr>
                        <td>${record.bookingId ?? "—"}</td>
                        <td>${record.customerId ?? "—"}</td>
                        <td>${record.eventId ?? "—"}</td>
                        <td>${record.numberOfSeats ?? 0}</td>
                        <td>₹${record.totalAmount ?? 0}</td>
                        <td>
                            <span class="badge ${bookingStatusClass(record.bookingStatus)}">
                                ${record.bookingStatus ?? "UNKNOWN"}
                            </span>
                        </td>
                        <td>
                            <button
                                class="btn small danger row-delete"
                                data-id="${record.bookingId}">
                                Delete
                            </button>
                        </td>
                    </tr>
                `).join("")
                : `
                    <tr>
                        <td colspan="7" class="empty">
                            No booking records found.
                        </td>
                    </tr>
                `;

        bindActions();
    }

    function bookingStatusClass(status) {

        if (status === "APPROVED") {
            return "green";
        }

        if (status === "PAYMENT_FAILED") {
            return "red";
        }

        if (status === "CANCELLED") {
            return "orange";
        }

        return "";
    }

    function bindActions() {

        if (!rows) {
            return;
        }

        rows.querySelectorAll(".row-delete")
            .forEach(button => {

                button.addEventListener("click", async () => {

                    if (!confirm("Delete this booking?")) {
                        return;
                    }

                    try {

                        await api(
                            base + "/delete/" + button.dataset.id,
                            { method: "DELETE" }
                        );

                        toast(
                            "Booking deleted successfully",
                            "success"
                        );

                        await loadBookings();

                    } catch (error) {

                        toast(error.message, "error");
                    }
                });
            });
    }

    if (search) {
        search.addEventListener("input", render);
    }

    if (rows || count) {
        loadBookings();
    }

    if (form) {

        loadBookingOptions();

        form.addEventListener("submit", async event => {

            event.preventDefault();

            const customerId =
                Number(customerSelect.value);

            const eventId =
                Number(eventSelect.value);

            const numberOfSeats =
                Number(seatsInput.value);

            if (!customerId || !eventId) {

                formMessage.textContent =
                    "Please select a customer and an event.";

                return;
            }

            const selected =
                eventSelect.options[eventSelect.selectedIndex];

            const available =
                Number(selected?.dataset?.available || 0);

            if (numberOfSeats < 1) {

                formMessage.textContent =
                    "Number of seats must be at least 1.";

                return;
            }

            if (numberOfSeats > available) {

                formMessage.textContent =
                    `Only ${available} seat(s) are currently available.`;

                return;
            }

            saveButton.disabled = true;
            saveButton.textContent = "Reserving seats…";
            formMessage.textContent =
                "Checking live availability and creating your booking…";
            formMessage.className = "form-message info";

            try {

                await api(base + "/add", {
                    method: "POST",
                    body: JSON.stringify({
                        customerId,
                        eventId,
                        numberOfSeats,
                        paymentMode: paymentMode.value
                    })
                });

                formMessage.textContent =
                    "Booking confirmed and payment completed successfully.";

                formMessage.className =
                    "form-message success";

                toast(
                    "Booking confirmed and payment completed",
                    "success"
                );

                setTimeout(() => {
                    location.href = "list.html";
                }, 600);

            } catch (error) {

                let message = error.message || "Unable to create booking.";
                if (/payment service|payment failed|payment could not/i.test(message)) message = "Payment could not be completed right now. Your booking has not been confirmed. Please try again.";

                if (
                    /seat reservation/i.test(message) ||
                    /no longer available/i.test(message) ||
                    /not enough seats/i.test(message)
                ) {

                    message =
                        "Those seats are no longer available. " +
                        "The event has changed, so refresh the event list " +
                        "and select the available seats again.";
                }

                formMessage.textContent = message;
                formMessage.className = "form-message error";

                saveButton.disabled = false;
                saveButton.textContent = "Save booking";
            }
        });
    }

    function escapeHtml(value) {

        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});