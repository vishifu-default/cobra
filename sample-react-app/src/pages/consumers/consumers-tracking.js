import React from 'react';
import { Accordion, AccordionItem, Button, ButtonSet } from '@carbon/react';
import { AccordionConsumer, ContentHeading, TrackConsumerModal } from '../../components';
import './consumers-tracking.scss';
import { useConsumerStore } from '../../store/use-consumer-store';

function ConsumerTracking() {
  const [trackConsumerModal, setTrackConsumerModal] = React.useState(false);
  const { consumers } = useConsumerStore();

  function addNewTrackingConsumer() {
    setTrackConsumerModal(true);
  }

  function closeAddTrackingConsumerModal() {
    setTrackConsumerModal(false);
  }

  function onReloadConsumer() {
    console.log('onReloadConsumer');
  }

  return (
    <>
      <ContentHeading displayName={'Consumers'} />

      <ButtonSet className={'cds--btn-controlled-set'}>
        <Button className={'accordion-btn'} onClick={addNewTrackingConsumer}>
          Track consumer
        </Button>
        <Button className={'accordion-btn'} onClick={onReloadConsumer}>
          Reload
        </Button>
      </ButtonSet>
      <TrackConsumerModal
        heading={'Add a consumer domain URL'}
        label={'Consumer'}
        open={trackConsumerModal}
        onClose={closeAddTrackingConsumerModal}
      />

      <Accordion align={'start'} disabled={false} isFlush={false} ordered={false} size={'lg'}>
        {consumers &&
          consumers.map((track) => (
            <AccordionConsumer title={track.url} key={track.url} url={track.url} />
          ))}
      </Accordion>
    </>
  );
}

export default ConsumerTracking;
