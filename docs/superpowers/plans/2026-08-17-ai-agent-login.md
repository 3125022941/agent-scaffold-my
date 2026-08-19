# AI Agent Login Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a responsive static AI-agent login page that authenticates the demonstration `admin` account with a Cookie and redirects to `index.html`.

**Architecture:** A standalone `login.html` contains the visual layout and small local authentication script. `js/config.js` owns the deploy-time API base address, while `images/ai-hero.svg` is an independently cacheable vector illustration. The login script validates the demonstration credentials, owns cookie parsing and creation, and redirects based on valid state.

**Tech Stack:** HTML5, CSS3, browser Cookie API, vanilla JavaScript, SVG.

## Global Constraints

- Create all deployable files under `docs/dev-ops/nginx/html`.
- Do not add runtime package dependencies or a build step.
- `window.APP_CONFIG.API_BASE` defaults to `http://127.0.0.1:8091`.
- The Cookie name is `ai_agent_login` and its JSON payload holds `user` and `ts`.
- Only `admin` / `admin` succeeds in this static demonstration.
- Successful or pre-existing valid login redirects to `index.html`.
- This workspace is not a Git repository; do not attempt commits.

---

### Task 1: Create Static Configuration and Visual Asset

**Files:**
- Create: `docs/dev-ops/nginx/html/js/config.js`
- Create: `docs/dev-ops/nginx/html/images/ai-hero.svg`
- Test: browser loads `login.html` without a network error for either asset.

**Interfaces:**
- Produces: `window.APP_CONFIG.API_BASE` as a string consumed by `index.html` and future static pages.
- Produces: `images/ai-hero.svg`, loaded by `<img src="images/ai-hero.svg">` in `login.html`.

- [ ] **Step 1: Verify the target assets do not exist before implementation**

Run: `python3 -m http.server 8088 --directory docs/dev-ops/nginx/html`

Expected: `curl -I http://127.0.0.1:8088/js/config.js` and `curl -I http://127.0.0.1:8088/images/ai-hero.svg` return `404`.

- [ ] **Step 2: Add the configuration file**

```javascript
window.APP_CONFIG = Object.freeze({
    API_BASE: "http://127.0.0.1:8091"
});
```

- [ ] **Step 3: Add the AI workflow SVG**

The SVG contains a `viewBox="0 0 760 560"`, a dark grid background, a central
agent node labelled `AI`, orbiting tool and memory nodes, and a color palette
of teal, coral, yellow, and charcoal. It uses only native SVG elements and
embedded CSS, so no external image or font request is required.

- [ ] **Step 4: Verify successful asset loading**

Run: `curl --fail http://127.0.0.1:8088/js/config.js && curl --fail http://127.0.0.1:8088/images/ai-hero.svg`

Expected: both commands exit with `0`.

### Task 2: Implement Login Page and Cookie State

**Files:**
- Create: `docs/dev-ops/nginx/html/login.html`
- Consumes: `window.APP_CONFIG.API_BASE` from `js/config.js`.
- Consumes: `images/ai-hero.svg` from Task 1.
- Produces: an `ai_agent_login` Cookie and redirect to `index.html`.

**Interfaces:**
- `readLoginCookie(): { user: string, ts: number } | null` safely parses the Cookie.
- `writeLoginCookie(user: string): void` serializes `{ user, ts: Date.now() }` using `encodeURIComponent`.
- `showError(message: string): void` updates `#form-error` and makes it visible.

- [ ] **Step 1: Write a browser-facing failing behavior checklist**

```text
Given no ai_agent_login Cookie, submitting admin/admin must set ai_agent_login and navigate to index.html.
Given any credential other than admin/admin, the page must remain on login.html and expose an error in #form-error.
Given ai_agent_login with a JSON user value, loading login.html must navigate to index.html.
Given a malformed ai_agent_login Cookie, loading login.html must clear it and render the form.
```

- [ ] **Step 2: Verify the behavior fails before the page exists**

Run: `curl --fail http://127.0.0.1:8088/login.html`

Expected: command exits nonzero with HTTP `404`.

- [ ] **Step 3: Implement semantic markup, two-column responsive layout, and login script**

```html
<form id="login-form" novalidate>
  <label for="username">账号</label>
  <input id="username" name="username" value="admin" autocomplete="username" required>
  <label for="password">密码</label>
  <div class="password-field">
    <input id="password" name="password" type="password" value="admin" autocomplete="current-password" required>
    <button id="toggle-password" type="button" aria-label="显示密码">显示</button>
  </div>
  <p id="form-error" role="alert" hidden></p>
  <button type="submit">登录控制台</button>
</form>
```

```javascript
function writeLoginCookie(user) {
    const payload = encodeURIComponent(JSON.stringify({ user, ts: Date.now() }));
    document.cookie = `ai_agent_login=${payload}; Path=/; Max-Age=28800; SameSite=Lax`;
}

form.addEventListener("submit", (event) => {
    event.preventDefault();
    if (username.value === "admin" && password.value === "admin") {
        writeLoginCookie("admin");
        window.location.assign("index.html");
        return;
    }
    showError("账号或密码不正确，请使用演示账号登录。");
});
```

The CSS uses CSS custom properties, a 12-column desktop grid, a mobile
single-column media query, visible focus states, 8px-or-smaller corners, and
fixed dimensions for the password visibility button. The left panel includes
the product name, short operational summary, feature list, and the SVG asset;
the form panel includes only sign-in controls.

- [ ] **Step 4: Verify the static page is reachable**

Run: `curl --fail http://127.0.0.1:8088/login.html`

Expected: command exits with `0` and output contains `id="login-form"` and `ai_agent_login`.

- [ ] **Step 5: Verify browser behavior manually at desktop and mobile widths**

At `http://127.0.0.1:8088/login.html`:

1. Clear `ai_agent_login`, submit wrong credentials, and confirm the inline error is visible with no redirect.
2. Submit `admin` / `admin`, confirm the Cookie JSON decodes to user `admin` and a numeric `ts`, then confirm the request changes to `index.html`.
3. Return to `login.html` with the Cookie present and confirm immediate redirect.
4. Set `ai_agent_login=not-json`, reload `login.html`, and confirm the cookie is removed and the form remains visible.
5. Inspect at `1440px` and `390px` widths; no text overlaps and the information panel stacks above the form on mobile.

### Task 3: Verify Configuration Contract and File Hygiene

**Files:**
- Modify: `docs/dev-ops/nginx/html/login.html` only if verification exposes an incorrect asset or cookie reference.
- Test: static file and configuration checks.

**Interfaces:**
- Consumes: all assets from Tasks 1 and 2.
- Produces: a Nginx-ready static directory with no dependency on a build tool.

- [ ] **Step 1: Validate the expected file structure**

Run: `test -f docs/dev-ops/nginx/html/login.html && test -f docs/dev-ops/nginx/html/js/config.js && test -f docs/dev-ops/nginx/html/images/ai-hero.svg`

Expected: exit code `0`.

- [ ] **Step 2: Validate the configuration protocol and port**

Run: `rg -n 'API_BASE: "http://127\.0\.0\.1:8091"' docs/dev-ops/nginx/html/js/config.js`

Expected: one matching line.

- [ ] **Step 3: Run JavaScript and HTML structural checks**

Run: `node --check docs/dev-ops/nginx/html/js/config.js && rg -n 'id="login-form"|id="username"|id="password"|id="form-error"|ai_agent_login' docs/dev-ops/nginx/html/login.html`

Expected: exit code `0` and one or more matches for every required login element.

- [ ] **Step 4: Confirm no unrequested external dependencies were added**

Run: `rg -n 'https?://' docs/dev-ops/nginx/html/login.html docs/dev-ops/nginx/html/images/ai-hero.svg docs/dev-ops/nginx/html/js/config.js`

Expected: the only match is the local `http://127.0.0.1:8091` API configuration.
