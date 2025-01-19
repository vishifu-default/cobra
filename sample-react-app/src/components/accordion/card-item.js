import { Tile } from '@carbon/react';
import './style.scss';
import { Text } from '@carbon/react/lib/components/Text';
import SimpleSection from '../section/simple-section';

const CardItem = ({ id, displayName, imageUrl }) => (
  <>
    <Tile id={id} title={displayName}>
      <SimpleSection>
        <Text>ID: {id}</Text>
      </SimpleSection>
      <SimpleSection>
        <Text>Name: {displayName}</Text>
      </SimpleSection>
      <br />
      <br />
      <img src={imageUrl} alt={imageUrl} className='accordion--card-img' />
    </Tile>
  </>
);

export default CardItem;
