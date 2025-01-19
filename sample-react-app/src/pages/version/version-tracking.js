import React from 'react';
import { ContentHeading } from '../../components';
import { Button } from '@carbon/react';
import { Text } from '@carbon/react/lib/components/Text';
import { useProducerStore } from '../../store/use-producer-store';

function VersionTracking() {
  const { producerNode } = useProducerStore();

  function handleGetVersion(e) {
    console.log('get version', e, producerNode);
  }

  return (
    <>
      <ContentHeading displayName={'Version Tracking'} />
      <div
        style={{
          width: '300px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'start',
          flexDirection: 'column'
        }}
      >
        <div
          style={{
            padding: '1rem 0'
          }}
        >
          <Text>Version: 1</Text>
        </div>
        <Button kind={'primary'} onClick={handleGetVersion}>
          Get
        </Button>
      </div>
    </>
  );
}

export default VersionTracking;
