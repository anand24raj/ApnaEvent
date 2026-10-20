# ApnaEvent — UI Design System

## 1. Intent

Preserve the existing ApnaEvent visual identity and make service pages feel consistent. The current UI is static HTML/CSS/JavaScript served by the Spring Boot services and routed through ApiGateway. This document describes the observed baseline; it is not permission to redesign every page.

## 2. Current visual baseline

Observed in existing `src/main/resources/static/assets/style.css` files:

- Font stack: `Inter`, `ui-sans-serif`, `system-ui`, `-apple-system`, `BlinkMacSystemFont`, `"Segoe UI"`, sans-serif.
- Main background: very light cool gray (`#f5f7fb` / `#f7f8fc`, depending on page).
- Main text: dark slate (`#111827`).
- Secondary text: muted slate (`#667085`).
- Primary brand: violet (`#6d28d9` or `#7c3aed`) with indigo (`#4f46e5`) and cyan (`#0891b2` / `#06b6d4`) accents.
- Sidebar/dark surfaces: deep navy (`#0b1020`, `#0d1427`, or `#111827`).
- Surfaces: white (`#ffffff`), subtle borders (`#e4e7ec`, `#eaecf0`).
- Semantic colors: success green (`#047857`), warning amber (`#b54708`), error red (`#b42318`).
- Rounded cards and controls, soft shadows, violet/cyan gradients, and responsive CSS layouts are established patterns.

Some service stylesheets have drifted in exact token values. Reuse the closest existing stylesheet in the page's own service and avoid introducing further palette variants without approval.

## 3. Typography

Prefer the current font stack. Use a clear hierarchy and preserve existing page patterns:

| Role | Existing pattern / guidance |
|---|---|
| Hero heading | Responsive `clamp()` sizing; large, tight line height and negative letter spacing |
| Page title | Around 30px in dashboard layouts |
| Section heading | Around 24–38px depending on page context |
| Card heading | Around 18–23px |
| Body text | 14–16px for dashboard pages; larger for marketing copy |
| Labels / metadata | 10–13px, with sufficient contrast and readable weight |

These are observed/approximate conventions, not a mandate to resize all existing pages. Do not use tiny text for important information.

## 4. Spacing and layout

- Use the existing spacing rhythm, generally based on 4px increments: 4, 8, 12, 16, 24, 32, 48, 64px.
- Dashboard layouts use a fixed-width sidebar on desktop with a main content region; preserve the existing responsive collapse behavior.
- Marketing pages use centered max-width sections, generous vertical spacing, and responsive grid layouts.
- Cards use consistent padding, borders, radius, and subtle shadows.
- Use `minmax(0, 1fr)` in grids where needed to prevent overflow.
- Avoid fixed widths that cause horizontal scrolling on mobile.

## 5. Component conventions

Reuse existing CSS classes and components when possible:

- `.site-shell`, `.site-header`, `.main-nav`, `.hero`, `.section`, `.site-footer`
- `.app`, `.sidebar`, `.main`, `.top`, `.card`, `.grid`
- `.btn.primary`, `.btn.ghost`, `.btn.glass`, `.badge`
- `.form`, `.field`, `.table-wrap`, `.table`
- Existing skeleton/loading, status badge, and RA assistant styles

Before creating a new shared component, search for an equivalent in the service's existing static assets. Do not assume every class exists in every service; inspect the local stylesheet first.

## 6. Buttons and forms

- Keep primary actions visually prominent and secondary actions quieter.
- Preserve visible hover, focus, disabled, and loading states.
- Keep button labels action-oriented and concise.
- Every button/link must perform its documented action; do not ship decorative controls that appear functional.
- Associate labels with inputs and show validation errors near the relevant field.
- Keep forms readable on small screens and preserve entered values after recoverable failures where practical.

## 7. Responsive and accessible UI

- Test at narrow mobile widths (320–375px), common mobile widths, tablet, and desktop.
- Check navigation, cards, forms, tables, dialogs, and assistant popup for overflow.
- Use semantic HTML, descriptive link/button names, appropriate image alt text, visible keyboard focus, and sufficient contrast.
- Do not communicate status by color alone.
- Respect reduced-motion preferences for non-essential animation.
- Avoid hover-only functionality.

## 8. Assets and content

- Reuse existing logos, SVGs, icons, and assistant assets.
- Do not replace brand assets or add a new icon library without approval.
- Avoid unrelated stock imagery or invented event data in production views.
- Escape untrusted data before rendering it into HTML.

## 9. UI change checklist

Before marking a UI task complete, check:

- Consistent font, color, spacing, and component usage
- Correct links, API calls, and button actions
- Loading, empty, error, and success states as applicable
- Responsive layout without unintended horizontal overflow
- Keyboard focus and accessible labels
- No broken assets or obvious browser-console errors
- JavaScript syntax validation for changed scripts

Record checks that could not be run. Do not claim a visual/browser test was performed unless it was.
