import React from 'react';
import { Accordion, AccordionItem, Button, ButtonSet } from '@carbon/react';
import { ContentHeading, TrackConsumerModal } from '../../components';
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

  return (
    <>
      <ContentHeading displayName={'Consumers'} />

      <ButtonSet className={'cds--btn-controlled-set'}>
        <Button className={'accordion-btn'} onClick={addNewTrackingConsumer}>
          Track consumer
        </Button>
      </ButtonSet>

      <Accordion align={'start'} disabled={false} isFlush={false} ordered={false} size={'lg'}>
        {consumers &&
          consumers.map((track) => (
            <AccordionItem key={track.started} disabled={false} title={track.url}>
              <p>Test {track.url}</p>
            </AccordionItem>
          ))}
      </Accordion>

      <TrackConsumerModal
        heading={'Add a consumer domain URL'}
        label={'Consumer'}
        open={trackConsumerModal}
        onClose={closeAddTrackingConsumerModal}
      />
    </>
  );
}

export default ConsumerTracking;
