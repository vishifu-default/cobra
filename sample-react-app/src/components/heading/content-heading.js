import { Heading, Section } from '@carbon/react';
import './content-heading.scss';

function ContentHeading({ displayName }) {
  return (
    <>
      <Section as={'section'}>
        <Heading aria-label={'Content heading'} className={'content-heading'}>
          {displayName}
        </Heading>
      </Section>
    </>
  );
}

export default ContentHeading;
