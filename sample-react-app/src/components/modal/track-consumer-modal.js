import { Modal, ModalBody, TextInput } from '@carbon/react';
import './track-consumer-modal.scss';
import React from 'react';
import { useConsumerStore } from '../../store/use-consumer-store';
import { Node } from '../../model/consumer';

function TrackConsumerModal({ heading, label, open, onClose }) {
  const inputUrlRef = React.useRef();
  const inputPortRef = React.useRef();

  const [consumerURL, setConsumerURL] = React.useState('');
  const [consumerPort, setConsumerPort] = React.useState(0);
  const { add } = useConsumerStore();

  function handleInputConsumerURL(event) {
    setConsumerURL(event.target.value);
  }

  function handleInputConsumerPort(event) {
    setConsumerPort(event.target.value);
  }

  function submit() {
    const consumerConfiguration = new Node(consumerURL, consumerPort);
    add(consumerConfiguration);
    resetState();
    onClose();
  }

  function close() {
    resetState();
    onClose();
  }

  function resetState() {
    setConsumerURL('');
    setConsumerPort(0);

    inputUrlRef.current.value = null;
    inputPortRef.current.value = null;
  }

  return (
    <>
      <Modal
        open={open}
        onRequestClose={close}
        onRequestSubmit={submit}
        modalHeading={heading}
        modalLabel={label}
        primaryButtonText={'Add'}
        secondaryButtonText={'Cancel'}
      >
        <ModalBody>
          <p className={'text-content'}>Add a consumer URL to track</p>
          <TextInput
            data-modal-primary-focus
            id='domain-input'
            ref={inputUrlRef}
            labelText='Domain name'
            placeholder='e.g. consumer1.com'
            onChange={(e) => handleInputConsumerURL(e)}
          />
          <br />
          <TextInput
            id={'port-input'}
            ref={inputPortRef}
            labelText={'Port'}
            placeholder='e.g. 7070'
            onChange={(e) => handleInputConsumerPort(e)}
          />
        </ModalBody>
      </Modal>
    </>
  );
}

export default TrackConsumerModal;
