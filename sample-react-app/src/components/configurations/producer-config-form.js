import React from 'react';
import { Button, FormGroup, NumberInput, Stack, TextInput } from '@carbon/react';
import './style.scss';
import { useProducerStore } from '../../store/use-producer-store';
import { Node } from '../../model/consumer';

function ProducerConfigForm() {
  const { producerNode, updateProducerNode } = useProducerStore();
  const [producerUrl, setProducerUrl] = React.useState(producerNode.domain);
  const [producerPort, setProducerPort] = React.useState(producerNode.port);

  function handleUrlChange(e) {
    setProducerUrl(e.target.value);
  }

  function handlePortChange(e) {
    setProducerPort(e.target.value);
  }

  function handleSubmit() {
    const node = new Node(producerUrl, producerPort);
    updateProducerNode(node);
  }

  return (
    <>
      <FormGroup legendText={'Producer configuration'}>
        <Stack gap={4}>
          <TextInput
            id={'producer-url'}
            labelText={'Producer URL'}
            placeholder={'eg. producer.com'}
            value={producerUrl}
            onChange={handleUrlChange}
          />
          <NumberInput
            id={'producer-port'}
            label={'Producer Port'}
            placeholder={'eg. 7070'}
            value={producerPort}
            onChange={handlePortChange}
          />
          <Button onClick={handleSubmit}>Submit</Button>
        </Stack>
      </FormGroup>
    </>
  );
}

export default ProducerConfigForm;
