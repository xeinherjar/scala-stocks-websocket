# Stocks

This assumes that `sbt` and `npm` are installed and available on your path.

Boring to watch if run when the market is closed :)

To run backend
```
sbt run
```

To run frontend
```
cd frontend
npm install
npm start
```
browse to [http://localhost:3000/](http://localhost:3000/)

## Backend
Play was setup with `sbt new playframework/play-scala-seed.g8`
- Websocket controller is at `app/controllers/WebsocketController.scala`
- Each connection has the ParentActor kick off a child actor, this way each connected user gets their own stocks to watch

## Frontend
- React was setup with `create-react-app`
- Since this is a demo, all code is in `frontend/src/App.js`