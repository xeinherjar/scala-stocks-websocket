import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [ticker, setTicker] = useState([]);
  const [ws, setWebSocket] = useState(null);
  const [symbol, setSymbol] = useState('');

  const onSymbolInput = (event) => {
    setSymbol(event.target.value);
  };

  const onAddStock = (event) => {
    event.preventDefault();
    ws.send(JSON.stringify({ action: 'subscribe', value: symbol.toUpperCase() }));
    setSymbol('');
  };

  const onRemoveStock = symbol => {
    ws.send(JSON.stringify({ action: 'unsubscribe', value: symbol }));
  };


  useEffect(() => {
    const webSocket = new WebSocket('ws://localhost:9000/ws');

    webSocket.onopen = event => {
      webSocket.send(JSON.stringify({ action: 'subscribe', value: 'AAPL' }));
      setWebSocket(webSocket);
    }
    webSocket.onerror = event => { console.log('error!!', event); }
    webSocket.onmessage = event => {
      const data = JSON.parse(event.data);

      setTicker(data.sort((stock1, stock2) => stock1.symbol > stock2.symbol ? 1 : -1));
    };

    return () => {
      webSocket.close();
    };
  }, [])

  return (
    <div className="App">
      <div className="stock-input-area">
        <form onSubmit={onAddStock}>
          <input type="text" name="symbol" placeholder="ticker symbol" value={symbol} onChange={onSymbolInput}/>
          <button type="submit" value="Watch" disabled={!ws}>
            <i>Watch</i>
          </button>
        </form>
      </div>
      <div className="stock-table">
        <table>
          <thead>
            <tr className="table-header">
              <th>Symbol</th>
              <th>Price</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            { ticker.map((v, k) => (
                <tr key={v.symbol}>
                  <td>{v.symbol}</td>
                  <td>{v.price}</td>
                  <td>
                    <span className="remove-stock"
                      onClick={() => onRemoveStock(v.symbol)}>
                      x
                    </span>
                  </td>
                </tr>
            )) }
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default App;
