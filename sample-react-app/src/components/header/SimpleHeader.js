import './header.scss';
import { Header, HeaderName, SkipToContent } from '@carbon/react';

function SimpleHeader() {
  return (
    <Header aria-label={'platform name'}>
      <SkipToContent />
      <HeaderName
        href={'#'}
        prefix={'Cobra'}
        isSideNavExpanded={true}
        className={'cds--header-name'}>
        [Platform]
      </HeaderName>
    </Header>
  );
}

export default SimpleHeader;
