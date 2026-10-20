(() => {
  const shell = document.getElementById("appShell");
  if (!shell || shell.dataset.initialized === "true") return;
  shell.dataset.initialized = "true";

  const path = window.location.pathname.toLowerCase();
  const token = localStorage.getItem("token");
  const role = (localStorage.getItem("role") || "").toUpperCase();
  const username = localStorage.getItem("username") || "Account";

  const esc = value => String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;",
    '"': "&quot;", "'": "&#39;"
  })[char]);

  const active = href => href === "/"
    ? path === "/" || path === "/index.html"
    : path.startsWith(href.toLowerCase());

  const link = (label, href) =>
    `<a href="${href}" class="${active(href) ? "active" : ""}">${label}</a>`;

  let links = [
    link("Home", "/"),
    link("Events", "/event-ui/list.html"),
    link("Venues", "/venue-ui/list.html")
  ];

  if (token && role === "ADMIN") {
    links = [
      link("Overview", "/"),
      link("Events", "/event-ui/list.html"),
      link("Venues", "/venue-ui/list.html"),
      link("Customers", "/customer-ui/list.html"),
      link("Bookings", "/booking-ui/list.html"),
      link("Payments", "/payment-ui/list.html"),
      link("Refund", "/payment-ui/refund.html"),
      link("Notifications", "/notification-ui/list.html"),
      link("Contact", "/contact.html")
    ];
  } else if (token && role === "VENUE_MANAGER") {
    links = [
      link("Home", "/"),
      link("My Venues", "/venue-ui/list.html"),
      link("My Events", "/event-ui/list.html"),
      link("Bookings", "/booking-ui/list.html"),
      link("Notifications", "/notification-ui/list.html"),
      link("Contact", "/contact.html")
    ];
  } else if (token) {
    links = [
      link("Home", "/"),
      link("Events", "/event-ui/list.html"),
      link("Venues", "/venue-ui/list.html"),
      link("My Bookings", "/booking-ui/list.html"),
      link("Payments", "/payment-ui/list.html"),
      link("Notifications", "/notification-ui/list.html"),
      link("Contact", "/contact.html")
    ];
  } else {
    links.push(link("Contact", "/contact.html"));
  }

  const accountLinks = token
    ? `<a class="top-account" href="/auth-ui/profile.html">
         <span class="top-avatar">${esc(username.charAt(0).toUpperCase())}</span>
         <span class="top-account-name">${esc(username)}</span>
       </a>
       <button class="top-signout" type="button" data-shell-logout>Sign out</button>`
    : `<a class="top-signin" href="/auth-ui/login.html">Sign in</a>
       <a class="top-register" href="/auth-ui/register.html">Get started</a>`;

  shell.innerHTML = `
    <header class="app-topbar">
      <a class="app-brand" href="/" aria-label="ApnaEvent home">
        <span class="app-brand-icon">A</span>
        <span>Apna<strong>Event</strong></span>
      </a>
      <nav class="app-topnav" aria-label="Main navigation">
        ${links.join("")}
        ${token ? link("Profile", "/auth-ui/profile.html") : ""}
      </nav>
      <div class="app-account">${accountLinks}</div>
    </header>
  `;

  const style = document.createElement("style");
  style.textContent = `
    #appShell { position:relative; z-index:100; }
    .app-topbar {
      display:flex; align-items:center; gap:22px;
      min-height:76px; padding:12px clamp(16px,3vw,44px);
      background:rgba(255,255,255,.96);
      border-bottom:1px solid #e4e7ec;
      box-shadow:0 4px 18px rgba(16,24,40,.04);
      font-family:Inter,system-ui,-apple-system,"Segoe UI",sans-serif;
    }
    .app-brand {
      display:flex; align-items:center; gap:10px; flex-shrink:0;
      color:#111827; text-decoration:none; font-size:20px;
      font-weight:800; letter-spacing:-.04em;
    }
    .app-brand strong { color:#6d28d9; }
    .app-brand-icon {
      display:grid; place-items:center; width:38px; height:38px;
      border-radius:12px; color:#fff;
      background:linear-gradient(135deg,#7c3aed,#0891b2);
      box-shadow:0 6px 16px rgba(109,40,217,.22);
    }
    .app-topnav {
      display:flex; align-items:center; gap:5px; flex:1;
      min-width:0; overflow-x:auto; scrollbar-width:thin;
    }
    .app-topnav a {
      display:inline-flex; align-items:center; white-space:nowrap;
      padding:10px 11px; border-radius:9px;
      color:#475467; font-size:13px; font-weight:650;
      text-decoration:none; transition:background .15s,color .15s;
    }
    .app-topnav a:hover,.app-topnav a.active {
      color:#5b21b6; background:#f3e8ff;
    }
    .app-account {
      display:flex; align-items:center; gap:9px; flex-shrink:0;
    }
    .top-account {
      display:flex; align-items:center; gap:8px;
      color:#344054; text-decoration:none; font-size:13px;
      font-weight:700;
    }
    .top-avatar {
      display:grid; place-items:center; width:34px; height:34px;
      border-radius:50%; background:#ede9fe; color:#5b21b6;
      font-weight:800;
    }
    .top-signin,.top-register,.top-signout {
      display:inline-flex; align-items:center; justify-content:center;
      padding:10px 13px; border-radius:10px;
      font:inherit; font-size:13px; font-weight:750;
      text-decoration:none; cursor:pointer;
    }
    .top-signin { color:#344054; border:1px solid #d0d5dd; background:#fff; }
    .top-register { color:#fff; background:#6d28d9; border:1px solid #6d28d9; }
    .top-signout { color:#b42318; background:#fff; border:1px solid #fecdca; }
    body .main {
      width:100% !important; max-width:1440px !important;
      margin:0 auto !important;
      padding:32px clamp(16px,4vw,48px) !important;
      box-sizing:border-box;
    }
    body .main.auth-main {
      min-height:calc(100vh - 170px);
      display:flex; align-items:center; justify-content:center;
    }
    body.auth,body.auth-page {
      display:block !important; min-height:100vh;
      padding:0 !important; background:#f7f8fc !important;
    }
    body.auth .auth-card { margin:44px auto; }
    body.auth .login-wrapper { margin:44px auto; }
    body.auth-page .auth-layout { margin:20px auto; }
    .site-footer {
      display:flex; align-items:center; justify-content:space-between;
      flex-wrap:wrap; gap:22px; padding:28px clamp(16px,4vw,48px);
      background:#0b1020; color:#fff;
      font-family:Inter,system-ui,-apple-system,"Segoe UI",sans-serif;
    }
    .site-footer .footer-brand,.site-footer>div:first-child {
      display:grid; gap:5px;
    }
    .site-footer span,.site-footer small { color:#98a2b3; }
    .site-footer .footer-links {
      display:flex; flex-wrap:wrap; gap:15px;
    }
    .site-footer .footer-links a {
      color:#d0d5dd; font-size:12px; text-decoration:none;
    }
    .site-footer .footer-links a:hover { color:#fff; }
    @media(max-width:1050px) {
      .app-topbar { flex-wrap:wrap; gap:10px; }
      .app-topnav { order:3; flex-basis:100%; }
      .app-account { margin-left:auto; }
    }
    @media(max-width:520px) {
      .app-topbar { padding:12px; }
      .app-brand { font-size:18px; }
      .top-account-name { display:none; }
      .app-topnav a { padding:9px; }
      .site-footer { align-items:flex-start; flex-direction:column; }
      .site-footer .footer-links { gap:12px; }
    }
  `;
  document.head.appendChild(style);

  shell.querySelector("[data-shell-logout]")?.addEventListener("click", () => {
    ["token", "role", "username", "customerId"].forEach(key =>
      localStorage.removeItem(key)
    );
    window.location.href = "/auth-ui/login.html";
  });

  if (!document.querySelector(".site-footer")) {
    const footer = document.createElement("footer");
    footer.className = "site-footer";
    footer.innerHTML = `
      <div class="footer-brand">
        <strong>ApnaEvent</strong>
        <span>Smart Event &amp; Venue Management Platform</span>
      </div>
      <div class="footer-links">
        <a href="/contact.html">Contact Us</a>
        <a href="mailto:anandraj24092002@gmail.com">Email</a>
        <a href="https://github.com/anand24raj" target="_blank" rel="noopener noreferrer">GitHub</a>
        <a href="https://linkedin.com/in/anandraj24/" target="_blank" rel="noopener noreferrer">LinkedIn</a>
        <a href="https://anandraj-porfolio.netlify.app/" target="_blank" rel="noopener noreferrer">Portfolio</a>
      </div>
      <small>© ${new Date().getFullYear()} ApnaEvent</small>
    `;
    document.body.appendChild(footer);
  }
})();
