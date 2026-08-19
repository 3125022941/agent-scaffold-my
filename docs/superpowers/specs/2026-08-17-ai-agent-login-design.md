# AI Agent Login Page Design

## Scope

Create the static login experience under `docs/dev-ops/nginx/html`:

- `login.html` provides the login page.
- `js/config.js` exposes `window.APP_CONFIG.API_BASE`.
- `images/ai-hero.svg` provides the login-page AI visual.

`index.html` is not created in this change. Successful login redirects to that
future page, and an existing valid login cookie redirects there immediately.

## Login Behavior

The form is prefilled with the demonstration credentials `admin` / `admin`.
Only those credentials authenticate. On success, JavaScript writes the
`ai_agent_login` cookie with JSON payload containing user `admin` and the
current epoch-millisecond timestamp in `ts`.
The cookie uses `Path=/`, `SameSite=Lax`, and a finite lifetime. Invalid
credentials remain on the page and show an accessible inline error.

The page reads and safely parses the cookie on load. A valid `user` value
redirects to `index.html`; malformed or expired values are cleared.

## UI

The desktop layout has an information and illustration panel on the left and a
compact sign-in panel on the right. On small screens, the information area
stacks above the form. The SVG is a native vector asset showing an AI workflow
scene, avoiding an external asset dependency.

The form uses semantic labels, password visibility control, keyboard submit,
and restrained error feedback. It has no external runtime dependencies.

## API Configuration

`js/config.js` uses `http://127.0.0.1:8091` as the default `API_BASE`. The
running Spring Boot application listens on HTTP at port 8091; HTTPS on that
port would fail TLS negotiation. The config file is intentionally separate so
the Nginx deployment can change only the server address.

## Verification

Verification will cover:

- Static JavaScript validation that accepts only `admin` / `admin`.
- Cookie creation and existing-cookie redirect behavior in a browser.
- Responsive layout and asset loading from the Nginx static directory.
