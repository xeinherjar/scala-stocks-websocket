import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const onAddStock = (event) => {
    event.preventDefault();
    const symbol = event.target.symbol.value;

    setSymbol(symbol);
  };

  const onRemoveStock = symbol => {
    ws.send(JSON.stringify({ action: 'unsubscribe', value: symbol }));
  };

  const [ticker, setTicker] = useState([]);
  const [ws, setWebSocket] = useState(null);
  const [symbol, setSymbol] = useState(null);

  // essentially componentDidMount
  useEffect(() => {
    const webSocket = new WebSocket('ws://localhost:9000/ws');

    webSocket.onopen = event => {
      console.log('connected!', webSocket);
      webSocket.send(JSON.stringify({ action: 'subscribe', value: 'AAPL' }));
      setWebSocket(webSocket);
    }
    webSocket.onerror = event => { console.log('error!!', event); }
    webSocket.onmessage = event => {
      const data = JSON.parse(event.data);

      setTicker(data);
      console.log('set ticker:', data);
    };

    return () => {
      webSocket.close();
    };
  }, [])

  useEffect(() => {
    console.log('symbol', symbol);
    if (!symbol) { return; }
    ws.send(JSON.stringify({ action: 'subscribe', value: symbol }));
    console.log('sent symbol', symbol);
  }, [symbol]);

  return (
    <div className="App">
      <div className="stock-input-area">
        <form onSubmit={onAddStock}>
          <label>
            Ticker Symbol:
            <input type="text" name="symbol" />
          </label>
          <input type="submit" value="Watch" />
        </form>
      </div>
      <div className="stock-table">
        <table>
          <thead>
            <tr>
              <th>Symbol</th>
              <th>Price</th>
            </tr>
          </thead>
          <tbody>
            { ticker.map((v, k) => (
                <tr key={v.symbol}>
                  <td>{v.symbol}</td>
                  <td>{v.price}</td>
                  <td onClick={() => onRemoveStock(v.symbol)}>-X-</td>
                </tr>
            )) }
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default App;
