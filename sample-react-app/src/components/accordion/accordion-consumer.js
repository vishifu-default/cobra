import { AccordionItem } from '@carbon/react';
import React from 'react';
import ScrollableCardContainer from './scrollable-card-container';

function AccordionConsumer({ title, url }) {
  const version = 1;

  function buildTitle() {
    return `${title}  [version: ${version}]`;
  }

  return (
    <>
      <AccordionItem key={url} disabled={false} title={buildTitle()}>
        <ScrollableCardContainer />
      </AccordionItem>
    </>
  );
}

export default AccordionConsumer;
