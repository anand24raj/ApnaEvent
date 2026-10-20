(() => {

    const params = new URLSearchParams(window.location.search);

    const venueId = params.get("id");

    const venueName = document.getElementById("venueName");
    const venueMeta = document.getElementById("venueMeta");
    const venueStatus = document.getElementById("venueStatus");
    const venueStatusText = document.getElementById("venueStatusText");
    const venueCapacity = document.getElementById("venueCapacity");
    const venueFacilities = document.getElementById("venueFacilities");
    const venueCity = document.getElementById("venueCity");
    const venueType = document.getElementById("venueType");


    if (!venueId) {

        venueName.textContent =
            "Venue not found";

        venueMeta.textContent =
            "No venue ID was provided.";

        venueStatus.textContent =
            "Unavailable";

        venueStatus.className =
            "badge";

        return;
    }


    async function loadVenue() {

        try {

            const response =
                await fetch(`/venue/view/${venueId}`);


            if (!response.ok) {

                throw new Error(
                    `Venue Service returned ${response.status}`
                );

            }


            const result =
                await response.json();


            const venue =
                result.data || result;


            venueName.textContent =
                venue.venueName || "Unnamed Venue";


            venueMeta.textContent =
                `${venue.city || "Location unavailable"}`
                + " • "
                + `${venue.venueType || "Venue"}`;


            venueCapacity.textContent =
                venue.capacity ?? "—";


            venueCity.textContent =
                venue.city || "—";


            venueType.textContent =
                venue.venueType || "—";


            venueFacilities.textContent =
                formatFacilities(venue.facilities);


            const status =
                String(
                    venue.status || "UNKNOWN"
                );


            venueStatus.textContent =
                status;


            venueStatusText.textContent =
                status;


            if (
                status.toUpperCase() === "ACTIVE"
            ) {

                venueStatus.className =
                    "badge green";

            } else {

                venueStatus.className =
                    "badge";

            }


        } catch (error) {

            console.error(
                "Unable to load venue:",
                error
            );


            venueName.textContent =
                "Unable to load venue";


            venueMeta.textContent =
                "The Venue Service could not provide this venue.";


            venueStatus.textContent =
                "Error";


            venueStatus.className =
                "badge";


            venueCapacity.textContent =
                "—";


            venueFacilities.textContent =
                "—";


            venueCity.textContent =
                "—";


            venueType.textContent =
                "—";


            venueStatusText.textContent =
                "Venue data could not be loaded.";

        }

    }


    function formatFacilities(facilities) {

        if (!facilities) {
            return "No facility information available.";
        }


        if (typeof facilities === "string") {
            return facilities;
        }


        if (Array.isArray(facilities)) {

            return facilities.join(" • ");

        }


        if (typeof facilities === "object") {

            return Object.entries(facilities)

                .filter(([, value]) =>
                    value !== null &&
                    value !== undefined &&
                    value !== false
                )

                .map(([key, value]) => {

                    const label =
                        key
                            .replace(/([A-Z])/g, " $1")
                            .replace(/^./, char => char.toUpperCase());

                    return `${label}: ${value}`;

                })

                .join(" • ") || "No facilities listed.";

        }

  return String(facilities);

    }


    loadVenue();

})();