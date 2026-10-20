(() => {

    const params = new URLSearchParams(window.location.search);

    const eventId = params.get("id");

    const eventName = document.getElementById("eventName");
    const eventMeta = document.getElementById("eventMeta");
    const eventStatus = document.getElementById("eventStatus");
    const eventStatusText = document.getElementById("eventStatusText");
    const eventSchedule = document.getElementById("eventSchedule");
    const eventVenue = document.getElementById("eventVenue");
    const eventSeats = document.getElementById("eventSeats");
    const eventTicketInfo = document.getElementById("eventTicketInfo");
    const bookLink = document.getElementById("bookLink");


    if (!eventId) {

        eventName.textContent = "Event not found";

        eventMeta.textContent =
            "No event ID was provided.";

        eventStatus.textContent = "Unavailable";

        eventStatus.className = "badge";

        return;
    }


    async function loadEvent() {

        try {

            const response =
                await fetch(`/event/view/${eventId}`);

            if (!response.ok) {

                throw new Error(
                    `Event Service returned ${response.status}`
                );

            }


            const result = await response.json();


            /*
             * ApnaEvent Event Service response:
             *
             * {
             *   data: {
             *      eventId,
             *      eventName,
             *      eventCategory,
             *      eventDate,
             *      eventTime,
             *      venueId,
             *      totalSeats,
             *      availableSeats,
             *      ticketPrice,
             *      status
             *   }
             * }
             */

            const event = result.data || result;


            eventName.textContent =
                event.eventName || "Unnamed Event";


            eventMeta.textContent =
                `${event.eventCategory || "Event"} • ₹${event.ticketPrice ?? 0}`;


            eventSchedule.textContent =
                `${event.eventDate || "Date not available"}`
                + ` • `
                + `${event.eventTime || "Time not available"}`;


            eventVenue.textContent =
                event.venueId
                    ? `Venue #${event.venueId}`
                    : "Venue information unavailable";


            const availableSeats =
                Number(event.availableSeats ?? 0);


            const totalSeats =
                Number(event.totalSeats ?? 0);


            eventSeats.textContent =
                availableSeats;


            eventTicketInfo.textContent =
                `${availableSeats} of ${totalSeats} seats available`;


            const status =
                String(event.status || "UNKNOWN");


            eventStatus.textContent =
                status;


            eventStatusText.textContent =
                status;


            if (
                availableSeats > 0 &&
                status.toUpperCase() !== "CANCELLED"
            ) {

                eventStatus.className =
                    "badge green";


                bookLink.href =
                    `/booking-ui/form.html?eventId=${encodeURIComponent(event.eventId)}`;

                bookLink.classList.remove("disabled");

            } else {

                eventStatus.className =
                    "badge";


                eventTicketInfo.textContent =
                    "This event is currently not available for booking.";


                bookLink.removeAttribute("href");

                bookLink.classList.add("disabled");

                bookLink.textContent =
                    "Booking Unavailable";
            }


        } catch (error) {

            console.error(
                "Unable to load event:",
                error
            );


            eventName.textContent =
                "Unable to load event";


            eventMeta.textContent =
                "The Event Service could not provide this event.";


            eventStatus.textContent =
                "Error";


            eventStatus.className =
                "badge";


            eventStatusText.textContent =
                "Event data could not be loaded.";


            eventSchedule.textContent =
                "—";


            eventVenue.textContent =
                "—";


            eventSeats.textContent =
                "—";


            eventTicketInfo.textContent =
                "Please return to the event list and try again.";

        }

    }


    loadEvent();

})();