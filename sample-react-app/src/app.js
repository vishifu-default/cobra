import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import SimpleLayout from './layout/simple-layout';
import { Configuration, ConsumerTracking, VersionTracking } from './pages';

function App() {
  return (
    <React.Fragment>
      <BrowserRouter>
        <Routes>
          <Route path={'/'} element={<SimpleLayout />}>
            <Route index={true} element={<ConsumerTracking />} />
            <Route path={'/version'} element={<VersionTracking />} />
            <Route path={'/configuration'} element={<Configuration />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </React.Fragment>
  );
}

export default App;
