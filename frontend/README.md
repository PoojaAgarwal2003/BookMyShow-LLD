# BookMyShow React Frontend

A responsive React UI for the Spring Boot backend in `../bookmyshow-springboot`.

## Run

Start PostgreSQL and Redis:

```bash
cd ../bookmyshow-springboot
docker compose up -d
```

Start the backend:

```bash
gradle bootRun
```

Start the frontend:

```bash
cd ../frontend
npm run dev
```

Open the URL printed by Vite (normally `http://localhost:5173`).

The Vite server proxies `/api` calls to `http://localhost:8080`.

## Included flows

- Movie catalog and movie details
- Available showtimes
- Registration and login with JWT
- Interactive show-seat selection
- Redis-backed seat locking
- Idempotent pending booking creation
- Mock payment and booking confirmation
