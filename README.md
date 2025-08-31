![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-purple)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.5.5-green)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-blue)
![Vue.js](https://img.shields.io/badge/Vue-3.5.18-brightgreen)
![Node.js](https://img.shields.io/badge/Node-20.19.0-yellow)

# Reversi

<div align="center">
  <img src="./images/image.png" width="480"/>
</div>

A **web-based Reversi (Othello) game** with **AI and multiplayer support**, built with **Vue.js** frontend and **Kotlin/Spring Boot** backend. Designed for learning, experimentation, and fun.

---

## Table of Contents

* [Tech Stack](#tech-stack)
* [Quick Start (Docker)](#quick-start-docker)
* [Running Locally (Development Mode)](#running-locally-development-mode)
* [Backend API (Testing Only)](#backend-api-testing-only)
* [WebSocket (Real-Time Gameplay)](#websocket-real-time-gameplay)
* [Features](#features)
* [Docker Notes](#docker-notes)
* [Configuration](#configuration)
* [Contributing](#contributing)
* [License](#license)

---

## Tech Stack

* **Frontend:** Vue 3, TypeScript, Nginx for static hosting
* **Backend:** Kotlin, Spring Boot, WebSocket for real-time gameplay, REST API endpoints for testing
* **Build & Deployment:** Docker, Docker Compose

---

## Quick Start (Docker)

⚠️ Make sure `.env` exists and is configured appropriately

```bash
git clone git@github.com:marcothuemmler/org.thuemmler.reversi.git
cd org.thuemmler.reversi
docker compose up --build -d
```

Open in browser: `http://localhost:3000`

API requests proxy to backend on `localhost:8080`.

---

## Running Locally (Development Mode)

### Requirements

* JVM 21 or later
* Node.js 20+

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Backend

```bash
cd backend
./gradlew bootRun
```

> Spring Boot reads `.env` variables for ports and allowed origins. Make sure `.env` exists (see `.env.template`).

---

## Configuration

* `.env.template`:

```env
SERVER_PORT=8080
ALLOWED_ORIGINS=http://localhost:3000,https://yourserver.com
```

* Copy to `.env` for local development:

```bash
cp .env.template .env
```

---

## Backend API (Testing Only)

| Method | Endpoint                | Description                 |
| ------ | ----------------------- | --------------------------- |
| POST   | `/games`                | Start a new game            |
| GET    | `/games`                | List existing games         |
| GET    | `/games/{gameId}`       | Get a game by ID            |
| POST   | `/games/{gameId}/moves` | Make a move                 |
| GET    | `/games/{gameId}/moves` | Fetch valid moves           |
| POST   | `/games/{gameId}/undo`  | Undo last move              |
| POST   | `/games/{gameId}/redo`  | Redo previously undone move |
| DELETE | `/games/{gameId}`       | Delete a game               |

---

## WebSocket (Real-Time Gameplay)

**URL:**

```
ws://localhost:8080/ws/games
```

**Message Types:**

| Type       | Description                                             |
| ---------- | ------------------------------------------------------- |
| CREATE     | Client requests a new game                              |
| JOIN       | Client joins an existing game                           |
| MAKE\_MOVE | Player makes a move; server responds with updated board |
| UNDO       | Undo last move                                          |
| REDO       | Redo previously undone move                             |

**Example JSON for creating a game:**

```json
{
  "type": "CREATE",
  "payload": {
    "playerTypes": { "BLACK": "HUMAN", "WHITE": "AI" },
    "currentPlayer": "BLACK",
    "preferredSide": "BLACK"
  }
}
```

**Example JSON for making a move:**

```json
{
  "type": "MAKE_MOVE",
  "gameId": "1234-abcd",
  "payload": { "row": 2, "col": 3 }
}
```

---

## Features

* Single-player vs AI or multiplayer
* Real-time updates via WebSocket
* Undo/Redo moves
* Move highlighting
* Fully reactive board with Vue 3

---

## Docker Notes

* Frontend runs on port `3000`, served via Nginx (see `frontend/nginx.conf`), and proxies requests to the backend.
* Backend runs on port `8080` (see `docker-compose.yml`).

---

## Contributing

Contributions are welcome!

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
