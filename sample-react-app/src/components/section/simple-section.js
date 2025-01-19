import { Section } from '@carbon/react';

function SimpleSection({ children }) {
  return (
    <>
      <Section className={'cds--section'}>{children}</Section>
    </>
  );
}

export default SimpleSection;
