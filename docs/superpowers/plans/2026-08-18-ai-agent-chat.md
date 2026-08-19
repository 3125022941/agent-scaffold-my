# AI Agent Chat Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a static agent conversation page and make session creation callable through browser-friendly JSON POST while retaining GET query compatibility.

**Architecture:** `index.html` is a self-contained vanilla JavaScript client that consumes `window.APP_CONFIG.API_BASE`, the login Cookie, and the existing REST response envelope. `AgentServiceController` separates request binding from a shared `createSessionResponse` method so POST JSON and GET query calls have one business path. Controller MockMvc tests cover the new HTTP behavior.

**Tech Stack:** Spring Boot 3, JUnit 5, Mockito, MockMvc, HTML5, CSS3, browser Fetch API, vanilla JavaScript.

## Global Constraints

- Create the chat page at `docs/dev-ops/nginx/html/index.html` with no third-party runtime dependency.
- Read `window.APP_CONFIG.API_BASE` from `docs/dev-ops/nginx/html/js/config.js`.
- Read and clear the `ai_agent_login` Cookie defined by `login.html`.
- Use `POST /api/v1/create_session` for JSON clients and retain `GET /api/v1/create_session?agentId=&userId=`.
- A successful API response has `code` equal to `"0000"`.
- Reuse a session until the selected agent changes or the user requests a new session.
- This workspace is not a Git repository; do not attempt commits.

---

### Task 1: Add Failing HTTP Test for Session Creation

**Files:**
- Modify: `ai-agent-scaffold-trigger/src/test/java/org/example/trigger/http/AgentServiceControllerTest.java`
- Test: `AgentServiceControllerTest#createSessionAcceptsPostJson`

**Interfaces:**
- Consumes: `POST /api/v1/create_session` with `{"agentId":"test-agent","userId":"user-1"}`.
- Produces: HTTP `200`, response `$.code == "0000"`, and `$.data.sessionId == "session-1"`.

- [ ] **Step 1: Add standalone MockMvc setup and the POST contract test**

```java
private MockMvc mockMvc;

@BeforeEach
void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
}

@Test
void createSessionAcceptsPostJson() throws Exception {
    when(chatService.createSession("test-agent", "user-1")).thenReturn("session-1");

    mockMvc.perform(post("/api/v1/create_session")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"agentId\":\"test-agent\",\"userId\":\"user-1\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("0000"))
            .andExpect(jsonPath("$.data.sessionId").value("session-1"));

    verify(chatService).createSession("test-agent", "user-1");
}
```

- [ ] **Step 2: Run the single test and confirm the current GET-only mapping fails**

Run: `mvn -pl ai-agent-scaffold-trigger -am test -Dtest=AgentServiceControllerTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: `createSessionAcceptsPostJson` fails with HTTP status `405`.

### Task 2: Implement POST and GET Session Bindings

**Files:**
- Modify: `ai-agent-scaffold-trigger/src/main/java/org/example/trigger/http/AgentServiceController.java:64-91`
- Test: `AgentServiceControllerTest#createSessionAcceptsPostJson`

**Interfaces:**
- `createSession(CreateSessionRequestDTO requestDTO)` binds POST JSON and satisfies `IAgentService`.
- `createSessionByQuery(String agentId, String userId)` binds GET query parameters.
- `createSessionResponse(String agentId, String userId)` produces `Response<CreateSessionResponseDTO>` for both mappings.

- [ ] **Step 1: Replace the existing GET-plus-request-body mapping with POST JSON binding**

```java
@PostMapping("create_session")
@Override
public Response<CreateSessionResponseDTO> createSession(@RequestBody CreateSessionRequestDTO requestDTO) {
    return createSessionResponse(requestDTO.getAgentId(), requestDTO.getUserId());
}
```

- [ ] **Step 2: Add a GET query-parameter compatibility mapping**

```java
@GetMapping("create_session")
public Response<CreateSessionResponseDTO> createSessionByQuery(
        @RequestParam String agentId,
        @RequestParam String userId) {
    return createSessionResponse(agentId, userId);
}
```

- [ ] **Step 3: Extract the current try/catch implementation into one private method**

```java
private Response<CreateSessionResponseDTO> createSessionResponse(String agentId, String userId) {
    try {
        String sessionId = chatService.createSession(agentId, userId);
        CreateSessionResponseDTO data = new CreateSessionResponseDTO();
        data.setSessionId(sessionId);
        return Response.<CreateSessionResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    } catch (AppException exception) {
        return Response.<CreateSessionResponseDTO>builder()
                .code(exception.getCode())
                .info(exception.getInfo())
                .build();
    }
}
```

Retain the existing unexpected-exception response branch and logging, using
the extracted `agentId` and `userId` values.

- [ ] **Step 4: Run the controller test suite and verify it passes**

Run: `mvn -pl ai-agent-scaffold-trigger -am test -DskipTests=false`

Expected: `AgentServiceControllerTest` reports zero failures and errors.

### Task 3: Build the Static Conversation Page

**Files:**
- Create: `docs/dev-ops/nginx/html/index.html`
- Consumes: `js/config.js`, `ai_agent_login`, the list, session, and chat APIs.
- Test: browser behavior at desktop and mobile widths.

**Interfaces:**
- `readLogin(): { user: string, ts: number } | null` validates the Cookie.
- `request(path: string, options?: RequestInit): Promise<object>` handles JSON and validates response `code`.
- `createSession(): Promise<string>` stores the active session ID.
- `sendMessage(): Promise<void>` appends left user and right agent messages.

- [ ] **Step 1: Create a full-page accessible layout**

```html
<aside class="agent-panel">
  <select id="agent-select" aria-label="选择智能体"></select>
  <button id="new-session" type="button">新建会话</button>
  <button id="logout" type="button">退出登录</button>
</aside>
<main class="chat-workspace">
  <header id="conversation-title"></header>
  <section id="message-list" aria-live="polite"></section>
  <form id="composer"><textarea id="message-input"></textarea><button type="submit">发送</button></form>
</main>
```

Use a work-focused layout, constrained controls, visible keyboard focus, user
messages on the left, agent responses on the right, and a mobile breakpoint
that places the agent controls before the timeline.

- [ ] **Step 2: Implement login-state and agent-list loading**

```javascript
const login = readLogin();
if (!login) window.location.replace("login.html");

const response = await request("/api/v1/query_ai_agent_config_list");
for (const agent of response.data) {
    const option = new Option(agent.agentName, agent.agentId);
    option.dataset.description = agent.agentDesc || "";
    agentSelect.add(option);
}
```

- [ ] **Step 3: Implement session, sending, errors, and logout**

```javascript
async function createSession() {
    const response = await request("/api/v1/create_session", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ agentId: agentSelect.value, userId: login.user })
    });
    currentSessionId = response.data.sessionId;
    return currentSessionId;
}

async function sendMessage() {
    const message = messageInput.value.trim();
    if (!message) return;
    const sessionId = currentSessionId || await createSession();
    appendMessage("user", message);
    const response = await request("/api/v1/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ agentId: agentSelect.value, userId: login.user, sessionId, message })
    });
    appendMessage("agent", response.data.content);
}
```

The `request` helper throws an Error when fetch fails, JSON parsing fails, or
the response code is not `0000`; the caller appends a system message and
restores the composer button. Changing agents resets `currentSessionId` and
clears the current message list. Logout clears `ai_agent_login` with `Path=/`
and `Max-Age=0` before redirecting to `login.html`.

- [ ] **Step 4: Start a static server and validate file loading**

Run: `python3 -m http.server 8088 --directory docs/dev-ops/nginx/html`

Expected: `curl --fail http://127.0.0.1:8088/index.html` exits with `0`.

- [ ] **Step 5: Validate the page in Chrome**

At `http://127.0.0.1:8088/index.html`:

1. Without the Cookie, confirm redirect to `login.html`.
2. With `ai_agent_login` for `admin`, confirm the agent selector contains data from the list API.
3. Click New Session, confirm a session ID is obtained, and send a short message.
4. Confirm user text renders left and response text renders right.
5. Change agent and confirm the visible timeline clears before the next session.
6. Inspect `1440px` and `390px` widths for readable, non-overlapping controls.

### Task 4: Final Contract Validation

**Files:**
- Verify: `docs/dev-ops/nginx/html/index.html`
- Verify: `ai-agent-scaffold-trigger/src/main/java/org/example/trigger/http/AgentServiceController.java`

**Interfaces:**
- Consumes: all output of Tasks 1 through 3.
- Produces: a browser-consumable chat flow and compatible session endpoint.

- [ ] **Step 1: Validate static API references and login state handling**

Run: `rg -n 'query_ai_agent_config_list|create_session|/api/v1/chat|ai_agent_login|login.html' docs/dev-ops/nginx/html/index.html`

Expected: all five API or state references appear.

- [ ] **Step 2: Validate both session endpoints against the running application**

Run: `curl --fail --request GET 'http://127.0.0.1:8091/api/v1/create_session?agentId=100001&userId=manual-check' && curl --fail --request POST --header 'Content-Type: application/json' --data '{"agentId":"100001","userId":"browser-check"}' http://127.0.0.1:8091/api/v1/create_session`

Expected: both JSON responses have `code` equal to `0000` and contain a `data.sessionId`.

- [ ] **Step 3: Confirm formatting and test status**

Run: `git diff --check && mvn -pl ai-agent-scaffold-trigger -am test -DskipTests=false`

Expected: no whitespace errors and zero test failures.
