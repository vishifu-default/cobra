import './simple-header.scss';
import { Header, HeaderName } from '@carbon/react';

const SimpleHeader = () => (
  <Header aria-label={'platform name'} className={'header--block'}>
    <HeaderName href={'/'} prefix={'Cobra'} isSideNavExpanded={true}>
      [Platform]
    </HeaderName>
  </Header>
);

export default SimpleHeader;
