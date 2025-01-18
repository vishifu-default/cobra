import { Modal, ModalBody, TextInput } from '@carbon/react';
import './track-consumer-modal.scss';
import { useState } from 'react';
import {useConsumerStore} from '../../store/use-consumer-store'

function TrackConsumerModal({ heading, label, open, onClose }) {
  const [consumerURL, setConsumerURL] = useState('');
  const {add} = useConsumerStore();

  function handleInputConsumerURL(event) {
    setConsumerURL(event.target.value);
  }

  function submit() {
    add(consumerURL);
    onClose();
  }

  return (
    <>
      <Modal
        open={open}
        onRequestClose={onClose}
        onRequestSubmit={submit}
        modalHeading={heading}
        modalLabel={label}
        primaryButtonText={'Add'}
        secondaryButtonText={'Cancel'}>
        <ModalBody>
          <p className={'text-content'}>Add a consumer URL to track</p>
          <TextInput
            data-modal-primary-focus
            id='domain-input'
            labelText='Domain name'
            placeholder='e.g. consumer1.com'
            onChange={(e) => handleInputConsumerURL(e)}
          />
        </ModalBody>
      </Modal>
    </>
  );
}

export default TrackConsumerModal;
