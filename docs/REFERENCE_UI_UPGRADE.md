# ApnaEvent Reference UI Upgrade

Implemented in the existing project root. Do not create another Eclipse workspace.

- Reference-quality branded RA assistant with clickable quick actions and live record cards.
- Real Event-Service and Venue-Service data on public pages.
- Event/Venue detail pages with live data.
- Event/Venue View/Edit/Cancel or Deactivate actions.
- Booking form can be opened from RA with an event preselected.
- Auth-aware Sign in/Sign out navigation.
- Favicon, responsive layouts, subtle animation, loading/empty/error states.
- Booking seat reservation now preserves the real Event-Service failure reason instead of hiding all Feign failures behind one generic message.
- GitHub, LinkedIn, portfolio and email footer links retained.

Run from PowerShell:
`cd "C:\Users\anand\ApnaEvent"`
`Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`
`.\scripts\start-all.ps1`

Open `http://localhost:8080`.
