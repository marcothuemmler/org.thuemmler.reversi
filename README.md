# Reversi

A simple web-based Reversi (Othello) game built with Vue.js frontend and Kotlin/Spring Boot backend. Designed for fun, learning, and experimentation.

## Tech Stack

* **Frontend:** Vue 3, TypeScript, Nginx for static hosting
* **Backend:** Kotlin, Spring Boot, REST API
* **Build & Deployment:** Docker, Docker Compose

## Running Locally

### Requirements

* JVM 21 or later
* Node.js
* Docker & Docker Compose

### Steps

1. Clone the repository:

```bash
git clone git@github.com:marcothuemmler/org.thuemmler.reversi.git
cd org.thuemmler.reversi
```

2. Build and start containers:

```bash
docker-compose build --no-cache
docker-compose up
```

3. Open the game in your browser:

```
http://localhost:3000
```

### Development Mode

To run the frontend with hot reload outside Docker:

```bash
cd frontend
npm install
npm run dev
```

Open:

```
http://localhost:3000
```

API requests will proxy to the backend running on `localhost:8080`.

## Backend API Endpoints

* `POST /games` – Start a new game
* `POST /games/{gameId}/moves` – Make a move
* `GET /games/{gameId}/moves` – Fetch valid moves

## Docker Notes

* Frontend is served via Nginx and proxies `/games` requests to the backend.
* Backend runs on port `8080`.
* Ensure JVM 21+ is used to run the backend container.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
