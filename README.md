# Reversi

A simple web-based Reversi (Othello) game built with Vue.js frontend and Kotlin/Spring Boot backend. Designed for fun, learning, and experimentation.

## Tech Stack

* **Frontend:** Vue 3, TypeScript, Nginx for static hosting
* **Backend:** Kotlin, Spring Boot, REST API
* **Build & Deployment:** Docker, Docker Compose

## Quick Start

Clone the repository and run the application using Docker:

```bash
git clone git@github.com:marcothuemmler/org.thuemmler.reversi.git
cd org.thuemmler.reversi
docker compose up --build -d
```

Open the game in your browser:

```
http://localhost:3000
```

API requests from the frontend are automatically proxied to the backend running on port `8080`.

---

## Running Locally (Development Mode)

### Requirements

* JVM 21 or later
* Node.js

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open in browser:

```
http://localhost:3000
```

API requests will proxy to the backend on `localhost:8080`.

### Backend

```bash
cd backend
./gradlew bootRun
```

---

## Backend API Endpoints

* `POST /games` – Start a new game
* `GET /games` – Get a list of games
* `GET /games/{gameId}` – Get a game by ID
* `POST /games/{gameId}/moves` – Make a move
* `GET /games/{gameId}/moves` – Fetch valid moves
* `DELETE /games/{gameId}` – Delete a game

---

## Docker Notes

* Frontend runs on port `3000`, served via Nginx, and proxies `/games` requests to the backend (see `frontend/nginx.conf`).
* Backend runs on port `8080` (see `docker-compose.yml`).

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
