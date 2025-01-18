import './simple-header.scss';
import { Header, HeaderName } from '@carbon/react';

function SimpleHeader() {
  return (
    <Header aria-label={'platform name'} className={'cds--header-block'}>
      <HeaderName href={'/'} prefix={'Cobra'} isSideNavExpanded={true}>
        [Platform]
      </HeaderName>
    </Header>
  );
}

export default SimpleHeader;
