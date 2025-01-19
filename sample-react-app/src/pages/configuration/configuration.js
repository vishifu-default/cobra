import React from 'react';
import { ConsumerConfigTable, ContentHeading, ProducerConfigForm } from '../../components';
import './style.scss';

const Configuration = () => (
  <>
    <ContentHeading displayName={'Producer configuration'} />
    <div style={{ marginBottom: '5rem' }}>
      <ProducerConfigForm />
    </div>

    <ContentHeading displayName={'Consumers configuration'} />
    <div style={{ marginBottom: '5rem', marginTop: '2rem' }}>
      <ConsumerConfigTable />
    </div>
  </>
);

export default Configuration;
