import React from 'react';
import { Accordion, Button, ButtonSet } from '@carbon/react';
import { AccordionConsumer, ContentHeading } from '../../components';
import './style.scss';
import { useConsumerStore } from '../../store/use-consumer-store';

const ConsumerTracking = () => {
  const { consumers } = useConsumerStore();

  const onReloadConsumer = () => {
    console.log('onReloadConsumer');
  };

  return (
    <>
      <ContentHeading displayName={'Consumers'} />

      <ButtonSet className={'cds--btn-controlled-set'}>
        <Button className={'accordion-btn'} onClick={onReloadConsumer}>
          Reload
        </Button>
      </ButtonSet>
      <Accordion align={'start'} disabled={false} isFlush={false} ordered={false} size={'lg'}>
        {consumers &&
          consumers.map((track) => (
            <AccordionConsumer title={track.domain} key={track.domain} url={track.domain} />
          ))}
      </Accordion>
    </>
  );
};

export default ConsumerTracking;
