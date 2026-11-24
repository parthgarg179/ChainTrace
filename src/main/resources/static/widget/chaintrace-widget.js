/*!
 * ChainTrace embeddable widget
 * ---------------------------------------------------------------------------
 * A company adds ChainTrace to its own support page with two lines:
 *
 *   <div id="chaintrace"></div>
 *   <script src="https://<chaintrace-host>/widget/chaintrace-widget.js"
 *           data-api-key="<company licence key>"
 *           data-target="chaintrace"
 *           data-accent="#2457ff"></script>
 *
 * The widget then handles login (with the customer's existing company account),
 * product lookup and the ownership timeline. No framework, no build step.
 */
(function () {
  "use strict";

  var script = document.currentScript;
  if (!script) {
    console.error("[ChainTrace] Could not locate the widget <script> tag.");
    return;
  }

  var API_KEY = script.getAttribute("data-api-key");
  var TARGET_ID = script.getAttribute("data-target") || "chaintrace";
  var ACCENT = script.getAttribute("data-accent") || "#2457ff";
  var BASE = new URL(script.src, window.location.href).origin;
  var API = BASE + "/api/v1";
  var STORAGE_KEY = "chaintrace:" + API_KEY;
  // Optional, for demos only: data-auto-login="email:password" signs the visitor in
  // with that company account automatically, so the page opens straight on the ID box.
  // The backend flow is unchanged (a normal login and token), only the form is skipped.
  var AUTO_LOGIN = parseAutoLogin(script.getAttribute("data-auto-login"));

  function parseAutoLogin(value) {
    if (!value || value.indexOf(":") < 1) return null;
    var cut = value.indexOf(":");
    return { email: value.substring(0, cut), password: value.substring(cut + 1) };
  }

  var state = {
    company: null,   // { id, displayName, productType, idLabel, idHint }
    session: null,   // { token, customerName, expiresAt }
    lastResult: null
  };

  var root;

  // ------------------------------------------------------------------ helpers

  /** Builds DOM nodes. Text is always set via textContent, so data can't inject HTML. */
  function el(tag, attrs, children) {
    var node = document.createElement(tag);
    if (attrs) {
      Object.keys(attrs).forEach(function (key) {
        var value = attrs[key];
        if (value === null || value === undefined || value === false) return;
        if (key === "text") node.textContent = value;
        else if (key === "class") node.className = value;
        else if (key.indexOf("on") === 0) node.addEventListener(key.substring(2), value);
        else node.setAttribute(key, value === true ? "" : value);
      });
    }
    (children || []).forEach(function (child) {
      if (child === null || child === undefined || child === false) return;
      node.appendChild(typeof child === "string" ? document.createTextNode(child) : child);
    });
    return node;
  }

  function loadSession() {
    try {
      var raw = window.sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      var session = JSON.parse(raw);
      if (new Date(session.expiresAt) <= new Date()) return null;
      return session;
    } catch (e) {
      return null; // storage blocked (e.g. private mode) - just log in again
    }
  }

  function saveSession(session) {
    try {
      if (session) window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session));
      else window.sessionStorage.removeItem(STORAGE_KEY);
    } catch (e) { /* ignore */ }
  }

  /** Calls the ChainTrace API with the licence key and (if logged in) the bearer token. */
  function api(path, options) {
    options = options || {};
    var headers = { "X-ChainTrace-Key": API_KEY };
    if (options.body) headers["Content-Type"] = "application/json";
    if (state.session) headers["Authorization"] = "Bearer " + state.session.token;

    return fetch(API + path, {
      method: options.method || "GET",
      headers: headers,
      body: options.body ? JSON.stringify(options.body) : undefined
    }).then(function (response) {
      if (options.raw && response.ok) return response;
      return response.text().then(function (text) {
        var data = null;
        try { data = text ? JSON.parse(text) : null; } catch (e) { data = null; }
        if (!response.ok) {
          var error = new Error((data && data.message) || ("Request failed (" + response.status + ")"));
          error.code = data && data.error;
          error.status = response.status;
          throw error;
        }
        return data;
      });
    });
  }

  function formatDate(iso) {
    if (!iso) return "—";
    var d = new Date(iso + "T00:00:00");
    return d.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" });
  }

  function formatDuration(days) {
    if (days === null || days === undefined) return "";
    if (days < 31) return days + (days === 1 ? " day" : " days");
    var months = Math.round(days / 30.44);
    if (months < 12) return months + (months === 1 ? " month" : " months");
    var years = Math.floor(months / 12);
    var rest = months % 12;
    return years + (years === 1 ? " yr" : " yrs") + (rest ? " " + rest + " mo" : "");
  }

  /** "odometer_km" -> "Odometer km" */
  function prettyKey(key) {
    var words = key.replace(/_/g, " ");
    return words.charAt(0).toUpperCase() + words.slice(1);
  }

  function prettyValue(value) {
    if (typeof value === "number") return value.toLocaleString();
    if (typeof value === "boolean") return value ? "Yes" : "No";
    return String(value);
  }

  function icon() {
    if (!state.company) return "🔎";
    if (state.company.productType === "CAR") return "🚗";
    if (state.company.productType === "SMARTPHONE") return "📱";
    return "📦";
  }

  function render(view) {
    root.innerHTML = "";
    root.appendChild(view);
  }

  function errorBox(message) {
    return el("div", { class: "ct-error", role: "alert", text: message });
  }

  function header(subtitle) {
    return el("div", { class: "ct-header" }, [
      el("div", { class: "ct-brand" }, [
        el("span", { class: "ct-logo", text: "⛓" }),
        el("span", { text: "ChainTrace" }),
        el("span", { class: "ct-for", text: state.company ? " for " + state.company.displayName : "" })
      ]),
      subtitle ? el("p", { class: "ct-subtitle", text: subtitle }) : null,
      state.session && !AUTO_LOGIN
        ? el("div", { class: "ct-user" }, [
            el("span", { text: "Signed in as " + state.session.customerName }),
            el("button", { class: "ct-link", type: "button", text: "Sign out", onclick: logout })
          ])
        : null
    ]);
  }

  // ------------------------------------------------------------------ views

  function showLogin(message) {
    var email = el("input", { type: "email", id: "ct-email", required: true, autocomplete: "username",
      placeholder: "you@example.com" });
    var password = el("input", { type: "password", id: "ct-password", required: true,
      autocomplete: "current-password" });
    var button = el("button", { class: "ct-button", type: "submit", text: "Sign in" });
    var status = el("div");

    var form = el("form", {
      class: "ct-form",
      onsubmit: function (event) {
        event.preventDefault();
        button.disabled = true;
        button.textContent = "Signing in…";
        status.innerHTML = "";
        api("/auth/login", { method: "POST", body: { email: email.value, password: password.value } })
          .then(function (session) {
            state.session = session;
            saveSession(session);
            showLookup();
          })
          .catch(function (error) {
            status.appendChild(errorBox(error.message));
            button.disabled = false;
            button.textContent = "Sign in";
          });
      }
    }, [
      el("label", { for: "ct-email", text: "Email" }), email,
      el("label", { for: "ct-password", text: "Password" }), password,
      button, status
    ]);

    render(el("div", { class: "ct-card" }, [
      header("Sign in with your " + (state.company ? state.company.displayName : "company") +
        " account to view a product's verified ownership history."),
      message ? errorBox(message) : null,
      form
    ]));
    email.focus();
  }

  function showLookup() {
    var company = state.company;
    var input = el("input", { type: "text", id: "ct-product-id", required: true, autocomplete: "off",
      spellcheck: "false", placeholder: "Enter " + company.idLabel });
    var button = el("button", { class: "ct-button", type: "submit", text: "Verify" });
    var results = el("div", { class: "ct-results", "aria-live": "polite" });

    function runLookup() {
      var id = input.value.trim();
      if (!id) return;
      button.disabled = true;
      button.textContent = "Checking…";
      results.innerHTML = "";
      results.appendChild(el("div", { class: "ct-loading", text: "Reading " + company.displayName + " records…" }));

      api("/products/" + encodeURIComponent(id) + "/history")
        .then(function (history) {
          state.lastResult = history;
          results.innerHTML = "";
          results.appendChild(renderHistory(history));
        })
        .catch(function (error) {
          if (error.code === "SESSION_EXPIRED") {
            state.session = null;
            saveSession(null);
            if (AUTO_LOGIN) {
              // Demo mode: sign in again silently and repeat the lookup.
              autoLogin().then(runLookup).catch(function (e) { showLogin(e.message); });
              return;
            }
            showLogin("Your session expired. Please sign in again.");
            return;
          }
          results.innerHTML = "";
          results.appendChild(error.code === "PRODUCT_NOT_FOUND"
            ? el("div", { class: "ct-notfound" }, [
                el("div", { class: "ct-notfound-title", text: "✕ No record found" }),
                el("p", { text: error.message }),
                el("p", { class: "ct-muted", text: "This product may not be genuine, or the ID may have been typed incorrectly." })
              ])
            : errorBox(error.message));
        })
        .then(function () {
          button.disabled = false;
          button.textContent = "Verify";
        });
    }

    var form = el("form", {
      class: "ct-search",
      onsubmit: function (event) { event.preventDefault(); runLookup(); }
    }, [
      el("label", { for: "ct-product-id", class: "ct-search-label", text: icon() + " " + company.idLabel }),
      el("div", { class: "ct-search-row" }, [input, button]),
      company.idHint ? el("small", { class: "ct-muted", text: company.idHint }) : null
    ]);

    render(el("div", { class: "ct-card" }, [header(null), form, results]));
    input.focus();
  }

  function renderHistory(h) {
    var p = h.product;
    var warrantyClass = p.warrantyStatus === "ACTIVE" ? "ct-chip ct-chip-ok"
      : p.warrantyStatus === "EXPIRED" ? "ct-chip ct-chip-muted" : "ct-chip";
    var warrantyText = p.warrantyStatus === "ACTIVE" ? "Warranty active until " + formatDate(p.warrantyExpiry)
      : p.warrantyStatus === "EXPIRED" ? "Warranty expired " + formatDate(p.warrantyExpiry) : "Warranty unknown";

    var facts = [
      [h.idLabel, h.productId],
      ["Manufactured", formatDate(p.manufactureDate)],
      ["Sold / registered in", p.soldIn || "—"]
    ];
    Object.keys(p.details || {}).forEach(function (key) {
      if (p.details[key] !== null) facts.push([prettyKey(key), prettyValue(p.details[key])]);
    });

    var productCard = el("div", { class: "ct-product" }, [
      el("div", { class: "ct-verified", text: "✓ Verified by " + h.companyName }),
      el("h3", { class: "ct-model", text: p.model }),
      el("span", { class: warrantyClass, text: warrantyText }),
      p.alert ? el("div", { class: "ct-alert", role: "alert", text: "⚠ " + p.alert }) : null,
      el("dl", { class: "ct-facts" }, facts.reduce(function (nodes, pair) {
        nodes.push(el("dt", { text: pair[0] }));
        nodes.push(el("dd", { text: pair[1] }));
        return nodes;
      }, []))
    ]);

    var summary = el("p", { class: "ct-summary" }, [
      "Current owner: ",
      el("strong", { text: h.currentOwner || "—" }),
      " · " + h.previousOwnerCount + (h.previousOwnerCount === 1 ? " previous owner" : " previous owners")
    ]);

    var timeline = el("ol", { class: "ct-timeline" }, h.owners.slice().reverse().map(function (o) {
      var period = formatDate(o.purchaseDate) + " → " + (o.current ? "Present" : formatDate(o.ownedUntil));
      var extra = Object.keys(o.details || {})
        .filter(function (key) { return o.details[key] !== null; })
        .map(function (key) { return prettyKey(key) + ": " + prettyValue(o.details[key]); });

      return el("li", { class: o.current ? "ct-owner ct-owner-current" : "ct-owner" }, [
        el("div", { class: "ct-owner-head" }, [
          el("span", { class: "ct-owner-seq", text: "#" + o.sequence }),
          el("span", { class: "ct-owner-name", text: o.ownerName }),
          o.current ? el("span", { class: "ct-chip ct-chip-ok", text: "Current owner" }) : null
        ]),
        el("div", { class: "ct-owner-meta", text: period + (o.ownershipDays !== null ? "  ·  " + formatDuration(o.ownershipDays) : "") }),
        el("div", { class: "ct-owner-meta", text: [o.channel, o.location].filter(Boolean).join(" · ") }),
        extra.length ? el("div", { class: "ct-owner-meta ct-muted", text: extra.join(" · ") }) : null
      ]);
    }));

    var actions = el("div", { class: "ct-actions" }, [
      el("button", { class: "ct-button ct-button-ghost", type: "button", text: "⬇ Download certificate (CSV)",
        onclick: function () { downloadCertificate(h.productId); } })
    ]);

    return el("div", {}, [
      productCard,
      summary,
      el("h4", { class: "ct-section", text: "Ownership history (newest first)" }),
      timeline,
      actions,
      el("p", { class: "ct-footnote ct-muted",
        text: "Read live from " + h.companyName + "'s records at " + new Date(h.verifiedAt).toLocaleString() +
          ". ChainTrace does not store this data." })
    ]);
  }

  function downloadCertificate(productId) {
    api("/products/" + encodeURIComponent(productId) + "/certificate.csv", { raw: true })
      .then(function (response) { return response.blob(); })
      .then(function (blob) {
        var url = URL.createObjectURL(blob);
        var link = el("a", { href: url, download: "chaintrace-" + productId + ".csv" });
        document.body.appendChild(link);
        link.click();
        link.remove();
        setTimeout(function () { URL.revokeObjectURL(url); }, 1000);
      })
      .catch(function (error) { alertInline(error.message); });
  }

  function alertInline(message) {
    var box = errorBox(message);
    root.appendChild(box);
    setTimeout(function () { box.remove(); }, 5000);
  }

  /** Demo mode: logs in with the account from data-auto-login and stores the session. */
  function autoLogin() {
    return api("/auth/login", { method: "POST", body: AUTO_LOGIN })
      .then(function (session) {
        state.session = session;
        saveSession(session);
      });
  }

  function logout() {
    api("/auth/logout", { method: "POST" }).catch(function () { /* token may already be gone */ });
    state.session = null;
    saveSession(null);
    showLogin();
  }

  // ------------------------------------------------------------------ start

  function start() {
    root = document.getElementById(TARGET_ID);
    if (!root) {
      console.error("[ChainTrace] No element with id '" + TARGET_ID + "' found on the page.");
      return;
    }
    root.classList.add("ct-root");
    root.style.setProperty("--ct-accent", ACCENT);

    var css = el("link", { rel: "stylesheet", href: BASE + "/widget/chaintrace-widget.css" });
    document.head.appendChild(css);

    if (!API_KEY) {
      render(errorBox("ChainTrace is not configured: data-api-key is missing."));
      return;
    }

    render(el("div", { class: "ct-card" }, [el("div", { class: "ct-loading", text: "Loading ChainTrace…" })]));

    api("/company")
      .then(function (company) {
        state.company = company;
        state.session = loadSession();
        if (state.session) showLookup();
        else if (AUTO_LOGIN) autoLogin().then(showLookup).catch(function (e) { showLogin(e.message); });
        else showLogin();
      })
      .catch(function (error) {
        render(errorBox("ChainTrace could not start: " + error.message));
      });
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start);
  else start();
})();
