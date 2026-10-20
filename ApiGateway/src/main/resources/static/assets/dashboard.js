document.addEventListener("DOMContentLoaded", async () => {
  const json = async (url) => {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`Request failed (${response.status})`);
    const body = await response.json();
    return body?.data ?? body;
  };
  const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;
  const element = (tag, className, text) => {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined && text !== null) node.textContent = String(text);
    return node;
  };
  const link = (href, label) => {
    const a = element("a", "card-link", label);
    a.href = href;
    return a;
  };
  const validDate = (value) => {
    const date = value ? new Date(value) : null;
    return date && !Number.isNaN(date.getTime()) ? date : null;
  };
  const setEmpty = (container, message) => {
    container.replaceChildren(element("div", "empty", message));
  };

  try {
    const eventRows = await json("/event/view-all");
    const events = Array.isArray(eventRows) ? eventRows : [];
    document.getElementById("events").textContent = String(events.length);
    const cards = document.getElementById("event-cards");
    cards.replaceChildren();
    events.slice(0, 4).forEach((event) => {
      const card = element("article", "event-card");
      const art = element("div", "event-art");
      art.append(
        element("span", "", String(event.eventCategory || "EVENT").toUpperCase()),
        element("b", "", validDate(event.eventDate)
          ? validDate(event.eventDate).toLocaleDateString("en-IN", { day: "2-digit", month: "short" })
          : "DATE TBA")
      );
      const body = element("div", "event-body");
      body.append(
        element("div", "status-dot", event.status || "UPCOMING"),
        element("h3", "", event.eventName || "Untitled event"),
        element("p", "", [event.eventDate, event.eventTime].filter(Boolean).join(" · "))
      );
      const footer = element("div", "event-footer");
      footer.append(
        element("strong", "", `${Number(event.availableSeats ?? 0)} seats`),
        element("span", "", money(event.ticketPrice))
      );
      body.append(footer, link(`/event-ui/details.html?id=${encodeURIComponent(event.eventId ?? "")}`, "View event →"));
      card.append(art, body);
      cards.appendChild(card);
    });
    if (!events.length) setEmpty(cards, "No events are available yet.");

    const first = events[0];
    if (first) {
      document.getElementById("hero-event-name").textContent = first.eventName || "Upcoming event";
      document.getElementById("hero-event-meta").textContent =
        [first.eventDate, first.eventTime, money(first.ticketPrice)].filter(Boolean).join(" · ");
      document.getElementById("hero-event-seats").textContent = `${first.availableSeats ?? 0} seats available`;
      const preview = document.querySelector(".preview-date");
      const date = validDate(first.eventDate);
      preview.replaceChildren(document.createTextNode(date ? String(date.getDate()).padStart(2, "0") : "—"));
      preview.appendChild(element("small", "", date ? date.toLocaleDateString("en-IN", { month: "short" }).toUpperCase() : "TBA"));
    }
  } catch (_) {
    setEmpty(document.getElementById("event-cards"), "Events are temporarily unavailable.");
  }

  try {
    const venueRows = await json("/venue/view-all");
    const venues = Array.isArray(venueRows) ? venueRows : [];
    document.getElementById("venues").textContent = String(venues.length);
    const cards = document.getElementById("venue-cards");
    cards.replaceChildren();
    venues.slice(0, 4).forEach((venue) => {
      const card = element("article", "venue-card");
      const art = element("div", "venue-art");
      art.append(
        element("span", "", String(venue.venueType || "VENUE").replaceAll("_", " ")),
        element("i", "", "⌂")
      );
      const body = element("div", "venue-body");
      body.append(
        element("span", "location", venue.city || "Location not set"),
        element("h3", "", venue.venueName || "Venue"),
        element("p", "", `Capacity up to ${venue.capacity ?? "—"} guests`)
      );
      const footer = element("div", "event-footer");
      footer.append(
        element("strong", "", venue.status || "ACTIVE"),
        element("span", "", venue.facilities || "Facilities not specified")
      );
      body.append(footer, link(`/venue-ui/details.html?id=${encodeURIComponent(venue.venueId ?? "")}`, "View venue →"));
      card.append(art, body);
      cards.appendChild(card);
    });
    if (!venues.length) setEmpty(cards, "No venues are available yet.");
  } catch (_) {
    setEmpty(document.getElementById("venue-cards"), "Venues are temporarily unavailable.");
  }

  try {
    const dashboard = await json("/admin/dashboard");
    document.getElementById("customers").textContent = String(dashboard.totalCustomers ?? 0);
    document.getElementById("bookings").textContent = String(dashboard.totalBookings ?? 0);
  } catch (_) {
    document.getElementById("customers").textContent = "—";
    document.getElementById("bookings").textContent = "—";
  }
  const year = document.getElementById("copyright-year");
  if (year) year.textContent = String(new Date().getFullYear());
});
