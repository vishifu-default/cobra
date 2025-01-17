import React from 'react';
import { SimpleHeader } from '../components/header';
import { SimpleSidebar } from '../components/sidebar';
import { Outlet } from 'react-router-dom';
import { Content } from '@carbon/react';

function SimpleLayout() {
  return (
    <React.Fragment>
      <SimpleHeader />
      <SimpleSidebar />
      <Content>
        <Outlet />
      </Content>
    </React.Fragment>
  );
}

export default SimpleLayout;