# Fishing Advisor — Backend

Spring Boot backend for "What should I throw right now?" — an AI fishing
technique advisor. This is the v1 MVP: one endpoint, no database, no auth.

## Requirements

- Java 21
- Maven 3.9+
- An Anthropic API key (console.anthropic.com)

## Setup

1. Set your API key as an environment variable. Never put it directly in
   `application.properties` or commit it to git.

   macOS/Linux:
   ```bash
   export ANTHROPIC_API_KEY=sk-ant-xxxxxxxx
   ```

   Windows (PowerShell):
   ```powershell
   $env:ANTHROPIC_API_KEY="sk-ant-xxxxxxxx"
   ```

   For a permanent setup, add the export line to your `~/.zshrc` / `~/.bashrc`,
   or use a `.env` file with a tool like `direnv` — just don't commit it.

2. From the project root:
   ```bash
   mvn spring-boot:run
   ```

   The server starts on `http://localhost:8080`.

## Testing the endpoint

```bash
curl -X POST http://localhost:8080/api/advice \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Lake Fork, Texas",
    "targetSpecies": "largemouth bass",
    "season": "early summer",
    "waterClarity": "stained",
    "timeOfDay": "early morning",
    "waterTemp": "78F"
  }'
```

You should get back JSON like:
```json
{
  "advice": "Top Techniques:\n1. ...",
  "remainingFreeQueries": 2,
  "paywalled": false
}
```

Call it 3 times (the default free limit) and the 4th call returns
HTTP 402 Payment Required with `"paywalled": true` — that's your hook point
for the Stripe paywall on the frontend.

Check remaining free queries without using one:
```bash
curl http://localhost:8080/api/advice/remaining
```

## What's NOT here yet (by design — lean MVP)

- No frontend (React form comes next)
- No database / user accounts (free-query tracking is in-memory per session
  and resets on server restart)
- No Stripe integration (the 402 response is the hook point for it)
- No weather/water-temp API auto-fill (users type it in manually for now)

## Project structure

```
src/main/java/com/fishingadvisor/app/
├── FishingAdvisorApplication.java   # entry point
├── controller/
│   ├── AdviceController.java        # POST /api/advice, GET /api/advice/remaining
│   └── GlobalExceptionHandler.java  # clean JSON error responses
├── service/
│   ├── FishingAdvisorService.java   # builds prompt, calls Anthropic API
│   └── QueryLimitService.java       # in-memory free-query gate
├── dto/
│   ├── AdviceRequest.java
│   └── AdviceResponse.java
└── config/
    └── WebClientConfig.java         # HTTP client wired to Anthropic API
```

## Tightening before you deploy

- `@CrossOrigin(origins = "*")` in `AdviceController` is wide open for local
  dev — restrict it to your real frontend domain before going live.
- Swap `QueryLimitService` for a Postgres-backed table once you add real
  accounts (the in-memory map won't survive a server restart or scale past
  one instance).
