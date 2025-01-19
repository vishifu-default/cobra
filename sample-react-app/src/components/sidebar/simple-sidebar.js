import { SideNav, SideNavItem, SideNavItems } from '@carbon/react';
import './simple-sidebar.scss';
import { Link } from 'react-router-dom';

const SimpleSidebar = () => (
  <SideNav
    isFixedNav={true}
    isChildOfHeader={false}
    expanded={true}
    aria-label={'Simple sidebar navigation'}
    className={'nav'}
  >
    <SideNavItems className={'nav-set'}>
      <SideNavItem>
        <Link to='/'>Consumer</Link>
      </SideNavItem>
      <SideNavItem>
        <Link to={'/version'}>Version</Link>
      </SideNavItem>
      <SideNavItem>
        <Link to={'/configuration'}>Configuration</Link>
      </SideNavItem>
    </SideNavItems>
  </SideNav>
);

export default SimpleSidebar;
