# AI Agent Chat Page Design

## Scope

Add `docs/dev-ops/nginx/html/index.html` as the static AI-agent conversation
page. It uses the existing `login.html`, Cookie convention, local `config.js`,
and Spring controller. It also corrects the session-creation HTTP contract for
browser JSON clients while preserving query-string compatibility.

## Authentication State

On load, `index.html` reads `ai_agent_login` and decodes its JSON payload. A
payload with a non-empty `user` string supplies the `userId`; a missing or
malformed value redirects to `login.html`. The user can sign out from the chat
page, which clears the Cookie before returning to the login page.

## Conversation UI

The page has a work-focused three-part layout: an agent selector in a compact
sidebar, a central message timeline, and a persistent composer. It loads agent
options with `GET ${API_BASE}/api/v1/query_ai_agent_config_list`, displaying
agent names and descriptions. User messages render left-aligned, while agent
responses render right-aligned. It remains usable at mobile widths by stacking
the sidebar controls above the conversation.

## Session and API Flow

Selecting an agent clears the active session ID. The New Session command and
the first send without a session call `POST ${API_BASE}/api/v1/create_session`
with JSON `{ agentId, userId }`; its successful response supplies `sessionId`.
Each send then calls `POST ${API_BASE}/api/v1/chat` with `agentId`, `userId`,
`sessionId`, and `message`. Network or API errors appear as an inline system
message and leave the composer usable.

`AgentServiceController` exposes both forms of session creation:

- `POST /api/v1/create_session` accepts a JSON `CreateSessionRequestDTO`.
- `GET /api/v1/create_session?agentId=...&userId=...` accepts query parameters
  for manual verification.

Both request forms delegate to the same private response-building method.

## Testing

Add MockMvc coverage that verifies the POST JSON session endpoint returns a
success response and calls `IChatService` with the submitted fields. Existing
controller unit tests remain. Browser checks cover login-state redirect, agent
list loading, session reset, chat rendering, and mobile layout.
