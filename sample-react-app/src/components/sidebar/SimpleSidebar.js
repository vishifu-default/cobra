import { SideNav, SideNavItems, SideNavLink } from '@carbon/react';

function SimpleSidebar() {
  return (
    <SideNav
      isFixedNav={true}
      isChildOfHeader={false}
      expanded={true}
      aria-label={'Simple sidebar navigation'}>
      <SideNavItems>
        <SideNavLink>Link 1</SideNavLink>
        <SideNavLink>Link 2</SideNavLink>
      </SideNavItems>
    </SideNav>
  );
}

export default SimpleSidebar;
