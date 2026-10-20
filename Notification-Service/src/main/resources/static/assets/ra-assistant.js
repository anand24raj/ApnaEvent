(() => {
  'use strict';
  const API = localStorage.getItem('gatewayUrl') || window.location.origin;
  const $ = (selector) => document.querySelector(selector);
  const token = () => localStorage.getItem('token');
  const node = (tag, attrs = {}, text) => {
    const element = document.createElement(tag);
    Object.entries(attrs).forEach(([key, value]) => element.setAttribute(key, value));
    if (text !== undefined) element.textContent = String(text);
    return element;
  };
  const authHeaders = () => ({
    ...(token() ? { Authorization: `Bearer ${token()}` } : {})
  });

  async function ask(question) {
    const response = await fetch(`${API}/ai/ask`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify({ question })
    });
    if (response.status === 401) throw new Error('Please sign in again to use RA.');
    if (response.status === 403) throw new Error('Your account is not authorized to view that information.');
    if (!response.ok) throw new Error('RA could not read ApnaEvent data right now. Please try again.');
    return response.json();
  }

  function addMessage(text, isUser = false) {
    const box = $('#ra-messages');
    const message = node('div', {
      class: `ra-message ${isUser ? 'user' : 'assistant'}`,
      role: isUser ? 'note' : 'status'
    }, text);
    box.appendChild(message);
    box.scrollTop = box.scrollHeight;
    return message;
  }

  function appendField(card, value, fallback = '') {
    if (value === null || value === undefined || value === '') return;
    card.appendChild(node('p', {}, value || fallback));
  }

  function appendLink(container, label, href, primary = false) {
    // RA actions must stay within known application routes.
    if (typeof href !== 'string' || !href.startsWith('/') || href.startsWith('//')) return;
    const link = node('a', { class: primary ? 'primary' : '' }, label);
    link.href = href;
    container.appendChild(link);
  }

  function addRecords(result) {
    const box = $('#ra-messages');
    const items = Array.isArray(result.items) ? result.items : [];
    if (result.type === 'action') {
      items.forEach((item) => {
        const actions = node('div', { class: 'ra-card-actions' });
        appendLink(actions, item.label, item.url, true);
        if (actions.children.length) box.appendChild(actions);
      });
      return;
    }
    if (!['events', 'venues', 'bookings', 'customers', 'profile', 'payments'].includes(result.type)) return;
    if (!items.length) return;

    const grid = node('div', { class: 'ra-card-grid' });
    items.forEach((item) => {
      const card = node('article', { class: 'ra-record-card' });
      if (result.type === 'events') {
        card.appendChild(node('h4', {}, item.eventName || 'Event'));
        appendField(card, [item.eventDate, item.eventTime].filter(Boolean).join(' · '));
        appendField(card, `${item.availableSeats ?? 0} seats available · ₹${item.ticketPrice ?? '—'}`);
        const actions = node('div', { class: 'ra-card-actions' });
        appendLink(actions, 'View event', `/event-ui/details.html?id=${encodeURIComponent(item.eventId ?? '')}`);
        appendLink(actions, 'Book', `/booking-ui/form.html?eventId=${encodeURIComponent(item.eventId ?? '')}`, true);
        card.appendChild(actions);
      } else if (result.type === 'venues') {
        card.appendChild(node('h4', {}, item.venueName || 'Venue'));
        appendField(card, [item.city, item.venueType].filter(Boolean).join(' · '));
        appendField(card, `Capacity ${item.capacity ?? '—'} · ${item.status ?? 'Status unavailable'}`);
        const actions = node('div', { class: 'ra-card-actions' });
        appendLink(actions, 'View venue', `/venue-ui/details.html?id=${encodeURIComponent(item.venueId ?? '')}`, true);
        card.appendChild(actions);
      } else if (result.type === 'bookings') {
        card.appendChild(node('h4', {}, `Booking #${item.bookingId ?? '—'}`));
        appendField(card, `Event ${item.eventId ?? '—'} · ${item.numberOfSeats ?? 0} seat(s)`);
        appendField(card, `Total ₹${item.totalAmount ?? '—'}`);
        appendField(card, `Status: ${item.bookingStatus ?? 'Unavailable'}`);
      } else if (result.type === 'customers' || result.type === 'profile') {
        card.appendChild(node('h4', {}, item.customerName || 'ApnaEvent profile'));
        appendField(card, item.email);
        appendField(card, item.mobile);
        appendField(card, item.status);
      } else if (result.type === 'payments') {
        card.appendChild(node('h4', {}, `Payment #${item.paymentId ?? '—'}`));
        appendField(card, `Booking #${item.bookingId ?? '—'}`);
        appendField(card, `Amount ₹${item.amount ?? '—'} · ${item.paymentMode ?? 'Mode unavailable'}`);
        appendField(card, `Status: ${item.paymentStatus ?? 'Unavailable'}`);
      }
      grid.appendChild(card);
    });
    box.appendChild(grid);
    box.scrollTop = box.scrollHeight;
  }

  function openPanel() {
    $('#ra-panel').classList.add('open');
    $('#ra-launcher').setAttribute('aria-expanded', 'true');
    $('#ra-input').focus();
  }

  function closePanel() {
    $('#ra-panel').classList.remove('open');
    $('#ra-launcher').setAttribute('aria-expanded', 'false');
    $('#ra-launcher').focus();
  }

  let busy = false;
  async function send(question) {
    const q = String(question || '').trim();
    if (!q || busy) return;
    busy = true;
    const form = $('#ra-form');
    const input = $('#ra-input');
    const submit = form.querySelector('button[type="submit"]');
    addMessage(q, true);
    const pending = addMessage('Checking authorized ApnaEvent data…');
    input.disabled = true;
    submit.disabled = true;
    try {
      const result = await ask(q);
      pending.remove();
      addMessage(result.message || 'No response was returned by RA.');
      addRecords(result);
    } catch (error) {
      pending.textContent = error.message || 'RA is temporarily unavailable.';
    } finally {
      busy = false;
      input.disabled = false;
      submit.disabled = false;
      input.focus();
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    if ($('#ra-launcher')) return;
    const launcher = node('button', {
      id: 'ra-launcher', type: 'button', 'aria-label': 'Open RA assistant',
      'aria-controls': 'ra-panel', 'aria-expanded': 'false'
    });
    launcher.append(
      node('span', { class: 'ra-launcher-avatar', 'aria-hidden': 'true' }),
      node('span', { class: 'ra-launcher-copy' })
    );
    const copy = launcher.querySelector('.ra-launcher-copy');
    copy.append(node('strong', {}, 'Ask RA'), node('span', {}, 'Your ApnaEvent assistant'));
    launcher.appendChild(node('span', { class: 'ra-launcher-arrow', 'aria-hidden': 'true' }, '↑'));
    launcher.addEventListener('click', openPanel);
    document.body.appendChild(launcher);

    const panel = node('section', {
      id: 'ra-panel', 'aria-label': 'RA ApnaEvent assistant',
      'aria-modal': 'false', role: 'region'
    });
    const header = node('header', { class: 'ra-header' });
    const headLeft = node('div', { class: 'ra-head-left' });
    headLeft.append(node('span', { class: 'ra-avatar', 'aria-hidden': 'true' }));
    const identity = node('div');
    identity.append(node('strong', {}, 'RA'), node('span', {}, 'ApnaEvent Assistant'));
    headLeft.appendChild(identity);
    const closeButton = node('button', { id: 'ra-close', type: 'button', 'aria-label': 'Close RA' }, '×');
    header.append(headLeft, closeButton);
    const subtitle = node('div', { class: 'ra-subtitle' }, 'ApnaEvent project data only · no public web research');
    const suggestions = node('div', { id: 'ra-suggestions', class: 'ra-suggestions', 'aria-label': 'Suggested questions' });
    const messages = node('div', { id: 'ra-messages', class: 'ra-messages', 'aria-live': 'polite', 'aria-relevant': 'additions text' });
    const form = node('form', { id: 'ra-form', class: 'ra-form' });
    const input = node('input', {
      id: 'ra-input', autocomplete: 'off', placeholder: 'Ask about ApnaEvent…',
      'aria-label': 'Ask RA a question', maxlength: '500'
    });
    const submit = node('button', { type: 'submit', 'aria-label': 'Send question' }, '↑');
    form.append(input, submit);
    panel.append(header, subtitle, suggestions, messages, form);
    document.body.appendChild(panel);

    closeButton.addEventListener('click', closePanel);
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && $('#ra-panel').classList.contains('open')) closePanel();
    });
    form.addEventListener('submit', (event) => {
      event.preventDefault();
      const question = input.value.trim();
      input.value = '';
      send(question);
    });
    addMessage('Hi! I’m RA. I can help with ApnaEvent events, venues, and account information you are authorized to access.');

    fetch(`${API}/ai/suggestions`, { headers: authHeaders() })
      .then((response) => response.ok ? response.json() : [])
      .then((items) => {
        if (!Array.isArray(items)) return;
        items.forEach((item) => {
          const button = node('button', { type: 'button', class: 'ra-suggestion' }, item.label);
          button.addEventListener('click', () => send(item.question));
          suggestions.appendChild(button);
        });
      }).catch(() => {});
  });
})();
